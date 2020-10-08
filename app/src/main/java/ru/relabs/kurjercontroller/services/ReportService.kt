package ru.relabs.kurjercontroller.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.application.ControllApplication
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.data.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.utils.NetworkHelper
import ru.relabs.kurjercontroller.ui.activities.MainActivity
import ru.relabs.kurjercontroller.utils.CancelableScope
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

const val CHANNEL_ID = "controller_notification_channel"
const val CLOSE_SERVICE_TIMEOUT = 80 * 60 * 1000

enum class ServiceState {
    TRANSFER, IDLE, UNAVAILABLE
}

class ReportService : Service() {
    private var thread: Job? = null
    private val bgScope = CancelableScope(Dispatchers.Default)

    private var currentIconBitmap: Bitmap? = null
    private var lastState: ServiceState =
        ServiceState.IDLE
    private var lastActivityResumeTime = 0L
    private var lastActivityRunningState = false
    private var lastGPSPending = System.currentTimeMillis()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun notification(body: String, status: ServiceState, update: Boolean = false): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID, getString(
                        R.string.app_name
                    ), NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }

        val ic = when (status) {
            ServiceState.TRANSFER -> R.drawable.ic_service_ok
            ServiceState.IDLE -> R.drawable.ic_service_idle
            ServiceState.UNAVAILABLE -> R.drawable.ic_service_error
        }

        val color = when (status) {
            ServiceState.TRANSFER -> Color.parseColor("#00aa33")
            ServiceState.IDLE -> Color.parseColor("#555555")
            ServiceState.UNAVAILABLE -> Color.parseColor("#bb0011")
        }

        if (lastState != status) {
            currentIconBitmap?.recycle()
            currentIconBitmap = BitmapFactory.decodeResource(application.resources, ic)
            lastState = status
        }

        val millisToClose = CLOSE_SERVICE_TIMEOUT - (System.currentTimeMillis() - lastActivityResumeTime)
        val closeNotifyText = if (status == ServiceState.IDLE && !lastActivityRunningState && millisToClose > 0) {
            val secondsToClose = millisToClose / 1000
            val timeWithUnit = if (secondsToClose < 60) {
                secondsToClose to "сек"
            } else {
                (secondsToClose / 60).toInt() to "мин"
            }
            " Закрытие через ${timeWithUnit.first} ${timeWithUnit.second}."
        } else {
            ""
        }

        val notification = NotificationCompat.Builder(applicationContext,
            CHANNEL_ID
        )
            .setContentTitle(getString(R.string.app_name))
            .setContentText(body + closeNotifyText)
            .setSmallIcon(ic)
            .setColor(color)
            .setWhen(System.currentTimeMillis())
            .setChannelId(CHANNEL_ID)
            .setOnlyAlertOnce(true)

        return notification.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras?.getBoolean("stopService") == true) {
            stopSelf()
            return Service.START_STICKY
        }

        startForeground(1, notification("Сервис отправки данных.",
            ServiceState.IDLE
        ))
        isRunning = true

        val db = ControllApplication.instance.database
        var lastTasksChecking = System.currentTimeMillis()
        var lastNetworkEnabledChecking = System.currentTimeMillis()

        thread?.cancel()
        thread = bgScope.launch(Dispatchers.Default) {
            while (true) {
                var isTaskSended = false
                if (NetworkHelper.isNetworkAvailable(applicationContext)) {
                    val sendQuery = getSendQuery(db)
                    val reportQuery = getReportQuery(db)

                    if (reportQuery != null) {
                        try {
                            sendReportQuery(db, reportQuery)
                            isTaskSended = true
                            application().tasksRepository.removeReport(db, reportQuery)
                        } catch (e: Exception) {
                            e.logError()
                        }
                    } else if (sendQuery != null) {
                        try {
                            sendSendQuery(sendQuery)
                            isTaskSended = true
                            db.sendQueryDao().delete(sendQuery)
                        } catch (e: Exception) {
                            e.logError()
                        }
                    } else if (System.currentTimeMillis() - lastTasksChecking > 25 * 60 * 1000) {
                        val user = application().user.getUserCredentials() ?: continue
                        val time = DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")
                        try {
                            val tasks = DeliveryServerAPI.api.getTasks(user.token, time).await()
                            if (application().tasksRepository.isMergeNeeded(tasks.map { it.toModel() })
                            ) {
                                val int = Intent().apply {
                                    putExtra("tasks_changed", true)
                                    action = "NOW"
                                }
                                sendBroadcast(int)
                            }
                        } catch (e: Exception) {
                            e.logError()
                        }
                        lastTasksChecking = System.currentTimeMillis()
                    }
                }

                if (System.currentTimeMillis() - lastNetworkEnabledChecking > 10 * 60 * 1000) {
                    lastNetworkEnabledChecking = System.currentTimeMillis()
                    if (!NetworkHelper.isNetworkEnabled(applicationContext)) {
                        val int = Intent().apply {
                            putExtra("network_disabled", true)
                            action = "NOW"
                        }
                        sendBroadcast(int)
                    }
                }

                updateNotificationText(db)

                pendingGPS()
                updateActivityState()

                delay(if (!isTaskSended) 5000 else 1000)
            }
        }

        return START_STICKY
    }

    private fun pendingGPS() {
        if (System.currentTimeMillis() - lastGPSPending > 1 * 60 * 1000) {
            ControllApplication.instance.requestLocation()
            lastGPSPending = System.currentTimeMillis()
        }
    }

    private fun updateActivityState() {
        if (!lastActivityRunningState && MainActivity.isRunning) {
            lastActivityResumeTime = System.currentTimeMillis()
        }

        if (!MainActivity.isRunning && (System.currentTimeMillis() - lastActivityResumeTime) > CLOSE_SERVICE_TIMEOUT) {
            val int = Intent().apply {
                putExtra("force_finish", true)
                action = "NOW"
            }
            sendBroadcast(int)

            stopSelf()
            stopForeground(true)
        }
        lastActivityRunningState = MainActivity.isRunning
    }

    private fun updateNotificationText(db: AppDatabase) {
        val isNetworkAvailable = NetworkHelper.isNetworkAvailable(applicationContext)
        val count = db.sendQueryDao().all.size + db.entranceReportDao().all.size

        val state = if (!isNetworkAvailable) {
            ServiceState.UNAVAILABLE
        } else if (count > 0) {
            ServiceState.TRANSFER
        } else {
            ServiceState.IDLE
        }

        val text = "Сервис. В очереди: $count." + if (!isNetworkAvailable) " Сеть недоступна." else ""

        NotificationManagerCompat.from(this).notify(1, notification(text, state))
    }

    private suspend fun sendReportQuery(db: AppDatabase, item: EntranceReportEntity) {
         NetworkHelper.sendReport(
             item,
             db.entrancePhotoDao().getEntrancePhoto(item.taskId, item.taskItemId, item.entranceNumber)
         )
    }

    private fun getReportQuery(db: AppDatabase): EntranceReportEntity? {
        return db.entranceReportDao().all.firstOrNull()
    }

    private fun sendSendQuery(item: SendQueryItemEntity) {
        val urlConnection = URL(item.url)
        with(urlConnection.openConnection() as HttpURLConnection) {
            requestMethod = "POST"

            val wr = OutputStreamWriter(outputStream)
            wr.write(item.post_data)
            wr.flush()

            if (responseCode != 200) {
                throw Exception("Wrong response code.")
            }
        }
    }

    private fun getSendQuery(db: AppDatabase): SendQueryItemEntity? {
        return db.sendQueryDao().all.firstOrNull()
    }

    override fun onDestroy() {
        thread?.cancel()
        stopForeground(true)
        isRunning = false
        super.onDestroy()
    }

    companion object{
        var isRunning: Boolean = false
    }
}
