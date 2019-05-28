package ru.relabs.kurjercontroller

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.application.MyApplication
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.network.NetworkHelper
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class ReportService : Service() {
    private var thread: Job? = null
    private val bgScope = CancelableScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun notification(body: String): Notification {
        val channelId = "controller_notification_channel"
        val pi = PendingIntent.getService(
            this,
            0,
            Intent(this, ReportService::class.java).apply { putExtra("stopService", true) },
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }

        return NotificationCompat.Builder(applicationContext)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_arrow)
            .setWhen(System.currentTimeMillis())
            .addAction(R.drawable.ic_stop_black_24dp, "Отключить", pi)
            .setChannelId(channelId)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras?.getBoolean("stopService") == true) {
            stopSelf()
            return Service.START_STICKY
        }

        startForeground(1, notification("Сервис отправки данных."))

        val db = MyApplication.instance.database
        var lastTasksChecking = System.currentTimeMillis()
        var lastNetworkEnabledChecking = System.currentTimeMillis()

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
                updateNotificationText(db)

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

                delay(if (!isTaskSended) 10000 else 1000)
            }
        }

        return START_STICKY
    }

    private fun updateNotificationText(db: AppDatabase) {
        val count = db.sendQueryDao().all.size + db.entranceReportDao().all.size
        startForeground(1, notification("Сервис отправки данных. В очереди $count"))
    }

    private suspend fun sendReportQuery(db: AppDatabase, item: EntranceReportEntity) {
         NetworkHelper.sendReport(
             item,
             db.entrancePhotoDao().getEntrancePhoto(item.taskItemId, item.entranceNumber)
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
        super.onDestroy()
    }
}
