package ru.relabs.kurjercontroller.domain.models

import org.joda.time.DateTime

data class GPSCoordinatesModel(
        val lat: Double,
        val long: Double,
        val time: DateTime
)
