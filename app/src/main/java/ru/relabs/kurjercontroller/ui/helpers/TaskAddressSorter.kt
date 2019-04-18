package ru.relabs.kurjercontroller.ui.helpers

import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel

/**
 * Created by ProOrange on 31.08.2018.
 */
object TaskAddressSorter {

    fun getAddressesWithTasksList(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel> {
        val result = mutableListOf<AddressListModel>()
        var lastAddressId = -1
        var lastAddressModel: AddressListModel.Address = AddressListModel.Address(mutableListOf())
        taskItems.forEach {
            if (lastAddressId != it.taskItem.address.idnd) {
                lastAddressId = it.taskItem.address.idnd
                lastAddressModel = AddressListModel.Address(mutableListOf(it.taskItem))
                result.add(lastAddressModel)
            }
            lastAddressModel.taskItems.add(it.taskItem)
            result.add(AddressListModel.TaskItem(it.taskItem, it.parentTask))
        }
        return result
    }

    fun sortTaskItemsStandart(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem> {
        return taskItems.sortedWith(compareBy<AddressListModel.TaskItem> { it.taskItem.address.city }
            .thenBy { it.taskItem.address.street }
            .thenBy { it.taskItem.address.house }
            .thenBy { it.taskItem.address.houseName }
            .thenBy { !it.taskItem.isClosed }
        ).groupBy {
            it.taskItem.address.idnd
        }.toList().sortedBy {
            !it.second.any { !it.taskItem.isClosed }
        }.toMap().flatMap {
            it.value
        }
    }

    fun sortTaskItemsAlphabetic(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem> {
        return taskItems.sortedWith(compareBy<AddressListModel.TaskItem> { it.taskItem.address?.city }
            .thenBy { it.taskItem.address.street }
            .thenBy { it.taskItem.address.house }
            .thenBy { it.taskItem.address.houseName }
            .thenBy { !it.taskItem.isClosed }
        ).groupBy {
            it.taskItem.address.idnd
        }.toList().sortedBy {
            !it.second.any { !it.taskItem.isClosed }
        }.toMap().flatMap {
            it.value
        }
    }

    val STANDART = 1
    val ALPHABETIC = 2
}