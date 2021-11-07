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
    tableName = "closed_addresses"
)
data class ClosedAddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "address_id")
    val addressId: Int,
    @ColumnInfo(name = "entrance_number")
    val entranceNumber: Int,
    @ColumnInfo(name = "user_id")
    val userId: String
)