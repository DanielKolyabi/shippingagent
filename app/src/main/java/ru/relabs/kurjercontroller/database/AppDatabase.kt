package ru.relabs.kurjercontroller.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.relabs.kurjercontroller.database.daos.*
import ru.relabs.kurjercontroller.database.entities.*

/**
 * Created by ProOrange on 30.08.2018.
 */
@Database(
    entities = [AddressEntity::class, EntranceAppartamentReportEntity::class, EntranceEntity::class,
        EntrancePhotoEntity::class, EntranceReportEntity::class, TaskEntity::class, TaskItemEntity::class,
        TaskPublisherEntity::class, SendQueryItemEntity::class, EntranceResultEntity::class,
        ApartmentResultEntity::class, EntranceKeyEntity::class, EntranceEuroKeyEntity::class,
        FilterEntity::class],
    version = 35
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressEntityDao
    abstract fun entranceAppartamentReportDao(): EntranceAppartamentReportEntityDao
    abstract fun entranceDao(): EntranceEntityDao
    abstract fun entrancePhotoDao(): EntrancePhotoEntityDao
    abstract fun entranceReportDao(): EntranceReportEntityDao
    abstract fun entranceResultDao(): EntranceResultEntityDao
    abstract fun taskDao(): TaskEntityDao
    abstract fun taskItemDao(): TaskItemEntityDao
    abstract fun filtersDao(): FilterEntityDao
    abstract fun taskPublisherDao(): TaskPublisherEntityDao
    abstract fun sendQueryDao(): SendQueryDao
    abstract fun apartmentResultDao(): ApartmentResultEntityDao
    abstract fun entranceKeysDao(): EntranceKeyEntityDao
    abstract fun entranceEuroKeysDao(): EntranceEuroKeyEntityDao
}