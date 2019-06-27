package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by ProOrange on 26.06.2019.
 */
data class UserLocationsResponse(
    val status: Boolean,
    val name: String,
    val locations: List<UserLocationResponse>
)

data class UserLocationResponse(
    @SerializedName("device_id")
    val deviceId: String,
    val location: LocationResponse
)

data class LocationResponse(
    val lat: Float,
    val long: Float,
    val time: Date
)