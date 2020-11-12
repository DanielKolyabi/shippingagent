package ru.relabs.kurjercontroller.presentation.reportPager

import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
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
        {
            listOf(
                ReportPagerEffects.effectInit(taskIds, selectedTask),
                ReportPagerEffects.effectSubscribeEvents()
            )
        }
    )

    fun msgAddLoaders(i: Int): ReportPagerMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgTaskClicked(item: TaskItem): ReportPagerMessage =
        msgState { it.copy(selectedTask = item) }

    fun msgTasksLoaded(tasks: List<TaskItem>, selectedTask: TaskItem): ReportPagerMessage =
        msgState { it.copy(tasks = tasks, selectedTask = selectedTask) }

    fun msgEntranceClosed(task: TaskId, taskItem: TaskItemId, entrance: EntranceNumber): ReportPagerMessage =
        msgEffect(ReportPagerEffects.effectCloseEntrance(task, taskItem, entrance))

    fun msgNavigateBack(): ReportPagerMessage =
        msgEffect(ReportPagerEffects.effectNavigateBack())

    fun msgCloseTaskItemEntrance(targetTaskItem: TaskItem, entrance: EntranceNumber): ReportPagerMessage = msgState { s ->
        val newTasks = s.tasks.map { t ->
            if (t.id == targetTaskItem.id && t.taskId == targetTaskItem.taskId) {
                t.copy(entrances = t.entrances.map { e ->
                    if (e.number == entrance) {
                        e.copy(state = EntranceState.CLOSED)
                    } else {
                        e
                    }
                })
            } else {
                t
            }
        }
        s.copy(tasks = newTasks, selectedTask = newTasks.firstOrNull { it.id == s.selectedTask?.id })
    }

    fun msgCloseTaskItem(targetTaskItem: TaskItem): ReportPagerMessage = msgState { s ->
        s.copy(tasks = s.tasks.mapNotNull { t ->
            if (t.id == targetTaskItem.id && t.taskId == targetTaskItem.taskId) {
                null
            } else {
                t
            }
        })
    }
}


