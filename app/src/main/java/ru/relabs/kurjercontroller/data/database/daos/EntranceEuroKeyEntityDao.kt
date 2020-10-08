package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.EntranceEuroKeyEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceEuroKeyEntityDao {

    @get:Query("SELECT * FROM entrance_euro_keys")
    val all: List<EntranceEuroKeyEntity>

    @Query("DELETE FROM entrance_euro_keys")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(keys: List<EntranceEuroKeyEntity>)
}