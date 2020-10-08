package ru.relabs.kurjercontroller.presentation.fragments.yandexMap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.domain.models.Address

/**
 * Created by ProOrange on 06.06.2019.
 */
data class AddressWithColor(
    val address: Address,
    val color: Int = Color.BLUE,
    val outlineColor: Int = color
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Address::class.java.classLoader),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(address, flags)
        parcel.writeInt(color)
        parcel.writeInt(outlineColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressWithColor> {
        override fun createFromParcel(parcel: Parcel): AddressWithColor {
            return AddressWithColor(parcel)
        }

        override fun newArray(size: Int): Array<AddressWithColor?> {
            return arrayOfNulls(size)
        }
    }
}