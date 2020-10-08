package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 31.08.2018.
 */
@Entity(tableName = "entrance_keys")
data class EntranceKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var key: String
)