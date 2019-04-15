package ru.relabs.kurjercontroller.database.daos

import androidx.room.*
import ru.relabs.kurjercontroller.database.entities.EntranceAppartamentReportEntity

/**
 * Created by ProOrange on 30.08.2018.
 */
@Dao
interface EntranceAppartamentReportEntityDao {

    @get:Query("SELECT * FROM entrance_appartament_reports")
    val all: List<EntranceAppartamentReportEntity>

    @Query("SELECT * FROM entrance_appartament_reports WHERE id = :id")
    fun getById(id: Int): EntranceAppartamentReportEntity?

    @Update
    fun update(address: EntranceAppartamentReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(address: EntranceAppartamentReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(address: List<EntranceAppartamentReportEntity>)

    @Delete
    fun delete(address: EntranceAppartamentReportEntity)
}