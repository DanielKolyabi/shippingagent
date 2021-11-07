package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.relabs.kurjercontroller.data.database.entities.ClosedAddressEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface ClosedAddressesDao {

    @Query("SELECT * FROM closed_addresses WHERE address_id = :idnd AND entrance_number = :entranceNumber AND user_id = :userLogin")
    fun findEntrance(idnd: Int, entranceNumber: Int, userLogin: String): ClosedAddressEntity?

    @Query("SELECT COUNT(*) FROM closed_addresses WHERE user_id = :userLogin")
    fun getClosedCount(userLogin: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: ClosedAddressEntity)

    @Query("DELETE FROM closed_addresses WHERE user_id = :userLogin")
    fun cleanForUser(userLogin: String)
}