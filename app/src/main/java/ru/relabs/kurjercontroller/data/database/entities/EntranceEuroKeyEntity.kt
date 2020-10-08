package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by ProOrange on 31.08.2018.
 */
@Entity(tableName = "entrance_euro_keys")
data class EntranceEuroKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var key: String
)