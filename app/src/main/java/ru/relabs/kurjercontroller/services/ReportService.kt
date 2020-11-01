package ru.relabs.kurjercontroller.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ControllApplication
import ru.relabs.kurjercontroller.data.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.data.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.domain.controllers.ServiceEvent
import ru.relabs.kurjercontroller.domain.controllers.ServiceEventController
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.host.HostActivity
import ru.relabs.kurjercontroller.utils.*

const val CHANNEL_ID = "controller_notification_channel"
const val CLOSE_SERVICE_TIMEOUT = 80 * 60 * 1000
const val TASK_CHECK_DELAY = 25 * 60 * 1000

enum class ServiceState {
    TRANSFER, IDLE, UNAVAILABLE
}

class ReportService : Service(), KoinComponent {
    private val repository: ControlRepository by inject()
    private val databaseRepository: DatabaseRepository by inject()
    private val taskEventController: TaskEventController by inject()
    private val serviceEventController: ServiceEventController by inject()

    private var looperJob: Job? = null
    private val bgScope = CoroutineScope(Dispatchers.IO)

    private var currentIconBitmap: Bitmap? = null
    private var lastState: ServiceState = ServiceState.IDLE
    private var lastActivityResumeTime = 0L
    private var lastActivityRunningState = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras?.getBoolean("stopService") == true) {
            stopSelf()
            return Service.START_STICKY
        }

        instance = this

        startForeground(
            1, notification(
                "Сервис отправки данных.",
                ServiceState.IDLE
            )
        )
        isRunning = true

        var lastTasksChecking = System.currentTimeMillis()

        looperJob?.cancel()
        looperJob = bgScope.launch(Dispatchers.Default) {

            while (isActive) {
                var isTaskSended = false

                if (NetworkHelper.isNetworkAvailable(applicationContext)) {
                    val sendQuery = databaseRepository.getNextSendQuery()
                    val reportQuery = databaseRepository.getNextReportQuery()

                    when {
                        reportQuery != null -> {
                            when (val r = sendReportQuery(reportQuery)) {
                                is Left -> r.value.log()
                                is Right -> {
                                    isTaskSended = true
                                    databaseRepository.removeReport(reportQuery)
                                }
                            }
                        }
                        sendQuery != null -> {
                            when (val r = sendSendQuery(sendQuery)) {
                                is Left -> r.value.log()
                                is Right -> {
                                    isTaskSended = true
                                    databaseRepository.removeSendQuery(sendQuery)
                                }
                            }
                        }
                        System.currentTimeMillis() - lastTasksChecking > TASK_CHECK_DELAY -> {
                            when (val tasks = repository.getRemoteTasks()) {
                                is Right -> if (databaseRepository.isMergeNeeded(tasks.value)) {
                                    taskEventController.send(TaskEvent.TasksUpdateRequired(false))
                                }
                            }
                            lastTasksChecking = System.currentTimeMillis()
                        }
                    }
                }

                updateNotificationText()
                updateActivityState()
                delay(if (!isTaskSended) 5000 else 1000)
            }
        }

        return START_STICKY
    }

    private fun updateActivityState() {
        if (!lastActivityRunningState && !isAppPaused) {
            lastActivityResumeTime = System.currentTimeMillis()
        }

        if (isAppPaused && (System.currentTimeMillis() - lastActivityResumeTime) > CLOSE_SERVICE_TIMEOUT) {
            serviceEventController.send(ServiceEvent.Stop)
            stopSelf()
            stopForeground(true)
        }
        lastActivityRunningState = !isAppPaused
    }

    private suspend fun updateNotificationText() {
        val isNetworkAvailable = NetworkHelper.isNetworkAvailable(applicationContext)
        val count = databaseRepository.getQueryItemsCount()

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

    private suspend fun sendReportQuery(item: EntranceReportEntity): Either<Exception, Unit> =
        repository.sendReport(item)

    private suspend fun sendSendQuery(item: SendQueryItemEntity): Either<Exception, Unit> =
        repository.sendQuery(item)

    override fun onDestroy() {
        looperJob?.cancel()
        stopForeground(true)
        isRunning = false
        super.onDestroy()
    }

    private fun notification(body: String, status: ServiceState, update: Boolean = false): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(notificationChannel)
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

        val notification = NotificationCompat.Builder(
            applicationContext,
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

    companion object {
        var instance: ReportService? = null
        var isRunning: Boolean = false
        var isAppPaused: Boolean = false
        private val appCtx: Context = ControllApplication.appContext

        fun startService() = appCtx.startService(Intent(appCtx, ReportService::class.java))
    }
}
