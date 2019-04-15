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

    @Query("SELECT * FROM entrances WHERE task_item_id = :taskItemId")
    fun getByTaskItemId(taskItemId: Int): List<EntranceEntity>

    @Update
    fun update(address: EntranceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntranceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntranceEntity>)

    @Delete
    fun delete(address: EntranceEntity)
}