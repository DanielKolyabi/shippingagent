package ru.relabs.kurjercontroller.presentation.taskDetails

import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgState

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object TaskDetailsMessages {
    fun msgInit(task: Task?): TaskDetailsMessage = msgEffects(
        { it.copy(task = task) },
        { listOf() }
    )

    fun msgAddLoaders(i: Int): TaskDetailsMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgNavigateBack(): TaskDetailsMessage =
        msgEffect(TaskDetailsEffects.effectNavigateBack())

    fun msgInfoClicked(taskItem: TaskItem): TaskDetailsMessage =
        msgEffect(TaskDetailsEffects.effectNavigateTaskItemDetails(taskItem))

    fun msgExamineClicked(): TaskDetailsMessage =
        msgEffect(TaskDetailsEffects.effectExamine())

    fun msgOpenMap(): TaskDetailsMessage =
        msgEffect(TaskDetailsEffects.effectOpenMap())

    fun msgTargetItem(address: Address): TaskDetailsMessage =
        msgState {it.copy(targetAddress = address)}
}