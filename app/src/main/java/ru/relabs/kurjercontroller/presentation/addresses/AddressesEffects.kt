package ru.relabs.kurjercontroller.presentation.addresses

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.fragmentsOld.AddressYandexMapScreen
import ru.relabs.kurjercontroller.presentation.fragmentsOld.TasksYandexMapScreen
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.WRONG_METHOD_OUTLINE_COLOR
import ru.relabs.kurjercontroller.utils.extensions.placemarkColor

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object AddressesEffects {

    fun effectLoadTasks(taskIds: List<TaskId>): AddressesEffect = { c, s ->
        messages.send(AddressesMessages.msgAddLoaders(1))
        val tasks = taskIds.mapNotNull {
            c.databaseRepository.getTask(it)
        }
        messages.send(AddressesMessages.msgTasksLoaded(tasks))
        messages.send(AddressesMessages.msgAddLoaders(-1))
    }

    fun effectNavigateBack(): AddressesEffect = { c, s ->
        if (s.exits == 1) {
            withContext(Dispatchers.Main) {
                c.router.exit()
            }
        }
    }

    fun effectNavigateReport(task: Task, item: TaskItem): AddressesEffect = { c, s ->
        val sameAddressItems = s.tasks
            .flatMap { it.taskItems.map { taskItem -> taskItem } }
            .filter { it.address.idnd == item.address.idnd && it.isClosed == item.isClosed }

        withContext(Dispatchers.Main) {
            c.router.navigateTo(
                RootScreen.Report(
                    sameAddressItems,
                    item.taskId,
                    item.id
                )
            )
        }
    }

    fun effectLaunchEventConsumer(): AddressesEffect = { c, s ->
        coroutineScope {
            launch {
                c.taskEventController.subscribe().collect {
                    when (it) {
                        is TaskEvent.TaskClosed ->
                            messages.send(AddressesMessages.msgRemoveTask(it.taskId))
                        is TaskEvent.TaskItemClosed ->
                            messages.send(AddressesMessages.msgTaskItemClosed(it.taskId, it.taskItemId))
                    }
                }
            }
        }
    }

    fun effectValidateTasks(): AddressesEffect = { c, s ->
        messages.send(AddressesMessages.msgAddLoaders(1))
        s.tasks.forEach { t ->
            val updatedTask = c.databaseRepository.getTask(t.id) ?: return@forEach
            if (updatedTask.taskItems.none { !it.isClosed }) {
                c.databaseRepository.closeTaskById(updatedTask.id, true)
            }
            if (!(updatedTask.state.state == TaskState.EXAMINED || updatedTask.state.state == TaskState.STARTED)) {
                messages.send(AddressesMessages.msgRemoveTask(updatedTask.id))
            }
        }
        messages.send(AddressesMessages.msgAddLoaders(-1))
    }

    fun effectOpenYandexMap(taskItems: List<TaskItem>): AddressesEffect = { c, s ->
        val placemarkColor = taskItems.placemarkColor()
        withContext(Dispatchers.Main) {
            c.router.navigateTo(
                AddressYandexMapScreen(
                    listOf(
                        AddressWithColor(
                            taskItems.first().address,
                            placemarkColor,
                            if (taskItems.any { it.wrongMethod }) WRONG_METHOD_OUTLINE_COLOR else placemarkColor
                        )
                    ),
                    listOf(taskItems.first().deliverymanId),
                    s.tasks.firstOrNull { it.id == taskItems.first().taskId }?.storages ?: listOf()
                ) {
                    return@AddressYandexMapScreen
                }
            )
        }
    }

    fun effectCloseCurrentTask(): AddressesEffect = { c, s ->
        //TODO: Show confirmation dialog
        messages.send(AddressesMessages.msgAddLoaders(1))
        val task = s.tasks.firstOrNull()
        if (task == null || s.tasks.size != 1) {
            withContext(Dispatchers.Main) {
                c.showSnackbar(R.string.close_task_error_single)
            }
        } else {
            c.databaseRepository.closeTaskById(task.id, !task.isOnline)
            withContext(Dispatchers.Main) {
                c.router.exit()
            }
        }
        messages.send(AddressesMessages.msgAddLoaders(-1))
    }

    private fun effectYandexMapAddressSelected(address: Address): AddressesEffect = { c, s ->
        messages.send(AddressesMessages.msgSelectedListAddress(address))
        delay(1000)
        messages.send(AddressesMessages.msgSelectedListAddress(null))
    }

    fun effectOpenGlobalMap(): AddressesEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.navigateTo(
                TasksYandexMapScreen(
                    s.tasks,
                    { address ->
                        messages.offer(msgEffect(effectYandexMapAddressSelected(address)))
                    },
                    {
                        messages.offer(msgEffect(effectLoadTasks(s.tasks.map { it.id })))
                    }
                )
            )
        }
    }
}