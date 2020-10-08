package ru.relabs.kurjercontroller.presentation.fragments.taskInfo

import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskModel

/**
 * Created by ProOrange on 18.03.2019.
 */
sealed class TaskInfoModel {
    class Task(val task: TaskModel) : TaskInfoModel()
    class TaskItem(val taskItem: ru.relabs.kurjercontroller.domain.models.TaskItem) : TaskInfoModel()
    class FilterItem(val filterName: String, val filterItem: TaskFilter) : TaskInfoModel()
    object DetailsAddressTableHeader : TaskInfoModel()
    object DetailsFiltersTableHeader : TaskInfoModel()
}