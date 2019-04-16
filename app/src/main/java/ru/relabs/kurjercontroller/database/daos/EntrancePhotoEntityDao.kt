package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.EntranceEntity
import ru.relabs.kurjercontroller.database.entities.EntrancePhotoEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntrancePhotoEntityDao {

    @get:Query("SELECT * FROM entrance_photos")
    val all: List<EntrancePhotoEntity>

    @Query("SELECT * FROM entrance_photos WHERE id = :id")
    fun getById(id: Int): EntrancePhotoEntity?

    @Update
    fun update(address: EntrancePhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntrancePhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntrancePhotoEntity>)

    @Delete
    fun delete(address: EntrancePhotoEntity)

    @Query("DELETE FROM entrance_photos WHERE id = :id")
    fun deleteById(id: Int)
}