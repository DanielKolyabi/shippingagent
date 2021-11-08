package ru.relabs.kurjercontroller.presentation.host

import android.net.Uri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.controllers.ServiceEvent
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.providers.FirebaseToken
import ru.relabs.kurjercontroller.domain.useCases.AppUpdateUseCase
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.host.featureCheckers.FeatureChecker
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import ru.relabs.kurjercontroller.utils.extensions.getFirebaseToken
import java.io.File

object HostEffects {
    fun effectInit(restored: Boolean): HostEffect = { c, _ ->
        if (!restored) {
            if (c.repository.isAuthenticated() && c.loginUseCase.isAutologinEnabled() && c.loginUseCase.loginOffline() != null) {
                withContext(Dispatchers.Main) {
                    c.router.newRootScreen(RootScreen.Tasks(true))
                }
                withContext(Dispatchers.IO) {
                    when (val result = FirebaseInstanceId.getInstance().getFirebaseToken()) {
                        is Right -> c.repository.updatePushToken(FirebaseToken(result.value))
                        is Left -> FirebaseCrashlytics.getInstance().log("Can't get firebase token")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    c.router.newRootScreen(RootScreen.Login())
                }
            }
        }
    }

    fun effectLogout(): HostEffect = { c, s ->
        c.loginUseCase.logout()
        withContext(Dispatchers.Main) {
            c.router.newRootScreen(RootScreen.Login())
        }
    }

    fun effectCopyDeviceUUID(): HostEffect = { c, _ ->
        val uuid = c.deviceUUIDProvider.getOrGenerateDeviceUUID()
        withContext(Dispatchers.Main) {
            c.copyToClipboard(uuid.id)
        }
    }

    fun effectEnableLocation(): HostEffect = { c, s ->
        withContext(Dispatchers.Main) {
            if (c.featureCheckersContainer?.gps?.isFeatureEnabled() == true && c.featureCheckersContainer?.permissions?.isFeatureEnabled() == true) {
                if (!c.locationProvider.startInBackground()) {
                    CustomLog.writeToFile("Unable to launch gps in background")
                }
            }
        }
    }

    fun effectDisableLocation(): HostEffect = { c, s ->
        withContext(Dispatchers.Main) {
            if (c.featureCheckersContainer?.gps?.isFeatureEnabled() == true) {
                c.locationProvider.stopInBackground()
            }
        }
    }

    //UPDATES
    fun effectCheckUpdates(): HostEffect = { c, s ->
        messages.send(HostMessages.msgAddLoaders(1))
        when (val r = s.appUpdates?.let { Right(it) } ?: c.updatesUseCase.getAppUpdatesInfo()) {
            is Right -> {
                messages.send(HostMessages.msgUpdatesInfo(r.value))
                if (r.value.required != null && r.value.required.version > BuildConfig.VERSION_CODE) {
                    withContext(Dispatchers.Main) {
                        if (c.showUpdateDialog(r.value.required)) {
                            messages.send(HostMessages.msgUpdateDialogShowed(true))
                        }
                    }
                } else if (r.value.optional != null && r.value.optional.version > BuildConfig.VERSION_CODE) {
                    withContext(Dispatchers.Main) {
                        if (c.showUpdateDialog(r.value.optional)) {
                            messages.send(HostMessages.msgUpdateDialogShowed(true))
                        }
                    }
                }
            }
            is Left -> withContext(Dispatchers.Main) {
                c.showErrorDialog(R.string.update_cant_get_info)
            }

        }
        messages.send(HostMessages.msgAddLoaders(-1))
    }

    fun effectLoadUpdate(uri: Uri): HostEffect = { c, s ->
        messages.send(HostMessages.msgLoadProgress(0))
        c.updatesUseCase.downloadUpdate(uri).collect {
            when (it) {
                is AppUpdateUseCase.DownloadState.Progress ->
                    messages.send(HostMessages.msgLoadProgress(((it.current / it.total.toFloat()) * 100).toInt()))
                is AppUpdateUseCase.DownloadState.Success -> {
                    messages.send(HostMessages.msgUpdateLoaded(it.file))
                    messages.send(msgEffect(effectInstallUpdate(it.file)))
                }
                AppUpdateUseCase.DownloadState.Failed -> {
                    messages.send(HostMessages.msgUpdateLoadingFailed())
                    withContext(Dispatchers.Main) {
                        c.showErrorDialog(R.string.update_install_error)
                    }
                }
            }
        }
        messages.send(HostMessages.msgLoadProgress(null))
    }

    fun effectInstallUpdate(file: File): HostEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.installUpdate(file)
        }
    }

    suspend fun checkFeature(check: FeatureChecker): Boolean {
        if (!check.isFeatureEnabled()) {
            withContext(Dispatchers.Main) {
                check.requestFeature()
            }
            return false
        }
        return true
    }

    fun effectCheckRequirements(): HostEffect = { c, s ->
        val featureCheckersContainer = c.featureCheckersContainer
        if (featureCheckersContainer != null) {
            if (
                checkFeature(featureCheckersContainer.permissions) &&
                checkFeature(featureCheckersContainer.xiaomiPermissions) &&
                checkFeature(featureCheckersContainer.network) &&
                checkFeature(featureCheckersContainer.gps) &&
                checkFeature(featureCheckersContainer.time)
            ) {

                if (isUpdateRequired(s) &&
                    s.updateFile == null &&
                    !s.isUpdateDialogShowed &&
                    s.updateLoadProgress == null
                ) {

                    messages.send(msgEffect(effectCheckUpdates()))
                } else if (s.updateFile != null && isUpdateRequired(s)) {
                    messages.send(msgEffect(effectInstallUpdate(s.updateFile)))
                }
            }
        }
    }

    private fun isUpdateRequired(state: HostState): Boolean =
        state.appUpdates == null || ((state.appUpdates.required?.version
            ?: 0) > BuildConfig.VERSION_CODE && !state.isUpdateLoadingFailed)

    fun effectSubscribe(): HostEffect = { c, s ->
        coroutineScope {
            launch {
                c.taskEventController.subscribe().collect {
                    messages.send(msgEffect(effectRefreshEntranceMonitoringData()))
                    when (it) {
                        is TaskEvent.TasksUpdateRequired -> withContext(Dispatchers.Main) {
                            if (!it.showDialogInTasks) {
                                c.showTaskUpdateRequired()
                            }
                        }
                    }
                }
            }
            launch {
                for (user in c.userRepository.currentUser.openSubscription()) {
                    messages.send(HostMessages.msgUserLoaded(user))
                }
            }
            launch {
                while (isActive) {
                    delay(5000)
                    if (!c.updatesUseCase.isAppUpdated) {
                        messages.send(HostMessages.msgRequestUpdates())
                    }
                }
            }
            launch {
                c.serviceEventController.subscribe().collect {
                    if (it == ServiceEvent.Stop) {
                        withContext(Dispatchers.Main) {
                            c.finishApp()
                        }
                    }
                }
            }
            launch {
                c.entranceMonitoringRepository.closedCountFlow.collect {
                    messages.send(HostMessages.msgClosedEntrancesCountUpdated(it))
                }
            }
        }
    }

    fun effectNavigateUpdateTaskList(): HostEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.newRootScreen(RootScreen.Tasks(true))
        }
    }

    fun effectNotifyUpdateRequiredOnTasksOpen(): HostEffect = { c, s ->
        c.taskEventController.send(TaskEvent.TasksUpdateRequired(true))
    }

    fun effectRefreshEntranceMonitoringData(): HostEffect = { c, s ->
        val isCounterEnabled = c.settingsRepository.entrancesMonitoring.isCounterEnabled
        val requiredEntrances = c.entranceMonitoringRepository.getRequiredEntrancesCount()
        val closedEntrances = c.entranceMonitoringRepository.closedCountFlow.value

        messages.send(HostMessages.msgEntranceMonitoringDataLoaded(isCounterEnabled, requiredEntrances, closedEntrances))
    }
}