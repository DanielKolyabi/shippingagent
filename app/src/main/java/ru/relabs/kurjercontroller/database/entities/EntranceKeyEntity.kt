package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.relabs.kurjercontroller.models.AddressModel

/**
 * Created by ProOrange on 31.08.2018.
 */
@Entity(tableName = "entrance_keys")
data class EntranceKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var key: String
)