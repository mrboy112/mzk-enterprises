package com.example.data.repository

import com.example.data.local.BookingDao
import com.example.data.local.ProductDao
import com.example.data.local.TechnicianDao
import com.example.data.model.Booking
import com.example.data.model.Product
import com.example.data.model.Technician
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MZKRepository(
    private val bookingDao: BookingDao,
    private val productDao: ProductDao,
    private val technicianDao: TechnicianDao
) {
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allTechnicians: Flow<List<Technician>> = technicianDao.getAllTechnicians()

    fun getBookingsByCustomer(phone: String): Flow<List<Booking>> {
        return bookingDao.getBookingsByCustomer(phone)
    }

    fun getBookingsByTechnician(techId: Long): Flow<List<Booking>> {
        return bookingDao.getBookingsByTechnician(techId)
    }

    suspend fun getBookingById(id: Long): Booking? = withContext(Dispatchers.IO) {
        bookingDao.getBookingById(id)
    }

    suspend fun insertBooking(booking: Booking): Long = withContext(Dispatchers.IO) {
        bookingDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: Booking) = withContext(Dispatchers.IO) {
        bookingDao.updateBooking(booking)
    }

    suspend fun deleteBooking(booking: Booking) = withContext(Dispatchers.IO) {
        bookingDao.deleteBooking(booking)
    }

    suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun getTechnicianById(id: Long): Technician? = withContext(Dispatchers.IO) {
        technicianDao.getTechnicianById(id)
    }

    suspend fun insertTechnician(technician: Technician) = withContext(Dispatchers.IO) {
        technicianDao.insertTechnician(technician)
    }

    suspend fun updateTechnician(technician: Technician) = withContext(Dispatchers.IO) {
        technicianDao.updateTechnician(technician)
    }

    // Curated initial database populations
    suspend fun prepopulateDatabase() = withContext(Dispatchers.IO) {
        // 1. Populate products if empty
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isEmpty()) {
            val defaultProducts = listOf(
                Product(
                    name = "Daikin 1.5 Ton Split AC (Premium)",
                    brand = "Daikin",
                    price = 42000.0,
                    originalPrice = 48000.0,
                    type = "NEW",
                    specs = "1.5 Ton, Direct Inverter, 5 Star Rating, Pure Copper Condenser",
                    description = "Daikin's premium 1.5-ton split AC is super efficient, silent, and offers custom cooling swings with intelligent motion detection sensors."
                ),
                Product(
                    name = "Voltas 1 Ton Window AC",
                    brand = "Voltas",
                    price = 26500.0,
                    originalPrice = 31000.0,
                    type = "NEW",
                    specs = "1 Ton, Eco Copper, 3 Star Energy Rated",
                    description = "A compact, robust window AC ideal for small master bedrooms. Designed for peak Indian summers with self-diagnosis systems."
                ),
                Product(
                    name = "LG Dual Inverter Split AC",
                    brand = "LG",
                    price = 38999.0,
                    originalPrice = 46000.0,
                    type = "NEW",
                    specs = "1.5 Ton, AI Dual Inverter, 4 Star, Smart WiFi",
                    description = "Dual Rotary compressor offers faster cooling, longer life, and extremely silent performance. Connects to LG ThinQ app."
                ),
                Product(
                    name = "Samsung Convertible 5-in-1 AC",
                    brand = "Samsung",
                    price = 35500.0,
                    originalPrice = 42000.0,
                    type = "NEW",
                    specs = "1.5 Ton, 5-in-1 Modes, 3 Star, Anti-Bacteria Filters",
                    description = "Adjust cooling capacity dynamically on 5 levels to minimize power bills. Easy Clean filters help capture dust and mold."
                ),
                Product(
                    name = "Blue Star Heavy-Duty Split AC",
                    brand = "Blue Star",
                    price = 45000.0,
                    originalPrice = 51200.0,
                    type = "NEW",
                    specs = "2.0 Ton, Gold Fin Coils, 5 Star Rated",
                    description = "Built for commercial zones or spacious drawing rooms. Incredible air throw distance of up to 40 feet with anti-corrosive gold fins."
                ),
                Product(
                    name = "Panasonic Smart Inverter Split AC",
                    brand = "Panasonic",
                    price = 39500.0,
                    originalPrice = 45500.0,
                    type = "NEW",
                    specs = "1.5 Ton, WiFi Enabled, PM 0.1 Filter, 5 Star",
                    description = "Features MirAIe IoT capabilities. In-built air purification system active even with compressor off, capturing particulate matter down to PM 0.1."
                ),
                Product(
                    name = "Pre-Loved Split AC Daikin (Certified)",
                    brand = "Daikin",
                    price = 18000.0,
                    originalPrice = 38000.0,
                    type = "USED",
                    specs = "1.5 Ton, 5 Star, Checked Gas, 1 Year Service Warranty",
                    description = "Certified used AC fully flushed, tested for leaks, and filled with genuine brand lubricants. Includes a 1-year complimentary service package."
                ),
                Product(
                    name = "Pre-Loved Window AC Voltas (Economic)",
                    brand = "Voltas",
                    price = 11000.0,
                    originalPrice = 22000.0,
                    type = "USED",
                    specs = "1 Ton, Fully Serviced & Overhauled, 6-Month Warranty",
                    description = "Affordable window AC in clean working condition. Perfect for rentals or short term stays. Free delivery inside Pin 733201."
                )
            )
            productDao.insertProducts(defaultProducts)
        }

        // 2. Populate technicians if empty
        val existingTechnicians = technicianDao.getAllTechnicians().first()
        if (existingTechnicians.isEmpty()) {
            val defaultTechnicians = listOf(
                Technician(id = 101, name = "Rohan Das", phone = "9876543210", status = "Available", rating = 4.8f, totalEarnings = 12500.0, jobsCompleted = 32),
                Technician(id = 102, name = "Amit Sharma", phone = "8765432109", status = "Available", rating = 4.9f, totalEarnings = 18400.0, jobsCompleted = 45),
                Technician(id = 103, name = "Sanjay Kumar", phone = "7654321098", status = "Available", rating = 4.7f, totalEarnings = 9200.0, jobsCompleted = 21),
                Technician(id = 104, name = "Rahul Mishra", phone = "6543210987", status = "Busy", rating = 4.6f, totalEarnings = 15300.0, jobsCompleted = 39)
            )
            technicianDao.insertTechnicians(defaultTechnicians)
        }
    }
}
