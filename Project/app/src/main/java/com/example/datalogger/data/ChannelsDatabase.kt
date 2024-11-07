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
    version = 5,
)
abstract class ChannelsDatabase : RoomDatabase() {

    abstract fun dao(): ChannelDao

    companion object {
        @Volatile
        var INSTANCE: ChannelsDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN isActivated INTEGER DEFAULT 0 NOT NULL")
            }
        }

        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN staticValue FLOAT DEFAULT 0 NOT NULL; ADD COLUMN startTime STRING DEFAULT \"00:00\" NOT NULL; ADD COLUMN stopTime STRING DEFAULT \"00:00\" NOT NULL;")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN isStatic INTEGER DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN hasTriggerLevel INTEGER DEFAULT 0 NOT NULL; ADD COLUMN triggerLevel FLOAT DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Channel ADD COLUMN aboveTriggerLevel INTEGER DEFAULT 0 NOT NULL")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // 添加迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}