package ru.relabs.kurjercontroller.domain.mappers.network

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.models.tasks.TaskItemResponse
import ru.relabs.kurjercontroller.domain.models.PublisherId
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.models.TaskItemId
import ru.relabs.kurjercontroller.presentation.report.toApartmentButtonsMode

object TaskItemMapper {
    fun fromRaw(raw: TaskItemResponse): TaskItem = TaskItem(
        id = TaskItemId(raw.id),
        taskId = TaskId(raw.taskId),
        publisherName = raw.publisherName,
        defaultReportType = (raw.defaultReportType + 1).toApartmentButtonsMode(),
        required = raw.required,
        address = AddressMapper.fromRaw(raw.address),
        entrances = raw.entrances.map { EntranceMapper.fromRaw(it) },
        notes = raw.notes,
        closeTime = raw.closeTime?.let { DateTime(it) },
        deliverymanId = raw.deliverymanId,
        isNew = false,
        wrongMethod = raw.wrongMethod,
        buttonName = raw.buttonName,
        requiredApartments = raw.requiredApartments,
        publisherId = PublisherId(raw.orderId)
    )
}