package ru.relabs.kurjercontroller.ui.fragments.taskInfo

import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel

/**
 * Created by ProOrange on 18.03.2019.
 */
sealed class TaskInfoModel {
    class Task(val task: TaskModel) : TaskInfoModel()
    class TaskItem(val taskItem: TaskItemModel) : TaskInfoModel()
    object DetailsTableHeader : TaskInfoModel()
}