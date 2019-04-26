package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.EntranceEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceEntityDao {

    @get:Query("SELECT * FROM entrances")
    val all: List<EntranceEntity>

    @Query("SELECT * FROM entrances WHERE id = :id")
    fun getById(id: Int): EntranceEntity?

    @Query("SELECT * FROM entrances WHERE task_id = :taskId AND task_item_id = :taskItemId")
    fun getByTaskItemId(taskId: Int, taskItemId: Int): List<EntranceEntity>

    @Query("SELECT * FROM entrances WHERE task_id = :taskId AND task_item_id = :taskItemId AND number = :number")
    fun getByNumber(taskId: Int, taskItemId: Int, number: Int): EntranceEntity?

    @Update
    fun update(address: EntranceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntranceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntranceEntity>)

    @Delete
    fun delete(address: EntranceEntity)

    @Query("DELETE FROM entrances WHERE id = :id")
    fun deleteById(id: Int)
}