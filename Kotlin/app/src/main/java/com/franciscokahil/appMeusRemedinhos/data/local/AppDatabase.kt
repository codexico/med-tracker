package com.franciscokahil.appMeusRemedinhos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        EventEntity::class,
        Medication::class,
        EventMedicationEntity::class,
        DoseHistoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(EventTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun medicationDao(): MedicationDao
    abstract fun doseHistoryDao(): DoseHistoryDao

    fun ensureSeeded() {
        // Seeding removed for UX 2.0. Users start with an empty state.
    }

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_tracker_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
