package ru.relabs.kurjercontroller.data.models.common

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.data.models.radius.RadiusResponse

data class SettingsResponse(
    @SerializedName("radius")
    val radius: RadiusResponse,
    @SerializedName("gpsRefreshTime")
    val gpsRefreshTimes: GpsRefreshTimesResponse
)
