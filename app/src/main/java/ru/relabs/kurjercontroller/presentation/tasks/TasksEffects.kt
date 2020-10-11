package ru.relabs.kurjercontroller.presentation.tasks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskState
import ru.relabs.kurjercontroller.domain.repositories.MergeResult
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.base.tea.CommonMessages
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object TasksEffects {

    fun effectNavigateTaskInfo(task: Task): TasksEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.navigateTo(RootScreen.TaskInfo(task, c.examinedConsumer))
        }
    }

    fun effectTaskSelected(task: Task): TasksEffect = { c, s ->
        when {
            task.state.state == TaskState.CREATED ->
                c.showSnackbar(R.string.task_list_not_examined)
            s.selectedTasks.count { it.filtered } > 3 ->
                c.showSnackbar(R.string.task_list_too_many_filtered)
            else ->
                messages.send(TasksMessages.msgTaskSelected(task))
        }
    }

    fun effectTaskUnselected(task: Task): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgTaskUnselected(task))
    }

    fun effectNavigateAddresses(): TasksEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.navigateTo(RootScreen.Addresses(s.selectedTasks))
        }
    }

    fun effectLoadTasks(withNetwork: Boolean): TasksEffect = { c, s ->
        messages.send(TasksMessages.msgAddLoaders(1))
        c.databaseRepository.closeOutdatedOnlineTask()
        if (withNetwork) {
            var tasksUpdated = false
            var tasksCreated = false

            when (val r = c.deliveryRepository.getRemoteTasks()) {
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
        messages.send(TasksMessages.msgTasksLoaded(c.databaseRepository.getTasks()))
        messages.send(TasksMessages.msgAddLoaders(-1))
    }

    fun effectRefresh(): TasksEffect = { c, s ->
        when (s.loaders > 0) {
            true -> c.showSnackbar(R.string.task_list_update_in_progress)
            false -> messages.send(msgEffect(effectLoadTasks(true)))
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
}