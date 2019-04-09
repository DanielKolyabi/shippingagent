package ru.relabs.kurjercontroller

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.application.MyApplication
import ru.relabs.kurjercontroller.application.UserModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI

/**
 * Created by ProOrange on 11.08.2018.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    val bgScope = CancelableScope(Dispatchers.Default)

    override fun onNewToken(pushToken: String) {
        super.onNewToken(pushToken)
        bgScope.launch(Dispatchers.Default) {
            (application as? MyApplication)?.let {
                it.savePushToken(pushToken)
                it.sendPushToken(pushToken)
            }
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        bgScope.launch(Dispatchers.Main) {
            processMessageData(msg.data)
        }
    }

    fun processMessageData(data: Map<String, String>) {
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
            val int = Intent().apply {
                putExtra("tasks_changed", true)
                action = "NOW"
            }
            sendBroadcast(int)
        }
        if (data.containsKey("closed_task_id")) {
            run {
                val taskItemId = data["closed_task_id"]?.toIntOrNull()
                taskItemId ?: return@run

                val int = Intent().apply {
                    putExtra("task_item_closed", taskItemId)
                    action = "NOW"
                }
                sendBroadcast(int)
            }
        }
    }

}