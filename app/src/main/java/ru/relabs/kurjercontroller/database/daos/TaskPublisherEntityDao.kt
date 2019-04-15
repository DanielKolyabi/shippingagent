package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.TaskItemEntity
import ru.relabs.kurjercontroller.database.entities.TaskPublisherEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface TaskPublisherEntityDao {

    @get:Query("SELECT * FROM task_publishers")
    val all: List<TaskPublisherEntity>

    @Query("SELECT * FROM task_publishers WHERE id = :id")
    fun getById(id: Int): TaskPublisherEntity?

    @Query("SELECT * FROM task_publishers WHERE task_id = :taskId")
    fun getByTaskId(taskId: Int): List<TaskPublisherEntity>

    @Update
    fun update(item: TaskPublisherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TaskPublisherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<TaskPublisherEntity>)

    @Delete
    fun delete(item: TaskPublisherEntity)
}