package ru.relabs.kurjercontroller.presentation

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.presentation.login.LoginFragment
import ru.relabs.kurjercontroller.presentation.tasks.TasksFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen


sealed class RootScreen(protected val fabric: () -> Fragment) : SupportAppScreen() {

    override fun getFragment(): Fragment = fabric()

    object Login : RootScreen({ LoginFragment.newInstance() })

    class Tasks(withRefresh: Boolean) : RootScreen({ TasksFragment.newInstance(withRefresh) })
}
