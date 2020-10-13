package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.TaskStorageEntity
import ru.relabs.kurjercontroller.domain.models.StorageId
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskStorage

object DatabaseStorageMapper {
    fun fromEntity(entity: TaskStorageEntity) = TaskStorage(
        id = StorageId(entity.id),
        address = entity.address,
        lat = entity.gpsLat,
        long = entity.gpsLong
    )

    fun toEntity(model: TaskStorage, taskId: TaskId) = TaskStorageEntity(
        id = 0,
        taskId = taskId.id,
        storageId = model.id.id,
        address = model.address,
        gpsLat = model.lat,
        gpsLong = model.long
    )

}
