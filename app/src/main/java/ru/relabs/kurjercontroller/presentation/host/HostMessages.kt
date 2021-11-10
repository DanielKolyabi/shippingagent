package ru.relabs.kurjercontroller.presentation.host

import android.net.Uri
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.domain.models.AppUpdatesInfo
import ru.relabs.kurjercontroller.presentation.base.fragment.AppBarSettings
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import java.io.File

object HostMessages {
    fun msgInit(restored: Boolean): HostMessage = msgEffects(
        { it },
        {
            listOf(
                HostEffects.effectSubscribe(),
                HostEffects.effectInit(restored)
            )
        }
    )

    fun msgUpdateAppBar(settings: AppBarSettings): HostMessage =
        msgState { it.copy(settings = settings) }

    fun msgResume(): HostMessage = msgEffects(
        { it },
        { state ->
            listOfNotNull(
                HostEffects.effectCheckRequirements(), //Updates, Xiaomi Permissions, GPS, Network, Time
                HostEffects.effectEnableLocation()
            )
        }
    )

    fun msgPause(): HostMessage = msgEffects(
        { it },
        { listOf(HostEffects.effectDisableLocation()) }
    )

    fun msgAddLoaders(i: Int): HostMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgLogout(): HostMessage =
        msgEffect(HostEffects.effectLogout())

    fun msgCopyDeviceUUID(): HostMessage =
        msgEffect(HostEffects.effectCopyDeviceUUID())

    fun msgStartUpdateLoading(url: Uri): HostMessage =
        msgEffect(HostEffects.effectLoadUpdate(url))

    fun msgLoadProgress(progress: Int?): HostMessage =
        msgState { it.copy(updateLoadProgress = progress) }

    fun msgRequestUpdates(): HostMessage =
        msgEffects(
            { it },
            { state ->
                listOfNotNull(
                    HostEffects.effectCheckUpdates()
                        .takeIf { !state.isUpdateDialogShowed && state.updateLoadProgress == null }
                )
            }
        )

    fun msgUpdatesInfo(value: AppUpdatesInfo): HostMessage =
        msgState { it.copy(appUpdates = value) }

    fun msgUpdateLoadingFailed(): HostMessage =
        msgState { it.copy(isUpdateLoadingFailed = true) }

    fun msgUpdateLoaded(file: File): HostMessage =
        msgState { it.copy(updateFile = file) }

    fun msgUpdateDialogShowed(b: Boolean): HostMessage =
        msgState { it.copy(isUpdateDialogShowed = b) }

    fun msgRequiredUpdateOk(): HostMessage =
        msgEffect(HostEffects.effectNavigateUpdateTaskList())

    fun msgRequiredUpdateLater(): HostMessage =
        msgEffect(HostEffects.effectNotifyUpdateRequiredOnTasksOpen())

    fun msgUserLoaded(user: UserLogin?): HostMessage = msgEffects(
        { it.copy(userLogin = user) },
        { listOf(HostEffects.effectRefreshEntranceMonitoringData()) }
    )

    fun msgEntranceMonitoringDataLoaded(counterEnabled: Boolean, requiredEntrances: Int, closedEntrances: Int): HostMessage =
        msgState {
            it.copy(
                isClosedCounterEnabled = counterEnabled,
                requiredEntrances = requiredEntrances,
                closedEntrances = closedEntrances
            )
        }

    fun msgClosedEntrancesCountUpdated(count: Int): HostMessage =
        msgState { it.copy(closedEntrances = count) }

    fun msgRequiredEntrancesCountUpdated(count: Int): HostMessage =
        msgState { it.copy(requiredEntrances = count) }

    fun msgEntranceCounterEnabledUpdated(counterEnabled: Boolean): HostMessage =
        msgState { it.copy(isClosedCounterEnabled = counterEnabled) }

}