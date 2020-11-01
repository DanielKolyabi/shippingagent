package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.database.entities.AddressEntity
import ru.relabs.kurjercontroller.data.models.auth.AddressResponse
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId

object AddressMapper {
    fun fromRaw(raw: AddressResponse) = Address(
        id = AddressId(raw.id),
        idnd = raw.idnd,
        city = raw.city,
        street = raw.street,
        house = raw.house,
        houseName = raw.houseName,
        lat = raw.lat.toDouble(),
        long = raw.long.toDouble()
    )

    fun fromEntity(entity: AddressEntity) = Address(
        id = AddressId(entity.id),
        idnd = entity.idnd,
        city = entity.city,
        street = entity.street,
        house = entity.house,
        houseName = entity.houseName,
        lat = entity.gpsLat,
        long = entity.gpsLong
    )
}