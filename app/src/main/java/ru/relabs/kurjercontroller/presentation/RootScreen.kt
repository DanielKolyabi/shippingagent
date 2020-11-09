package ru.relabs.kurjercontroller.presentation

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.addresses.AddressesFragment
import ru.relabs.kurjercontroller.presentation.filters.editor.FiltersEditorFragment
import ru.relabs.kurjercontroller.presentation.filters.editor.IFiltersEditorConsumer
import ru.relabs.kurjercontroller.presentation.filters.pager.FiltersPagerFragment
import ru.relabs.kurjercontroller.presentation.filters.pager.IFiltersConsumer
import ru.relabs.kurjercontroller.presentation.login.LoginFragment
import ru.relabs.kurjercontroller.presentation.reportPager.ReportPagerFragment
import ru.relabs.kurjercontroller.presentation.reportPager.TaskItemWithTaskIds
import ru.relabs.kurjercontroller.presentation.taskDetails.IExaminedConsumer
import ru.relabs.kurjercontroller.presentation.taskDetails.TaskDetailsFragment
import ru.relabs.kurjercontroller.presentation.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.presentation.tasks.TasksFragment
import ru.relabs.kurjercontroller.presentation.yandexMap.YandexMapFragment
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.IAddressClickedConsumer
import ru.terrakok.cicerone.android.support.SupportAppScreen


sealed class RootScreen(protected val fabric: () -> Fragment) : SupportAppScreen() {

    override fun getFragment(): Fragment = fabric()

    object Login : RootScreen({ LoginFragment.newInstance() })

    class Tasks(withRefresh: Boolean) : RootScreen({ TasksFragment.newInstance(withRefresh) })

    class TaskInfo<F>(task: Task, parent: F) :
        RootScreen({ TaskDetailsFragment.newInstance(task, parent) }) where F : Fragment, F : IExaminedConsumer

    class TaskItemDetails(taskItem: TaskItem) : RootScreen({ TaskItemExplanationFragment.newInstance(taskItem) })

    class Addresses(tasks: List<Task>) : RootScreen({ AddressesFragment.newInstance(tasks.map { it.id }) })

    class Filters<T>(
        private val tasks: List<Task>,
        private val target: T
    ) : RootScreen({ FiltersPagerFragment.newInstance(tasks, target) }) where T : Fragment, T : IFiltersConsumer

    class OnlineFilters<T>(private val target: T) : RootScreen({
        FiltersEditorFragment.newInstance(
            TaskId(-1),
            null,
            false,
            true,
            target
        )
    }) where T : Fragment, T : IFiltersEditorConsumer

    class Report(
        private val taskItems: List<TaskItem>,
        private val selectedTaskId: TaskId,
        private val selectedTaskItemId: TaskItemId
    ) : RootScreen({
        ReportPagerFragment.newInstance(
            taskItems.map { TaskItemWithTaskIds(it.taskId, it.id) },
            TaskItemWithTaskIds(selectedTaskId, selectedTaskItemId)
        )
    })

    class AddressMap<T>(
        private val addresses: List<AddressWithColor>,
        private val deliverymanIds: List<Int>,
        private val storages: List<TaskStorage>,
        private val target: T
    ) : RootScreen({
        YandexMapFragment.newInstance(
            addresses.map { AddressIdWithColor(it.address.id.id, it.color, it.outlineColor) },
            deliverymanIds,
            storages,
            target
        )
    }) where T : Fragment, T : IAddressClickedConsumer
}
