package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.AddressEntity
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId

object DatabaseAddressMapper {
    fun fromEntity(entity: AddressEntity): Address = Address(
        id = AddressId(entity.id),
        idnd = entity.idnd,
        city = entity.city,
        street = entity.street,
        house = entity.house,
        houseName = entity.houseName,
        lat = entity.gpsLat,
        long = entity.gpsLong
    )

    fun toEntity(model: Address): AddressEntity = AddressEntity(
        id = model.id.id,
        idnd = model.idnd,
        city = model.city,
        street = model.street,
        house = model.house,
        houseName = model.houseName,
        gpsLat = model.lat,
        gpsLong = model.long
    )
}
