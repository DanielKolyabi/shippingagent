package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.mappers.database.DatabaseTaskMapper
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.providers.TaskRepository

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "tasks"
)
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
    val description: String,
    val state: Int,
    val iteration: Int,
    @ColumnInfo(name = "first_examined_device_id")
    val firstExaminedDeviceId: String?,
    val filtered: Boolean,
    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,
    @ColumnInfo(name = "with_planned")
    val withPlanned: Boolean = false,
    @ColumnInfo(name = "by_other_user")
    val byOtherUser: Boolean = false //TODO: Need migration
)