package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.TaskItemEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface TaskItemEntityDao {

    @get:Query("SELECT * FROM task_items")
    val all: List<TaskItemEntity>

    @Query("SELECT * FROM task_items WHERE id = :id")
    fun getById(id: Int): TaskItemEntity?

    @Query("SELECT * FROM task_items WHERE task_id = :taskId")
    fun getByTaskId(taskId: Int): List<TaskItemEntity>

    @Update
    fun update(item: TaskItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TaskItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<TaskItemEntity>)

    @Delete
    fun delete(item: TaskItemEntity)

}