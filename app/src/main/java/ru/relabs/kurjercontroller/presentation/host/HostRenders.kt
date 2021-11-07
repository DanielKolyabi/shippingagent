package ru.relabs.kurjercontroller.presentation.host

import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import com.mikepenz.materialdrawer.Drawer
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.utils.extensions.visible

object HostRenders {
    fun renderDrawer(navigationDrawer: Drawer): HostRender = renderT(
        { state -> state.settings.navDrawerLocked },
        { locked ->
            navigationDrawer.drawerLayout.setDrawerLockMode(
                if (locked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED
            )
        }
    )

    fun renderFullScreen(window: Window): HostRender = renderT(
        { it.settings.isFullScreen },
        {
            when (it) {
                true -> {
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        window.attributes.layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        )
                    }
                }
                false -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        window.attributes.layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                    }
                }
            }
        }
    )

    fun renderUpdateLoading(view: View, progressBar: ProgressBar, progressText: TextView): HostRender = renderT(
        { it.updateLoadProgress },
        { progress ->
            view.visible = progress != null
            progressBar.isIndeterminate = progress == null
            progressText.visible = progress != null

            if (progress != null) {
                progressBar.max = 100
                progressBar.progress = progress

                progressText.text = progressText.resources.getString(R.string.update_progress, progress)
            }

        }
    )

    fun renderLoader(view: View): HostRender = renderT(
        { it.loaders > 0 },
        { visible ->
            view.visible = visible
        }
    )

    fun renderAppInfo(item: MenuDrawerItem, resources: Resources, navDrawer: Drawer): HostRender = renderT(
        { it.userLogin },
        {
            navDrawer.updateItem(
                item.withName(
                    resources.getString(
                        R.string.menu_bottom_info,
                        BuildConfig.VERSION_CODE,
                        it?.login ?: "???"
                    )
                )
            )
        }
    )

    fun renderEntrancesInfo(item: MenuDrawerItem, resources: Resources, navDrawer: Drawer): HostRender = renderT(
        { Triple(it.closedEntrances, it.requiredEntrances, it.isClosedCounterEnabled) },
        { (closed, required, isCounterEnabled) ->
            val text = resources.getString(
                R.string.menu_entrances_required,
                required
            ) + if (isCounterEnabled) {
                "\n" + resources.getString(
                    R.string.menu_entrances_closed,
                    closed
                )
            } else {
                ""
            }

            navDrawer.updateItem(item.withName(text))
        }
    )
}