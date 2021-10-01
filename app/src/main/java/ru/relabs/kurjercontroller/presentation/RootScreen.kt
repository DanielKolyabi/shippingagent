package ru.relabs.kurjercontroller.presentation

import androidx.fragment.app.Fragment
import com.github.terrakok.cicerone.androidx.Creator
import com.github.terrakok.cicerone.androidx.FragmentScreen
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
import ru.relabs.kurjercontroller.presentation.yandexMap.models.INewItemsAddedConsumer


object RootScreen {

    fun Login() =
        KurjerFragmentScreen { LoginFragment.newInstance() }

    fun Tasks(withRefresh: Boolean) =
        KurjerFragmentScreen { TasksFragment.newInstance(withRefresh) }

    fun <F> TaskInfo(task: Task, parent: F) where F : Fragment, F : IExaminedConsumer =
        KurjerFragmentScreen { TaskDetailsFragment.newInstance(task, parent) }

    fun TaskItemDetails(taskItem: TaskItem) =
        KurjerFragmentScreen { TaskItemExplanationFragment.newInstance(taskItem) }

    fun Addresses(tasks: List<Task>) =
        KurjerFragmentScreen { AddressesFragment.newInstance(tasks.map { it.id }) }

    fun <T> Filters(
        tasks: List<Task>,
        target: T
    ) where T : Fragment, T : IFiltersConsumer =
        KurjerFragmentScreen { FiltersPagerFragment.newInstance(tasks, target) }

    fun <T> OnlineFilters(target: T) where T : Fragment, T : IFiltersEditorConsumer = KurjerFragmentScreen {
        FiltersEditorFragment.newInstance(
            TaskId(-1),
            null,
            false,
            true,
            target
        )
    }

    fun Report(
        taskItems: List<TaskItem>,
        selectedTaskId: TaskId,
        selectedTaskItemId: TaskItemId
    ) = KurjerFragmentScreen {
        ReportPagerFragment.newInstance(
            taskItems.map { TaskItemWithTaskIds(it.taskId, it.id) },
            TaskItemWithTaskIds(selectedTaskId, selectedTaskItemId)
        )
    }

    fun <T> AddressMap(
        addresses: List<AddressWithColor>,
        deliverymanIds: List<Int>,
        storages: List<TaskStorage>,
        target: T
    ) where T : Fragment, T : IAddressClickedConsumer, T : INewItemsAddedConsumer = KurjerFragmentScreen {
        YandexMapFragment.newInstance(
            emptyList(),
            addresses.map { AddressIdWithColor(it.address.id.id, it.color, it.outlineColor) },
            deliverymanIds,
            storages,
            target
        )
    }

    fun <T> TasksMap(
        tasks: List<Task>,
        target: T
    ) where T : Fragment, T : IAddressClickedConsumer, T : INewItemsAddedConsumer = KurjerFragmentScreen {
        YandexMapFragment.newInstance(
            tasks.map { it.id },
            emptyList(),
            emptyList(),
            emptyList(),
            target
        )
    }


    inline fun <reified F : Fragment> KurjerFragmentScreen(crossinline fragmentCreator: () -> F) =
        FragmentScreen(F::class.java.simpleName, fragmentCreator = Creator { fragmentCreator() })
}