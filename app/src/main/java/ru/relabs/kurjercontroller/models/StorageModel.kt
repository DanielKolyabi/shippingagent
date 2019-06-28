package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.database.entities.TaskStorageEntity

/**
 * Created by ProOrange on 27.06.2019.
 */
data class StorageModel(
    val id: Int,
    val address: String,
    val lat: Float,
    val long: Float
) : Parcelable {
    fun toEntity(taskId: Int) = TaskStorageEntity(
        id = 0,
        taskId = taskId,
        storageId = id,
        gpsLat = lat,
        gpsLong = long,
        address = address
    )

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readFloat()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(address)
        parcel.writeFloat(lat)
        parcel.writeFloat(long)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StorageModel> {
        override fun createFromParcel(parcel: Parcel): StorageModel {
            return StorageModel(parcel)
        }

        override fun newArray(size: Int): Array<StorageModel?> {
            return arrayOfNulls(size)
        }
    }
}