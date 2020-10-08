package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.mappers.database.DatabaseTaskItemMapper
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.providers.TaskRepository

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
    val notes: List<String>,
    @ColumnInfo(name = "close_time")
    val closeTime: DateTime?,
    @ColumnInfo(name = "deliveryman_id")
    val deliverymanId: Int,
    @ColumnInfo(name = "is_new")
    val isNew: Boolean,
    @ColumnInfo(name = "wrong_method")
    val wrongMethod: Boolean,
    @ColumnInfo(name = "button_name")
    val buttonName: String,
    @ColumnInfo(name = "required_apartments")
    val requiredApartments: String,
    @ColumnInfo(name = "order_id")
    val publisherId: Int
)