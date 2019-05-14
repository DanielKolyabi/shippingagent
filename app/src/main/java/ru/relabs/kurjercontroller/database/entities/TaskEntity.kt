package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.providers.TaskRepository

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: Int, //id_ct
    @ColumnInfo(name = "user_id")
    val userId: Int,
    val initiator: String,
    @ColumnInfo(name = "start_control_date")
    val startControlDate: DateTime,
    @ColumnInfo(name = "end_control_date")
    val endControlDate: DateTime,
    val storages: List<String>,
    val description: String,
    val state: Int,
    val iteration: Int,
    @ColumnInfo(name = "first_examined_device_id")
    val firstExaminedDeviceId: String?,
    val filtered: Boolean
) {
    suspend fun toModel(repository: TaskRepository): TaskModel = withContext(Dispatchers.IO) {
        return@withContext TaskModel(
            id = id,
            userId = userId,
            initiator = initiator,
            description = description,
            endControlDate = endControlDate,
            startControlDate = startControlDate,
            state = state,
            storages = storages,
            taskFilters = repository.loadTaskFilters(id),
            publishers = repository.db.taskPublisherDao().getByTaskId(id).map { it.toModel() },
            taskItems = repository.db.taskItemDao().getByTaskId(id).map { it.toModel(repository) },
            iteration = iteration,
            firstExaminedDeviceId = firstExaminedDeviceId,
            filtered = filtered
        )
    }
}