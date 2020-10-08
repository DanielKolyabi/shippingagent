package ru.relabs.kurjercontroller.domain.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.data.database.entities.EntranceEntity

/**
 * Created by ProOrange on 19.03.2019.
 */

data class EntranceModel(
    val number: Int,
    val euroKey: String,
    val key: String,
    val code: String,
    var startApartments: Int,
    var endApartments: Int,
    val floors: Int,
    val mailboxType: Int,
    var state: Int,
    val hasLookout: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt() != 0
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(number)
        parcel.writeString(euroKey)
        parcel.writeString(key)
        parcel.writeString(code)
        parcel.writeInt(startApartments)
        parcel.writeInt(endApartments)
        parcel.writeInt(floors)
        parcel.writeInt(mailboxType)
        parcel.writeInt(state)
        parcel.writeInt(if (hasLookout) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toEntity(taskId: Int, taskItemId: Int): EntranceEntity =
        EntranceEntity(
            id = 0,
            taskItemId = taskItemId,
            state = state,
            euroKey = euroKey,
            key = key,
            code = code,
            endApartments = endApartments,
            floors = floors,
            mailboxType = mailboxType,
            startApartments = startApartments,
            number = number,
            taskId = taskId,
            hasLookout = hasLookout
        )

    companion object CREATOR : Parcelable.Creator<EntranceModel> {
        override fun createFromParcel(parcel: Parcel): EntranceModel {
            return EntranceModel(parcel)
        }

        override fun newArray(size: Int): Array<EntranceModel?> {
            return arrayOfNulls(size)
        }

        val CREATED = 0
        val CLOSED = 1
    }
}