package com.example.datalogger.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


//actual database class, syntax copied by a video tutorial
@Database(
    entities = [Channel::class],
    version = 3,
)
abstract class ChannelsDatabase : RoomDatabase() {

    abstract fun dao(): ChannelDao

    companion object {
        @Volatile
        var INSTANCE: ChannelsDatabase? = null

        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN staticValue FLOAT DEFAULT 0 NOT NULL; ADD COLUMN startTime STRING DEFAULT NULL; ADD COLUMN stopTime STRING DEFAULT NULL;")
            }
        }

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN isActivated INTEGER DEFAULT 0 NOT NULL")
            }
        }

        // Get the database instance with migration
        fun getDatabase(context: Context): ChannelsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChannelsDatabase::class.java,
                    "channels.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // 添加迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}