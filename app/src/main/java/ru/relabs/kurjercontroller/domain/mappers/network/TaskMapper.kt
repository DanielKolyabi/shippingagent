package ru.relabs.kurjercontroller.domain.mappers.network

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.models.tasks.TaskResponse
import ru.relabs.kurjercontroller.domain.models.DeviceId
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskState

object TaskMapper {
    fun fromRaw(raw: TaskResponse, deviceId: DeviceId): Task = Task(
        id = TaskId(raw.id),
        userId = raw.userId,
        initiator = raw.initiator,
        startControlDate = DateTime(raw.startControlDate),
        endControlDate = DateTime(raw.endControlDate),
        description = raw.description,
        storages = raw.storages.map { StorageMapper.fromRaw(it) },
        publishers = raw.publishers.map { PublisherMapper.fromRaw(it) },
        taskItems = raw.items.map { TaskItemMapper.fromRaw(it) },
        taskFilters = FilterMapper.fromRaw(raw.filters),
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
        iteration = raw.iteration,
        firstExaminedDeviceId = raw.firstExaminedDeviceId,
        filtered = raw.filtered,
        isOnline = false,
        withPlanned = false
    )
}