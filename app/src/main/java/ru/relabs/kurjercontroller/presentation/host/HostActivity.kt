package ru.relabs.kurjercontroller.presentation.host

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.github.terrakok.cicerone.NavigatorHolder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialize.util.UIUtils
import kotlinx.android.synthetic.main.activity_host.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.AppUpdate
import ru.relabs.kurjercontroller.presentation.base.fragment.AppBarSettings
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.fragment.IFragmentStyleable
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.host.featureCheckers.FeatureCheckersContainer
import ru.relabs.kurjercontroller.presentation.host.systemWatchers.SystemWatchersContainer
import ru.relabs.kurjercontroller.presentation.сustomView.drawable.NavDrawerBackgroundDrawable
import ru.relabs.kurjercontroller.services.ReportService
import ru.relabs.kurjercontroller.utils.*
import ru.relabs.kurjercontroller.utils.extensions.showDialog
import ru.relabs.kurjercontroller.utils.extensions.showSnackbar
import java.io.File
import java.io.FileNotFoundException


class HostActivity : AppCompatActivity(), IFragmentHolder {

    private val supervisor = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + supervisor)
    private val controller = defaultController(HostState(), HostContext())

    private val navigationHolder: NavigatorHolder by inject()
    private lateinit var navigationDrawer: Drawer

    private val navigator = CiceroneNavigator(this)

    private val featureCheckersContainer = FeatureCheckersContainer(this)
    private val systemWatchersContainer =
        SystemWatchersContainer(this, featureCheckersContainer.network, featureCheckersContainer.gps)

    private var taskUpdateRequiredDialogShowed: Boolean = false
    private var isUpdateAppDialogShowed: Boolean = false

    override fun onFragmentAttached(fragment: Fragment) {
        when (fragment) {
            is IFragmentStyleable -> updateAppBar(
                AppBarSettings(
                    navDrawerLocked = fragment.navDrawerLocked,
                    isFullScreen = fragment.isFullScreen
                )
            )
            else -> updateAppBar(AppBarSettings())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler())
        controller.start(HostMessages.msgInit(savedInstanceState != null))
        uiScope.launch {
            val renders = listOfNotNull(
                HostRenders.renderDrawer(navigationDrawer),
                HostRenders.renderFullScreen(window),
                HostRenders.renderLoader(loading_overlay),
                HostRenders.renderUpdateLoading(loading_overlay, pb_loading, tv_loader),
                (navigationDrawer.drawerItems.first { it.identifier == NAVIGATION_INFO } as? MenuDrawerItem)?.let {
                    HostRenders.renderAppInfo(it, resources, navigationDrawer)
                },
                (navigationDrawer.drawerItems.first { it.identifier == NAVIGATION_ENTRANCES_INFO } as? MenuDrawerItem)?.let {
                    HostRenders.renderEntrancesInfo(it, resources, navigationDrawer)
                },
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            //launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        prepareNavigation()
        controller.context.errorContext.attach(window.decorView.rootView)
        controller.context.copyToClipboard = ::copyToClipboard
        controller.context.showUpdateDialog = ::showUpdateDialog
        controller.context.showErrorDialog = ::showErrorDialog
        controller.context.installUpdate = ::installUpdate
        controller.context.showTaskUpdateRequired = ::showTaskUpdateRequiredDialog
        controller.context.finishApp = { finish() }

        controller.context.featureCheckersContainer = featureCheckersContainer
    }

    private fun showTaskUpdateRequiredDialog() {
        if (taskUpdateRequiredDialogShowed) {
            return
        }
        taskUpdateRequiredDialogShowed = true
        showDialog(
            R.string.task_update_required,
            R.string.ok to {
                uiScope.sendMessage(controller, HostMessages.msgRequiredUpdateOk())
                taskUpdateRequiredDialogShowed = false
            },
            R.string.later to {
                uiScope.sendMessage(controller, HostMessages.msgRequiredUpdateLater())
                taskUpdateRequiredDialogShowed = false
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        featureCheckersContainer.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_INSTALL_PACKAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (resultCode == Activity.RESULT_OK && packageManager.canRequestPackageInstalls()) {
                uiScope.sendMessage(controller, HostMessages.msgRequestUpdates())
            } else {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(
                        Uri.parse(
                            String.format(
                                "package:%s",
                                packageName
                            )
                        )
                    ),
                    REQUEST_CODE_INSTALL_PACKAGE
                )
            }
        }
    }

    private fun showErrorDialog(stringResource: Int) {
        showDialog(
            stringResource,
            R.string.ok to {}
        )
    }

    private fun installUpdate(updateFile: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setDataAndType(
                    FileProvider.getUriForFile(this@HostActivity, "ru.relabs.kurjercontroller.file_provider", updateFile),
                    "application/vnd.android.package-archive"
                )
            } else {
                setDataAndType(Uri.fromFile(updateFile), "application/vnd.android.package-archive")
            }
        }
        startActivity(intent)
    }

    private fun showUpdateDialog(appUpdate: AppUpdate): Boolean {
        if (isUpdateAppDialogShowed) {
            return true
        }
        if (appUpdate.version > BuildConfig.VERSION_CODE) {
            isUpdateAppDialogShowed = true

            showDialog(
                R.string.update_new_available,

                R.string.update_install to {
                    isUpdateAppDialogShowed = false
                    checkUpdateRequirements(appUpdate.url)
                    uiScope.sendMessage(controller, HostMessages.msgUpdateDialogShowed(false))
                },

                (R.string.update_later to {
                    isUpdateAppDialogShowed = false
                    uiScope.sendMessage(
                        controller,
                        HostMessages.msgUpdateDialogShowed(false)
                    )
                }).takeIf { !appUpdate.isRequired }
            )
            return true
        }
        return false
    }

    private fun checkUpdateRequirements(url: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(
                        Uri.parse(
                            String.format(
                                "package:%s",
                                packageName
                            )
                        )
                    ),
                    REQUEST_CODE_INSTALL_PACKAGE
                )
            } else {
                uiScope.sendMessage(controller, HostMessages.msgStartUpdateLoading(url))
            }
        } else {
            uiScope.sendMessage(controller, HostMessages.msgStartUpdateLoading(url))
        }
    }

    private fun copyToClipboard(text: String) {
        when (val r = ClipboardHelper.copyToClipboard(this, text)) {
            is Right ->
                showSnackbar(
                    resources.getString(R.string.copied_to_clipboard),
                    resources.getString(R.string.send) to { sendDeviceUUID(text) }
                )
            is Left ->
                showSnackbar(resources.getString(R.string.unknown_runtime_error))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        featureCheckersContainer.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun sendDeviceUUID(text: String) {
        startActivity(
            Intent.createChooser(
                IntentUtils.getShareTextIntent(getString(R.string.share_device_uuid_subject), text),
                getString(R.string.share_device_uuid_title)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
        supervisor.cancelChildren()
        controller.context.errorContext.detach()
        controller.context.showUpdateDialog = { false }
        controller.context.showErrorDialog = {}
        controller.context.installUpdate = {}
        controller.context.showTaskUpdateRequired = {}
        controller.context.finishApp = {}
        navigationHolder.removeNavigator()
        featureCheckersContainer.onDestroy()
        systemWatchersContainer.onDestroy()
    }

    private fun prepareNavigation() {
        navigationHolder.setNavigator(navigator)
        navigationDrawer = with(DrawerBuilder()) {
            withActivity(this@HostActivity)

            withSliderBackgroundDrawable(NavDrawerBackgroundDrawable(resources))
            withTranslucentStatusBar(false)
            withDisplayBelowStatusBar(true)
            withActionBarDrawerToggle(true)
            withHeaderPadding(false)
            withHeaderDivider(false)

            withHeader(inflateNavigationHeader())

            withHeaderHeight(
                DimenHolder.fromPixel(
                    resources.getDimensionPixelSize(R.dimen.navigation_header_height) + UIUtils.getStatusBarHeight(
                        this@HostActivity
                    )
                )
            )

            addDrawerItems(*buildDrawerItems())
            withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    return when (drawerItem.identifier) {
                        NAVIGATION_CRASH -> sendCrashLog()
                        NAVIGATION_UUID -> copyDeviceId()
                        NAVIGATION_LOGOUT -> logout()
                        else -> true
                    }
                }
            })

            build()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun copyDeviceId(): Boolean {
        uiScope.sendMessage(controller, HostMessages.msgCopyDeviceUUID())
        return false
    }

    private fun sendCrashLog(): Boolean {
        when (val r = CustomLog.share(this)) {
            is Left -> when (val e = r.value) {
                is FileNotFoundException -> showSnackbar(resources.getString(R.string.crash_log_not_found))
                else -> showSnackbar(resources.getString(R.string.unknown_runtime_error))
            }
        }
        return false
    }

    private fun logout(): Boolean {
        uiScope.sendMessage(controller, HostMessages.msgLogout())
        return false
    }

    private fun inflateNavigationHeader(): View {
        val header = LayoutInflater.from(this).inflate(R.layout.nav_header, null, false)
        header.text_container.setPadding(0, UIUtils.getStatusBarHeight(this), 0, 0)
        return header
    }

    private fun buildDrawerItems(): Array<IDrawerItem<*>> {
        return arrayOf(
            buildDrawerItem(
                NAVIGATION_ENTRANCES_INFO,
                resources.getString(R.string.menu_entrances_required, 0),
                false
            ),
            buildDrawerItem(
                NAVIGATION_CRASH,
                R.string.menu_info
            ),
            buildDrawerItem(
                NAVIGATION_UUID,
                R.string.menu_uuid
            ),
            buildDrawerItem(
                NAVIGATION_LOGOUT,
                R.string.menu_logout
            ),
            buildDrawerItem(
                NAVIGATION_INFO,
                resources.getString(R.string.menu_bottom_info, BuildConfig.VERSION_CODE, "-"),
                false
            )
        )
    }

    private fun buildDrawerItem(id: Long, stringResId: Int): IDrawerItem<*> {
        return MenuDrawerItem(0)
            .withIdentifier(id)
            .withName(stringResId)
    }

    private fun buildDrawerItem(id: Long, string: String, selectable: Boolean = true): IDrawerItem<*> {
        return MenuDrawerItem(0)
            .withIdentifier(id)
            .withName(string)
            .withSelectable(selectable)
    }

    override fun onResume() {
        super.onResume()
        systemWatchersContainer.onResume()
        uiScope.sendMessage(controller, HostMessages.msgResume())
        ReportService.isAppPaused = false

        if (!ReportService.isRunning) {
            startService(Intent(this, ReportService::class.java))
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        systemWatchersContainer.onPause()
        navigationHolder.removeNavigator()
        uiScope.sendMessage(controller, HostMessages.msgPause())
        ReportService.isAppPaused = true
    }


    override fun onBackPressed() {
        if (navigationDrawer.isDrawerOpen) {
            navigationDrawer.closeDrawer()
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if ((currentFragment as? BaseFragment)?.interceptBackPressed() != true) {
                super.onBackPressed()
            }
        }
    }

    fun updateAppBar(settings: AppBarSettings) {
        uiScope.sendMessage(controller, HostMessages.msgUpdateAppBar(settings))
    }

    fun changeNavigationDrawerState() {
        if (navigationDrawer.isDrawerOpen) {
            navigationDrawer.closeDrawer()
        } else {
            navigationDrawer.openDrawer()
        }
    }

    companion object {
        const val REQUEST_CODE_INSTALL_PACKAGE = 997

        const val NAVIGATION_CRASH = 2L
        const val NAVIGATION_UUID = 3L
        const val NAVIGATION_LOGOUT = 4L
        const val NAVIGATION_INFO = 999L
        const val NAVIGATION_ENTRANCES_INFO = 998L

        fun getIntent(parentContext: Context) = Intent(parentContext, HostActivity::class.java)
    }
}
