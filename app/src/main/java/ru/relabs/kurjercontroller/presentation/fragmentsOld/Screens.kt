package ru.relabs.kurjercontroller.presentation.fragmentsOld

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.AddressListFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressYandexMapFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.FiltersFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.FiltersPagerFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.ReportPagerFragment
import ru.relabs.kurjercontroller.presentation.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.TaskListFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.TasksYandexMapFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.BaseYandexMapFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

/**
 * Created by ProOrange on 20.03.2019.
 */


class AddressListScreen(private val taskIds: List<Int>) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return AddressListFragment.newInstance(taskIds)
    }
}

class ReportScreen(
    private val taskItems: List<Pair<Task, TaskItem>>,
    private val selectedTaskId: Int,
    private val selectedTaskItemId: Int
) :
    SupportAppScreen() {
    override fun getFragment(): Fragment {
        return ReportPagerFragment.newInstance(
            taskItems.map { it.first },
            taskItems.map { it.second },
            selectedTaskId,
            selectedTaskItemId
        )
    }
}

class TasksYandexMapScreen(
    private val tasks: List<Task>,
    private val onAddressClicked: suspend (address: Address) -> Unit,
    private val onNewAddressesAdded: () -> Unit
) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TasksYandexMapFragment.newInstance(tasks).apply {
            setOnClickCallback(object : BaseYandexMapFragment.Callback {
                override suspend fun onAddressClicked(address: Address) {
                    this@TasksYandexMapScreen.onAddressClicked(address)
                }
            })
            this.onNewTaskItemsAdded = onNewAddressesAdded
        }
    }
}

class AddressYandexMapScreen(
    private val addresses: List<AddressWithColor>,
    private val deliverymanIds: List<Int>,
    private val storages: List<TaskStorage>,
    private val onAddressClicked: suspend (address: Address) -> Unit
) :
    SupportAppScreen() {
    override fun getFragment(): Fragment {
        return AddressYandexMapFragment.newInstance(addresses, deliverymanIds, storages).apply {
            setOnClickCallback(object : BaseYandexMapFragment.Callback {
                override suspend fun onAddressClicked(address: Address) {
                    this@AddressYandexMapScreen.onAddressClicked(address)
                }
            })
        }
    }
}

class FiltersScreen(
    val tasks: List<Task>,
    private val onAllFiltersApplied: () -> Unit
) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        val fragment = FiltersPagerFragment.newInstance(tasks)
        fragment.onAllFiltersApplied = onAllFiltersApplied
        return fragment
    }
}

class OnlineFiltersScreen(
    private val onStartClicked: (filters: TaskFilters, withPlanned: Boolean) -> Unit
) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return FiltersFragment.newInstance(null, false).apply {
            onStartClicked = this@OnlineFiltersScreen.onStartClicked
        }
    }
}