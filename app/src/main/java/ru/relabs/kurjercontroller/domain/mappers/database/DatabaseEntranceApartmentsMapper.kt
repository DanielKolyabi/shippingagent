package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.ApartmentResultEntity
import ru.relabs.kurjercontroller.domain.mappers.MappingException
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.report.ReportApartmentButtonsMode
import ru.relabs.kurjercontroller.presentation.report.toApartmentButtonsMode
import ru.relabs.kurjercontroller.presentation.report.toInt

object DatabaseEntranceApartmentsMapper {
    fun fromEntity(entity: ApartmentResultEntity) = ApartmentResult(
        id = ApartmentResultId(entity.id),
        taskId = TaskId(entity.taskId),
        taskItemId = TaskItemId(entity.taskItemId),
        entranceNumber = EntranceNumber(entity.entranceNumber),
        apartmentNumber = ApartmentNumber(entity.apartmentNumber),
        buttonGroup = entity.buttonGroup.toApartmentButtonsMode(),
        buttonState = entity.buttonState,
        description = entity.description
    )

    fun toEntity(apartment: ApartmentResult) = ApartmentResultEntity(
        id = apartment.id.id,
        taskId = apartment.taskId.id,
        taskItemId = apartment.taskItemId.id,
        entranceNumber = apartment.entranceNumber.number,
        apartmentNumber = apartment.apartmentNumber.number,
        buttonGroup = apartment.buttonGroup.toInt(),
        buttonState = apartment.buttonState,
        description = apartment.description
    )
}
