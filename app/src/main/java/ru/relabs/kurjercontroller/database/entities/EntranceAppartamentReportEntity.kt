package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "entrance_appartament_reports")
data class EntranceAppartamentReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "entrance_report_id")
    val entranceReportId: Int,

    val regular: Boolean,
    val notRegular: Boolean,
    val notConfirmed: Boolean,
    val hasNewspaper: Boolean,
    val hasntNewspaper: Boolean,
    val broken: Boolean
)