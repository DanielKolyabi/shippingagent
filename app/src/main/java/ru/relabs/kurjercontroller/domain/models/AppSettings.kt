package ru.relabs.kurjercontroller.domain.models

data class AppSettings(
    val radius: AllowedCloseRadius,
    val gpsRefreshTimes: GpsRefreshTimes
)