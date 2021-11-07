package ru.relabs.kurjercontroller.data.models.common

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.data.models.settings.EntrancesMonitoringSettingsResponse
import ru.relabs.kurjercontroller.data.models.settings.RadiusResponse

data class SettingsResponse(
    @SerializedName("radius")
    val radius: RadiusResponse,
    @SerializedName("gpsRefreshTime")
    val gpsRefreshTimes: GpsRefreshTimesResponse,
    @SerializedName("entrancesMonitoring")
    val entrancesMonitoring: EntrancesMonitoringSettingsResponse
)
