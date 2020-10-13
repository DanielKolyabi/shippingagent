package ru.relabs.kurjercontroller.domain.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.data.database.entities.TaskStorageEntity

/**
 * Created by ProOrange on 27.06.2019.
 */
@Parcelize
data class StorageId(val id: Int): Parcelable

@Parcelize
data class TaskStorage(
    val id: StorageId,
    val address: String,
    val lat: Float,
    val long: Float
) : Parcelable