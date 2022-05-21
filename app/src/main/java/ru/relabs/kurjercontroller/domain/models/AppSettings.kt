package ru.relabs.kurjercontroller.domain.models

data class AppSettings(
    val isCloseRadiusRequired: Boolean,
    val isPhotoRadiusRequired: Boolean,
    val gpsRefreshTimes: GpsRefreshTimes,
    val entrancesMonitoring: EntrancesMonitoring
)