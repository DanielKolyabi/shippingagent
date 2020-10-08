package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.EntranceDataEntity
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.TaskItemEntrance
import ru.relabs.kurjercontroller.domain.models.TaskItemId

object DatabaseEntranceDataMapper {
    fun fromEntity(entranceDataEntity: EntranceDataEntity): TaskItemEntrance = TaskItemEntrance(
        number = EntranceNumber(entranceDataEntity.number),
        apartmentsCount = entranceDataEntity.apartmentsCount,
        isEuroBoxes = entranceDataEntity.isEuroBoxes,
        hasLookout = entranceDataEntity.hasLookout,
        isStacked = entranceDataEntity.isStacked,
        isRefused = entranceDataEntity.isRefused,
        photoRequired = entranceDataEntity.photoRequired
    )

    fun toEntity(it: TaskItemEntrance, id: TaskItemId) = EntranceDataEntity(
        id = 0,
        taskItemId = id.id,
        number = it.number.number,
        apartmentsCount = it.apartmentsCount,
        isEuroBoxes = it.isEuroBoxes,
        hasLookout = it.hasLookout,
        isStacked = it.isStacked,
        isRefused = it.isRefused,
        photoRequired = it.photoRequired
    )
}
