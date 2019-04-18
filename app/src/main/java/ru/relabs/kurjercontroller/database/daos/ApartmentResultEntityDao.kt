package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.ApartmentResultEntity
import ru.relabs.kurjercontroller.database.entities.EntranceResultEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface ApartmentResultEntityDao {

    @get:Query("SELECT * FROM apartment_results")
    val all: List<ApartmentResultEntity>

    @Query("SELECT * FROM apartment_results WHERE task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun getByEntrance(taskItemId: Int, entranceNumber: Int): List<ApartmentResultEntity>

    @Update
    fun update(address: ApartmentResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: ApartmentResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<ApartmentResultEntity>)

    @Delete
    fun delete(address: ApartmentResultEntity)

    @Query("DELETE FROM apartment_results WHERE task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun deleteByEntrance(taskItemId: Int, entranceNumber: Int)
}