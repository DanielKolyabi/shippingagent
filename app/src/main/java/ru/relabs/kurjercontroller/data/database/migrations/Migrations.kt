package ru.relabs.kurjercontroller.data.database.migrations

import android.content.SharedPreferences
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.repositories.SettingsRepository

object Migrations: KoinComponent {
    private val preferences by inject<SharedPreferences>()

    fun getMigrations(): Array<Migration> = arrayOf(
        migration_36_37,
        migration_37_38,
        migration_38_39,
        migration_39_40,
        migration_40_41,
        migration_42_43,
        migration_43_44,
        migration_44_45,
        migration_45_46,
        migration_46_47,
        migration_47_48,
        migration_48_49,
        migration_49_51
    )


    val migration_36_37 = object : Migration(36, 37) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE task_items ADD COLUMN is_new INTEGER NOT NULL DEFAULT 0")
        }
    }
    val migration_37_38 = object : Migration(37, 38) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE task_items ADD COLUMN wrong_method INTEGER NOT NULL DEFAULT 0")
        }
    }
    val migration_38_39 = object : Migration(38, 39) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN with_planned INTEGER NOT NULL DEFAULT 0")
        }
    }
    val migration_39_40 = object : Migration(39, 40) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE task_items ADD COLUMN button_name TEXT NOT NULL DEFAULT ''")
        }
    }
    val migration_40_41 = object : Migration(40, 41) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE task_items ADD COLUMN required_apartments TEXT NOT NULL DEFAULT ''")
        }
    }
    val migration_42_43 = object : Migration(42, 43) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE entrance_photos ADD COLUMN real_path TEXT")
        }
    }
    val migration_43_44 = object : Migration(43, 44) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE entrance_photos ADD COLUMN is_entrance_photo INTEGER NOT NULL DEFAULT 0")
        }
    }
    val migration_44_45 = object : Migration(44, 45) {
        override fun migrate(database: SupportSQLiteDatabase) {
            fun createTable(name: String) = """
                    CREATE TABLE $name(
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        task_id INTEGER NOT NULL,
                        storage_id INTEGER NOT NULL,
                        address TEXT NOT NULL,
                        gpsLat REAL NOT NULL,
                        gpsLong REAL NOT NULL
                    )
                """.trimIndent()

            database.execSQL(createTable("storages_temp"))
            database.execSQL("INSERT INTO storages_temp SELECT * FROM task_storages")
            database.execSQL("DROP TABLE task_storages")
            database.execSQL(createTable("task_storages"))
            database.execSQL("CREATE UNIQUE INDEX `index_task_storages_storage_id_task_id` ON `task_storages`(`storage_id`, `task_id`);")
            database.execSQL("INSERT INTO task_storages SELECT * FROM storages_temp")
            database.execSQL("DROP TABLE storages_temp")
        }
    }

    val migration_45_46 = object : Migration(45, 46) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE entrance_reports ADD COLUMN remove_after_send INTEGER NOT NULL DEFAULT 1")
            database.execSQL("ALTER TABLE entrance_reports ADD COLUMN close_distance INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE entrance_reports ADD COLUMN allowed_distance INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE entrance_reports ADD COLUMN radius_required INTEGER NOT NULL DEFAULT 0")
        }
    }
    val migration_46_47 = object : Migration(46, 47) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                    CREATE TABLE closed_addresses(
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        address_id INTEGER NOT NULL,
                        entrance_number INTEGER NOT NULL,
                        user_id TEXT NOT NULL
                    )
                """.trimIndent()
            )
        }
    }
    val migration_47_48 = object : Migration(47, 48) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE task_items ADD COLUMN entrances_monitoring_mode INTEGER NOT NULL DEFAULT 0")
        }
    }
    val migration_48_49 = object : Migration(48, 49) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val storedRequiredRadius = preferences.getInt(SettingsRepository.RADIUS_KEY, SettingsRepository.DEFAULT_REQUIRED_RADIUS)
            database.execSQL("ALTER TABLE task_items ADD COLUMN close_radius INTEGER NOT NULL DEFAULT $storedRequiredRadius")
        }
    }
    val migration_49_51 = object : Migration(49, 51) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE entrances ADD COLUMN is_stacked INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE entrance_reports ADD COLUMN is_stacked INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE entrance_results ADD COLUMN is_stacked INTEGER DEFAULT NULL")
        }
    }
}