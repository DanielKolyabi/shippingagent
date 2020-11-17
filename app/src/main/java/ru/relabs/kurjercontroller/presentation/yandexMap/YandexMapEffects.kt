package ru.relabs.kurjercontroller.presentation.yandexMap

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.presentation.yandexMap.YandexMapRenders.isAddressInVisibleRegion
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.DeliverymanPositionData
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object YandexMapEffects {

    fun effectInit(
        addressIds: List<AddressIdWithColor>,
        taskIds: List<TaskId>,
        firstInit: Boolean
    ): YandexMapEffect = { c, s ->
        val addresses = addressIds.mapNotNull { awc ->
            c.databaseRepository.getAddress(AddressId(awc.id))?.let { a ->
                AddressWithColor(
                    a,
                    awc.color,
                    awc.outlineColor
                )
            }
        }.distinctBy {
            it.address.idnd
        }

        messages.send(YandexMapMessages.msgAddressesLoaded(addresses))

        val tasks = taskIds.mapNotNull {
            c.databaseRepository.getTask(it)
        }

        messages.send(YandexMapMessages.msgTasksLoaded(tasks))

        if (tasks.isNotEmpty()) {
            if (firstInit) {
                if (tasks.size > 1) {
                    messages.send(YandexMapMessages.msgCommonLayerClicked())
                } else {
                    tasks.firstOrNull()?.let { messages.send(YandexMapMessages.msgTaskLayerClicked(it)) }
                }
            }
            messages.send(msgEffect(effectLoadNewTaskItems()))
        }
        if (firstInit) {
            messages.send(msgEffect(effectFocusCamera()))
        }
    }

    private fun effectFocusCamera(): YandexMapEffect = { c, s ->
        val addresses = s.addresses.map { it.address }
        val addressPosition = when {
            addresses.isEmpty() -> null

            addresses.size == 1 -> {
                val a = addresses.firstOrNull()
                a?.let { Pair(a.lat, a.long) }
            }

            else -> {
                val filtered = addresses.filter { it.lat != 0.0 && it.long != 0.0 }
                val minLat = filtered.minBy { it.lat }?.lat
                val maxLat = filtered.maxBy { it.lat }?.lat
                val minLong = filtered.minBy { it.long }?.long
                val maxLong = filtered.maxBy { it.long }?.long
                if (minLat == null || maxLat == null || minLong == null || maxLong == null) {
                    val a = addresses.firstOrNull()
                    a?.let { Pair(a.lat, a.long) }
                } else {
                    c.getCameraPositionFromBound(BoundingBox(Point(minLat, minLong), Point(maxLat, maxLong)))
                        ?: run {
                            val a = addresses.firstOrNull()
                            a?.let { Pair(a.lat, a.long) }
                        }
                }
            }
        }
        val cameraPosition = c.mapCameraStorage.getCameraPosition()
            ?: addressPosition
            ?: c.locationProvider.lastReceivedLocation()?.let { Pair(it.latitude, it.longitude) }
            ?: c.locationProvider.updatesChannel().let {
                val coord = it.receive()
                it.cancel()
                coord.let { Pair(it.latitude, it.longitude) }
            }

        messages.send(
            YandexMapMessages.msgCameraChanged(
                CameraPosition(
                    Point(cameraPosition.first, cameraPosition.second),
                    c.mapCameraStorage.getCameraZoom() ?: 14f,
                    0f, 0f
                )
            )
        )
        messages.send(msgState { it.copy(cameraUpdateRequired = true) })
        delay(1000)
        messages.send(msgState { it.copy(cameraUpdateRequired = false) })
    }


    private fun effectLoadNewTaskItems(): YandexMapEffect = { c, s ->
        val newTaskItems = s.tasks.filter { it.filtered }.flatMap { task ->
            messages.send(YandexMapMessages.msgTaskLayerLoading(task, true))

            val currentAddresses = task.taskItems.map { it.address.id }

            val taskItems = when (val r = c.controlRepository.getFilteredTaskItems(task.taskFilters.all, task.withPlanned)) {
                is Right -> r.value
                    .items
                    .filter { !currentAddresses.contains(it.address.id) }
                    .map { it.copy(taskId = task.id) }
                is Left -> {
                    FirebaseCrashlytics.getInstance().recordException(r.value)
                    emptyList()
                }
            }
            messages.send(YandexMapMessages.msgTaskLayerLoading(task, false))

            taskItems
        }

        messages.send(YandexMapMessages.msgNewTaskItemsLoaded(newTaskItems))
    }

    fun effectNotifyAddressClicked(address: Address): YandexMapEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.notifyAddressClicked(address)
            c.router.exit()
        }
    }

    fun effectMoveCameraToUser(): YandexMapEffect = { c, s ->
        val coordinates = c.locationProvider.lastReceivedLocation()
            ?: c.locationProvider.updatesChannel().let {
                val coord = it.receive()
                it.cancel()
                coord
            }

        withContext(Dispatchers.Main) {
            c.moveCameraToUser(Point(coordinates.latitude, coordinates.longitude))
        }
    }

    fun effectLoadDeliverymans(): YandexMapEffect = { c, s ->
        coroutineScope {
            val deliverymanPositions = s.deliverymanIds
                .map {
                    async(Dispatchers.IO) {
                        when (val r = c.controlRepository.getUserPosition(it)) {
                            is Left -> {
                                FirebaseCrashlytics.getInstance().recordException(r.value)
                                null
                            }
                            is Right -> it to r.value
                        }
                    }
                }
                .awaitAll()
                .mapNotNull { it }
                .map { pair ->
                    pair.second.mapNotNull {
                        if (Date().time - it.time.millis < 6 * 60 * 1000)
                            DeliverymanPositionData(
                                pair.first,
                                it.name,
                                it.lat,
                                it.long
                            )
                        else
                            null

                    }
                }.flatten()

            messages.send(YandexMapMessages.msgDeliverymansLoaded(deliverymanPositions))
        }
    }

    fun effectNavigateBack(): YandexMapEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.exit()
        }
    }

    fun effectAddNewAddresses(visibleRegion: VisibleRegion): YandexMapEffect = { c, s ->
        if (s.selectedLayer is MapLayer.TaskLayer) {
            val selectedNewTaskItems = s.newTaskItems
                .filter { it.taskId == s.selectedLayer.task.id && isAddressInVisibleRegion(it.address, visibleRegion) }
            selectedNewTaskItems.forEach {
                c.databaseRepository.saveTaskItem(it)
            }
            messages.send(YandexMapMessages.msgNewTaskItemsAdded(selectedNewTaskItems))
            withContext(Dispatchers.Main) {
                c.notifyItemsAdded(selectedNewTaskItems)
            }
        }
    }

    fun effectReloadTasks(): YandexMapEffect = { c, s ->
        val tasks = s.tasks.mapNotNull { c.databaseRepository.getTask(it.id) }

        messages.send(YandexMapMessages.msgTasksLoaded(tasks, true))
    }

    fun effectSaveCameraPosition(): YandexMapEffect = { c, s ->
        if (s.cameraPosition != null) {
            c.mapCameraStorage.saveCameraSettings(s.cameraPosition, s.cameraZoom)
        }
    }

    fun effectLaunchCosumers(): YandexMapEffect = { c, s ->
        coroutineScope {
            launch {
                c.taskEventController.subscribe().collect {
                    when (it) {
                        is TaskEvent.TaskItemClosedByDeliveryMan ->
                            messages.send(YandexMapMessages.msgTaskItemClosedByDeliveryMan(it.taskItemId, it.closeTime))
                    }
                }
            }
        }
    }

    fun effectRefreshSelectedLayer(): YandexMapEffect = { c, s ->
        when (s.selectedLayer) {
            MapLayer.Common -> messages.send(YandexMapMessages.msgCommonLayerClicked())
            MapLayer.Predefined -> messages.send(YandexMapMessages.msgPredefinedLayerClicked())
            is MapLayer.TaskLayer -> s.tasks
                .firstOrNull { it.id == s.selectedLayer.task.id }
                ?.let { messages.send(YandexMapMessages.msgTaskLayerClicked(it)) }
        }
    }
}