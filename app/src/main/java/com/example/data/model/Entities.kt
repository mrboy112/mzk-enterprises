package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val serviceType: String,
    val acBrand: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val address: String,
    val notes: String = "",
    val status: String = "Pending", // Pending, Confirmed, In Progress, Completed, Cancelled
    val assignedTechnicianId: Long? = null,
    val assignedTechnicianName: String? = null,
    val finalPrice: Double,
    val paymentMethod: String = "Cash on Service", // UPI, Card, Net Banking, Cash on Service
    val paymentStatus: String = "Unpaid", // Unpaid, Paid
    val reviewRating: Float = 0.0f,
    val reviewText: String? = null,
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val customerSignature: String? = null, // Path coordinates or base64 visual signature representation
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val price: Double,
    val originalPrice: Double,
    val type: String, // NEW, USED
    val rating: Float = 4.5f,
    val reviewsCount: Int = 12,
    val description: String,
    val specs: String, // e.g., "1.5 Ton, 3 Star, Inverter Split AC"
    val isWishlist: Boolean = false
) : Serializable

@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey val id: Long,
    val name: String,
    val phone: String,
    val status: String = "Available", // Available, Offline, Busy
    val rating: Float = 4.8f,
    val totalEarnings: Double = 0.0,
    val jobsCompleted: Int = 0
) : Serializable
