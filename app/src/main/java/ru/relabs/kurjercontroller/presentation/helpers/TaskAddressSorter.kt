package ru.relabs.kurjercontroller.presentation.helpers

import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.AddressListModel

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


    private fun internalSortTaskItemsCloseTime(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem>{
        return taskItems.sortedWith(compareByDescending<AddressListModel.TaskItem> { it.taskItem.closeTime }
            .thenBy { it.taskItem.address.city }
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

    fun sortTaskItemsCloseTime(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem> {
        val new = taskItems.filter { it.taskItem.isNew }
        val old = taskItems.filter { !it.taskItem.isNew }

        return internalSortTaskItemsCloseTime(new) + internalSortTaskItemsCloseTime(old)
    }

    private fun internalSortTaskItemsStandart(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem> {
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

    fun sortTaskItemsStandart(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem> {
        val new = taskItems.filter { it.taskItem.isNew }
        val old = taskItems.filter { !it.taskItem.isNew }

        return internalSortTaskItemsStandart(new) + internalSortTaskItemsStandart(old)
    }

    fun sortInfoTaskItemsAlphabetic(taskItems: List<TaskItem>): List<TaskItem> {
        return taskItems.asSequence().sortedWith(compareBy<TaskItem> { it.address.city }
            .thenBy { it.address.street }
            .thenBy { it.address.house }
            .thenBy { it.address.houseName }
        ).groupBy {
            it.address.idnd
        }.toMap().flatMap {
            it.value
        }
    }

    private fun internalSortTaskItemsAlphabetic(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel.TaskItem> {
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
        val new = taskItems.filter { it.taskItem.isNew }
        val old = taskItems.filter { !it.taskItem.isNew }

        return internalSortTaskItemsAlphabetic(new) + internalSortTaskItemsAlphabetic(old)
    }

    const val STANDART = 1
    const val ALPHABETIC = 2
    const val CLOSE_TIME = 3
}