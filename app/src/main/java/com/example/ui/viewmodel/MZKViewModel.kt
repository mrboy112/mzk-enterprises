package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MZKDatabase
import com.example.data.model.Booking
import com.example.data.model.Product
import com.example.data.model.Technician
import com.example.data.repository.MZKRepository
import com.example.data.repository.FirestoreSyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole {
    CUSTOMER, TECHNICIAN, ADMIN
}

data class AppNotification(
    val id: Long,
    val bookingId: Long,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

class MZKViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MZKDatabase.getDatabase(application)
    private val repository = MZKRepository(
        database.bookingDao(),
        database.productDao(),
        database.technicianDao()
    )

    // Active screen selection inside roles
    val currentRole = MutableStateFlow(UserRole.CUSTOMER)
    
    // User Session Mock State
    val loggedInPhone = MutableStateFlow("8540888704") // Default company contact or user's phone
    val loggedInEmail = MutableStateFlow("mzk03official@gmail.com")
    val loggedInName = MutableStateFlow("Md Zubair Khan")
    val isLoggedIn = MutableStateFlow(true) // Automatically logged in for seamless demo testing

    // Selected Technician ID for Technician Role Mocking (e.g., Rohan Das - 101)
    val selectedTechnicianId = MutableStateFlow<Long>(101)

    // Data streams from Room
    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTechnicians: StateFlow<List<Technician>> = repository.allTechnicians
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wishlist Product IDs
    val wishlist = MutableStateFlow<Set<Long>>(emptySet())

    // Filter states for AC Store
    val storeTypeFilter = MutableStateFlow("ALL") // ALL, NEW, USED, EXCHANGE
    val storeBrandFilter = MutableStateFlow("ALL") // ALL, Daikin, Voltas, LG, etc.
    val storeSearchQuery = MutableStateFlow("")

    // Notifications State Flow
    val notifications = MutableStateFlow<List<AppNotification>>(emptyList())

    fun addNotification(bookingId: Long, title: String, message: String) {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val timeStr = sdf.format(java.util.Date())
        val newNotif = AppNotification(
            id = System.currentTimeMillis() + (0..1000).random(),
            bookingId = bookingId,
            title = title,
            message = message,
            timestamp = timeStr,
            isRead = false
        )
        notifications.value = listOf(newNotif) + notifications.value
    }
    
    fun markAllNotificationsAsRead() {
        notifications.value = notifications.value.map { it.copy(isRead = true) }
    }

    fun clearNotification(notificationId: Long) {
        notifications.value = notifications.value.filter { it.id != notificationId }
    }

    // Active real-time progress simulation feedback map (Booking ID -> Stage description)
    val bookingSimulationState = MutableStateFlow<Map<Long, String>>(emptyMap())

    init {
        viewModelScope.launch {
            // Curate default data on launch
            repository.prepopulateDatabase()
        }

        // Real-time notification updates based on status transitions
        viewModelScope.launch {
            var previousBookingsList: List<Booking>? = null
            combine(allBookings, loggedInPhone) { currentList, phone ->
                currentList.filter { it.customerPhone == phone }
            }.collect { userCurrentBookings ->
                if (previousBookingsList != null) {
                    val prevList = previousBookingsList!!
                    for (curr in userCurrentBookings) {
                        val prev = prevList.find { it.id == curr.id }
                        if (prev != null) {
                            if (prev.status != curr.status) {
                                val title = when (curr.status) {
                                    "Confirmed" -> "Technician Dispatched 🚀"
                                    "In Progress" -> "Service In-Progress 🛠️"
                                    "Completed" -> "Job Completed successfully! ✅"
                                    "Cancelled" -> "Booking Cancelled ❌"
                                    else -> "Appointment Status Updated 🔔"
                                }
                                val message = when (curr.status) {
                                    "Confirmed" -> "Great news! Technician ${curr.assignedTechnicianName ?: "your assigned professional"} has been dispatched for your ${curr.serviceType} appointment."
                                    "In Progress" -> "Technician ${curr.assignedTechnicianName ?: "your expert"} has arrived and is currently performing ${curr.serviceType}."
                                    "Completed" -> "The service was completed successfully! You can now view and download your digital tax invoice."
                                    "Cancelled" -> "Your booking for ${curr.serviceType} has been cancelled."
                                    else -> "Your booking #${curr.id} status was updated to ${curr.status}."
                                }
                                addNotification(curr.id, title, message)
                            } else if (prev.assignedTechnicianId == null && curr.assignedTechnicianId != null) {
                                val title = "Technician Assigned & Dispatched 🚀"
                                val message = "Technician ${curr.assignedTechnicianName} has been assigned to your ${curr.serviceType} booking on ${curr.scheduledDate}."
                                addNotification(curr.id, title, message)
                            }
                        } else {
                            // New booking created during the active session
                            addNotification(
                                curr.id,
                                "Doorstep Booking Registered 📅",
                                "Your doorstep request for ${curr.serviceType} has been received and is pending technician allocation."
                            )
                        }
                    }
                }
                previousBookingsList = userCurrentBookings
            }
        }
    }

    // Toggle Wishlist
    fun toggleWishlist(productId: Long) {
        val current = wishlist.value
        if (current.contains(productId)) {
            wishlist.value = current - productId
        } else {
            wishlist.value = current + productId
        }
    }

    // Create a new Booking
    fun createBooking(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        serviceType: String,
        acBrand: String,
        scheduledDate: String,
        scheduledTime: String,
        address: String,
        notes: String,
        price: Double,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val isPaid = if (paymentMethod == "Cash on Service") "Unpaid" else "Paid"
            val newBooking = Booking(
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                serviceType = serviceType,
                acBrand = acBrand,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                address = address,
                notes = notes,
                status = "Pending",
                finalPrice = price,
                paymentMethod = paymentMethod,
                paymentStatus = isPaid
            )
            val generatedId = repository.insertBooking(newBooking)
            val insertedBooking = newBooking.copy(id = generatedId)
            
            // Sync to Firebase of simulated cloud
            FirestoreSyncManager.syncBookingToCloud(insertedBooking)
        }
    }

    // Admin Action: Assign Technician to Booking
    fun assignTechnician(bookingId: Long, techId: Long, techName: String) {
        viewModelScope.launch {
            val booking = allBookings.value.find { it.id == bookingId }
            if (booking != null) {
                val updatedBooking = booking.copy(
                    assignedTechnicianId = techId,
                    assignedTechnicianName = techName,
                    status = "Confirmed"
                )
                repository.updateBooking(updatedBooking)
                FirestoreSyncManager.syncBookingToCloud(updatedBooking)
            }
        }
    }

    // Admin/Technician Action: Update Booking Status
    fun updateBookingStatus(bookingId: Long, nextStatus: String) {
        viewModelScope.launch {
            val booking = allBookings.value.find { it.id == bookingId }
            if (booking != null) {
                var paymentStatus = booking.paymentStatus
                if (nextStatus == "Completed" && booking.paymentMethod == "Cash on Service") {
                    // Update technician ratings/counters
                    booking.assignedTechnicianId?.let { techId ->
                        repository.getTechnicianById(techId)?.let { tech ->
                            val updatedTech = tech.copy(
                                jobsCompleted = tech.jobsCompleted + 1,
                                totalEarnings = tech.totalEarnings + (booking.finalPrice * 0.7) // Technicians keep 70%
                            )
                            repository.updateTechnician(updatedTech)
                        }
                    }
                }
                
                val updatedBooking = booking.copy(
                    status = nextStatus,
                    paymentStatus = paymentStatus
                )
                repository.updateBooking(updatedBooking)
                FirestoreSyncManager.syncBookingToCloud(updatedBooking)
            }
        }
    }

    // Technician Action: Update own profile availability status
    fun updateTechnicianStatus(techId: Long, status: String) {
        viewModelScope.launch {
            repository.getTechnicianById(techId)?.let { tech ->
                val updated = tech.copy(status = status)
                repository.updateTechnician(updated)
            }
        }
    }

    // Technician Action: Complete Job with Signature, Photos, and Mark Paid
    fun completeJob(bookingId: Long, signatureSvgData: String, beforePhoto: String?, afterPhoto: String?) {
        viewModelScope.launch {
            val booking = allBookings.value.find { it.id == bookingId }
            if (booking != null) {
                // Update Technician Earnings and Counters
                booking.assignedTechnicianId?.let { techId ->
                    repository.getTechnicianById(techId)?.let { tech ->
                        val updatedTech = tech.copy(
                            jobsCompleted = tech.jobsCompleted + 1,
                            totalEarnings = tech.totalEarnings + (booking.finalPrice * 0.70)
                        )
                        repository.updateTechnician(updatedTech)
                    }
                }

                val updatedBooking = booking.copy(
                    status = "Completed",
                    paymentStatus = "Paid", // marked paid upon technician completion
                    customerSignature = signatureSvgData,
                    beforePhotoUri = beforePhoto ?: "ic_ac_dirty",
                    afterPhotoUri = afterPhoto ?: "ic_ac_clean"
                )
                repository.updateBooking(updatedBooking)
                FirestoreSyncManager.syncBookingToCloud(updatedBooking)
            }
        }
    }

    // Customer Action: Rate Service
    fun rateService(bookingId: Long, rating: Float, review: String) {
        viewModelScope.launch {
            val booking = allBookings.value.find { it.id == bookingId }
            if (booking != null) {
                val updatedBooking = booking.copy(
                    reviewRating = rating,
                    reviewText = review
                )
                repository.updateBooking(updatedBooking)
                FirestoreSyncManager.syncBookingToCloud(updatedBooking)

                // Update Technician overall rating as average
                booking.assignedTechnicianId?.let { techId ->
                    repository.getTechnicianById(techId)?.let { tech ->
                        val newRating = ((tech.rating * tech.jobsCompleted) + rating) / (tech.jobsCompleted + 1)
                        val rounded = Math.round(newRating * 10) / 10.0f
                        val updatedTech = tech.copy(
                            rating = rounded
                        )
                        repository.updateTechnician(updatedTech)
                    }
                }
            }
        }
    }

    // Submit Old AC Exchange Request
    fun submitExchangeRequest(acBrand: String, acCondition: String, oldAcSpecs: String) {
        viewModelScope.launch {
            // Exchanges are inserted as custom Service Bookings automatically
            val desc = "Exchange Request: Condition: $acCondition. Details: $oldAcSpecs"
            val exchangeBooking = Booking(
                customerName = loggedInName.value,
                customerPhone = loggedInPhone.value,
                customerEmail = loggedInEmail.value,
                serviceType = "AC Exchange Audit",
                acBrand = acBrand,
                scheduledDate = "Pending schedule",
                scheduledTime = "Flexible",
                address = "User Address",
                notes = desc,
                status = "Pending",
                finalPrice = 499.0, // cost of home visit inspection
                paymentMethod = "Cash on Service",
                paymentStatus = "Unpaid"
            )
            val generatedId = repository.insertBooking(exchangeBooking)
            val insertedBooking = exchangeBooking.copy(id = generatedId)
            
            // Sync to Firestore
            FirestoreSyncManager.syncBookingToCloud(insertedBooking)
        }
    }

    /**
     * Simulation: Cycle booking stages in real time (each step takes 6-8 seconds)
     */
    fun simulateRealTimeTracking(bookingId: Long) {
        viewModelScope.launch {
            val booking = allBookings.value.find { it.id == bookingId } ?: return@launch
            
            // Step 1: Booking Confirmed (Status: Confirmed)
            bookingSimulationState.value = bookingSimulationState.value + (bookingId to "Allocating nearest customer support coordinator and assigning technician...")
            kotlinx.coroutines.delay(4000)
            
            val confirmedBooking = booking.copy(
                status = "Confirmed",
                assignedTechnicianId = 102,
                assignedTechnicianName = "Amit Sharma"
            )
            repository.updateBooking(confirmedBooking)
            FirestoreSyncManager.syncBookingToCloud(confirmedBooking)
            bookingSimulationState.value = bookingSimulationState.value + (bookingId to "Technician Amit Sharma is now assigned. Traveling with gear.")
            
            // Step 2: Service In-Progress (Status: In Progress / In-service)
            kotlinx.coroutines.delay(6000)
            val inProgressBooking = confirmedBooking.copy(
                status = "In Progress"
            )
            repository.updateBooking(inProgressBooking)
            FirestoreSyncManager.syncBookingToCloud(inProgressBooking)
            bookingSimulationState.value = bookingSimulationState.value + (bookingId to "Amit Sharma is on-site. Initiating cooling test and system diagnosis.")

            // Step 3: Service Completed (Status: Completed, Payment: Paid)
            kotlinx.coroutines.delay(6000)
            val completedBooking = inProgressBooking.copy(
                status = "Completed",
                paymentStatus = "Paid"
            )
            repository.updateBooking(completedBooking)
            FirestoreSyncManager.syncBookingToCloud(completedBooking)
            bookingSimulationState.value = bookingSimulationState.value + (bookingId to "AC servicing completed! Airflow optimization and pressure parameters look good.")
            
            // Hold message brief then remove from active simulations
            kotlinx.coroutines.delay(5000)
            bookingSimulationState.value = bookingSimulationState.value - bookingId
        }
    }

    // Update Customer Profile details
    fun updateProfile(name: String, email: String, phone: String) {
        loggedInName.value = name
        loggedInEmail.value = email
        loggedInPhone.value = phone
    }
}

class MZKViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MZKViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MZKViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
