package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.TaskEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface TaskEntityDao {

    @get:Query("SELECT * FROM tasks")
    val all: List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getById(id: Int): TaskEntity?

    @Update
    fun update(address: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<TaskEntity>)

    @Delete
    fun delete(address: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM tasks WHERE is_online = 1")
    fun deleteOnlineTask()

    @Query("SELECT * FROM tasks WHERE is_online = 1")
    fun getOnlineTask(): TaskEntity?
}