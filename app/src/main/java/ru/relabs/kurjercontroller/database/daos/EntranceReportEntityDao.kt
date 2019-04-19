package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.EntranceReportEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceReportEntityDao {

    @get:Query("SELECT * FROM entrance_reports")
    val all: List<EntranceReportEntity>

    @Query("SELECT * FROM entrance_reports WHERE id = :id")
    fun getById(id: Int): EntranceReportEntity?

    @Query("SELECT * FROM entrance_reports WHERE task_item_id = :taskItemId AND entrance_number = :number")
    fun getByNumber(taskItemId: Int, number: Int): EntranceReportEntity?

    @Update
    fun update(address: EntranceReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntranceReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntranceReportEntity>)

    @Delete
    fun delete(address: EntranceReportEntity)
}