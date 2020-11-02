package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PhotoId(val id: Int): Parcelable

@Parcelize
data class EntrancePhoto(
    val id: PhotoId,
    val UUID: String,
    var taskId: TaskId,
    val taskItemId: TaskItemId,
    val entranceNumber: EntranceNumber,
    var gps: GPSCoordinatesModel,
    var idnd: Int,
    var realPath: String?,
    var isEntrancePhoto: Boolean //TODO: WTF?
) : Parcelable