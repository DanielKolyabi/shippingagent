package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.TaskItemEntranceResponse
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.EntranceState
import java.lang.RuntimeException

object EntranceMapper {
    fun fromRaw(raw: TaskItemEntranceResponse): Entrance = Entrance(
        number = EntranceNumber(raw.number),
        euroKey = raw.euroKey,
        key = raw.key,
        code = raw.code ?: "",
        startApartments = raw.startApartments,
        endApartments = raw.endApartments,
        floors = raw.floors,
        mailboxType = raw.mailboxType,
        state = when(raw.state){
            0 -> EntranceState.CREATED
            1 -> EntranceState.CLOSED
            else -> throw RuntimeException("Unknown entrance state ${raw.state}")
        },
        hasLookout = raw.hasLookout
    )
}