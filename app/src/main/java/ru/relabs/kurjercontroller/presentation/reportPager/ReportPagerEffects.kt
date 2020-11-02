package ru.relabs.kurjercontroller.presentation.reportPager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                }
                ReportTaskWithItem(task, taskItem)
            } else {
                null
            }
        }
        val selectedTask = taskWithItems.firstOrNull {
            it.task.id == selectedTaskId?.taskId && it.taskItem.id == selectedTaskId?.taskItemId
        } ?: taskWithItems.first()

        messages.send(ReportPagerMessages.msgTasksLoaded(taskWithItems, selectedTask))
    }

    fun effectNavigateBack(): ReportPagerEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.exit()
        }
    }
}