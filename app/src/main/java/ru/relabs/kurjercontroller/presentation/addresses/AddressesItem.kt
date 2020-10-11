package ru.relabs.kurjercontroller.presentation.addresses

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem

sealed class AddressesItem {

    data class GroupHeader(val subItems: List<TaskItem>, val showBypass: Boolean): AddressesItem()
    data class AddressItem(val taskItem: TaskItem, val task: Task): AddressesItem()
    data class Sorting(val sorting: AddressesSortingMethod, val isTaskFiltered: Boolean): AddressesItem()
    data class OtherAddresses(val count: Int): AddressesItem()

    object Loading: AddressesItem()
    object Blank: AddressesItem()
    data class Search(val filter: String): AddressesItem()
}

