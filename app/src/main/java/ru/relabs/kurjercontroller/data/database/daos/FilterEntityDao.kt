package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.FilterEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface FilterEntityDao {

    @Query("SELECT * FROM filters WHERE task_id = :taskId")
    fun getByTaskId(taskId: Int): List<FilterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: FilterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<FilterEntity>)

    @Delete
    fun delete(item: FilterEntity)

    @Query("DELETE FROM filters WHERE task_id = :taskId")
    fun deleteByTaskId(taskId: Int)
}