package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class UserLocation(
    val deviceId: String,
    val lat: Float,
    val long: Float,
    val time: DateTime,
    val name: String
): Parcelable