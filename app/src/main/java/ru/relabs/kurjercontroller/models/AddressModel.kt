package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by ProOrange on 19.03.2019.
 */

data class AddressModel(
    val id: Int, //IDDOT
    val idnd: Int, //IDND
    val city: String,
    val street: String,
    val house: Int,
    val houseName: String,
    val lat: Double,
    val long: Double
) : Parcelable {
    val name: String
        get() = "$street, д. $houseName"

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(idnd)
        parcel.writeString(city)
        parcel.writeString(street)
        parcel.writeInt(house)
        parcel.writeString(houseName)
        parcel.writeDouble(lat)
        parcel.writeDouble(long)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressModel> {
        override fun createFromParcel(parcel: Parcel): AddressModel {
            return AddressModel(parcel)
        }

        override fun newArray(size: Int): Array<AddressModel?> {
            return arrayOfNulls(size)
        }
    }
}