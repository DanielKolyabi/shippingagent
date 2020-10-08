package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.StorageResponse
import ru.relabs.kurjercontroller.domain.models.TaskStorage
import ru.relabs.kurjercontroller.domain.models.StorageId

object StorageMapper {
    fun fromRaw(raw: StorageResponse) = TaskStorage(
        id = StorageId(raw.id),
        address = raw.address,
        lat = raw.lat,
        long = raw.long
    )
}