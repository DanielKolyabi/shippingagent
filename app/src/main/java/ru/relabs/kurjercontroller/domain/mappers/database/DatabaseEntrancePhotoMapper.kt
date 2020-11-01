package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.EntrancePhotoEntity
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.EntrancePhoto
import ru.relabs.kurjercontroller.domain.models.PhotoId
import ru.relabs.kurjercontroller.domain.models.TaskItemId

object DatabaseEntrancePhotoMapper {
    fun fromEntity(photo: EntrancePhotoEntity) = EntrancePhoto(
        id = PhotoId(photo.id),
        UUID = photo.UUID,
        taskItemId = TaskItemId(photo.taskItemId),
        entranceNumber = EntranceNumber(photo.entranceNumber)
    )
}