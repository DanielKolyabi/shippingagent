package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PhotoId(val id: Int): Parcelable

@Parcelize
data class TaskItemPhoto(
    val id: PhotoId,
    val UUID: String,
    val taskItemId: TaskItemId,
    val entranceNumber: EntranceNumber
) : Parcelable