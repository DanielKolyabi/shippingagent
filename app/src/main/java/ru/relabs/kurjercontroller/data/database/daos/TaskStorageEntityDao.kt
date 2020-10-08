package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.TaskStorageEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface TaskStorageEntityDao {

    @get:Query("SELECT * FROM task_storages")
    val all: List<TaskStorageEntity>

    @Query("SELECT * FROM task_storages WHERE id = :id")
    fun getById(id: Int): TaskStorageEntity?

    @Query("SELECT * FROM task_storages WHERE task_id = :taskId")
    fun getByTaskId(taskId: Int): List<TaskStorageEntity>

    @Update
    fun update(item: TaskStorageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TaskStorageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<TaskStorageEntity>)

    @Delete
    fun delete(item: TaskStorageEntity)

    @Query("DELETE FROM task_storages WHERE task_id = :taskId")
    fun deleteByTaskId(taskId: Int)
}