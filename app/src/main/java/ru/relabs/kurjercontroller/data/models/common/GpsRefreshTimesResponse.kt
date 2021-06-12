package ru.relabs.kurjercontroller.data.models.common

import com.google.gson.annotations.SerializedName

data class GpsRefreshTimesResponse(
    @SerializedName("close")
    val close: Int,
    @SerializedName("photo")
    val photo: Int
)
