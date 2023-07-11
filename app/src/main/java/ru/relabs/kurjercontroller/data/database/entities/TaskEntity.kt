package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

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
    val byOtherUser: Boolean = false
)
