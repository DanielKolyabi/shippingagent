package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.providers.TaskRepository

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "entrance_photos")
data class EntrancePhotoEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "uuid")
    var UUID: String,
    var gps: GPSCoordinatesModel,
    @ColumnInfo(name = "task_id")
    var taskId: Int, //iddot
    @ColumnInfo(name = "task_item_id")
    var taskItemId: Int, //iddot
    var idnd: Int,
    @ColumnInfo(name = "entrance_number")
    var entranceNumber: Int,
    @ColumnInfo(name = "real_path")
    var realPath: String?
) {
    suspend fun toModel(repository: TaskRepository): EntrancePhotoModel = withContext(Dispatchers.IO) {
        val taskItem = repository.db.taskItemDao().getByTaskItemId(taskId, taskItemId)?.toModel(repository)
            ?: throw Exception("TaskItem ${taskItemId} not found")
        val entrance = repository.db.entranceDao().getByNumber(taskId, taskItemId, entranceNumber)?.toModel()
            ?: throw Exception("Entrance ${taskItemId} ${entranceNumber} not found")

        EntrancePhotoModel(
            id = id,
            entranceModel = entrance,
            gps = gps,
            uuid = UUID,
            taskItem = taskItem,
            realPath = realPath
        )
    }
}