package ru.relabs.kurjercontroller.domain.mappers.database

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.TaskEntity
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.toTaskState

object DatabaseTaskMapper {

    fun fromEntity(taskEntity: TaskEntity, db: AppDatabase): Task = Task(
        id = TaskId(taskEntity.id),
        userId = taskEntity.userId,
        initiator = taskEntity.initiator,
        startControlDate = taskEntity.startControlDate.toDate(),
        endControlDate = taskEntity.endControlDate.toDate(),
        description = taskEntity.description,
        storages = db.taskStorageDao().getByTaskId(taskEntity.id).map{
            DatabaseStorageMapper.fromEntity(it)
        },
        publishers = db.taskPublisherDao().getByTaskId(taskEntity.id).map{
            DatabasePublisherMapper.fromEntity(it)
        },
        taskItems = db.taskItemDao().getByTaskId(taskEntity.id).map{
            DatabaseTaskItemMapper.fromEntity(it, db)
        },
        taskFilters = DatabaseFilterMapper.fromEntities(db.filtersDao().getByTaskId(taskEntity.id)),
        state = Task.State(
            taskEntity.state.toTaskState(),
            taskEntity.byOtherUser
        ),
        iteration = taskEntity.iteration,
        firstExaminedDeviceId = taskEntity.firstExaminedDeviceId,
        filtered = taskEntity.filtered,
        isOnline = taskEntity.isOnline,
        withPlanned = taskEntity.withPlanned
    )


    fun toEntity(task: Task): TaskEntity = TaskEntity(
        id = task.id.id,
        userId = task.userId,
        initiator = task.initiator,
        startControlDate = DateTime(task.startControlDate),
        endControlDate = DateTime(task.endControlDate),
        description = task.description,
        state = task.state.state.toInt(),
        iteration = task.iteration,
        firstExaminedDeviceId = task.firstExaminedDeviceId,
        filtered = task.filtered,
        isOnline = task.isOnline,
        withPlanned = task.withPlanned,
        byOtherUser = task.state.byOtherUser
    )
}