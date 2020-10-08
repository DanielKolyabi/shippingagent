package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.TaskResponse
import ru.relabs.kurjercontroller.domain.models.*

object TaskMapper {
    fun fromRaw(raw: TaskResponse, deviceId: DeviceId): Task = Task(
        id = TaskId(raw.id),
        state = Task.State(
            state = when (raw.state) {
                0, 10, 11, 20 -> TaskState.CREATED
                30 -> TaskState.EXAMINED
                40, 41, 42 -> TaskState.STARTED
                50, 51, 60, 61 -> TaskState.COMPLETED
                12 -> TaskState.CANCELED
                else -> TaskState.COMPLETED
            },
            byOtherUser = raw.firstExaminedDeviceId != deviceId.id
        ),
        name = raw.name,
        edition = raw.edition,
        copies = raw.copies,
        packs = raw.packs,
        remain = raw.remain,
        area = raw.area,
        startTime = raw.startTime,
        endTime = raw.endTime,
        brigade = raw.brigade,
        brigadier = raw.brigadier,
        rastMapUrl = raw.rastMapUrl,
        userId = raw.userId,
        city = raw.city,
        storageAddress = raw.storageAddress,
        iteration = raw.iteration,
        items = raw.items.map {
            TaskItemMapper.fromRaw(it)
        },
        coupleType = CoupleType(raw.coupleType)
    )
}