package ru.relabs.kurjercontroller.data.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.data.database.entities.ApartmentResultEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface ApartmentResultEntityDao {

    @get:Query("SELECT * FROM apartment_results")
    val all: List<ApartmentResultEntity>

    @Query("SELECT * FROM apartment_results WHERE task_id = :taskId AND task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun getByEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int): List<ApartmentResultEntity>

    @Query("SELECT * FROM apartment_results WHERE task_id = :taskId AND task_item_id = :taskItemId AND entrance_number = :entranceNumber AND apartment_number = :apartmentNumber")
    fun getByEntranceApartment(taskId: Int, taskItemId: Int, entranceNumber: Int, apartmentNumber: Int): ApartmentResultEntity?

    @Update
    fun update(address: ApartmentResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: ApartmentResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<ApartmentResultEntity>)

    @Delete
    fun delete(address: ApartmentResultEntity)

    @Query("DELETE FROM apartment_results WHERE task_id = :taskId AND task_item_id = :taskItemId AND entrance_number = :entranceNumber")
    fun deleteByEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int)
}