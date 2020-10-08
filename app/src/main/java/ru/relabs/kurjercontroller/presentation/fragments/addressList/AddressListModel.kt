package ru.relabs.kurjercontroller.presentation.fragments.addressList

import ru.relabs.kurjercontroller.domain.models.TaskModel

/**
 * Created by ProOrange on 09.08.2018.
 */
sealed class AddressListModel {

    data class Address(
        val taskItems: MutableList<ru.relabs.kurjercontroller.domain.models.TaskItem>
    ) : AddressListModel()

    data class TaskItem(
        val taskItem: ru.relabs.kurjercontroller.domain.models.TaskItem,
        val parentTask: TaskModel
    ) : AddressListModel()

    data class SortingItem(val selectedSortType: Int, val isTaskFiltered: Boolean = false) : AddressListModel()
    object Loader : AddressListModel()
}