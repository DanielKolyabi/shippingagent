package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "apartment_results",
    indices = [Index(value = ["task_id", "task_item_id", "apartment_number", "entrance_number"], unique = true)]
)
data class ApartmentResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int,
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int,
    @ColumnInfo(name = "entrance_number")
    val entranceNumber: Int,
    @ColumnInfo(name = "apartment_number")
    val apartmentNumber: Int,
    @ColumnInfo(name = "button_group")
    val buttonGroup: Int,
    @ColumnInfo(name = "button_state")
    val buttonState: Int,
    val description: String
)