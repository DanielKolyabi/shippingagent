package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FilteredTasksData(
    val items: List<TaskItem>,
    val storages: List<TaskStorage>,
    val publishers: List<TaskPublisher>
) : Parcelable