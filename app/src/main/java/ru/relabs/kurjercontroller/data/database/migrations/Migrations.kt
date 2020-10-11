package ru.relabs.kurjercontroller.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    fun getMigrations(): Array<Migration> = arrayOf(
        migration_36_37,
        migration_37_38,
        migration_38_39,
        migration_39_40,
        migration_40_41,
        migration_42_43,
        migration_43_44
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
}