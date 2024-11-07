package com.example.datalogger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


//actual database class, syntax copied by a video tutorial
@Database(
    entities = [Channel::class],
    version = 2,
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

        // Get the database instance with migration
        fun getDatabase(context: Context): ChannelsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChannelsDatabase::class.java,
                    "channels.db"
                )
                    .addMigrations(MIGRATION_1_2) // 添加迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}