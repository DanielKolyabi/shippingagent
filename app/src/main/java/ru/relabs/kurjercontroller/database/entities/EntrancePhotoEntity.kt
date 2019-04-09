package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "entrance_photos")
data class TaskItemPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "uuid")
    var UUID: String,
    var gps: GPSCoordinatesModel,
    @ColumnInfo(name = "task_item_id")
    var taskItemId: Int, //iddot
    var idnd: Int,
    @ColumnInfo(name = "entrance_report_id")
    var entranceReportId: Int
)