package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.EntranceResultEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceResultEntityDao {

    @get:Query("SELECT * FROM entrance_results")
    val all: List<EntranceResultEntity>

    @Query("SELECT * FROM entrance_results WHERE task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun getByEntrance(taskItemId: Int, entranceNumber: Int): EntranceResultEntity?

    @Update
    fun update(address: EntranceResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntranceResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntranceResultEntity>)

    @Delete
    fun delete(address: EntranceResultEntity)

    @Query("DELETE FROM entrance_results WHERE task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun deleteByEntrance(taskItemId: Int, entranceNumber: Int)
}