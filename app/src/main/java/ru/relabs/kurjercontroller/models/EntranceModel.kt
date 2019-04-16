package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.database.entities.EntranceEntity

/**
 * Created by ProOrange on 19.03.2019.
 */

data class EntranceModel(
    val number: Int,
    val availableEuroKeys: List<String>,
    val availableKeys: List<String>,
    val code: String,
    val startApartments: Int,
    val endApartments: Int,
    val floors: Int,
    val mailboxType: Int,
    val state: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.createStringArrayList().orEmpty(),
        parcel.createStringArrayList().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(number)
        parcel.writeStringList(availableEuroKeys)
        parcel.writeStringList(availableKeys)
        parcel.writeString(code)
        parcel.writeInt(startApartments)
        parcel.writeInt(endApartments)
        parcel.writeInt(floors)
        parcel.writeInt(mailboxType)
        parcel.writeInt(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toEntity(taskItemId: Int): EntranceEntity =
        EntranceEntity(
            id = 0,
            taskItemId = taskItemId,
            state = state,
            availableEuroKeys = availableEuroKeys,
            availableKeys = availableKeys,
            code = code,
            endApartments = endApartments,
            floors = floors,
            mailboxType = mailboxType,
            startApartments = startApartments,
            number = number
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