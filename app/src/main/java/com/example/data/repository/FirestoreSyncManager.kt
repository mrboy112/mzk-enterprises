package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Booking
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private var firestore: FirebaseFirestore? = null
    var isCloudEnabled: Boolean = false
        private set
    var connectionError: String? = null
        private set

    // Allow user to supply custom keys dynamically in UI, or fall back to mock
    var customProjectId: String = "mzk-enterprises-ac"
    var customApiKey: String = "AIzaSyFakeKeyForDemonstrationOnly"
    var customAppId: String = "1:1234567890:android:abc123xyz"

    fun initialize(context: Context, force: Boolean = false) {
        if (firestore != null && !force) return
        
        try {
            // Locate or programmatically initialize Firebase with available config
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(customAppId)
                    .setProjectId(customProjectId)
                    .setApiKey(customApiKey)
                    .build()
                FirebaseApp.initializeApp(context, options)
            } else {
                FirebaseApp.getInstance()
            }
            
            firestore = FirebaseFirestore.getInstance(app)
            isCloudEnabled = true
            connectionError = null
            Log.d(TAG, "Programmatical Firestore initialized successfully on Project: $customProjectId")
        } catch (e: Exception) {
            connectionError = e.localizedMessage ?: "Unknown initialization error"
            isCloudEnabled = false
            firestore = null
            Log.w(TAG, "Firestore initialization failed: $connectionError. Operating in local-only mode.")
        }
    }

    /**
     * Upload or update a booking in Firestore
     */
    fun syncBookingToCloud(booking: Booking, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val db = firestore ?: return
        
        val bookingMap = hashMapOf(
            "id" to booking.id,
            "customerName" to booking.customerName,
            "customerPhone" to booking.customerPhone,
            "customerEmail" to booking.customerEmail,
            "serviceType" to booking.serviceType,
            "acBrand" to booking.acBrand,
            "scheduledDate" to booking.scheduledDate,
            "scheduledTime" to booking.scheduledTime,
            "address" to booking.address,
            "notes" to booking.notes,
            "status" to booking.status,
            "assignedTechnicianId" to booking.assignedTechnicianId,
            "assignedTechnicianName" to booking.assignedTechnicianName,
            "finalPrice" to booking.finalPrice,
            "paymentMethod" to booking.paymentMethod,
            "paymentStatus" to booking.paymentStatus,
            "reviewRating" to booking.reviewRating,
            "reviewText" to booking.reviewText,
            "timestamp" to booking.timestamp
        )

        db.collection("bookings")
            .document(booking.id.toString())
            .set(bookingMap)
            .addOnSuccessListener {
                Log.d(TAG, "Sent Booking #${booking.id} to Firestore collection")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error syncing Booking #${booking.id} to Firestore: ${e.localizedMessage}")
                onFailure(e)
            }
    }

    /**
     * Delete booking from Cloud Firestore
     */
    fun deleteBookingFromCloud(bookingId: Long) {
        val db = firestore ?: return
        db.collection("bookings")
            .document(bookingId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Deleted Booking #$bookingId from Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed deleting Booking #$bookingId from Firestore: ${e.localizedMessage}")
            }
    }

    /**
     * Real-time listener from Cloud Firestore for a specific booking
     */
    fun listenToBookingRealtime(bookingId: Long): Flow<Booking?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("bookings")
            .document(bookingId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listen error: ${error.localizedMessage}")
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val id = snapshot.getLong("id") ?: bookingId
                        val customerName = snapshot.getString("customerName") ?: ""
                        val customerPhone = snapshot.getString("customerPhone") ?: ""
                        val customerEmail = snapshot.getString("customerEmail") ?: ""
                        val serviceType = snapshot.getString("serviceType") ?: ""
                        val acBrand = snapshot.getString("acBrand") ?: ""
                        val scheduledDate = snapshot.getString("scheduledDate") ?: ""
                        val scheduledTime = snapshot.getString("scheduledTime") ?: ""
                        val address = snapshot.getString("address") ?: ""
                        val notes = snapshot.getString("notes") ?: ""
                        val status = snapshot.getString("status") ?: "Pending"
                        val assignedTechnicianId = snapshot.getLong("assignedTechnicianId")
                        val assignedTechnicianName = snapshot.getString("assignedTechnicianName")
                        val finalPrice = snapshot.getDouble("finalPrice") ?: 0.0
                        val paymentMethod = snapshot.getString("paymentMethod") ?: "Cash on Service"
                        val paymentStatus = snapshot.getString("paymentStatus") ?: "Unpaid"
                        val reviewRating = snapshot.getDouble("reviewRating")?.toFloat() ?: 0.0f
                        val reviewText = snapshot.getString("reviewText")
                        val timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis()

                        val booking = Booking(
                            id = id,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            customerEmail = customerEmail,
                            serviceType = serviceType,
                            acBrand = acBrand,
                            scheduledDate = scheduledDate,
                            scheduledTime = scheduledTime,
                            address = address,
                            notes = notes,
                            status = status,
                            assignedTechnicianId = assignedTechnicianId,
                            assignedTechnicianName = assignedTechnicianName,
                            finalPrice = finalPrice,
                            paymentMethod = paymentMethod,
                            paymentStatus = paymentStatus,
                            reviewRating = reviewRating,
                            reviewText = reviewText,
                            timestamp = timestamp
                        )
                        trySend(booking)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing Firestore document: ${e.localizedMessage}")
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}
