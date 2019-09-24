package ru.relabs.kurjercontroller

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.application.MyApplication
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI

/**
 * Created by ProOrange on 11.08.2018.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    val bgScope = CancelableScope(Dispatchers.Default)

    override fun onNewToken(pushToken: String) {
        super.onNewToken(pushToken)
        Log.d("Firebase", "Got token $pushToken")
        bgScope.launch(Dispatchers.Default) {
            (application as? MyApplication)?.let {
                it.savePushToken(pushToken)
                it.sendPushToken(pushToken)
            }
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        Log.d("Firebase", "$msg")
        bgScope.launch(Dispatchers.Main) {
            processMessageData(msg.data)
        }
    }

    suspend fun processMessageData(data: Map<String, String>) {
        if (data.containsKey("request_gps")) {
            val user = (application as? MyApplication)?.user?.getUserCredentials() ?: return
            bgScope.launch(Dispatchers.Default) {
                val coordinates = MyApplication.instance.currentLocation
                val token = user.token
                try {
                    DeliveryServerAPI.api.sendGPS(
                        token,
                        coordinates.lat,
                        coordinates.long,
                        DateTime(coordinates.time).toString("yyyy-MM-dd'T'HH:mm:ss")
                    ).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (data.containsKey("tasks_update")) {
            val taskId = data.getOrElse("task_id", { null })?.toIntOrNull()
            val int = Intent().apply {
                putExtra("tasks_changed", true)
                putExtra("task_id", taskId)
                action = "NOW"
            }
            sendBroadcast(int)
        }
        //Controller entrance closed
        if (data.containsKey("closed_entrance_number")) {
            run {
                val taskId = data["closed_task_id"]?.toIntOrNull()
                taskId ?: return@run
                val taskItemId = data["closed_task_item_id"]?.toIntOrNull()
                taskItemId ?: return@run
                val entranceNumber = data["closed_entrance_number"]?.toIntOrNull()
                entranceNumber ?: return@run

                val int = Intent().apply {
                    putExtra("task_closed", taskId)
                    putExtra("task_item_closed", taskItemId)
                    putExtra("entrance_number_closed", entranceNumber)
                    action = "NOW"
                }
                sendBroadcast(int)
            }
        }

        //Deliveryman address closed
        if (data.containsKey("closed_task_id") && data.containsKey("deliveryman_task")) {
            withContext(Dispatchers.IO) {
                val taskItemId = data["closed_task_id"]?.toIntOrNull()
                taskItemId ?: return@withContext
                val closeTime = data["close_time"]?.toDoubleOrNull()?.toLong()
                closeTime ?: return@withContext
                val closeDate = DateTime(closeTime * 1000)
                val items = MyApplication.instance.tasksRepository.getTaskItems(taskItemId)
                if (items.isNotEmpty()) {
                    items.forEach {
                        if (MyApplication.instance.database.taskDao().getById(it.taskId)?.state != TaskModel.CREATED) {
                            MyApplication.instance.database.taskItemDao().insert(
                                it.copy(isNew = true, closeTime = closeDate).toEntity()
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        val int = Intent().apply {
                            putExtra("task_item_id_closed_by_deliveryman", taskItemId)
                            putExtra("task_item_date_closed_by_deliveryman", closeTime)
                            action = "NOW"
                        }
                        sendBroadcast(int)
                    }
                }
            }
        }
    }
}