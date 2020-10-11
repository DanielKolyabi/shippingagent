package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList

import ru.relabs.kurjercontroller.domain.models.Task

/**
 * Created by ProOrange on 18.03.2019.
 */

sealed class TaskListModel {

    data class Loader(
        val text: String = ""
    ) : TaskListModel()
    data class TaskItem(
        val task: Task,
        var selected: Boolean = false,
        var hasAddressIntersection: Boolean = false
    ): TaskListModel()
    data class GroupHeader(
        val title: String
    ): TaskListModel()
}