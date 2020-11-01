package ru.relabs.kurjercontroller.presentation.helpers

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem

/**
 * Created by ProOrange on 31.08.2018.
 */
object TaskAddressSorter {


    private fun internalSortTaskItemsCloseTime(taskItems: List<Pair<Task, TaskItem>>): List<Pair<Task, TaskItem>>{
        return taskItems.sortedWith(compareByDescending<Pair<Task, TaskItem>> { it.second.closeTime }
            .thenBy { it.second.address.city }
            .thenBy { it.second.address.street }
            .thenBy { it.second.address.house }
            .thenBy { it.second.address.houseName }
            .thenBy { !it.second.isClosed }
        ).groupBy {
            it.second.address.idnd
        }.toList().sortedBy {
            !it.second.any { !it.second.isClosed }
        }.toMap().flatMap {
            it.value
        }
    }

    fun sortTaskItemsCloseTime(taskItems: List<Pair<Task, TaskItem>>): List<Pair<Task, TaskItem>> {
        val new = taskItems.filter { it.second.isNew }
        val old = taskItems.filter { !it.second.isNew }

        return internalSortTaskItemsCloseTime(new) + internalSortTaskItemsCloseTime(old)
    }

    private fun internalSortTaskItemsStandart(taskItems: List<Pair<Task, TaskItem>>): List<Pair<Task, TaskItem>> {
        return taskItems.sortedWith(compareBy<Pair<Task, TaskItem>> { it.second.address.city }
            .thenBy { it.second.address.street }
            .thenBy { it.second.address.house }
            .thenBy { it.second.address.houseName }
            .thenBy { !it.second.isClosed }
        ).groupBy {
            it.second.address.idnd
        }.toList().sortedBy {
            !it.second.any { !it.second.isClosed }
        }.toMap().flatMap {
            it.value
        }
    }

    fun sortTaskItemsStandart(taskItems: List<Pair<Task, TaskItem>>): List<Pair<Task, TaskItem>> {
        val new = taskItems.filter { it.second.isNew }
        val old = taskItems.filter { !it.second.isNew }

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

    private fun internalSortTaskItemsAlphabetic(taskItems: List<Pair<Task, TaskItem>>): List<Pair<Task, TaskItem>> {
        return taskItems.sortedWith(compareBy<Pair<Task, TaskItem>> { it.second.address.city }
            .thenBy { it.second.address.street }
            .thenBy { it.second.address.house }
            .thenBy { it.second.address.houseName }
            .thenBy { !it.second.isClosed }
        ).groupBy {
            it.second.address.idnd
        }.toList().sortedBy {
            !it.second.any { !it.second.isClosed }
        }.toMap().flatMap {
            it.value
        }
    }

    fun sortTaskItemsAlphabetic(taskItems: List<Pair<Task, TaskItem>>): List<Pair<Task, TaskItem>> {
        val new = taskItems.filter { it.second.isNew }
        val old = taskItems.filter { !it.second.isNew }

        return internalSortTaskItemsAlphabetic(new) + internalSortTaskItemsAlphabetic(old)
    }

    const val STANDART = 1
    const val ALPHABETIC = 2
    const val CLOSE_TIME = 3
}