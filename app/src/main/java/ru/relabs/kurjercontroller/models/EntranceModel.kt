package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by ProOrange on 19.03.2019.
 */

data class EntranceModel(
    val id: Int,
    @SerializedName("start_appartaments")
    val startAppartaments: Int,
    @SerializedName("end_appartaments")
    val endAppartaments: Int,
    @SerializedName("available_keys")
    val availableKeys: List<String>,
    @SerializedName("available_euro_keys")
    val availableEuroKeys: List<String>,
    val code: String,
    val floors: Int,
    val state: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(startAppartaments)
        parcel.writeInt(endAppartaments)
        parcel.writeStringList(availableKeys)
        parcel.writeStringList(availableEuroKeys)
        parcel.writeString(code)
        parcel.writeInt(floors)
        parcel.writeInt(state)
    }

    override fun describeContents(): Int {
        return 0
    }

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