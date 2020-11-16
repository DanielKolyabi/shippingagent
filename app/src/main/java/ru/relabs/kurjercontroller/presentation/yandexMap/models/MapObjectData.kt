package ru.relabs.kurjercontroller.presentation.yandexMap.models

import ru.relabs.kurjercontroller.domain.models.Address


sealed class MapObjectData(val type: MapObjectType) {
    data class TaskItem(val address: Address) : MapObjectData(MapObjectType.Address)
    object Storage : MapObjectData(MapObjectType.Storage)
    object DeliveryMan : MapObjectData(MapObjectType.DeliveryMan)
}


enum class MapObjectType {
    Address, Storage, DeliveryMan
}