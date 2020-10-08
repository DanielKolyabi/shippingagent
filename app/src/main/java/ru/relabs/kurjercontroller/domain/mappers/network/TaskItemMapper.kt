package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.TaskItemResponse
import ru.relabs.kurjercontroller.domain.models.PublisherId
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.models.TaskItemId

object TaskItemMapper {
    fun fromRaw(raw: TaskItemResponse): TaskItem = TaskItem(
        id = TaskItemId(raw.id),
        taskId = TaskId(raw.taskId),
        publisherName = raw.publisherName,
        defaultReportType = raw.defaultReportType,
        required = raw.required,
        address = AddressMapper.fromRaw(raw.address),
        entrances = raw.entrances.map { EntranceMapper.fromRaw(it) },
        notes = raw.notes,
        closeTime = raw.closeTime,
        deliverymanId = raw.deliverymanId,
        isNew = false,
        wrongMethod = raw.wrongMethod,
        buttonName = raw.buttonName,
        requiredApartments = raw.requiredApartments,
        publisherId = PublisherId(raw.orderId)
    )
}