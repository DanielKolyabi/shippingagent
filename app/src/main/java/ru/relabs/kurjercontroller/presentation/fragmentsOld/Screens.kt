package ru.relabs.kurjercontroller.presentation.fragmentsOld

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.filters.editor.FiltersEditorFragment
import ru.relabs.kurjercontroller.presentation.filters.editor.IFiltersEditorConsumer
import ru.relabs.kurjercontroller.presentation.filters.pager.FiltersPagerFragment
import ru.relabs.kurjercontroller.presentation.filters.pager.IFiltersConsumer
import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.ReportPagerFragment
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressYandexMapFragment
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
    private val selectedTaskId: TaskId,
    private val selectedTaskItemId: TaskItemId
) :
    SupportAppScreen() {
    override fun getFragment(): Fragment {
        return ReportPagerFragment.newInstance(
            taskItems.map { it.first },
            taskItems.map { it.second },
            selectedTaskId.id,
            selectedTaskItemId.id
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

