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

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    val initiator: String,
    @ColumnInfo(name = "start_control_date")
    val startControlDate: DateTime,
    @ColumnInfo(name = "end_control_date")
    val endControlDate: DateTime,
    val storages: List<String>,
    //TODO: Filters
    val description: String,
    val state: Int,
    val iteration: Int
) {
    suspend fun toModel(db: AppDatabase): TaskModel = withContext(Dispatchers.IO) {
        return@withContext TaskModel(
            id = id,
            userId = userId,
            initiator = initiator,
            description = description,
            endControlDate = endControlDate,
            startControlDate = startControlDate,
            state = state,
            storages = storages,
            taskFilters = TaskFiltersModel.blank(), //TODO: Filters
            publishers = db.taskPublisherDao().getByTaskId(id).map { it.toModel() },
            taskItems = db.taskItemDao().getByTaskId(id).map { it.toModel(db) },
            iteration = iteration
        )
    }
}