package ru.relabs.kurjercontroller.presentation.fragments.taskInfo

import ru.relabs.kurjercontroller.domain.models.FilterModel
import ru.relabs.kurjercontroller.domain.models.TaskItemModel
import ru.relabs.kurjercontroller.domain.models.TaskModel

/**
 * Created by ProOrange on 18.03.2019.
 */
sealed class TaskInfoModel {
    class Task(val task: TaskModel) : TaskInfoModel()
    class TaskItem(val taskItem: TaskItemModel) : TaskInfoModel()
    class FilterItem(val filterName: String, val filterItem: FilterModel) : TaskInfoModel()
    object DetailsAddressTableHeader : TaskInfoModel()
    object DetailsFiltersTableHeader : TaskInfoModel()
}