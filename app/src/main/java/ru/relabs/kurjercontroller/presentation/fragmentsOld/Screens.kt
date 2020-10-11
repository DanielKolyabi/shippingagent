package ru.relabs.kurjercontroller.presentation.fragmentsOld

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.AddressListFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressYandexMapFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.FiltersFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.FiltersPagerFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.login.LoginFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.ReportPagerFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.TaskInfoFragment
import ru.relabs.kurjercontroller.presentation.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.TaskListFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.TasksYandexMapFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.BaseYandexMapFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

/**
 * Created by ProOrange on 20.03.2019.
 */

class LoginScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return LoginFragment()
    }
}

class TaskListScreen(private val loadFromNetwork: Boolean) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TaskListFragment.newInstance(loadFromNetwork)
    }
}

class AddressListScreen(private val taskIds: List<Int>) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return AddressListFragment.newInstance(taskIds)
    }
}

class ReportScreen(
    private val taskItems: List<Pair<TaskModel, TaskItem>>,
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
    private val tasks: List<TaskModel>,
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

class TaskInfoScreen(private val task: TaskModel) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TaskInfoFragment.newInstance(task)
    }
}

class TaskItemExplanationScreen(private val taskItem: TaskItem) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TaskItemExplanationFragment.newInstance(taskItem)
    }
}

class FiltersScreen(
    val tasks: List<TaskModel>,
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