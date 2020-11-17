package ru.relabs.kurjercontroller.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItemId
import ru.relabs.kurjercontroller.domain.models.TaskState
import ru.relabs.kurjercontroller.domain.providers.FirebaseToken
import ru.relabs.kurjercontroller.domain.providers.LocationProvider
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.utils.CancelableScope

/**
 * Created by ProOrange on 11.08.2018.
 */
class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {
    private val scope = CancelableScope(Dispatchers.Default)
    private val repository: ControlRepository by inject()
    private val databaseRepository: DatabaseRepository by inject()
    private val locationProvider: LocationProvider by inject()
    private val taskEventController: TaskEventController by inject()

    override fun onNewToken(pushToken: String) {
        super.onNewToken(pushToken)
        scope.launch {
            repository.updatePushToken(FirebaseToken(pushToken))
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        Log.d("Firebase", "$msg")
        scope.launch(Dispatchers.Main) {
            processMessageData(msg.data)
        }
    }

    suspend fun processMessageData(data: Map<String, String>) {
        if (data.containsKey("request_gps")) {
            scope.launch {
                val coordinates = locationProvider.lastReceivedLocation()
                    ?: locationProvider.updatesChannel().let {
                        val c = it.receive()
                        it.cancel()
                        c
                    }

                repository.updateLocation(coordinates)
            }
        }
        if (data.containsKey("tasks_update")) {
            taskEventController.send(TaskEvent.TasksUpdateRequired())
        }
        //Controller entrance closed
        if (data.containsKey("closed_entrance_number")) {
            run {
                val taskId = data["closed_task_id"]?.toIntOrNull()?.let { TaskId(it) }
                taskId ?: return@run
                val taskItemId = data["closed_task_item_id"]?.toIntOrNull()?.let { TaskItemId(it) }
                taskItemId ?: return@run
                val entranceNumber = data["closed_entrance_number"]?.toIntOrNull()?.let { EntranceNumber(it) }
                entranceNumber ?: return@run

                databaseRepository.closeEntrance(taskId, taskItemId, entranceNumber)
                taskEventController.send(TaskEvent.EntranceClosed(taskId, taskItemId, entranceNumber))
            }
        }

        //Deliveryman address closed
        if (data.containsKey("closed_task_id") && data.containsKey("deliveryman_task")) {
            withContext(Dispatchers.IO) {
                val taskItemId = data["closed_task_id"]?.toIntOrNull()?.let { TaskItemId(it) }
                taskItemId ?: return@withContext
                val closeTime = data["close_time"]?.toDoubleOrNull()?.toLong()
                closeTime ?: return@withContext
                val closeDate = DateTime(closeTime * 1000)
                val items = databaseRepository.getTaskItems(taskItemId)
                val tasks = items.distinctBy { it.taskId }.map { databaseRepository.getTask(it.taskId) }

                items
                    .mapNotNull { item ->
                        when (val task = tasks.firstOrNull { task -> task?.id == item.taskId }) {
                            null -> null
                            else -> item to task
                        }
                    }
                    .forEach { (item, task) ->
                        if (task.state.state != TaskState.CREATED) {
                            databaseRepository.updateTaskItem(item.copy(isNew = true, closeTime = closeDate))
                        }
                    }
                taskEventController.send(TaskEvent.TaskItemClosedByDeliveryMan(taskItemId, closeDate))
            }
        }
    }
}
