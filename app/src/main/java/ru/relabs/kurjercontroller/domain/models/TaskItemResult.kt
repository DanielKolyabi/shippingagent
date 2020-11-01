package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
//import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import java.util.*

@Parcelize
data class TaskItemResultId(val id: Int) : Parcelable

@Parcelize
data class TaskItemResult(
    val id: TaskItemResultId,
    val taskItemId: TaskItemId,
    val closeTime: Date?,
    val description: String,
    val entrances: List<TaskItemEntranceResult>,
    val gps: GPSCoordinatesModel
) : Parcelable