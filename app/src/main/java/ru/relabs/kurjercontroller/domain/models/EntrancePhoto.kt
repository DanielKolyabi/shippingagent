package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.domain.providers.PathsProvider
import java.io.File


@Parcelize
data class PhotoId(val id: Int) : Parcelable

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
    var isEntrancePhoto: Boolean
) : Parcelable

fun EntrancePhoto.getFile(pathsProvider: PathsProvider) =
    realPath?.let { File(it) } ?: pathsProvider.getEntrancePhotoFileByID(taskItemId, entranceNumber, UUID)