package ru.relabs.kurjercontroller.ui.activities

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.*
import ru.relabs.kurjercontroller.application.MyApplication
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.ISearchableFragment
import ru.relabs.kurjercontroller.ui.fragments.LoginScreen
import ru.relabs.kurjercontroller.ui.fragments.SearchInputAdapter
import ru.relabs.kurjercontroller.ui.fragments.TaskListScreen
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListFragment
import ru.relabs.kurjercontroller.ui.fragments.entrancesList.EntrancesListFragment
import ru.relabs.kurjercontroller.ui.fragments.filters.FiltersFragment
import ru.relabs.kurjercontroller.ui.fragments.login.LoginFragment
import ru.relabs.kurjercontroller.ui.fragments.report.ReportPagerFragment
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoFragment
import ru.relabs.kurjercontroller.ui.fragments.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.ui.fragments.taskList.TaskListFragment
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.YandexMapFragment
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command


class MainActivity : AppCompatActivity() {
    val bgScope = CancelableScope(Dispatchers.Main)
    private var needRefreshShowed = false
    private var needForceRefresh = false

    private var intentFilter = IntentFilter("NOW")
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
//            if (intent.getBooleanExtra("network_disabled", false)) {
//                showNetworkDisabledError(!blockingNetworkDisabled)
//            }
            if (intent.getBooleanExtra("tasks_changed", false)) {
                if (needRefreshShowed) return
                needRefreshShowed = true
                showTasksRefreshDialog(true)
            }
            if (intent.getIntExtra("task_closed", 0) != 0) {
                val repository = application().tasksRepository
                bgScope.launch(Dispatchers.Default) {
                    val taskId = intent.getIntExtra("task_closed", -1)
                    val taskItemId = intent.getIntExtra("task_item_closed", -1)
                    val entranceNumber = intent.getIntExtra("entrance_number_closed", -1)
                    if (taskId == -1 || taskItemId == -1 || entranceNumber == -1) {
                        return@launch
                    }
                    repository.closeEntrance(taskId, taskItemId, entranceNumber)
                }
            }
        }
    }

    private fun showTasksRefreshDialog(cancelable: Boolean) {
        val negative = if (cancelable) "Позже" else ""
        showError("Необходимо обновить список заданий.", object : ErrorButtonsListener {
            override fun positiveListener() {
                needRefreshShowed = false
                needForceRefresh = false
                application().router.backTo(TaskListScreen(true))
            }

            override fun negativeListener() {
                needForceRefresh = true
            }
        }, "Ок", negative)
    }

    private val navigator = object : SupportAppNavigator(this, supportFragmentManager, R.id.fragment_container) {

        override fun setupFragmentTransaction(
            command: Command?,
            currentFragment: Fragment?,
            nextFragment: Fragment?,
            fragmentTransaction: FragmentTransaction?
        ) {
            super.setupFragmentTransaction(command, currentFragment, nextFragment, fragmentTransaction)
            fragmentTransaction?.setCustomAnimations(
                R.anim.enter_from_left,
                R.anim.exit_to_right,
                R.anim.enter_from_right,
                R.anim.exit_to_left
            )
        }

        override fun createFragment(screen: SupportAppScreen?): Fragment {
            val fragment = super.createFragment(screen)
            onFragmentChanged(fragment)
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler())
        hideActionBar()
        setContentView(R.layout.activity_main)
        loading?.setVisible(false)
        requestPermissions()

        application().router.newRootScreen(LoginScreen())

        bindBackstackListener()
        bindControls()

        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun bindControls() {
        back_button?.setOnClickListener {
            onBackPressed()
        }
        device_uuid.setOnClickListener {
            val deviceUUID = (application as? MyApplication)?.deviceUUID?.split("-")?.last() ?: ""
            if (deviceUUID == "") {
                showError("Не удалось получить device UUID")
                return@setOnClickListener
            }
            showError("Device UUID part: $deviceUUID", object : ErrorButtonsListener {
                override fun positiveListener() {
                    try {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.primaryClip = ClipData.newPlainText("Device UUID", deviceUUID)
                        Toast.makeText(this@MainActivity, "Скопированно в буфер обмена", Toast.LENGTH_LONG).show()
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this@MainActivity, "Произошла ошибка", Toast.LENGTH_LONG).show()
                    }
                }

                override fun negativeListener() {
                    try {
                        CustomLog.share(this@MainActivity)
                    } catch (e: java.lang.Exception) {
                        CustomLog.writeToFile(CustomLog.getStacktraceAsString(e))
                        Toast.makeText(this@MainActivity, "Произошла ошибка", Toast.LENGTH_LONG).show()
                    }
                }
            }, "Скопировать", "Отправить crash.log", cancelable = true)
        }
        search_button.setOnClickListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (current !is TaskListFragment && current !is AddressListFragment) {
                setSearchButtonVisible(false)
                return@setOnClickListener
            }
            setSearchInputVisible(true)
            setTitleVisible(false)
            setDeviceIdButtonVisible(false)
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(
                search_input,
                InputMethodManager.SHOW_IMPLICIT
            )
            search_input.requestFocus()
        }

        val adapter = SearchInputAdapter(this, R.layout.item_search, R.id.text, supportFragmentManager)
        search_input.setAdapter(adapter)

        search_input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                event != null &&
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {

                val current = supportFragmentManager.findFragmentById(R.id.fragment_container) as? ISearchableFragment
                current ?: return@setOnEditorActionListener true

                current.onItemSelected(search_input?.text.toString(), search_input)

                (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    search_input.windowToken,
                    0
                )

                setSearchInputVisible(false)
                setTitleVisible(true)

                if (current is TaskListFragment) {
                    setDeviceIdButtonVisible(true)
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun bindBackstackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            onFragmentChanged(current)
        }
    }

    fun changeTitle(string: String) {
        top_app_bar?.title?.text = string
    }

    fun setTitleVisible(visible: Boolean) {
        top_app_bar?.title?.setVisible(visible)
    }

    private fun setDeviceIdButtonVisible(visible: Boolean) {
        device_uuid?.setVisible(visible)
    }

    private fun setSearchButtonVisible(visible: Boolean) {
        search_button.setVisible(visible)
        if (!visible) {
            setSearchInputVisible(visible)
        }
    }

    private fun setSearchInputVisible(visible: Boolean) {
        search_input.setVisible(visible)
        search_input.setText("")
    }


    private fun onFragmentChanged(current: Fragment?) {
        if (current == null) {
            return
        }

        refresh_button?.setVisible(current is TaskListFragment)
        setSearchButtonVisible(current is TaskListFragment || current is AddressListFragment)
        setDeviceIdButtonVisible(current is TaskListFragment || current is LoginFragment)
        setTitleVisible(true)

        when (current) {
            is LoginFragment -> {
                back_button?.setVisible(false)
                changeTitle("Авторизация")
            }
            is TaskListFragment -> {
                back_button?.setVisible(false)
                changeTitle("Список заданий")
            }
            is AddressListFragment -> {
                back_button?.setVisible(true)
                changeTitle("Список адресов")
            }
            is TaskInfoFragment -> {
                back_button?.setVisible(true)
                changeTitle("Детали задания")
            }
            is TaskItemExplanationFragment -> {
                back_button?.setVisible(true)
                changeTitle("Пояснения к заданию")
            }
            is EntrancesListFragment -> {
                back_button?.setVisible(true)
                changeTitle("Список подъездов")
            }
            is YandexMapFragment -> {
                back_button?.setVisible(true)
            }
            is FiltersFragment -> {
                back_button?.setVisible(true)
                changeTitle("Фильтры")
            }
            is ReportPagerFragment -> {
                back_button?.setVisible(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        application().enableLocationListening()
        application().navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        application().disableLocationListening()
        application().navigatorHolder.removeNavigator()
    }

    override fun onBackPressed() {
        if (search_input.visibility == View.VISIBLE) {
            setSearchInputVisible(false)
            setTitleVisible(true)
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (current is TaskListFragment) {
                setDeviceIdButtonVisible(true)
            }
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                search_input.windowToken,
                0
            )

        } else {
            application().router.exit()
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.FOREGROUND_SERVICE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        PermissionHelper.showPermissionsRequest(this, permissions.toTypedArray(), false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            permissions.indexOfFirst { it == android.Manifest.permission.ACCESS_FINE_LOCATION }.let {
                if (it >= 0 && grantResults[it] == PackageManager.PERMISSION_GRANTED && !application().enableLocationListening()) {
                    showError("Невозможно включить геолокацию")
                }
            }
            val deniedPermission = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            PermissionHelper.showPermissionsRequest(this, deniedPermission.toTypedArray(), true)
        } else {
            supportFragmentManager.findFragmentByTag("fragment")
                ?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
