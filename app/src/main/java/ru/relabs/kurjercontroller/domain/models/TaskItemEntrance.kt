package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

const val ENTRANCE_NUMBER_TASK_ITEM = -1

@Parcelize
data class EntranceNumber(val number: Int): Parcelable

@Parcelize
data class TaskItemEntrance(
    val number: EntranceNumber,
    val apartmentsCount: Int,
    val isEuroBoxes: Boolean,
    val hasLookout: Boolean,
    val isStacked: Boolean,
    val isRefused: Boolean,
    var photoRequired: Boolean
): Parcelable