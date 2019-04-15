package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.models.AddressModel

data class AddressResponseModel(
    val city: String,
    val house: Int,
    @SerializedName("house_name")
    val houseName: String,
    val street: String,
    val id: Int,
    val idnd: Int,
    val lat: Double,
    val long: Double
) {
    fun toModel(): AddressModel {
        return AddressModel(
            city = city,
            house = house,
            houseName = houseName,
            id = id,
            idnd = idnd,
            lat = lat,
            long = long,
            street = street
        )
    }
}
