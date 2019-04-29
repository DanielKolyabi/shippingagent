package ru.relabs.kurjercontroller.ui.fragments

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListFragment
import ru.relabs.kurjercontroller.ui.fragments.filters.FILTERS_REQUEST_CODE
import ru.relabs.kurjercontroller.ui.fragments.filters.FiltersFragment
import ru.relabs.kurjercontroller.ui.fragments.login.LoginFragment
import ru.relabs.kurjercontroller.ui.fragments.report.ReportPagerFragment
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoFragment
import ru.relabs.kurjercontroller.ui.fragments.taskItemExplanation.TaskItemExplanationFragment
import ru.relabs.kurjercontroller.ui.fragments.taskList.TaskListFragment
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.YandexMapFragment
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
    private val taskItems: List<Pair<TaskModel, TaskItemModel>>,
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

class YandexMapScreen(
    private val addresses: List<AddressModel>,
    private val onAddressClicked: suspend (address: AddressModel) -> Unit
) :
    SupportAppScreen() {
    override fun getFragment(): Fragment {
        return YandexMapFragment.newInstance(addresses.map { it.id }).apply {
            setCallback(object: YandexMapFragment.Callback{
                override suspend fun onAddressClicked(address: AddressModel) {
                    this@YandexMapScreen.onAddressClicked(address)
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

class TaskItemExplanationScreen(private val taskItem: TaskItemModel) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TaskItemExplanationFragment.newInstance(taskItem)
    }
}

class FiltersScreen(val target: Fragment, val filters: TaskFiltersModel? = null) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return FiltersFragment.newInstance(filters).apply {
            setTargetFragment(target, FILTERS_REQUEST_CODE)
        }
    }
}