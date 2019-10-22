package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by ProOrange on 06.06.2019.
 */

data class AddressIdWithColor(
    val id: Int,
    val color: Int = Color.BLUE,
    val outlineColor: Int = color
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(color)
        parcel.writeInt(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressIdWithColor> {
        override fun createFromParcel(parcel: Parcel): AddressIdWithColor {
            return AddressIdWithColor(parcel)
        }

        override fun newArray(size: Int): Array<AddressIdWithColor?> {
            return arrayOfNulls(size)
        }
    }
}

