package ru.relabs.kurjercontroller.presentation.yandexMap

import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskStorage
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgEmpty
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.DeliverymanPositionData
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object YandexMapMessages {
    fun msgInit(
        addressIds: List<AddressIdWithColor>,
        deliverymanIds: List<Int>,
        storages: List<TaskStorage>
    ): YandexMapMessage = msgEffects(
        { it.copy(deliverymanIds = deliverymanIds, storages = storages) },
        { listOf(YandexMapEffects.effectInit(addressIds)) }
    )

    fun msgAddressesLoaded(addresses: List<AddressWithColor>): YandexMapMessage =
        msgState { it.copy(addresses = addresses) }

    fun msgMyLocationClicked(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectMoveCameraToUser())

    fun msgDeliverymansLocationClicked(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectLoadDeliverymans())

    fun msgCommonLayerClicked(): YandexMapMessage =
        msgEmpty()

    fun msgPredefinedLayerClicked(): YandexMapMessage =
        msgEmpty()

    fun msgTaskLayerClicked(task: Task): YandexMapMessage =
        msgEmpty()

    fun msgAddressClicked(address: Address): YandexMapMessage =
        msgEffect(YandexMapEffects.effectNotifyAddressClicked(address))

    fun msgDeliverymansLoaded(deliverymanPositions: List<DeliverymanPositionData>): YandexMapMessage =
        msgState { it.copy(deliverymans = deliverymanPositions) }

    fun msgNavigateBack(): YandexMapMessage =
        msgEffect(YandexMapEffects.effectNavigateBack())


}