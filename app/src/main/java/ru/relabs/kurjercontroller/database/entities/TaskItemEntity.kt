package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.TaskItemModel

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "task_items",
    indices = [Index(value = ["task_item_id", "task_id"], unique = true)]
)
data class TaskItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int, //idct
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int, //iddot
    @ColumnInfo(name = "publisher_name")
    val publisherName: String,
    @ColumnInfo(name = "default_report_type")
    val defaultReportType: Int,
    @ColumnInfo(name = "required")
    val required: Boolean,
    @ColumnInfo(name = "address_id")
    val addressId: Int,
    val notes: List<String>
) {
    suspend fun toModel(db: AppDatabase): TaskItemModel = withContext(Dispatchers.IO) {
        return@withContext TaskItemModel(
            id = taskItemId,
            defaultReportType = defaultReportType,
            notes = notes,
            required = required,
            taskId = taskId,
            publisherName = publisherName,
            address = db.addressDao().getById(addressId)?.toModel() ?: AddressModel.blank(),
            entrances = db.entranceDao().getByTaskItemId(taskId, taskItemId).map { it.toModel() }.toMutableList()
        )
    }
}