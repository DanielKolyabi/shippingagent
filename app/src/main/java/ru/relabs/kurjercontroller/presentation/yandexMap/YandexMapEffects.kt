package ru.relabs.kurjercontroller.presentation.yandexMap

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.DeliverymanPositionData
import ru.relabs.kurjercontroller.presentation.yandexMap.YandexMapRenders.isAddressInVisibleRegion
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object YandexMapEffects {

    fun effectInit(
        addressIds: List<AddressIdWithColor>,
        taskIds: List<TaskId>
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
            if (tasks.size > 1) {
                messages.send(YandexMapMessages.msgCommonLayerClicked())
            } else {
                tasks.firstOrNull()?.let { messages.send(YandexMapMessages.msgTaskLayerClicked(it)) }
            }
            messages.send(msgEffect(effectLoadNewTaskItems()))
        }
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
                            DeliverymanPositionData(pair.first, it.name, it.lat, it.long)
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
            messages.send(msgEffect(effectInit(emptyList(), s.tasks.map { it.id })))
            //TODO: Notify parent taskItems added
        }
    }
}