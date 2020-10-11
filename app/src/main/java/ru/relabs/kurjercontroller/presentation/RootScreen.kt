package ru.relabs.kurjercontroller.presentation

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.addresses.AddressesFragment
import ru.relabs.kurjercontroller.presentation.login.LoginFragment
import ru.relabs.kurjercontroller.presentation.taskDetails.IExaminedConsumer
import ru.relabs.kurjercontroller.presentation.taskDetails.TaskDetailsFragment
import ru.relabs.kurjercontroller.presentation.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.presentation.tasks.TasksFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen


sealed class RootScreen(protected val fabric: () -> Fragment) : SupportAppScreen() {

    override fun getFragment(): Fragment = fabric()

    object Login : RootScreen({ LoginFragment.newInstance() })

    class Tasks(withRefresh: Boolean) : RootScreen({ TasksFragment.newInstance(withRefresh) })

    class TaskInfo<F>(task: Task, parent: F) :
        RootScreen({ TaskDetailsFragment.newInstance(task, parent) }) where F : Fragment, F : IExaminedConsumer

    class TaskItemDetails(taskItem: TaskItem) : RootScreen({ TaskItemExplanationFragment.newInstance(taskItem) })

    class Addresses(tasks: List<Task>) : RootScreen({ AddressesFragment.newInstance(tasks.map { it.id }) })
}
