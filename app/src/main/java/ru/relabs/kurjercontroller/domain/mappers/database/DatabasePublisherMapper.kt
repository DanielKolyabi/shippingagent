package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.TaskPublisherEntity
import ru.relabs.kurjercontroller.domain.models.PublisherId
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskPublisher

object DatabasePublisherMapper {
    fun fromEntity(entity: TaskPublisherEntity) = TaskPublisher(
        id = PublisherId(entity.id),
        name = entity.name,
        taskId = TaskId(entity.taskId),
        startDistributionDate = entity.startDistributionDate,
        endDistributionDate = entity.endDistributionDate
    )

    fun toEntity(model: TaskPublisher) = TaskPublisherEntity(
        id = 0,
        name = model.name,
        taskId = model.taskId.id,
        startDistributionDate = model.startDistributionDate,
        endDistributionDate = model.endDistributionDate,
        publisherId = model.id.id
    )
}
