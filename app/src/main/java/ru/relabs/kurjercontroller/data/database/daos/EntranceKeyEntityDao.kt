package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.EntranceKeyEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceKeyEntityDao {

    @get:Query("SELECT * FROM entrance_keys")
    val all: List<EntranceKeyEntity>

    @Query("DELETE FROM entrance_keys")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(keys: List<EntranceKeyEntity>)
}