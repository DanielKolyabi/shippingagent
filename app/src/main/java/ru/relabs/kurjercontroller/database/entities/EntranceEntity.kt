package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.models.EntranceModel

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "entrances")
data class EntranceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val number: Int,
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int,
    @ColumnInfo(name = "start_appartaments")
    val startAppartaments: Int,
    @ColumnInfo(name = "end_appartaments")
    val endAppartaments: Int,
    @ColumnInfo(name = "available_keys")
    val availableKeys: List<String>,
    @ColumnInfo(name = "available_euro_keys")
    val availableEuroKeys: List<String>,
    val code: String,
    val floors: Int,
    @ColumnInfo(name = "mailbox_type")
    val mailboxType: Int,
    val state: Int
) {
    fun toModel(): EntranceModel {
        return EntranceModel(
            number = number,
            availableEuroKeys = availableEuroKeys,
            availableKeys = availableKeys,
            state = state,
            code = code,
            endAppartaments = endAppartaments,
            floors = floors,
            mailboxType = mailboxType,
            startAppartaments = startAppartaments
        )
    }
}