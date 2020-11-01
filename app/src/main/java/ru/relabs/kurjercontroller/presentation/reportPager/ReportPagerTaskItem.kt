package ru.relabs.kurjercontroller.presentation.reportPager

sealed class ReportPagerTaskItem {
    data class TaskButton(val taskWithItem: ReportTaskWithItem, val active: Boolean): ReportPagerTaskItem()
}