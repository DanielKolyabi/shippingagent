package ru.relabs.kurjercontroller.presentation.reportPager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.EntranceState
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItemId
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.utils.CustomLog

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object ReportPagerEffects {

    fun effectInit(
        taskIds: List<TaskItemWithTaskIds>,
        selectedTaskId: TaskItemWithTaskIds?
    ): ReportPagerEffect = { c, s ->
        val taskWithItems = taskIds.mapNotNull {
            val task = c.databaseRepository.getTask(it.taskId)
            val taskItem = c.databaseRepository.getTaskItem(it.taskId, it.taskItemId)
            if (task != null && taskItem != null) {
                if (taskItem.isNew) {
                    c.databaseRepository.markAsOld(taskItem)
                    c.taskEventController.send(TaskEvent.TaskItemChanged(taskItem.copy(isNew = false)))
                }
                taskItem
            } else {
                null
            }
        }
        val selectedTask = taskWithItems.firstOrNull {
            it.taskId == selectedTaskId?.taskId && it.id == selectedTaskId.taskItemId
        } ?: taskWithItems.firstOrNull()

        if (selectedTask == null) {
            withContext(Dispatchers.Main){
                c.showSnackbar?.invoke(R.string.error_report_tasks_unavailable)
            }
            messages.send(ReportPagerMessages.msgNavigateBack())
        } else {
            messages.send(ReportPagerMessages.msgTasksLoaded(taskWithItems, selectedTask))
        }
    }

    fun effectNavigateBack(): ReportPagerEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.exit()
        }
    }

    fun effectSubscribeEvents(): ReportPagerEffect = { c, s ->
        coroutineScope {
            launch {
                c.taskEventController.subscribe().collect {
                    when (it) {
                        is TaskEvent.EntranceClosed ->
                            messages.send(ReportPagerMessages.msgEntranceClosed(it.taskId, it.taskItemId, it.number))
                        is TaskEvent.TaskItemChanged ->
                            messages.send(ReportPagerMessages.msgTaskItemChanged(it.taskItem))
                    }
                }
            }
        }
    }

    fun effectCloseEntrance(task: TaskId, taskItem: TaskItemId, entrance: EntranceNumber): ReportPagerEffect = { c, s ->
        val targetTaskItem = s.tasks.firstOrNull { it.id == taskItem && it.taskId == task }
        if (targetTaskItem?.entrances?.any { it.number == entrance && it.state == EntranceState.CREATED } == true) {
            messages.send(ReportPagerMessages.msgCloseTaskItemEntrance(targetTaskItem, entrance))
            if (targetTaskItem.entrances.count { it.state == EntranceState.CREATED } == 1) {
                messages.send(ReportPagerMessages.msgCloseTaskItem(targetTaskItem))
                c.taskEventController.send(TaskEvent.TaskItemClosed(task, taskItem))
                val otherTask = s.tasks.filter { it != targetTaskItem }.firstOrNull()
                if (otherTask == null) {
                    CustomLog.writeToFile("Navigate back from report, entrance closed tid: ${task.id}, tiid: ${taskItem.id}, entrance: ${entrance.number}")
                    CustomLog.writeToFile("Current taskItem: tid: ${s.selectedTask?.taskId?.id}, tiid: ${s.selectedTask?.id?.id}")
                    messages.send(msgEffect(effectNavigateBack()))
                } else if (s.selectedTask == targetTaskItem) {
                    messages.send(ReportPagerMessages.msgTaskClicked(otherTask))
                }
            }
        }
    }
}