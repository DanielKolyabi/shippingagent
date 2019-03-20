package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "entrance_reports")
data class EntranceReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int,
    val idnd: Int,
    val idot: Int,
    @ColumnInfo(name = "start_appartaments")
    val startAppartaments: Int,
    @ColumnInfo(name = "end_appartaments")
    val endAppartaments: Int,
    val description: String,
    val code: String,
    val key: String,
    @ColumnInfo(name = "euro_key")
    val euroKey: String,
    @ColumnInfo(name = "is_delivery_wrong")
    val isDeliveryWrong: Boolean,
    @ColumnInfo(name = "has_lookup_post")
    val hasLookupPost: Boolean
)