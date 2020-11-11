package ru.relabs.kurjercontroller.presentation.yandexMap

import android.graphics.Color
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.VisibleRegion
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.DeliverymanPositionData
import ru.relabs.kurjercontroller.presentation.yandexMap.YandexMapFragment.Companion.WRONG_METHOD_OUTLINE_COLOR
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.utils.extensions.placemarkColor

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object YandexMapMessages {
    fun msgInit(
        addressIds: List<AddressIdWithColor>,
        deliverymanIds: List<Int>,
        storages: List<TaskStorage>,
        tasks: List<TaskId>
    ): YandexMapMessage = msgEffects(
        {
            it.copy(
                deliverymanIds = deliverymanIds,
                storages = storages
            )
        },
        { listOf(YandexMapEffects.effectInit(addressIds, tasks, true)) }
    )

    fun msgAddressesLoaded(addresses: List<AddressWithColor>): YandexMapMessage =
        msgState { it.copy(addresses = addresses) }

    fun msgMyLocationClicked(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectMoveCameraToUser())

    fun msgDeliverymansLocationClicked(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectLoadDeliverymans())

    fun msgCommonLayerClicked(): YandexMapMessage = msgState {
        val items = it.tasks.flatMap { it.taskItems }
        val addresses = items.map { it.address }.distinctBy { it.idnd }
        val coloredAddresses = items
            .groupBy { it.address.idnd }
            .map {
                AddressWithColor(addresses.first { addr -> addr.idnd == it.key }, Color.CYAN)
            }
        val newAddresses = it.newTaskItems.map { AddressWithColor(it.address, Color.rgb(0, 100, 0)) }
        it.copy(
            addresses = coloredAddresses + newAddresses,
            storages = emptyList(),
            selectedLayer = MapLayer.Common
        )
    }

    fun msgPredefinedLayerClicked(): YandexMapMessage = msgState {
        val items = it.tasks
            .filter {
                !it.filtered
            }
            .flatMap {
                it.taskItems
            }
        val addresses = items.map { it.address }.distinctBy { it.idnd }
        val coloredAddresses = items.groupBy {
            it.address.idnd
        }.map {
            val placemarkColor = it.value.placemarkColor()
            AddressWithColor(
                addresses.first { addr -> addr.idnd == it.key },
                placemarkColor,
                if (it.value.any { it.wrongMethod && !it.isClosed }) WRONG_METHOD_OUTLINE_COLOR else placemarkColor
            )
        }

        it.copy(
            addresses = coloredAddresses,
            storages = emptyList(),
            selectedLayer = MapLayer.Predefined
        )
    }

    fun msgTaskLayerClicked(task: Task): YandexMapMessage = msgState {
        val addresses = task.taskItems.map { it.address }.distinctBy { it.idnd }
        val coloredAddresses = task.taskItems
            .groupBy { it.address.idnd }
            .map {
                val placemarkColor = it.value.placemarkColor()
                AddressWithColor(
                    addresses.first { addr -> addr.idnd == it.key },
                    placemarkColor,
                    if (it.value.any { it.wrongMethod && !it.isClosed }) WRONG_METHOD_OUTLINE_COLOR else placemarkColor
                )
            }

        val newAddresses = it.newTaskItems
            .filter { it.taskId == task.id }
            .map { AddressWithColor(it.address, Color.rgb(0, 100, 0)) }

        it.copy(
            addresses = coloredAddresses + newAddresses,
            storages = task.storages,
            selectedLayer = MapLayer.TaskLayer(task)
        )
    }

    fun msgAddressClicked(address: Address): YandexMapMessage =
        msgEffect(YandexMapEffects.effectNotifyAddressClicked(address))

    fun msgDeliverymansLoaded(deliverymanPositions: List<DeliverymanPositionData>): YandexMapMessage =
        msgState { it.copy(deliverymans = deliverymanPositions) }

    fun msgNavigateBack(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectNavigateBack())

    fun msgTasksLoaded(tasks: List<Task>): YandexMapMessage =
        msgState {
            it.copy(
                tasks = tasks,
                deliverymanIds = tasks.flatMap { it.taskItems }.map { it.deliverymanId }.distinct()
            )
        }

    fun msgTaskLayerLoading(task: Task, b: Boolean): YandexMapMessage =
        msgState { it.copy(taskLoadings = it.taskLoadings + Pair(task.id, b)) }

    fun msgNewTaskItemsLoaded(newTaskItems: List<TaskItem>): YandexMapMessage =
        msgState { it.copy(newTaskItems = newTaskItems) }

    fun msgAddNewAddresses(visibleRegion: VisibleRegion): YandexMapMessage =
        msgEffect(YandexMapEffects.effectAddNewAddresses(visibleRegion))

    fun msgNewTaskItemsAdded(selectedNewTaskItems: List<TaskItem>): YandexMapMessage =
        msgState { it.copy(newTaskItems = it.newTaskItems.filter { !selectedNewTaskItems.contains(it) }) }

    fun msgCameraChanged(cameraPosition: CameraPosition): YandexMapMessage =
        msgState {
            it.copy(
                cameraPosition = Pair(cameraPosition.target.latitude, cameraPosition.target.longitude),
                cameraZoom = cameraPosition.zoom
            )
        }

    fun msgSaveCameraPosition(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectSaveCameraPosition())
}