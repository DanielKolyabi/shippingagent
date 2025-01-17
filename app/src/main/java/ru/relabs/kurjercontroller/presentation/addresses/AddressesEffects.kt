package ru.relabs.kurjercontroller.presentation.addresses

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.yandexMap.YandexMapFragment.Companion.WRONG_METHOD_OUTLINE_COLOR
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.utils.CustomLog
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

    fun effectNavigateBack(exits: Int): AddressesEffect = { c, s ->
        if (exits == 1) {
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
                        is TaskEvent.TaskItemChanged ->
                            messages.send(AddressesMessages.msgTaskItemChanged(it.taskItem))
                        is TaskEvent.EntranceClosed ->
                            messages.send(AddressesMessages.msgEntranceClosed(it.taskId, it.taskItemId, it.number))
                        is TaskEvent.TaskItemClosedByDeliveryMan ->
                            messages.send(AddressesMessages.msgTaskItemClosedByDeliveryMan(it.taskItemId, it.closeTime))
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
        val consumer = c.addressClickedConsumer()
        when (consumer) {
            null -> FirebaseCrashlytics.getInstance().log("consumer is null")
            else -> withContext(Dispatchers.Main) {
                c.router.navigateTo(
                    RootScreen.AddressMap(
                        listOf(
                            AddressWithColor(
                                taskItems.first().address,
                                placemarkColor,
                                if (taskItems.any { it.wrongMethod }) WRONG_METHOD_OUTLINE_COLOR else placemarkColor
                            )
                        ),
                        listOf(taskItems.first().deliverymanId),
                        s.tasks.firstOrNull { it.id == taskItems.first().taskId }?.storages ?: listOf(),
                        consumer
                    )
                )
            }
        }
    }

    fun effectCloseCurrentTask(): AddressesEffect = { c, s ->
        messages.send(AddressesMessages.msgAddLoaders(1))
        val task = s.tasks.firstOrNull()
        if (task == null || s.tasks.size != 1) {
            withContext(Dispatchers.Main) {
                c.showSnackbar(R.string.close_task_error_single)
            }
        } else {
            c.databaseRepository.closeTaskById(task.id, !task.isOnline)
            c.taskEventController.send(TaskEvent.TaskClosed(task.id))
            CustomLog.writeToFile("Navigate back from addresses, current task closed")
            messages.send(AddressesMessages.msgNavigateBack())
        }
        messages.send(AddressesMessages.msgAddLoaders(-1))
    }

    fun effectYandexMapAddressSelected(address: Address): AddressesEffect = { c, s ->
        messages.send(AddressesMessages.msgSelectedListAddress(address))
        delay(1000)
        messages.send(AddressesMessages.msgSelectedListAddress(null))
    }

    fun effectOpenGlobalMap(): AddressesEffect = { c, s ->
        val consumer = c.addressClickedConsumer()
        when (consumer) {
            null -> FirebaseCrashlytics.getInstance().log("consumer is null")
            else -> withContext(Dispatchers.Main) {
                c.router.navigateTo(
                    RootScreen.TasksMap(
                        s.tasks,
                        consumer
                    )
                )
            }
        }
    }
}