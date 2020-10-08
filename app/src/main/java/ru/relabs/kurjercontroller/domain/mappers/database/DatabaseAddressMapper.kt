package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.AddressEntity
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId

object DatabaseAddressMapper {
    fun fromEntity(addressEntity: AddressEntity): Address = Address(
        id = AddressId(addressEntity.id),
        city = addressEntity.city,
        street = addressEntity.street,
        house = addressEntity.house,
        houseName = addressEntity.houseName,
        lat = addressEntity.gpsLat.toFloat(),
        long = addressEntity.gpsLong.toFloat()
    )

    fun toEntity(address: Address): AddressEntity = AddressEntity(
        id = address.id.id,
        city = address.city,
        street = address.street,
        house = address.house,
        houseName = address.houseName,
        gpsLat = address.lat.toDouble(),
        gpsLong = address.long.toDouble()
    )
}
