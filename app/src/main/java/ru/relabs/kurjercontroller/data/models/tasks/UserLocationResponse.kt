package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by ProOrange on 26.06.2019.
 */
data class UserLocationsResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("locations") val locations: List<UserLocationResponse>
)

data class UserLocationResponse(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("location") val location: LocationResponse
)

data class LocationResponse(
    @SerializedName("lat") val lat: Float,
    @SerializedName("long") val long: Float,
    @SerializedName("time") val time: Date
)