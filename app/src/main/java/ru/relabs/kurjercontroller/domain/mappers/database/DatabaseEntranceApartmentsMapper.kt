package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.ApartmentResultEntity
import ru.relabs.kurjercontroller.domain.models.*

object DatabaseEntranceApartmentsMapper {
    fun fromEntity(entity: ApartmentResultEntity) = ApartmentResult(
        id = ApartmentResultId(entity.id),
        taskId = TaskId(entity.taskId),
        taskItemId = TaskItemId(entity.taskItemId),
        entranceNumber = EntranceNumber(entity.entranceNumber),
        apartmentNumber = ApartmentNumber(entity.apartmentNumber),
        buttonGroup = entity.buttonGroup,
        buttonState = entity.buttonState,
        description = entity.description
    )

}
