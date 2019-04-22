package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(
    tableName = "entrance_results",
    indices = [Index(value = ["task_item_id", "entrance_number"], unique = true)]
)
data class EntranceResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
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
    val apartmentFrom: Int?,
    @ColumnInfo(name = "apartment_to")
    val apartmentTo: Int?,
    val floors: Int?,
    val key: String?,
    @ColumnInfo(name = "euro_key")
    val euroKey: String?,
    @ColumnInfo(name = "mailbox_type")
    val mailboxType: Int?
)