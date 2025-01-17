package ru.relabs.kurjercontroller.presentation.yandexMap

import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.domain.providers.LocationProvider
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.storage.MapCameraStorage
import ru.relabs.kurjercontroller.presentation.base.tea.*
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.DeliverymanPositionData

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

sealed class MapLayer {
    object Common : MapLayer()
    object Predefined : MapLayer()
    data class TaskLayer(val task: Task) : MapLayer()
}

data class YandexMapState(
    val addresses: List<AddressWithColor> = emptyList(),
    val deliverymanIds: List<Int> = emptyList(),
    val deliverymans: List<DeliverymanPositionData> = emptyList(),
    val storages: List<TaskStorage> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val taskLoadings: Map<TaskId, Boolean> = mapOf(),
    val newTaskItems: List<TaskItem> = emptyList(),
    val cameraPosition: Pair<Double, Double>? = null,
    val cameraZoom: Float = 14f,
    val cameraUpdateRequired: Boolean = false,
    val selectedLayer: MapLayer = MapLayer.Common,
    val deliverymansLoading: Boolean = false
)

class YandexMapContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val databaseRepository: DatabaseRepository by inject()
    val controlRepository: ControlRepository by inject()
    val locationProvider: LocationProvider by inject()
    val mapCameraStorage: MapCameraStorage by inject()
    val taskEventController: TaskEventController by inject()

    var notifyAddressClicked: (address: Address) -> Unit = {}
    var notifyItemsAdded: (items: List<TaskItem>) -> Unit = {}
    var moveCameraToUser: (location: Point) -> Unit = {}
    var getCameraPositionFromBound: (BoundingBox) -> Pair<Double, Double>? = { null }
}

typealias YandexMapMessage = ElmMessage<YandexMapContext, YandexMapState>
typealias YandexMapEffect = ElmEffect<YandexMapContext, YandexMapState>
typealias YandexMapRender = ElmRender<YandexMapState>