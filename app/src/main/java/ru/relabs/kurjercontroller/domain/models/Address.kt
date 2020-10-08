package ru.relabs.kurjercontroller.domain.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.data.database.entities.AddressEntity

/**
 * Created by ProOrange on 19.03.2019.
 */
@Parcelize
data class AddressId(val id: Int) : Parcelable

@Parcelize
data class Address(
    val id: AddressId, //IDDOT
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

    fun toEntity(): AddressEntity =
        AddressEntity(
            id = id.id,
            street = street,
            idnd = idnd,
            houseName = houseName,
            house = house,
            city = city,
            gpsLat = lat,
            gpsLong = long
        )

    companion object {
        fun blank(): Address {
            return Address(AddressId(-1), -1, "Unknown", "Unknown", -1, "Unknown", .0, .0)
        }
    }
}