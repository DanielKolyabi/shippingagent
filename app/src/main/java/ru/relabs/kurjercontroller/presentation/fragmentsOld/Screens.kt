package ru.relabs.kurjercontroller.presentation.fragmentsOld

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.presentation.tasks.TasksFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

/**
 * Created by ProOrange on 20.03.2019.
 */


//class AddressListScreen(private val taskIds: List<Int>) : SupportAppScreen() {
//    override fun getFragment(): Fragment {
//        return AddressListFragment.newInstance(taskIds)
//    }
//}


class TasksYandexMapScreen(
    private val tasks: List<Task>,
    private val onAddressClicked: suspend (address: Address) -> Unit,
    private val onNewAddressesAdded: () -> Unit
) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TasksFragment.newInstance(false)
        //TODO: Temp
//        return TasksYandexMapFragment.newInstance(tasks).apply {
//            setOnClickCallback(object : BaseYandexMapFragment.Callback {
//                override suspend fun onAddressClicked(address: Address) {
//                    this@TasksYandexMapScreen.onAddressClicked(address)
//                }
//            })
//            this.onNewTaskItemsAdded = onNewAddressesAdded
//        }
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
        return TasksFragment.newInstance(false)
//        return AddressYandexMapFragment.newInstance(addresses, deliverymanIds, storages).apply {
//            setOnClickCallback(object : BaseYandexMapFragment.Callback {
//                override suspend fun onAddressClicked(address: Address) {
//                    this@AddressYandexMapScreen.onAddressClicked(address)
//                }
//            })
//        }
    }
}

