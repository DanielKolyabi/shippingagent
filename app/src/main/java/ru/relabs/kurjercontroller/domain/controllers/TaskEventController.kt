package ru.relabs.kurjercontroller.domain.controllers

import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItemId

class TaskEventController: BaseEventController<TaskEvent>()

sealed class TaskEvent{
    data class TaskClosed(val taskId: TaskId): TaskEvent()
    data class TaskItemClosed(val taskItemId: TaskItemId): TaskEvent()
    data class TasksUpdateRequired(val showDialogInTasks: Boolean = false): TaskEvent()
}