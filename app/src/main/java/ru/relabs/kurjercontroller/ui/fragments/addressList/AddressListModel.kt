package ru.relabs.kurjercontroller.ui.fragments.addressList

import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel

/**
 * Created by ProOrange on 09.08.2018.
 */
sealed class AddressListModel {

    data class Address(
        val taskItems: MutableList<TaskItemModel>
    ) : AddressListModel()

    data class TaskItem(
        val taskItem: TaskItemModel,
        val parentTask: TaskModel
    ) : AddressListModel()

    data class SortingItem(val sortType: Int) : AddressListModel()
    object Loader : AddressListModel()
}