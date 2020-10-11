package ru.relabs.kurjercontroller.presentation.taskDetails

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskItem

sealed class TaskDetailsItem {
    data class PageHeader(val task: Task): TaskDetailsItem()
    data class AddressItem(val taskItem: TaskItem): TaskDetailsItem()
    data class FilterItem(val filterName: String, val filterItem: TaskFilter) : TaskDetailsItem()

    object ListAddressesHeader : TaskDetailsItem()
    object ListFiltersHeader : TaskDetailsItem()
}