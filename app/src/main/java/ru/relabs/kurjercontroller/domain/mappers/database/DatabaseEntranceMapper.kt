package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.EntranceEntity
import ru.relabs.kurjercontroller.domain.models.*

object DatabaseEntranceMapper {
    fun toEntity(model: Entrance, taskId: TaskId, taskItemId: TaskItemId) = EntranceEntity(
        id = 0,
        taskItemId = taskItemId.id,
        state = model.state.toInt(),
        euroKey = model.euroKey,
        key = model.key,
        code = model.code,
        endApartments = model.endApartments,
        floors = model.floors,
        mailboxType = model.mailboxType,
        startApartments = model.startApartments,
        number = model.number.number,
        taskId = taskId.id,
        hasLookout = model.hasLookout,
        isStacked = model.isStacked
    )

    fun fromEntity(entity: EntranceEntity) = Entrance(
        number = EntranceNumber(entity.number),
        euroKey = entity.euroKey,
        key = entity.key,
        code = entity.code,
        startApartments = entity.startApartments,
        endApartments = entity.endApartments,
        floors = entity.floors,
        mailboxType = entity.mailboxType,
        state = entity.state.toEntranceState(),
        hasLookout = entity.hasLookout,
        isStacked = entity.isStacked
    )
}
