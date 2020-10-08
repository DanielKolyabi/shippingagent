package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.TaskPublisherResponse
import ru.relabs.kurjercontroller.domain.models.TaskPublisher
import ru.relabs.kurjercontroller.domain.models.PublisherId
import ru.relabs.kurjercontroller.domain.models.TaskId

object PublisherMapper {
    fun fromRaw(raw: TaskPublisherResponse) = TaskPublisher(
        id = PublisherId(raw.id),
        name = raw.name,
        taskId = TaskId(raw.taskId),
        startDistributionDate = raw.startDistributionDate,
        endDistributionDate = raw.endDistributionDate
    )
}