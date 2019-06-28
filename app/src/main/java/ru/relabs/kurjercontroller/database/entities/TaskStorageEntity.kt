package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.models.StorageModel

/**
 * Created by ProOrange on 11.04.2019.
 */

@Entity(
    tableName = "task_storages",
    indices = [Index(value = ["storage_id"], unique = true)]
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
) {
    fun toModel() = StorageModel(
        id = storageId,
        address = address,
        lat = gpsLat,
        long = gpsLong
    )
}