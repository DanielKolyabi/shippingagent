package ru.relabs.kurjercontroller.presentation.reportPager

import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgEmpty
import ru.relabs.kurjercontroller.presentation.base.tea.msgState

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object ReportPagerMessages {
    fun msgInit(
        taskIds: List<TaskItemWithTaskIds>,
        selectedTask: TaskItemWithTaskIds?
    ): ReportPagerMessage = msgEffects(
        { it },
        { listOf(ReportPagerEffects.effectInit(taskIds, selectedTask)) }
    )

    fun msgAddLoaders(i: Int): ReportPagerMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgTaskClicked(item: ReportTaskWithItem): ReportPagerMessage =
        msgState { it.copy(selectedTask = item) }

    fun msgTasksLoaded(tasks: List<ReportTaskWithItem>, selectedTask: ReportTaskWithItem): ReportPagerMessage =
        msgState { it.copy(tasks = tasks, selectedTask = selectedTask) }

    fun msgEntranceClosed(task: Task, taskItem: TaskItem, entrance: Entrance): ReportPagerMessage =
        msgEmpty() //TODO
}