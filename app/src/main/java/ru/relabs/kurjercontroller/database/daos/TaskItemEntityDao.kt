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

    @Query("SELECT * FROM task_items WHERE task_item_id = :taskItemId")
    fun getAllByTaskItemId(taskItemId: Int): List<TaskItemEntity>

    @Query("SELECT * FROM task_items WHERE task_id = :taskId AND task_item_id = :taskItemId")
    fun getByTaskItemId(taskId: Int, taskItemId: Int): TaskItemEntity?

    @Query("SELECT * FROM task_items WHERE task_id = :taskId")
    fun getByTaskId(taskId: Int): List<TaskItemEntity>

    @Update
    fun update(item: TaskItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TaskItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<TaskItemEntity>)

    @Query("DELETE FROM task_items WHERE task_id = :taskId")
    fun deleteByTaskId(taskId: Int)

    @Delete
    fun delete(item: TaskItemEntity)

    @Query("SELECT * FROM task_items WHERE address_id = :addressId")
    fun getByAddressId(addressId: Int): List<TaskItemEntity>

}