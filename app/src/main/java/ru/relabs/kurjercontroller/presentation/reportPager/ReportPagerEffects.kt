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
            withContext(Dispatchers.Main) {
                c.showSnackbar?.invoke(R.string.error_report_tasks_unavailable)
            }
            CustomLog.writeToFile("Navigate user from report, because tasks is empty; (ids size: ${taskIds.size})")
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

    fun effectCloseEntrance(taskId: TaskId, taskItemId: TaskItemId, entrance: EntranceNumber): ReportPagerEffect = { c, s ->
        val targetTaskItem = s.tasks.firstOrNull { it.id == taskItemId && it.taskId == taskId }
        val isEntranceExists = targetTaskItem?.entrances?.any { it.number == entrance && it.state == EntranceState.CREATED }
        val isLatestEntrance = targetTaskItem?.entrances?.count { it.state == EntranceState.CREATED } == 1

        if (isEntranceExists == true) {
            messages.send(ReportPagerMessages.msgCloseTaskItemEntrance(targetTaskItem, entrance))
            if (isLatestEntrance) {
                messages.send(ReportPagerMessages.msgCloseTaskItem(targetTaskItem))
                c.taskEventController.send(TaskEvent.TaskItemClosed(taskId, taskItemId))
            }
        }
    }
}