package ru.relabs.kurjercontroller.data.models.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class AddressResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("id") val idnd: Int,
    @SerializedName("city") val city: String,
    @SerializedName("street") val street: String,
    @SerializedName("house") val house: Int,
    @SerializedName("house_name") val houseName: String,
    @SerializedName("lat") val lat: Float,
    @SerializedName("long") val long: Float
)
//    fun toModel(): AddressModel {
//        return AddressModel(
//            city = city,
//            house = house,
//            houseName = houseName,
//            id = id,
//            idnd = idnd,
//            lat = lat,
//            long = long,
//            street = street
//        )
//    }