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
        TaskPublisherEntity::class, SendQueryItemEntity::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressEntityDao
    abstract fun entranceAppartamentReportDao(): EntranceAppartamentReportEntityDao
    abstract fun entranceDao(): EntranceEntityDao
    abstract fun entrancePhotoDao(): EntrancePhotoEntityDao
    abstract fun entranceReportDao(): EntranceReportEntityDao
    abstract fun taskDao(): TaskEntityDao
    abstract fun taskItemDao(): TaskItemEntityDao
    abstract fun taskPublisherDao(): TaskPublisherEntityDao
    abstract fun sendQueryDao(): SendQueryDao
}