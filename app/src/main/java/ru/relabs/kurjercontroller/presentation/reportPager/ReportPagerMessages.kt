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
        val selectedTask = newTasks.firstOrNull { it.id == s.selectedTask?.id }
        val entrances = selectedTask?.entrances?.sortedBy { it.state == EntranceState.CLOSED }
        val newAdapterPosition = if (entrances?.getOrNull(s.selectedEntrancePosition)?.state == EntranceState.CREATED) {
            s.selectedEntrancePosition
        } else {
            0
        }
        s.copy(tasks = newTasks, selectedTask = selectedTask, selectedEntrancePosition = newAdapterPosition)
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

    fun msgTaskItemChanged(taskItem: TaskItem): ReportPagerMessage = msgState { s ->
        if (s.tasks.any { it.id == taskItem.id && it.taskId == taskItem.taskId }) {
            s.copy(
                tasks = s.tasks.map {
                    if (it.id == taskItem.id && it.taskId == taskItem.taskId) {
                        taskItem
                    } else {
                        it
                    }
                },
                selectedTask = taskItem.takeIf { it.id == taskItem.id && it.taskId == taskItem.taskId } ?: s.selectedTask
            )
        } else {
            s
        }
    }

    fun msgPageSelected(pos: Int): ReportPagerMessage =
        msgState { it.copy(selectedEntrancePosition = pos) }
}


