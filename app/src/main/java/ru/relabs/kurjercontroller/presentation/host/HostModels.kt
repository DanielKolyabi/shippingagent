package ru.relabs.kurjercontroller.presentation.host

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.domain.controllers.ServiceEventController
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.AppUpdate
import ru.relabs.kurjercontroller.domain.models.AppUpdatesInfo
import ru.relabs.kurjercontroller.domain.providers.DeviceUUIDProvider
import ru.relabs.kurjercontroller.domain.providers.LocationProvider
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.EntranceMonitoringRepository
import ru.relabs.kurjercontroller.domain.repositories.SettingsRepository
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage
import ru.relabs.kurjercontroller.domain.useCases.AppUpdateUseCase
import ru.relabs.kurjercontroller.presentation.base.fragment.AppBarSettings
import ru.relabs.kurjercontroller.presentation.base.tea.*
import ru.relabs.kurjercontroller.presentation.host.featureCheckers.FeatureCheckersContainer
import java.io.File

/**
 * Created by Daniil Kurchanov on 20.11.2019.
 */
data class HostState(
    val settings: AppBarSettings = AppBarSettings(),
    val loaders: Int = 0,
    val userLogin: UserLogin? = null,

    val updateLoadProgress: Int? = null,
    val appUpdates: AppUpdatesInfo? = null,
    val isUpdateLoadingFailed: Boolean = false,
    val updateFile: File? = null,
    val isUpdateDialogShowed: Boolean = false,

    val closedEntrances: Int = 0,
    val requiredEntrances: Int = 0,
    val isClosedCounterEnabled: Boolean = false
)

class HostContext(
    val errorContext: ErrorContextImpl = ErrorContextImpl()
) : KoinComponent,
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl() {

    val repository: ControlRepository by inject()
    val updatesUseCase: AppUpdateUseCase by inject()
    val deviceUUIDProvider: DeviceUUIDProvider by inject()
    val locationProvider: LocationProvider by inject()
    val taskEventController: TaskEventController by inject()
    val serviceEventController: ServiceEventController by inject()
    val userRepository: CurrentUserStorage by inject()
    val entranceMonitoringRepository: EntranceMonitoringRepository by inject()
    val settingsRepository: SettingsRepository by inject()

    var copyToClipboard: (String) -> Unit = {}
    var showUpdateDialog: (AppUpdate) -> Boolean = { false }
    var showErrorDialog: (id: Int) -> Unit = {}
    var installUpdate: (updateFile: File) -> Unit = {}
    var showTaskUpdateRequired: () -> Unit = {}

    var featureCheckersContainer: FeatureCheckersContainer? = null

    var finishApp: () -> Unit = {}
}

typealias HostRender = ElmRender<HostState>
typealias HostMessage = ElmMessage<HostContext, HostState>
typealias HostEffect = ElmEffect<HostContext, HostState>