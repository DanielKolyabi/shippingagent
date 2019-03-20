package ru.relabs.kurjercontroller.ui.fragments

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.ui.fragments.login.LoginFragment
import ru.relabs.kurjercontroller.ui.fragments.taskList.TaskListFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

/**
 * Created by ProOrange on 20.03.2019.
 */

class LoginScreen: SupportAppScreen() {
    override fun getFragment(): Fragment {
        return LoginFragment()
    }
}

class TaskListScreen: SupportAppScreen(){
    override fun getFragment(): Fragment {
        return TaskListFragment()
    }
}