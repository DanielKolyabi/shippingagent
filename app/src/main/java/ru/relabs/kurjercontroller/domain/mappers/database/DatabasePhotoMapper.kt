package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.TaskItemPhotoEntity
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.PhotoId
import ru.relabs.kurjercontroller.domain.models.TaskItemId
import ru.relabs.kurjercontroller.domain.models.TaskItemPhoto

object DatabasePhotoMapper {
    fun fromEntity(entity: TaskItemPhotoEntity): TaskItemPhoto = TaskItemPhoto(
        id = PhotoId(entity.id),
        UUID = entity.UUID,
        taskItemId = TaskItemId(entity.taskItemId),
        entranceNumber = EntranceNumber(entity.entranceNumber)
    )
}
