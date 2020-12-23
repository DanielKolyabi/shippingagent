package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.domain.mappers.database.DatabaseStorageMapper
import ru.relabs.kurjercontroller.domain.models.TaskStorage

/**
 * Created by ProOrange on 11.04.2019.
 */

@Entity(
    tableName = "task_storages",
    indices = [Index(value = ["storage_id", "task_id"], unique = true)]
)
data class TaskStorageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int,
    @ColumnInfo(name = "storage_id")
    val storageId: Int,
    val address: String,
    val gpsLat: Float,
    val gpsLong: Float
)