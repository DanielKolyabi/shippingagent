package ru.relabs.kurjercontroller.ui.fragments.report.models

import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel

/**
 * Created by ProOrange on 30.08.2018.
 */

sealed class ReportTasksListModel {
    data class TaskButton(val task: TaskModel, val taskItem: TaskItemModel, val pos: Int, var active: Boolean) :
        ReportTasksListModel()
}