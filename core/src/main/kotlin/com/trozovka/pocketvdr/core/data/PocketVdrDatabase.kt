package com.trozovka.pocketvdr.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VoyageEntity::class, FixEntity::class, FlagEventEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class PocketVdrDatabase : RoomDatabase() {
    abstract fun voyageDao(): VoyageDao
    abstract fun fixDao(): FixDao
    abstract fun flagEventDao(): FlagEventDao

    companion object {
        @Volatile
        private var instance: PocketVdrDatabase? = null

        fun getInstance(context: Context): PocketVdrDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PocketVdrDatabase::class.java,
                    "pocketvdr.db",
                )
                    // Pre-release schema churn only -- no real user data to preserve yet.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
