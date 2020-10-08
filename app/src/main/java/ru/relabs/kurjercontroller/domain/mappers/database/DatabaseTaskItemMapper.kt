package ru.relabs.kurjercontroller.domain.mappers.database

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.TaskItemEntity
import ru.relabs.kurjercontroller.domain.models.*

object DatabaseTaskItemMapper {
    fun fromEntity(entity: TaskItemEntity, db: AppDatabase): TaskItem = TaskItem(
        id = TaskItemId(entity.taskItemId),
        defaultReportType = entity.defaultReportType,
        notes = entity.notes,
        required = entity.required,
        taskId = TaskId(entity.taskId),
        publisherName = entity.publisherName,
        address = db.addressDao().getById(entity.addressId)?.let { DatabaseAddressMapper.fromEntity(it) }
            ?: Address.blank(),
        entrances = db.entranceDao().getByTaskItemId(entity.taskId, entity.taskItemId)
            .map { DatabaseEntranceMapper.fromEntity(it) },
        closeTime = entity.closeTime?.toDate(),
        deliverymanId = entity.deliverymanId,
        isNew = entity.isNew,
        wrongMethod = entity.wrongMethod,
        buttonName = entity.buttonName,
        requiredApartments = entity.requiredApartments,
        publisherId = PublisherId(entity.publisherId)
    )

    fun toEntity(model: TaskItem): TaskItemEntity = TaskItemEntity(
        id = 0,
        taskId = model.taskId.id,
        taskItemId = model.id.id,
        publisherName = model.publisherName,
        defaultReportType = model.defaultReportType,
        required = model.required,
        addressId = model.address.id.id,
        notes = model.notes,
        closeTime = model.closeTime?.let { DateTime(it) },
        deliverymanId = model.deliverymanId,
        isNew = model.isNew,
        wrongMethod = model.wrongMethod,
        buttonName = model.buttonName,
        requiredApartments = model.requiredApartments,
        publisherId = model.publisherId.id
    )
}
