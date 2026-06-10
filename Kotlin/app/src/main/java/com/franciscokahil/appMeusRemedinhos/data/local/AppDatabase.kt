package com.franciscokahil.appMeusRemedinhos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.franciscokahil.appMeusRemedinhos.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [EventEntity::class], version = 2, exportSchema = false)
@TypeConverters(MedicationTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database.eventDao(), context)
                }
            }
        }

        private suspend fun seedDatabase(eventDao: EventDao, context: Context) {
            val defaultEvents = listOf(
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.wake_up), "07:00", icon = "wb_sunny"),
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.breakfast), "08:00", icon = "local_cafe"),
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.morning), "10:00", icon = "work"),
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.lunch), "12:00", icon = "restaurant"),
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.afternoon), "16:00", icon = "wb_twilight"),
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.dinner), "20:00", icon = "dinner_dining"),
                EventEntity(UUID.randomUUID().toString(), context.getString(R.string.sleep), "22:00", icon = "bed")
            )
            for (event in defaultEvents) {
                eventDao.insertEvent(event)
            }
        }
    }
}
