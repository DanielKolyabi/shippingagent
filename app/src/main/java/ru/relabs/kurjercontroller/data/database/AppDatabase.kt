package ru.relabs.kurjercontroller.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.relabs.kurjercontroller.data.database.daos.*
import ru.relabs.kurjercontroller.data.database.entities.*

/**
 * Created by ProOrange on 30.08.2018.
 */
@Database(
    entities = [AddressEntity::class, EntranceAppartamentReportEntity::class, EntranceEntity::class,
        EntrancePhotoEntity::class, EntranceReportEntity::class, TaskEntity::class, TaskItemEntity::class,
        TaskPublisherEntity::class, SendQueryItemEntity::class, EntranceResultEntity::class,
        ApartmentResultEntity::class, EntranceKeyEntity::class, EntranceEuroKeyEntity::class,
        FilterEntity::class, TaskStorageEntity::class, ClosedAddressEntity::class],
    version = 48
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressEntityDao
    abstract fun entranceDao(): EntranceEntityDao
    abstract fun entrancePhotoDao(): EntrancePhotoEntityDao
    abstract fun entranceReportDao(): EntranceReportEntityDao
    abstract fun entranceResultDao(): EntranceResultEntityDao
    abstract fun taskDao(): TaskEntityDao
    abstract fun taskItemDao(): TaskItemEntityDao
    abstract fun filtersDao(): FilterEntityDao
    abstract fun taskPublisherDao(): TaskPublisherEntityDao
    abstract fun taskStorageDao(): TaskStorageEntityDao
    abstract fun sendQueryDao(): SendQueryDao
    abstract fun apartmentResultDao(): ApartmentResultEntityDao
    abstract fun entranceKeysDao(): EntranceKeyEntityDao
    abstract fun entranceEuroKeysDao(): EntranceEuroKeyEntityDao
    abstract fun closedAddressesDao(): ClosedAddressesDao
}