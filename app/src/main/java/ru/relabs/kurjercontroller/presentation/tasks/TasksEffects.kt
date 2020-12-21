package ru.relabs.kurjercontroller.presentation.tasks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskState
import ru.relabs.kurjercontroller.domain.repositories.MergeResult
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.base.tea.CommonMessages
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object TasksEffects {

    fun effectNavigateTaskInfo(task: Task): TasksEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.navigateTo(RootScreen.TaskInfo(task, c.consumer))
        }
    }

    fun effectTaskSelected(task: Task): TasksEffect = { c, s ->
        when {
            task.state.state == TaskState.CREATED ->
                c.showSnackbar(R.string.task_list_not_examined)
            s.tasks.filter { it.filtered && s.selectedTasks.contains(it.id) }.size > 3 ->
                c.showSnackbar(R.string.task_list_too_many_filtered)
            else ->
                messages.send(TasksMessages.msgTaskSelected(task))
        }
    }

    fun effectTaskUnselected(task: Task): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgTaskUnselected(task))
    }

    fun effectNavigateAddresses(withFilteredTasksReload: Boolean): TasksEffect = { c, s ->
        val filteredTasks = s.tasks.filter { it.filtered && s.selectedTasks.contains(it.id) }
        if (filteredTasks.isNotEmpty() && withFilteredTasksReload) {
            withContext(Dispatchers.Main) {
                c.router.navigateTo(RootScreen.Filters(filteredTasks, c.consumer))
            }
        } else {
            withContext(Dispatchers.Main) {
                c.router.navigateTo(RootScreen.Addresses(s.tasks.filter { s.selectedTasks.contains(it.id) }))
            }
        }
    }

    fun effectReloadFilteredItemsAndStart(): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgAddLoaders(1))
        effectLoadTasks(false, false)(c, s)
        messages.send(msgEffect(effectUpdateFilteredItems()))
        messages.send(TasksMessages.msgAddLoaders(-1))
    }

    private fun effectUpdateFilteredItems(): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgAddLoaders(1))
        val failed = s.tasks
            .filter { it.filtered && s.selectedTasks.contains(it.id) }
            .mapNotNull {
                when (val r = c.onlineTaskUseCase.updateFilteredTaskItems(it)) {
                    is Left -> it
                    is Right -> null
                }
            }
        when {
            failed.isNotEmpty() && failed.size < s.selectedTasks.size -> {
                messages.send(TasksMessages.msgTasksUnselected(failed))
                withContext(Dispatchers.Main) {
                    c.showPartialTaskItemsLoadingError(failed)
                }
            }
            failed.isNotEmpty() && failed.size == s.selectedTasks.size -> {
                messages.send(TasksMessages.msgTasksUnselected(s.tasks.filter { s.selectedTasks.contains(it.id) }))
                withContext(Dispatchers.Main) {
                    c.showTaskItemsLoadingError()
                }
            }
            else -> messages.send(msgEffect(effectNavigateAddresses(false)))
        }
        messages.send(TasksMessages.msgAddLoaders(-1))
    }

    fun effectLoadTasks(withNetwork: Boolean, withClearSelected: Boolean): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgAddLoaders(1))
        c.databaseRepository.closeOutdatedOnlineTask()
        if (withNetwork) {
            var tasksUpdated = false
            var tasksCreated = false

            c.controlRepository.refreshAvailableKeys()

            when (val r = c.controlRepository.getRemoteTasks()) {
                is Right -> c.databaseRepository.merge(r.value).collect {
                    when (it) {
                        is MergeResult.TaskCreated -> tasksCreated = true
                        is MergeResult.TaskUpdated -> tasksUpdated = true
                        is MergeResult.TaskRemoved -> tasksUpdated = true
                    }
                }
                is Left -> messages.send(CommonMessages.msgError(r.value))
            }

            val message = when {
                tasksUpdated -> R.string.task_list_tasks_updated
                tasksCreated -> R.string.task_list_tasks_created
                else -> R.string.task_list_tasks_not_changed
            }
            c.showSnackbar(message)
        }
        messages.send(TasksMessages.msgTasksLoaded(c.databaseRepository.getTasks(), withClearSelected))
        messages.send(TasksMessages.msgAddLoaders(-1))
    }

    fun effectRefresh(): TasksEffect = { c, s ->
        when (s.loaders > 0) {
            true -> c.showSnackbar(R.string.task_list_update_in_progress)
            false -> messages.send(msgEffect(effectLoadTasks(true, true)))
        }
    }

    fun effectLaunchEventConsumers(): TasksEffect = { c, s ->
        coroutineScope {
            launch {
                c.taskEventController.subscribe().collect { event ->
                    when (event) {
                        is TaskEvent.TaskClosed ->
                            messages.send(TasksMessages.msgTaskClosed(event.taskId))
                        is TaskEvent.TasksUpdateRequired -> withContext(Dispatchers.Main) {
                            if (event.showDialogInTasks && s.loaders == 0) {
                                c.showUpdateRequiredOnVisible()
                            }
                        }
                    }
                }
            }
        }
    }

    fun effectNavigateOnlineFilters(): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgAddLoaders(1))
        when (val r = c.controlRepository.getIsOnlineAvailable()) {
            is Left -> c.showSnackbar(R.string.unknown_network_error)
            is Right -> when (r.value) {
                false -> c.showSnackbar(R.string.online_no_access)
                true -> withContext(Dispatchers.Main) {
                    c.router.navigateTo(RootScreen.OnlineFilters(c.consumer))
                }
            }
        }
        messages.send(TasksMessages.msgAddLoaders(-1))
    }

    fun effectStartOnline(filters: TaskFilters, withPlanned: Boolean): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgAddLoaders(1))
        withContext(Dispatchers.Main) {
            CustomLog.writeToFile("Navigate back from tasks, close online")
            c.router.exit() //Exit from filters edit screen
        }
        when (val r = c.onlineTaskUseCase.createOnlineTask(filters, withPlanned)) {
            is Left -> c.showSnackbar(R.string.unknown_runtime_error)
            is Right -> {
                messages.send(msgEffect(effectLoadTasks(false, false)))
                withContext(Dispatchers.Main) {
                    c.router.navigateTo(RootScreen.Addresses(listOf(r.value)))
                }
            }
        }
        messages.send(TasksMessages.msgAddLoaders(-1))
    }

    fun effectRestoreMapCamera(): TasksEffect = { c, s ->
        c.mapCameraStorage.resetCameraSettings()
    }
}