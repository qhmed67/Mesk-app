package com.example.masjd2.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Room database for prayer times and user preferences
 */
@Database(
    entities = [PrayerEntity::class, UserPreferencesEntity::class, AthanSettingsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PrayerDatabase : RoomDatabase() {
    
    abstract fun prayerDao(): PrayerDao
    
    companion object {
        @Volatile
        private var INSTANCE: PrayerDatabase? = null
        
        fun getDatabase(context: Context): PrayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrayerDatabase::class.java,
                    "prayer_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
