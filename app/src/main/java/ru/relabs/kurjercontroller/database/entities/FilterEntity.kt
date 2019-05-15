package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "filters",
    indices = [Index(value = ["task_id", "filter_id", "filter_type"], unique = true)]
)
data class FilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int,
    @ColumnInfo(name = "filter_type")
    val type: Int,

    @ColumnInfo(name = "filter_id")
    val filterId: Int,
    val name: String,
    val fixed: Boolean,
    val active: Boolean
) {
    companion object {
        const val PUBLISHER_FILTER = 0
        const val BRIGADE_FILTER = 1
        const val USER_FILTER = 2
        const val DISTRICT_FILTER = 3
        const val REGION_FILTER = 4
    }
}