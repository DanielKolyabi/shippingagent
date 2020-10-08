package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PauseTimes(
    val loading: PauseTimeInterval,
    val lunch: PauseTimeInterval
) : Parcelable

@Parcelize
data class PauseTimeInterval(
    val start: Long,
    val end: Long
) : Parcelable

@Parcelize
data class PauseDurations(
    val loading: Long,
    val lunch: Long
) : Parcelable

