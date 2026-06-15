package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Booking
import com.example.data.model.Product
import com.example.data.model.Technician
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE customerPhone = :phone ORDER BY timestamp DESC")
    fun getBookingsByCustomer(phone: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE assignedTechnicianId = :techId ORDER BY timestamp DESC")
    fun getBookingsByTechnician(techId: Long): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE brand = :brand ORDER BY name ASC")
    fun getProductsByBrand(brand: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

@Dao
interface TechnicianDao {
    @Query("SELECT * FROM technicians ORDER BY name ASC")
    fun getAllTechnicians(): Flow<List<Technician>>

    @Query("SELECT * FROM technicians WHERE id = :id")
    suspend fun getTechnicianById(id: Long): Technician?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: Technician)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnicians(technicians: List<Technician>)

    @Update
    suspend fun updateTechnician(technician: Technician)

    @Delete
    suspend fun deleteTechnician(technician: Technician)
}
