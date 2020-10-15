package ru.relabs.kurjercontroller.presentation.tasks

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.utils.SearchUtils

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object TasksMessages {
    fun msgInit(refreshTasks: Boolean): TasksMessage = msgEffects(
        { it },
        {
            listOf(
                TasksEffects.effectLoadTasks(refreshTasks, true),
                TasksEffects.effectLaunchEventConsumers()
            )
        }
    )

    fun msgTaskSelectClick(task: Task): TasksMessage = msgEffects(
        { it },
        {
            listOf(
                if (it.selectedTasks.contains(task)) {
                    TasksEffects.effectTaskUnselected(task)
                } else {
                    TasksEffects.effectTaskSelected(task)
                }
            )
        }
    )

    fun msgTaskClicked(task: Task): TasksMessage =
        msgEffect(TasksEffects.effectNavigateTaskInfo(task))

    fun msgTaskSelected(task: Task): TasksMessage =
        msgState { it.copy(selectedTasks = (it.selectedTasks + listOf(task)).distinct()) }

    fun msgTaskUnselected(task: Task): TasksMessage =
        msgState { it.copy(selectedTasks = it.selectedTasks.filter { it != task }) }

    fun msgStartClicked(): TasksMessage =
        msgEffect(TasksEffects.effectNavigateAddresses(true))

    fun msgAddLoaders(i: Int): TasksMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgTasksLoaded(tasks: List<Task>, withClearSelected: Boolean): TasksMessage =
        msgState { it.copy(tasks = tasks, selectedTasks = if (withClearSelected) listOf() else it.selectedTasks) }

    fun msgRefresh(): TasksMessage =
        msgEffect(TasksEffects.effectRefresh())

    fun msgSearch(searchText: String): TasksMessage =
        msgState {
            it.copy(
                searchFilter = searchText,
                selectedTasks = it.selectedTasks.filter { t -> SearchUtils.isMatches(t.name, searchText) }
            )
        }

    fun msgTaskExamined(task: Task): TasksMessage =
        msgState { state ->
            state.copy(tasks = state.tasks.map { stateTask ->
                if (stateTask.id == task.id) {
                    task
                } else {
                    stateTask
                }
            })
        }

    fun msgTaskClosed(taskId: TaskId): TasksMessage =
        msgState { s ->
            s.copy(
                tasks = s.tasks.filter { it.id != taskId },
                selectedTasks = s.selectedTasks.filter { it.id != taskId }
            )
        }

    fun msgOnlineClicked(): TasksMessage = msgEffects(
        { s ->
            if (s.tasks.any { it.isOnline } && s.selectedTasks.none { it.isOnline }) {
                s.copy(selectedTasks = s.selectedTasks + s.tasks.filter { it.isOnline })
            } else {
                s
            }
        },
        { s ->
            listOfNotNull(
                TasksEffects.effectNavigateOnlineFilters().takeIf { s.tasks.none { it.isOnline } }
            )
        }
    )

    fun msgOnlineFiltersSelected(filters: TaskFilters, withPlanned: Boolean): TasksMessage =
        msgEffect(TasksEffects.effectStartOnline(filters, withPlanned))

    fun msgSelectedFiltersUpdated(): TasksMessage =
        msgEffect(TasksEffects.effectReloadFilteredItemsAndStart())

    fun msgTasksUnselected(unselected: List<Task>): TasksMessage =
        msgState { it.copy(selectedTasks = it.selectedTasks.filter { it.id !in unselected.map { it.id } }) }

    fun msgStartAfterPartialFail(): TasksMessage =
        msgEffect(TasksEffects.effectNavigateAddresses(false))
}