package ru.relabs.kurjercontroller.network.models

import ru.relabs.kurjercontroller.models.StorageModel

/**
 * Created by ProOrange on 27.06.2019.
 */
data class StorageResponseModel(
    val id: Int,
    val address: String,
    val lat: Float,
    val long: Float
) {
    fun toModel() = StorageModel(id, address, lat, long)
}