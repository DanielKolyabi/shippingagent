package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.auth.AddressResponse
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId

object AddressMapper {
    fun fromRaw(raw: AddressResponse) = Address(
        id = AddressId(raw.id),
        city = raw.city,
        street = raw.street,
        house = raw.house,
        houseName = raw.houseName,
        lat = raw.lat,
        long = raw.long
    )
}