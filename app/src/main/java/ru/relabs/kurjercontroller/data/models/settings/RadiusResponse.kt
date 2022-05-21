package ru.relabs.kurjercontroller.data.models.settings

import com.google.gson.annotations.SerializedName

/**
 * Created by Daniil Kurchanov on 06.01.2020.
 */
data class RadiusResponse(
    @SerializedName("closeAnyRadius") val closeAnyDistance: Boolean,
    @SerializedName("photoAnyDistance") val photoAnyDistance: Boolean
)