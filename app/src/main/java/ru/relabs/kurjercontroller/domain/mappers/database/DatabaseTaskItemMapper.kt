package ru.relabs.kurjercontroller.domain.mappers.database

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.TaskItemEntity
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.report.toApartmentButtonsMode
import ru.relabs.kurjercontroller.presentation.report.toInt

object DatabaseTaskItemMapper {
    fun fromEntity(entity: TaskItemEntity, db: AppDatabase): TaskItem = TaskItem(
        id = TaskItemId(entity.taskItemId),
        defaultReportType = entity.defaultReportType.toApartmentButtonsMode(),
        notes = entity.notes,
        required = entity.required,
        taskId = TaskId(entity.taskId),
        publisherName = entity.publisherName,
        address = db.addressDao().getById(entity.addressId)?.let { DatabaseAddressMapper.fromEntity(it) }
            ?: Address.blank(),
        entrances = db.entranceDao().getByTaskItemId(entity.taskId, entity.taskItemId)
            .map { DatabaseEntranceMapper.fromEntity(it) },
        closeTime = entity.closeTime,
        deliverymanId = entity.deliverymanId,
        isNew = entity.isNew,
        wrongMethod = entity.wrongMethod,
        buttonName = entity.buttonName,
        requiredApartments = entity.requiredApartments,
        publisherId = PublisherId(entity.publisherId),
        entrancesMonitoringMode = EntrancesMonitoringMode.values()[entity.entrancesMonitoringMode],
        closeRadius = entity.closeRadius
    )

    fun toEntity(model: TaskItem): TaskItemEntity = TaskItemEntity(
        id = 0,
        taskId = model.taskId.id,
        taskItemId = model.id.id,
        publisherName = model.publisherName,
        defaultReportType = model.defaultReportType.toInt(),
        required = model.required,
        addressId = model.address.id.id,
        notes = model.notes,
        closeTime = model.closeTime,
        deliverymanId = model.deliverymanId,
        isNew = model.isNew,
        wrongMethod = model.wrongMethod,
        buttonName = model.buttonName,
        requiredApartments = model.requiredApartments,
        publisherId = model.publisherId.id,
        entrancesMonitoringMode = model.entrancesMonitoringMode.ordinal,
        closeRadius = model.closeRadius
    )
}
