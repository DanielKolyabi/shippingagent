package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceResultEntityDao {

    @get:Query("SELECT * FROM entrance_results")
    val all: List<EntranceResultEntity>

    @Query("SELECT * FROM entrance_results WHERE task_id = :taskId AND task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun getByEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int): EntranceResultEntity?

    @Update
    fun update(address: EntranceResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntranceResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntranceResultEntity>)

    @Delete
    fun delete(address: EntranceResultEntity)

    @Query("DELETE FROM entrance_results WHERE task_id = :taskId AND task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun deleteByEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int)
}