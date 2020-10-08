package ru.relabs.kurjercontroller.domain.mappers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.TaskItemResultEntity
import ru.relabs.kurjercontroller.domain.models.TaskItemId
import ru.relabs.kurjercontroller.domain.models.TaskItemResult
import ru.relabs.kurjercontroller.domain.models.TaskItemResultId

object TaskItemResultMapper {
    suspend fun fromEntity(db: AppDatabase, entity: TaskItemResultEntity): TaskItemResult = withContext(Dispatchers.IO) {
        TaskItemResult(
            id = TaskItemResultId(entity.id),
            taskItemId = TaskItemId(entity.taskItemId),
            closeTime = entity.closeTime,
            description = entity.description,
            entrances = db.entrancesDao().getByTaskItemResultId(entity.id).map {
                TaskItemEntranceResultMapper.fromEntity(it)
            },
            gps = entity.gps
        )
    }

    fun fromModel(updatedReport: TaskItemResult): TaskItemResultEntity = TaskItemResultEntity(
        id = updatedReport.id.id,
        taskItemId = updatedReport.taskItemId.id,
        gps = updatedReport.gps,
        closeTime = updatedReport.closeTime,
        description = updatedReport.description
    )
}
