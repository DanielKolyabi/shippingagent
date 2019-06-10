package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.models.AddressModel

/**
 * Created by ProOrange on 06.06.2019.
 */
data class AddressWithColor(
    val address: AddressModel,
    val color: Int = Color.BLUE
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(AddressModel::class.java.classLoader),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(address, flags)
        parcel.writeInt(color)
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