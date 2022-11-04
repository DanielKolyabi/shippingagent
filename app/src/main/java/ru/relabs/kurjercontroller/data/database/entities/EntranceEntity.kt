package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.domain.models.Entrance

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "entrances",
    indices = [Index(value = ["number", "task_item_id", "task_id"], unique = true)]
)
data class EntranceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val number: Int,
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int,
    @ColumnInfo(name = "start_apartments")
    val startApartments: Int,
    @ColumnInfo(name = "end_apartments")
    val endApartments: Int,
    @ColumnInfo(name = "available_keys")
    val key: String,
    @ColumnInfo(name = "available_euro_keys")
    val euroKey: String,
    val code: String,
    val floors: Int,
    @ColumnInfo(name = "mailbox_type")
    val mailboxType: Int,
    var state: Int,
    @ColumnInfo(name = "has_lookout")
    val hasLookout: Boolean,
    @ColumnInfo(name = "is_stacked")
    val isStacked: Boolean,
) {
    companion object {
        const val STATE_CLOSED = 1
        const val STATE_CREATED = 0
    }
}