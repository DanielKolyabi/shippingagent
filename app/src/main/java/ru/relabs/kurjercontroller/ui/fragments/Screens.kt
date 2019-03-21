package ru.relabs.kurjercontroller.ui.fragments

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.login.LoginFragment
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoFragment
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
        return TaskListFragment.newInstance(true)
    }
}

class TaskInfoScreen(private val task: TaskModel): SupportAppScreen(){
    override fun getFragment(): Fragment {
        return TaskInfoFragment.newInstance(task)
    }
}