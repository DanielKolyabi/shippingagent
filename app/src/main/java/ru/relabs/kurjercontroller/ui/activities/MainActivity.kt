package ru.relabs.kurjercontroller.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.LoginScreen
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListFragment
import ru.relabs.kurjercontroller.ui.fragments.entrancesList.EntrancesListFragment
import ru.relabs.kurjercontroller.ui.fragments.login.LoginFragment
import ru.relabs.kurjercontroller.ui.fragments.report.ReportFragment
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoFragment
import ru.relabs.kurjercontroller.ui.fragments.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.ui.fragments.taskList.TaskListFragment
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.YandexMapFragment
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command


class MainActivity : AppCompatActivity() {
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
        hideActionBar()
        setContentView(R.layout.activity_main)
        loading?.setVisible(false)
        requestPermissions()
        application().router.newRootScreen(LoginScreen())

        bindBackstackListener()
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

    private fun onFragmentChanged(current: Fragment?) {
        if (current == null) {
            return
        }

        refresh_button?.setVisible(current is TaskListFragment)

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
            is ReportFragment -> {
                back_button?.setVisible(true)
            }
            is YandexMapFragment -> {
                back_button?.setVisible(true)
            }
            //TODO: Report
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
        application().router.exit()
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

    fun showLogin() {
    }
}
