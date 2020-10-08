package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.domain.models.StorageModel

/**
 * Created by ProOrange on 27.06.2019.
 */
data class StorageResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Float,
    @SerializedName("long") val long: Float
) {
//    fun toModel() = StorageModel(id, address, lat, long)
}