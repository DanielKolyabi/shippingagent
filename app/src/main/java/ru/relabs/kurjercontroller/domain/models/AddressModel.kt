package ru.relabs.kurjercontroller.domain.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.data.database.entities.AddressEntity

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
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readString().orEmpty(),
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

    fun toEntity(): AddressEntity =
        AddressEntity(
            id = id,
            street = street,
            idnd = idnd,
            houseName = houseName,
            house = house,
            city = city,
            gpsLat = lat,
            gpsLong = long
        )

    override fun equals(other: Any?): Boolean {
        if(other is AddressModel){
            return this.id == other.id
        }
        return super.equals(other)
    }

    companion object CREATOR : Parcelable.Creator<AddressModel> {
        override fun createFromParcel(parcel: Parcel): AddressModel {
            return AddressModel(parcel)
        }

        override fun newArray(size: Int): Array<AddressModel?> {
            return arrayOfNulls(size)
        }

        fun blank(): AddressModel {
            return AddressModel(-1, -1, "Unknown", "Unknown", -1, "Unknown", .0, .0)
        }
    }
}