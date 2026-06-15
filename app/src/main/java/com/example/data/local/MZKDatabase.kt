package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Booking
import com.example.data.model.Product
import com.example.data.model.Technician

@Database(entities = [Booking::class, Product::class, Technician::class], version = 1, exportSchema = false)
abstract class MZKDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun productDao(): ProductDao
    abstract fun technicianDao(): TechnicianDao

    companion object {
        @Volatile
        private var INSTANCE: MZKDatabase? = null

        fun getDatabase(context: Context): MZKDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MZKDatabase::class.java,
                    "mzk_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
