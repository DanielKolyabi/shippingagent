package ru.relabs.kurjercontroller.models

import org.joda.time.DateTime
import java.util.*

data class GPSCoordinatesModel(
        val lat: Double,
        val long: Double,
        val time: DateTime
)
