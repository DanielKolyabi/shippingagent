package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.EntrancePhotoEntity
import ru.relabs.kurjercontroller.domain.models.*

object DatabaseEntrancePhotoMapper {
    fun fromEntity(photo: EntrancePhotoEntity) = EntrancePhoto(
        id = PhotoId(photo.id),
        UUID = photo.UUID,
        taskId = TaskId(photo.id),
        taskItemId = TaskItemId(photo.taskItemId),
        entranceNumber = EntranceNumber(photo.entranceNumber),
        gps = photo.gps,
        idnd = photo.idnd,
        realPath = photo.realPath,
        isEntrancePhoto = photo.isEntrancePhoto
    )

    fun toEntity(photo: EntrancePhoto) = EntrancePhotoEntity(
        id = photo.id.id,
        UUID = photo.UUID,
        taskId = photo.taskId.id,
        taskItemId = photo.taskItemId.id,
        entranceNumber = photo.entranceNumber.number,
        gps = photo.gps,
        idnd = photo.idnd,
        realPath = photo.realPath,
        isEntrancePhoto = photo.isEntrancePhoto
    )
}