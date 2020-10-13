package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FilteredTasksCount(
    val planned: Int,
    val closed: Int
): Parcelable