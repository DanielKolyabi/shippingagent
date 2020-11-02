package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.TaskItem

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "entrance_results",
    indices = [Index(value = ["task_id", "task_item_id", "entrance_number"], unique = true)]
)
data class EntranceResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int, //id_ct
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int, //iddot
    @ColumnInfo(name = "entrance_number")
    val entranceNumber: Int?,
    @ColumnInfo(name = "has_lookup")
    val hasLookupPost: Boolean?,
    @ColumnInfo(name = "delivery_wrong")
    val isDeliveryWrong: Boolean?,
    val description: String?,
    val code: String?,
    @ColumnInfo(name = "apartment_from")
    var apartmentFrom: Int?,
    @ColumnInfo(name = "apartment_to")
    var apartmentTo: Int?,
    val floors: Int?,
    val key: String?,
    @ColumnInfo(name = "euro_key")
    val euroKey: String?,
    @ColumnInfo(name = "mailbox_type")
    val mailboxType: Int?,
    @ColumnInfo(name = "entrance_closed")
    val entranceClosed: Boolean?
) {
    companion object {
        fun fromEntrance(taskItem: TaskItem, entrance: Entrance) = EntranceResultEntity(
            id = 0,
            taskId = taskItem.taskId.id,
            taskItemId = taskItem.id.id,
            entranceNumber = entrance.number.number,
            hasLookupPost = entrance.hasLookout,
            isDeliveryWrong = false,
            description = "",
            code = entrance.code,
            apartmentFrom = entrance.startApartments,
            apartmentTo = entrance.endApartments,
            floors = entrance.floors,
            key = entrance.key,
            euroKey = entrance.euroKey,
            mailboxType = entrance.mailboxType,
            entranceClosed = false
        )
    }
}