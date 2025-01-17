package ru.relabs.kurjercontroller.domain.mappers.network

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.models.tasks.TaskPublisherResponse
import ru.relabs.kurjercontroller.domain.models.TaskPublisher
import ru.relabs.kurjercontroller.domain.models.PublisherId
import ru.relabs.kurjercontroller.domain.models.TaskId

object PublisherMapper {
    fun fromRaw(raw: TaskPublisherResponse) = TaskPublisher(
        id = PublisherId(raw.id),
        name = raw.name,
        taskId = TaskId(raw.taskId),
        startDistributionDate = DateTime(raw.startDistributionDate),
        endDistributionDate = DateTime(raw.endDistributionDate)
    )
}