package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class GPSCoordinatesModel(
        val lat: Double,
        val long: Double,
        val time: DateTime
): Parcelable
