package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.ApartmentResultEntity
import ru.relabs.kurjercontroller.domain.mappers.MappingException
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.report.ReportApartmentButtonsMode

object DatabaseEntranceApartmentsMapper {
    fun fromEntity(entity: ApartmentResultEntity) = ApartmentResult(
        id = ApartmentResultId(entity.id),
        taskId = TaskId(entity.taskId),
        taskItemId = TaskItemId(entity.taskItemId),
        entranceNumber = EntranceNumber(entity.entranceNumber),
        apartmentNumber = ApartmentNumber(entity.apartmentNumber),
        buttonGroup = when(entity.buttonGroup){
            0 -> ReportApartmentButtonsMode.Main
            1 -> ReportApartmentButtonsMode.Additional
            else -> throw MappingException("buttonGroup", entity.buttonGroup)
        },
        buttonState = entity.buttonState,
        description = entity.description
    )

}
