package ru.relabs.kurjercontroller.domain.controllers

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.models.TaskItemId

class TaskEventController : BaseEventController<TaskEvent>()

sealed class TaskEvent {
    data class TaskClosed(val taskId: TaskId) : TaskEvent()
    data class TaskItemClosed(val taskId: TaskId, val taskItemId: TaskItemId) : TaskEvent()
    data class TasksUpdateRequired(val showDialogInTasks: Boolean = false) : TaskEvent()
    data class EntranceClosed(val taskId: TaskId, val taskItemId: TaskItemId, val number: EntranceNumber) : TaskEvent()
    data class TaskItemChanged(val taskItem: TaskItem) : TaskEvent()
    data class TaskItemClosedByDeliveryMan(val taskItemId: TaskItemId, val closeTime: DateTime) : TaskEvent()
    object TaskUpdated: TaskEvent()
}