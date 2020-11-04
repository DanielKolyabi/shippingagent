package ru.relabs.kurjercontroller.presentation.reportPager

import ru.relabs.kurjercontroller.domain.models.TaskItem

sealed class ReportPagerTaskItem {
    data class TaskButton(val taskItem: TaskItem, val active: Boolean): ReportPagerTaskItem()
}