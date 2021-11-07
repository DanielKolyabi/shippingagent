package ru.relabs.kurjercontroller.data.models.settings

import com.google.gson.annotations.SerializedName

data class EntrancesMonitoringSettingsResponse(
    @SerializedName("isCounterEnabled")
    val isCounterEnabled: Boolean,
    @SerializedName("mode")
    val mode: Int
)
