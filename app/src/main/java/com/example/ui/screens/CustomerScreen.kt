package com.example.ui.screens

import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

import android.widget.Space
import com.example.ui.theme.isDarkThemeGlobal
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Product
import com.example.ui.viewmodel.MZKViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun generateAndDownloadInvoicePdf(context: android.content.Context, booking: Booking) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    try {
        var y = 60f

        // Draw header
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#0066CC") // Deep Blue theme color
        canvas.drawText("MZK Enterprises", 40f, y, paint)
        
        y += 24f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("TAX INVOICE / RECEIPTS", 40f, y, paint)

        // GST info
        y += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("GSTIN: 10MNZPK8219F1ZH", 40f, y, paint)

        y += 18f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.DKGRAY
        canvas.drawText("Address: Dalkhola 3 Stand, Near Railway Station, PIN 733201", 40f, y, paint)

        y += 15f
        canvas.drawText("Phone: 8540888704 | mzkinterprises3@gmail.com", 40f, y, paint)

        // Draw separator
        y += 20f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        // Invoice details
        y += 25f
        paint.textSize = 11f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Invoice Details:", 40f, y, paint)

        paint.isFakeBoldText = false
        paint.textSize = 10f
        y += 18f
        canvas.drawText("Invoice No: MZK/2026/0${booking.id}", 40f, y, paint)
        y += 15f
        canvas.drawText("Dated: ${booking.scheduledDate}", 40f, y, paint)
        y += 15f
        canvas.drawText("Client Name: ${booking.customerName}", 40f, y, paint)
        y += 15f
        canvas.drawText("Client Phone: ${booking.customerPhone}", 40f, y, paint)

        // Draw separator
        y += 20f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        // Service Table header
        y += 25f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Description", 40f, y, paint)
        canvas.drawText("Total", 480f, y, paint)

        y += 10f
        paint.color = android.graphics.Color.GRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        // Service Table row
        y += 25f
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(booking.serviceType, 40f, y, paint)
        paint.isFakeBoldText = true
        canvas.drawText("INR ${booking.finalPrice.toInt()}", 480f, y, paint)

        // Draw separator
        y += 25f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        // Calculations
        val priceBeforeGst = booking.finalPrice / 1.18
        val gstPart = booking.finalPrice - priceBeforeGst
        
        paint.isFakeBoldText = false
        paint.textSize = 10f
        y += 20f
        canvas.drawText("Taxable Base Amount (excl. CGST & SGST):", 40f, y, paint)
        canvas.drawText("INR ${priceBeforeGst.toInt()}", 480f, y, paint)
        
        y += 15f
        canvas.drawText("CGST @ 9%:", 40f, y, paint)
        canvas.drawText("INR ${(gstPart / 2).toInt()}", 480f, y, paint)
        
        y += 15f
        canvas.drawText("SGST @ 9%:", 40f, y, paint)
        canvas.drawText("INR ${(gstPart / 2).toInt()}", 480f, y, paint)

        y += 15f
        canvas.drawText("Payment Status:", 40f, y, paint)
        paint.color = android.graphics.Color.parseColor("#22C55E")
        paint.isFakeBoldText = true
        canvas.drawText(booking.paymentStatus, 480f, y, paint)

        y += 25f
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 13f
        canvas.drawText("Total Outflow Paid:", 40f, y, paint)
        paint.color = android.graphics.Color.parseColor("#0066CC")
        canvas.drawText("INR ${booking.finalPrice.toInt()}", 480f, y, paint)

        // Draw signatures
        if (booking.customerSignature != null) {
            y += 45f
            paint.textSize = 10f
            paint.color = android.graphics.Color.GRAY
            paint.isFakeBoldText = true
            canvas.drawText("Digital Signature Receipt:", 40f, y, paint)
            
            val signatureStr = booking.customerSignature
            if (signatureStr != "Signed_Blank" && signatureStr.isNotBlank()) {
                val signaturePaint = Paint().apply {
                    color = android.graphics.Color.BLUE
                    strokeWidth = 2.5f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                val sigPath = android.graphics.Path()
                try {
                    val tokens = signatureStr.split(";")
                    var isFirst = true
                    for (token in tokens) {
                        val coords = token.split(",")
                        if (coords.size == 2) {
                            val px = coords[0].toFloat() * 0.5f + 40f
                            val py = coords[1].toFloat() * 0.5f + y + 15f
                            if (isFirst) {
                                sigPath.moveTo(px, py)
                                isFirst = false
                            } else {
                                sigPath.lineTo(px, py)
                            }
                        }
                    }
                    canvas.drawPath(sigPath, signaturePaint)
                    y += 50f
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                y += 18f
                paint.textSize = 10f
                paint.isFakeBoldText = false
                paint.color = android.graphics.Color.DKGRAY
                canvas.drawText("[Signed digitally (No graphic details)]", 40f, y, paint)
                y += 15f
            }
        }

        // Footer note
        y += 60f
        paint.textSize = 9f
        paint.color = android.graphics.Color.GRAY
        paint.isFakeBoldText = false
        canvas.drawText("Thank you for choosing MZK Enterprises AC Portal. This is an electronically generated invoice.", 40f, y, paint)

    } catch (e: Exception) {
        e.printStackTrace()
    }

    pdfDocument.finishPage(page)

    val fileName = "Invoice_MZK_2026_0${booking.id}.pdf"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    
    try {
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()
        
        Toast.makeText(context, "Invoice PDF successfully generated!\nSaved in: Downloads/${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to download PDF invoice: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: MZKViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Home, 1: Shop, 2: My Bookings, 3: Account/Referral
    val bookings by viewModel.allBookings.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    
    // OTP State Variables
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpPhoneInput by remember { mutableStateOf("8540888704") }
    var isVerified by remember { mutableStateOf(true) } // Ready for demo
    
    // Service Booking States
    var selectedServiceForBooking by remember { mutableStateOf<String?>(null) }
    var serviceBasePrice by remember { mutableStateOf(0.0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MZKCardLight,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MZKPrimaryBlue
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "AC Store") },
                    label = { Text("AC Store", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MZKPrimaryBlue
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { 
                        BadgedBox(
                            badge = {
                                val pendingOrActive = bookings.filter { it.status != "Completed" && it.status != "Cancelled" }
                                if (pendingOrActive.isNotEmpty()) {
                                    Badge(containerColor = MZKPrimaryBlue, contentColor = Color.White) { Text("${pendingOrActive.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Bookings")
                        }
                    },
                    label = { Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MZKPrimaryBlue
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Referrals") },
                    label = { Text("Refer & Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MZKPrimaryBlue
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MZKDarkBackground)
        ) {
            when (activeTab) {
                0 -> CustomerHomeTab(
                    viewModel = viewModel,
                    onBookService = { service, price ->
                        selectedServiceForBooking = service
                        serviceBasePrice = price
                    },
                    onSwitchTab = { tab -> activeTab = tab }
                )
                1 -> CustomerShopTab(
                    viewModel = viewModel,
                    products = products,
                    onSwitchTab = { tab -> activeTab = tab }
                )
                2 -> CustomerBookingsTab(
                    viewModel = viewModel,
                    bookings = bookings,
                    onSwitchTab = { tab -> activeTab = tab }
                )
                3 -> CustomerProfileTab(
                    viewModel = viewModel,
                    onTriggerLogin = { showOtpDialog = true },
                    onSwitchTab = { tab -> activeTab = tab }
                )
            }

            // OTP Login Modal overlay
            if (showOtpDialog) {
                OtpVerificationOverlay(
                    initialPhone = otpPhoneInput,
                    onDismiss = { showOtpDialog = false },
                    onVerified = { verifiedPhone, verifiedName ->
                        viewModel.updateProfile(verifiedName, "mzkinterprises3@gmail.com", verifiedPhone)
                        isVerified = true
                        showOtpDialog = false
                    }
                )
            }

            // Service Booking Modal Form
            if (selectedServiceForBooking != null) {
                BookingFormDialog(
                    serviceType = selectedServiceForBooking!!,
                    basePrice = serviceBasePrice,
                    viewModel = viewModel,
                    onDismiss = { selectedServiceForBooking = null }
                )
            }
        }
    }
}

// ---------------------- HOME TAB ----------------------
@Composable
fun CustomerHomeTab(
    viewModel: MZKViewModel,
    onBookService: (String, Double) -> Unit,
    onSwitchTab: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    var serviceSearchQuery by remember { mutableStateOf("") }
    val technicians by viewModel.allTechnicians.collectAsState(initial = emptyList())
    
    val serviceOfferings = listOf(
        ServiceItem("AC Installation", 1499.0, Icons.Default.AddCircle, "Split/Window structural unit setup"),
        ServiceItem("AC Uninstallation", 699.0, Icons.Default.Cancel, "Safe dismount & gas lock-down"),
        ServiceItem("AC Repair", 999.0, Icons.Default.Build, "Fixing cooling loss/leak errors"),
        ServiceItem("AC Gas Charging", 2499.0, Icons.Default.OfflineBolt, "Refill refrigerant R32/R410"),
        ServiceItem("AC Deep Cleaning", 1199.0, Icons.Default.Opacity, "Jet pump intensive foaming split wash"),
        ServiceItem("AC Maintenance", 799.0, Icons.Default.Settings, "Routine filters check & airflow tuning"),
        ServiceItem("Annual Contract (AMC)", 4999.0, Icons.Default.Star, "Yearlong preventive service protection"),
        ServiceItem("Emergency AC Repair", 1499.0, Icons.Default.Warning, "Urgent troubleshooting under 2 hours"),
        ServiceItem("Commercial HVAC", 3499.0, Icons.Default.Business, "Complex VRF, Cassette, Duct systems"),
        ServiceItem("Cassette AC Service", 2999.0, Icons.Default.Refresh, "Ceiling mount cassette wash"),
        ServiceItem("Ductable AC Service", 3999.0, Icons.Default.GridOn, "Centralized ductwork maintenance"),
        ServiceItem("Window AC Service", 699.0, Icons.Default.Web, "Fast filter and condenser jet spray"),
        ServiceItem("Split AC Jet Service", 889.0, Icons.Default.Waves, "Deep jet-pressure indoor cleaning")
    )

    val filteredServices = serviceOfferings.filter {
        it.name.contains(serviceSearchQuery, ignoreCase = true) ||
        it.desc.contains(serviceSearchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Section Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MZKPrimaryBlue, MZKDarkBackground)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MZK Enterprises",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "GST No. 10MNZPK8219F1ZH",
                            color = MZKAccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = { onSwitchTab(1) }, // Go to Shop
                        colors = ButtonDefaults.buttonColors(containerColor = MZKAccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("AC Store 🛒", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Professional AC Service, Repair, Installation & AC Marketplace",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trusted Doorstep AC Solutions for Homes and Businesses",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { /* Jump scroll directly to services grid */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Book Service", color = MZKDarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onSwitchTab(1) },
                        border = BorderStroke(1.dp, Color.White),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Buy / Sell ACs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                TextField(
                    value = serviceSearchQuery,
                    onValueChange = { serviceSearchQuery = it },
                    placeholder = { Text("Search services (e.g. gas, repair, install)...", fontSize = 13.sp, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A)
                    ),
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Search services", 
                            tint = MZKPrimaryBlue
                        ) 
                    },
                    trailingIcon = {
                        if (serviceSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { serviceSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear, 
                                    contentDescription = "Clear search", 
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Tags:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    val tags = listOf("Gas", "Repair", "Installation", "Clean", "AMC", "Split", "Window")
                    tags.forEach { tag ->
                        val isSelected = serviceSearchQuery.equals(tag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MZKAccentOrange else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        serviceSearchQuery = ""
                                    } else {
                                        serviceSearchQuery = tag
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Trust Indicators Horizontal Panel
        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-14).dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MZKCardLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrustFeatureItem(Icons.Default.VerifiedUser, "Verified Experts")
                TrustFeatureItem(Icons.Default.EventAvailable, "Same-Day Visits")
                TrustFeatureItem(Icons.Default.PriceCheck, "Clear Pricing")
                TrustFeatureItem(Icons.Default.OfflinePin, "Genuine Spares")
            }
        }

        // Services Menu Grid Title
        Text(
            text = if (serviceSearchQuery.isEmpty()) "Select Doorstep AC Service" else "Search Results for '$serviceSearchQuery'",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
        )

        if (filteredServices.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "No results",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No services matching '$serviceSearchQuery'",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for installation, repair, cleaning, gas, amc etc.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Column with grouped Services
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredServices.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { item ->
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHovered by interactionSource.collectIsHoveredAsState()
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val isHoveredOrPressed = isHovered || isPressed

                            val scale by animateFloatAsState(
                                targetValue = if (isHoveredOrPressed) 1.05f else 1.0f,
                                animationSpec = tween(durationMillis = 200),
                                label = "scale"
                            )
                            val borderColor by animateColorAsState(
                                targetValue = if (isHoveredOrPressed) MZKPrimaryBlue else (if (isDarkThemeGlobal) Color(0xFF222222) else Color(0xFFE2E8F0)),
                                animationSpec = tween(durationMillis = 200),
                                label = "borderColor"
                            )
                            val bgColor by animateColorAsState(
                                targetValue = if (isHoveredOrPressed) (if (isDarkThemeGlobal) Color(0xFF0E2544) else Color(0xFFE6F0FF)) else MZKCardLight,
                                animationSpec = tween(durationMillis = 200),
                                label = "bgColor"
                            )
                            val iconBgColor by animateColorAsState(
                                targetValue = if (isHoveredOrPressed) MZKPrimaryBlue.copy(alpha = 0.25f) else (if (isDarkThemeGlobal) Color(0xFF0A1E36) else Color(0xFFE6F0FF)),
                                animationSpec = tween(durationMillis = 200),
                                label = "iconBgColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .background(bgColor, RoundedCornerShape(12.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = LocalIndication.current,
                                        onClick = { onBookService(item.name, item.price) }
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(iconBgColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, contentDescription = item.name, tint = MZKPrimaryBlue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(item.name, fontWeight = FontWeight.Bold, color = if (isDarkThemeGlobal) Color.White else Color(0xFF0F172A), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(item.desc, color = if (isDarkThemeGlobal) Color.LightGray else Color.DarkGray, fontSize = 11.sp, lineHeight = 13.sp, maxLines = 2)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("₹${item.price.toInt()} up", fontWeight = FontWeight.ExtraBold, color = if (isDarkThemeGlobal) Color.White else Color(0xFF0F172A), fontSize = 13.sp)
                                        Text("Book ›", color = MZKPrimaryBlue, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        // Handle odd item alignment layout padding
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // WhatsApp Direct Link and Contact Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCF8C6)), // Soft green WhatsApp color
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Contact MZK",
                        tint = Color(0xFF075E54),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WhatsApp & Customer Support",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF075E54),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Dalkhola: 8540888704 (Direct Helpdesk)",
                            fontSize = 12.sp,
                            color = Color(0xFF075E54)
                        )
                    }
                }
                Button(
                    onClick = { /* Simulated WhatsApp Launch */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF075E54)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Chat Now", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Dalkhola Interactive Service Coverage Area Map
        val textColorVal = if (isDarkThemeGlobal) Color.White else Color(0xFF0F172A)
        val subTextColorVal = if (isDarkThemeGlobal) Color.LightGray else Color(0xFF475569)

        Text(
            text = "Service Coverage Area Map",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = textColorVal,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp)
        )
        
        // Setup simple pulse animations for our interactive map
        val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
        val mapPulseRadius by infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = 60f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radiusAnim"
        )
        val mapPulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "alphaAnim"
        )

        // State for selected coverage zone view
        var selectedMapSector by remember { mutableStateOf(0) } // 0: All, 1: Core (0-5km), 2: Suburban (5-10km), 3: Border (10-15km)
        
        // Postcode check state
        var searchPostcode by remember { mutableStateOf("") }
        var checkResultText by remember { mutableStateOf<String?>(null) }
        var checkResultStatus by remember { mutableStateOf<String?>(null) } // "green", "orange", "red"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MZKCardLight),
            border = BorderStroke(1.dp, if (isDarkThemeGlobal) Color(0xFF222222) else Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                
                // HEADER CAPTION OF THE MAP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TravelExplore,
                            contentDescription = "Coverage Map",
                            tint = MZKPrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interactive Coverage Radar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textColorVal
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "9 Active Techs Live",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                }

                // THE ANIMATED CUSTOM MAP CANVAS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(if (isDarkThemeGlobal) Color(0xFF060914) else Color(0xFFEDF2F7)) // stylized grid dark/light base
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        // Center is MZK Office in Dalkhola (PIN 733201)
                        val cx = w * 0.5f
                        val cy = h * 0.5f

                        // Draw Grid lines representing latitude/longitude
                        val gridPaintVal = if (isDarkThemeGlobal) Color(0xFF111827) else Color(0xFFE2E8F0)
                        for (i in 1..8) {
                            val xPos = w * (i / 9f)
                            drawLine(gridPaintVal, Offset(xPos, 0f), Offset(xPos, h), strokeWidth = 1f)
                        }
                        for (i in 1..5) {
                            val yPos = h * (i / 6f)
                            drawLine(gridPaintVal, Offset(0f, yPos), Offset(w, yPos), strokeWidth = 1f)
                        }

                        // Draw River Mahananda (Sleek curving waterway across the region)
                        val riverPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h * 0.15f)
                            cubicTo(w * 0.3f, h * 0.2f, w * 0.4f, h * 0.8f, w, h * 0.85f)
                        }
                        drawPath(
                            path = riverPath,
                            color = if (isDarkThemeGlobal) Color(0xFF0F2537) else Color(0xFFD0E1FD),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Draw Highways passing Dalkhola crossing
                        // Highway NH-31 (Horizontal East-West)
                        val highwayColor = if (isDarkThemeGlobal) Color(0xFF1F2937) else Color(0xFFCBD5E1)
                        drawLine(
                            color = highwayColor,
                            start = Offset(0f, cy + 10f),
                            end = Offset(w, cy - 20f),
                            strokeWidth = 10f
                        )
                        // Highway NH-34 (Branching South/North)
                        drawLine(
                            color = highwayColor,
                            start = Offset(cx - 50f, 0f),
                            end = Offset(cx + 80f, h),
                            strokeWidth = 10f
                        )

                        // Railway tracks symbol (Vertical line with cross ticks)
                        val railColorVal = if (isDarkThemeGlobal) Color(0xFF2E3E5C) else Color(0xFF94A3B8)
                        val rx1 = cx * 0.25f
                        drawLine(
                            color = railColorVal,
                            start = Offset(rx1, 0f),
                            end = Offset(rx1 + 30f, h),
                            strokeWidth = 4f
                        )
                        for (i in 0..10) {
                            val ry = h * (i / 10f)
                            val rx = rx1 + (30f * (i / 10f))
                            drawLine(
                                color = railColorVal,
                                start = Offset(rx - 8f, ry),
                                end = Offset(rx + 8f, ry + 4f),
                                strokeWidth = 2f
                            )
                        }

                        // Drawing concentric service range boundaries around Dalkhola
                        val coreRadius = w * 0.12f
                        val suburbanRadius = w * 0.26f
                        val extendedRadius = w * 0.42f

                        // Draw glowing pulsing coverage rings from our animated states
                        drawCircle(
                            color = Color(0xFF0066CC).copy(alpha = mapPulseAlpha),
                            radius = mapPulseRadius * 2.5f + 10f,
                            center = Offset(cx, cy)
                        )

                        // Draw zone boundary rings with descriptive limits
                        // Core Ring (Green dashed)
                        drawCircle(
                            color = if (selectedMapSector == 1) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.3f),
                            radius = coreRadius,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                        // Suburban Ring (Blue)
                        drawCircle(
                            color = if (selectedMapSector == 2) Color(0xFF0066CC) else Color(0xFF0066CC).copy(alpha = 0.25f),
                            radius = suburbanRadius,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                        )
                        // Outer Border Ring (Orange)
                        drawCircle(
                            color = if (selectedMapSector == 3) Color(0xFFF59E0B) else Color(0xFFF59E0B).copy(alpha = 0.18f),
                            radius = extendedRadius,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )

                        // Draw Translucent overlay of the SELECTED Area
                        when (selectedMapSector) {
                            1 -> drawCircle(
                                color = Color(0xFF10B981).copy(alpha = 0.13f),
                                radius = coreRadius,
                                center = Offset(cx, cy)
                            )
                            2 -> drawCircle(
                                color = Color(0xFF0066CC).copy(alpha = 0.11f),
                                radius = suburbanRadius,
                                center = Offset(cx, cy)
                            )
                            3 -> drawCircle(
                                color = Color(0xFFF59E0B).copy(alpha = 0.08f),
                                radius = extendedRadius,
                                center = Offset(cx, cy)
                            )
                        }

                        // Active Technicians floating dispatch dots showing dynamic visual
                        // Tech Salim (Local)
                        drawCircle(Color(0xFF22C55E), 7f, Offset(cx - 20f, cy + 15f))
                        drawCircle(Color(0xFF22C55E).copy(alpha = 0.35f), 13f, Offset(cx - 20f, cy + 15f))

                        // Tech Subodh (Karandighi Highway)
                        drawCircle(Color(0xFF22C55E), 7f, Offset(cx + 45f, cy - 65f))
                        drawCircle(Color(0xFF22C55E).copy(alpha = 0.35f), 13f, Offset(cx + 45f, cy - 65f))

                        // Tech Vinod (Border outskirts)
                        drawCircle(Color(0xFFF59E0B), 6f, Offset(cx - 100f, cy - 35f))

                        // MZK enterprises main headquarters pin at center Dalkhola crossing
                        drawCircle(Color.Red, 11f, Offset(cx, cy))
                        drawCircle(Color.White, 4f, Offset(cx, cy))
                    }

                    // Floating text label for Center Headquarters
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-24).dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Dalkhola Base (Center point)",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Geographic Labels
                    Text(
                        text = "NH-31 Corridor",
                        color = if (isDarkThemeGlobal) Color.DarkGray else Color.Gray,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 65.dp)
                    )

                    Text(
                        text = "Kishanganj (15km)",
                        color = if (isDarkThemeGlobal) Color(0xFF4B5563) else Color(0xFF94A3B8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 25.dp)
                    )
                    
                    Text(
                        text = "Karandighi (10km)",
                        color = if (isDarkThemeGlobal) Color(0xFF4B5563) else Color(0xFF94A3B8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 36.dp, bottom = 12.dp)
                    )
                }

                // SEC SECTOR SELECTOR CHIPS
                Text(
                    text = "Select Range to Inspect:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subTextColorVal,
                    modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sectorNames = listOf("All Limits", "Core (0-5 km)", "Suburbs (5-10 km)", "Outer (10-15 km)")
                    sectorNames.forEachIndexed { index, name ->
                        val isSelected = selectedMapSector == index
                        val chipBg = if (isSelected) MZKPrimaryBlue else (if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        val chipTextCol = if (isSelected) Color.White else textColorVal
                        
                        Box(
                            modifier = Modifier
                                .background(chipBg, RoundedCornerShape(8.dp))
                                .clickable { selectedMapSector = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = chipTextCol
                            )
                        }
                    }
                }

                // DYNAMIC COVERAGE DETAIL CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .background(
                            color = if (isDarkThemeGlobal) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        when (selectedMapSector) {
                            1 -> {
                                Text("📍 Core Local Coverage Range (Green)", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF10B981))
                                Text("• Areas: Dalkhola Local, Dalkhola 3 Stand, High School Para, Station Road, Loknathpur.", fontSize = 11.sp, color = subTextColorVal, lineHeight = 14.sp)
                                Text("• Lead Dispatch Duration: superfast (within 30 - 45 mins).", fontSize = 11.sp, color = textColorVal, fontWeight = FontWeight.Bold)
                                Text("• Transit / Courier Expense: ₹0 (100% Free home visits).", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                            2 -> {
                                Text("🚗 Suburban Corridor Limits (Blue)", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MZKAccentBlue)
                                Text("• Areas: Karandighi, Kharba, Domna, NH-31, NH-34 highway toll radius, local bypass commercial blocks.", fontSize = 11.sp, color = subTextColorVal, lineHeight = 14.sp)
                                Text("• Lead Dispatch Duration: within 1 to 2 hours of booking confirmation.", fontSize = 11.sp, color = textColorVal, fontWeight = FontWeight.Bold)
                                Text("• Transit / Courier Expense: Standard ₹99 outer transit fee.", fontSize = 11.sp, color = MZKAccentBlue, fontWeight = FontWeight.Bold)
                            }
                            3 -> {
                                Text("🏢 Outer Border Cities Limit (Orange)", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFFF59E0B))
                                Text("• Areas: Kishanganj borders, Surajpur limits, Chakulia border, outer 15km ring perimeter.", fontSize = 11.sp, color = subTextColorVal, lineHeight = 14.sp)
                                Text("• Lead Dispatch Duration: standard scheduling (2.5 to 4 hours maximum delay).", fontSize = 11.sp, color = textColorVal, fontWeight = FontWeight.Bold)
                                Text("• Transit / Courier Expense: ₹199 geographical outer dispatch fee applies.", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                            }
                            else -> {
                                Text("🗺️ Comprehensive MZK Network Outline Map", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MZKPrimaryBlue)
                                Text("MZK Enterprises operates the fastest cooling repair dispatch network within 15 KM of Dalkhola 3 Stand crossing, handling Split & Cassette AC installations with expert engineers.", fontSize = 11.sp, color = subTextColorVal, lineHeight = 15.sp)
                                Text("Click any specific range tag layout above to preview exact transit schedules, real times, and pricing formulas.", fontSize = 11.sp, color = MZKPrimaryBlue, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp)
                            }
                        }
                    }
                }

                Divider(color = if (isDarkThemeGlobal) Color(0xFF222222) else Color(0xFFE2E8F0))

                // INTERACTIVE PIN CODE COVERAGE VALIDATOR TOOL
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔎 Quick Coverage Checker by Postcode/PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textColorVal
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchPostcode,
                            onValueChange = { input -> 
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    searchPostcode = input
                                }
                            },
                            placeholder = { Text("Enter 6-Digit PIN (e.g. 733201)", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColorVal,
                                unfocusedTextColor = textColorVal,
                                focusedBorderColor = MZKPrimaryBlue,
                                unfocusedBorderColor = if (isDarkThemeGlobal) Color(0xFF334155) else Color(0xFFCBD5E1),
                                focusedPlaceholderColor = Color.Gray,
                                unfocusedPlaceholderColor = Color.Gray
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Button(
                            onClick = {
                                if (searchPostcode.length < 6) {
                                    checkResultText = "Please type a complete 6-digit Indian postcode."
                                    checkResultStatus = "orange"
                                    return@Button
                                }
                                when (searchPostcode) {
                                    "733201" -> {
                                        checkResultText = "✅ Super Coverage in Dalkhola Base! Dispatch trucks can arrive within 30-45 minutes. Visit Charge: Free."
                                        checkResultStatus = "green"
                                    }
                                    "733215" -> {
                                        checkResultText = "✅ Covered: Karandighi Suburban limits! Regular dispatch arrives within 1.5 - 2 hours. Normal transit fee applies."
                                        checkResultStatus = "green"
                                    }
                                    "733156" -> {
                                        checkResultText = "✅ Covered: Chakulia / surrounding suburbs. Average dispatch window is 2.5 hours. Outer Transit fee ₹99."
                                        checkResultStatus = "green"
                                    }
                                    "855107", "855108" -> {
                                        checkResultText = "✅ Covered: Kishanganj outer margins! Dispatch scheduled easily (within 3-4 hours). Outer Transit fee ₹199."
                                        checkResultStatus = "orange"
                                    }
                                    else -> {
                                        checkResultText = "❌ Outer range boundary: Special service query is required. Call 8540888704 for custom booking estimates."
                                        checkResultStatus = "red"
                                    }
                                }
                            },
                            modifier = Modifier.height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Check Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Dynamically Show Checker Results
                    AnimatedVisibility(
                        visible = checkResultText != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        checkResultText?.let { msg ->
                            val alertColor = when (checkResultStatus) {
                                "green" -> Color(0xFF10B981)
                                "orange" -> Color(0xFFF59E0B)
                                "red" -> Color(0xFFEF4444)
                                else -> textColorVal
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(alertColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .border(1.dp, alertColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = when (checkResultStatus) {
                                            "green" -> Icons.Default.CheckCircle
                                            "orange" -> Icons.Default.Warning
                                            else -> Icons.Default.HelpOutline
                                        },
                                        contentDescription = "Status icon",
                                        tint = alertColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        color = textColorVal,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Meet Our Technicians Section
        val homeThemeTextColor = if (isDarkThemeGlobal) Color.White else Color(0xFF0F172A)
        val homeThemeSubTextColor = if (isDarkThemeGlobal) Color.LightGray else Color(0xFF475569)

        Text(
            text = "Meet Our Certified Technicians 🛠️",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = homeThemeTextColor,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
        )
        Text(
            text = "Top-rated background-verified field experts ready to deploy across Dalkhola dynamically.",
            fontSize = 11.sp,
            color = homeThemeSubTextColor,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        val techDetails = remember {
            mapOf(
                101L to Triple("Cooling & Airflow Pro", "5 Years", "Daikin & Voltas Expert"),
                102L to Triple("Gas Refills & Leak Detection", "7 Years", "HVAC Master Tech"),
                103L to Triple("Cassette & Commercial Systems", "4 Years", "Ductable AC Specialist"),
                104L to Triple("Jet Cleaning & Drainage Expert", "6 Years", "Multi-brand Certified Specialist")
            )
        }

        if (technicians.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Retrieving verified field staff...", color = Color.Gray, fontSize = 11.sp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                technicians.forEach { tech ->
                    val details = techDetails[tech.id] ?: Triple("Air Conditioning Specialist", "3 Years", "Certified Grade-A Installer")
                    val initials = if (tech.name.contains(" ")) {
                        val parts = tech.name.split(" ")
                        "${parts[0].take(1).uppercase()}${parts.getOrNull(1)?.take(1)?.uppercase() ?: ""}"
                    } else {
                        tech.name.take(2).uppercase()
                    }

                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .border(
                                width = 1.dp,
                                color = if (isDarkThemeGlobal) Color(0xFF222B3E) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MZKCardLight),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top row: Avatar & Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MZKPrimaryBlue.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            color = MZKPrimaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tech.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = homeThemeTextColor
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "RatingStar",
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${tech.rating} / 5.0",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = homeThemeTextColor
                                            )
                                        }
                                    }
                                }
                                
                                // Status Indicator Pill
                                val isAvailable = tech.status.equals("Available", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isAvailable) "Active" else "Busy",
                                        color = if (isAvailable) Color(0xFF15803D) else Color(0xFFB91C1C),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = if (isDarkThemeGlobal) Color(0xFF1E293B) else Color(0xFFF1F5F9), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Speciality & Experience details
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = "Verified Tech",
                                    tint = MZKPrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = details.third,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MZKPrimaryBlue
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = "Expertise: ${details.first}",
                                fontSize = 11.sp,
                                color = homeThemeSubTextColor
                            )
                            Text(
                                text = "Experience: ${details.second} • ${tech.jobsCompleted} tasks completed",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive click action to book services using this expert or show booking dialog
                            Button(
                                onClick = { onBookService("AC Deep Cleaning", 1199.0) },
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue.copy(alpha = 0.15f))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Book",
                                        tint = MZKPrimaryBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Request Appointment", color = MZKPrimaryBlue, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAQs
        val faqTitleColor = if (isDarkThemeGlobal) Color.White else Color(0xFF0F172A)
        Text(
            text = "Frequently Asked Questions",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = faqTitleColor,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp)
        )
        
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FaqItem("Does MZK offer transparent invoices?", "Yes! Every single order receives a digital GST tax receipt matching corporate code 10MNZPK8219F1ZH, with spare parts and labor detailed carefully.")
            FaqItem("Are your technicians background checked?", "Every technician dispatched is background verified, highly skilled, and undergoes regular training on premium split and cassette models.")
            FaqItem("What if something fails post-service?", "We offer an official 30-day customer satisfaction guarantee. If any issues recur, our technicians visit free of cost.")
        }
        
        MZKFooter(onNavigate = onSwitchTab)
    }
}

@Composable
fun TrustFeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Icon(icon, contentDescription = text, tint = MZKPrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 11.sp)
    }
}

@Composable
fun FaqItem(q: String, a: String) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = MZKCardLight),
        border = BorderStroke(0.5.dp, Color(0xFF222222))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(q, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand FAQ",
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = a,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

data class ServiceItem(
    val name: String,
    val price: Double,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val desc: String
)


// ---------------------- SHOP TAB ----------------------
@Composable
fun CustomerShopTab(
    viewModel: MZKViewModel,
    products: List<Product>,
    onSwitchTab: ((Int) -> Unit)? = null
) {
    val filterType by viewModel.storeTypeFilter.collectAsState()
    val filterBrand by viewModel.storeBrandFilter.collectAsState()
    val query by viewModel.storeSearchQuery.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()

    var showBuyDialog by remember { mutableStateOf<Product?>(null) }
    var buyGstInByCustomer by remember { mutableStateOf("") }
    var buySuccessAlert by remember { mutableStateOf(false) }
    var buyPaymentMethodSelection by remember { mutableStateOf("UPI Payment Gateway") }

    val brandsPool = listOf("ALL", "Daikin", "Voltas", "LG", "Samsung", "Blue Star", "Hitachi", "Panasonic", "Carrier")

    Column(modifier = Modifier.fillMaxSize()) {
        // Shop Search and Category Filter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MZKPrimaryBlue)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("AC Store & Exchange Depot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                TextField(
                    value = query,
                    onValueChange = { viewModel.storeSearchQuery.value = it },
                    placeholder = { Text("Search by AC model name...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.storeSearchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }
        }

        // Shop Category Switch Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "NEW", "USED").forEach { type ->
                val selected = filterType == type
                Box(
                    modifier = Modifier
                        .background(
                            color = if (selected) MZKPrimaryBlue else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.storeTypeFilter.value = type }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (type == "ALL") "All Models" else if (type == "NEW") "New AC Unit" else "Certified Used",
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color.DarkGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Horizontal Brand Filter Scroller
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            brandsPool.forEach { brand ->
                val selected = filterBrand == brand
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.storeBrandFilter.value = brand },
                    label = { Text(brand, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Filter algorithm applied locally
        val filteredList = products.filter {
            (filterType == "ALL" || it.type == filterType) &&
            (filterBrand == "ALL" || it.brand.equals(filterBrand, ignoreCase = true)) &&
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.specs.contains(query, ignoreCase = true))
        }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, contentDescription = "No products", tint = Color.LightGray, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Matching AC Units Found", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Try resetting your filters or typing different letters", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (item.type == "NEW") Color(0xFFE0F2FE) else Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (item.type == "NEW") "NEW UNIT" else "CERTIFIED PRE-OWNED",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = if (item.type == "NEW") Color(0xFF0369A1) else Color(0xFFB45309)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    Text(item.specs, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                }
                                
                                IconButton(onClick = { viewModel.toggleWishlist(item.id) }) {
                                    Icon(
                                        imageVector = if (wishlist.contains(item.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Wishlist",
                                        tint = if (wishlist.contains(item.id)) Color.Red else Color.LightGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(item.description, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("₹${item.price.toInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MZKPrimaryBlue)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("₹${item.originalPrice.toInt()}", textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough, fontSize = 12.sp, color = Color.LightGray)
                                    }
                                    Text("Free Installation Included", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { showBuyDialog = item },
                                    colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Purchase Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                item {
                    MZKFooter(onNavigate = onSwitchTab)
                }
            }
        }

        // Purchase Modal Overlay with digital GST receipt quotation preview
        if (showBuyDialog != null) {
            val p = showBuyDialog!!
            val cgst = p.price * 0.09
            val sgst = p.price * 0.09
            val total = p.price + cgst + sgst

            AlertDialog(
                onDismissRequest = { showBuyDialog = null },
                title = { Text("Checkout Confirmation", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("MZK Enterprises billing department will prepare your tax invoice under Indian GST law.", fontSize = 12.sp)
                        
                        Divider()
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("AC Unit cost:", fontSize = 12.sp, color = Color.Gray)
                            Text("₹${p.price.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST (9%):", fontSize = 12.sp, color = Color.Gray)
                            Text("₹${cgst.toInt()}", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST (9%):", fontSize = 12.sp, color = Color.Gray)
                            Text("₹${sgst.toInt()}", fontSize = 12.sp)
                        }
                        Divider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Outflow Due:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("₹${total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MZKPrimaryBlue)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = buyGstInByCustomer,
                            onValueChange = { buyGstInByCustomer = it },
                            label = { Text("Customer GST Number (Optional)") },
                            placeholder = { Text("e.g. 10ABCDE1234F1Z0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Divider()

                        Text("Select Checkout Payment Method", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        val storePaymentOptions = listOf(
                            "UPI / Instant QR Payment" to "UPI Payment Gateway",
                            "Debit Card (All Banks accepted)" to "Debit Card Payment",
                            "Credit Card (Slab-free EMI available)" to "Credit Card Payment",
                            "Cash on Delivery / Installation" to "Cash on Service"
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            storePaymentOptions.forEach { (label, value) ->
                                val isSelected = buyPaymentMethodSelection == value
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MZKPrimaryBlue else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            color = if (isSelected) MZKPrimaryBlue.copy(alpha = 0.05f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { buyPaymentMethodSelection = value }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { buyPaymentMethodSelection = value },
                                        colors = RadioButtonDefaults.colors(selectedColor = MZKPrimaryBlue)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val iconImage = when (value) {
                                        "UPI Payment Gateway" -> Icons.Default.QrCodeScanner
                                        "Debit Card Payment", "Credit Card Payment" -> Icons.Default.CreditCard
                                        else -> Icons.Default.Payments
                                    }
                                    Icon(
                                        imageVector = iconImage,
                                        contentDescription = null,
                                        tint = if (isSelected) MZKPrimaryBlue else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = label, fontSize = 11.sp, color = Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text("Company Invoice Provider: MZK Enterprises (GSTIN: 10MNZPK8219F1ZH)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createBooking(
                                customerName = viewModel.loggedInName.value,
                                customerPhone = viewModel.loggedInPhone.value,
                                customerEmail = viewModel.loggedInEmail.value,
                                serviceType = "Marketplace purchase: ${p.name}",
                                acBrand = p.brand,
                                scheduledDate = "Tomorrow",
                                scheduledTime = "10 AM - 1 PM",
                                address = "Default address",
                                notes = "Product Sale invoice direct. Cust GST: $buyGstInByCustomer",
                                price = total,
                                paymentMethod = buyPaymentMethodSelection
                            )
                            buySuccessAlert = true
                            showBuyDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue)
                    ) {
                        Text("Secure Pay & Deliver")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBuyDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (buySuccessAlert) {
            AlertDialog(
                onDismissRequest = { buySuccessAlert = false },
                title = { Text("Order Placed Successfully!") },
                text = { Text("Thank you for choosing MZK Enterprises. A delivery coordinator and technician is scheduled to visit your home tomorrow with the unit. Details entered on 'Bookings' tab.") },
                confirmButton = {
                    Button(onClick = { buySuccessAlert = false }) {
                        Text("Excellent")
                    }
                }
            )
        }
    }
}


// --- CUSTOM CODE SCROLLER EXTENSION ---
@Composable
fun FilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) MZKAccentBlue else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (selected) MZKPrimaryBlue else Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        label()
    }
}


// ---------------------- BOOKINGS TAB ----------------------
@Composable
fun CustomerBookingsTab(
    viewModel: MZKViewModel,
    bookings: List<Booking>,
    onSwitchTab: ((Int) -> Unit)? = null
) {
    var showReviewModal by remember { mutableStateOf<Booking?>(null) }
    var reviewRating by remember { mutableStateOf(5.0f) }
    var reviewText by remember { mutableStateOf("") }
    
    var showInvoiceDetail by remember { mutableStateOf<Booking?>(null) }
    
    // Firestore Setup State Connection Controller
    var showFirebaseSetupHelp by remember { mutableStateOf(false) }
    var inputProjectId by remember { mutableStateOf(com.example.data.repository.FirestoreSyncManager.customProjectId) }
    var inputApiKey by remember { mutableStateOf(com.example.data.repository.FirestoreSyncManager.customApiKey) }
    var inputAppId by remember { mutableStateOf(com.example.data.repository.FirestoreSyncManager.customAppId) }
    
    val context = LocalContext.current
    val isFirestoreActive = com.example.data.repository.FirestoreSyncManager.isCloudEnabled
    
    val myPersonalBookings = bookings.filter { it.customerPhone == viewModel.loggedInPhone.value }
    val activeBookings = myPersonalBookings.filter { it.status != "Completed" && it.status != "Cancelled" }
    val pastBookings = myPersonalBookings.filter { it.status == "Completed" || it.status == "Cancelled" }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MZKPrimaryBlue)
                .padding(16.dp)
        ) {
            Text("Service History & Active Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Firebase Sync Status Tracker Control Board
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFirestoreActive) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isFirestoreActive) Color(0xFFDCF8C6) else Color(0xFFE2E8F0)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isFirestoreActive) Color(0xFF22C55E) else Color(0xFFF59E0B),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFirestoreActive) "Firebase Real-time Sync: Enabled" else "Local SQLite Engine Active (Firestore Offline)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirestoreActive) Color(0xFF14532D) else Color(0xFF334155)
                        )
                    }
                    Text(
                        text = if (showFirebaseSetupHelp) "Close Config" else "Cloud Config",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MZKPrimaryBlue,
                        modifier = Modifier.clickable { showFirebaseSetupHelp = !showFirebaseSetupHelp }
                    )
                }
                
                if (showFirebaseSetupHelp) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Supply direct Google Cloud Firestore connection specs below. If empty, the app compiles beautifully out-of-the-box and runs on its local SQL tracking engine.",
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputProjectId,
                        onValueChange = { inputProjectId = it },
                        label = { Text("Firestore Project ID", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        label = { Text("Firebase API Key", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = inputAppId,
                        onValueChange = { inputAppId = it },
                        label = { Text("Firebase App ID", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            com.example.data.repository.FirestoreSyncManager.customProjectId = inputProjectId
                            com.example.data.repository.FirestoreSyncManager.customApiKey = inputApiKey
                            com.example.data.repository.FirestoreSyncManager.customAppId = inputAppId
                            com.example.data.repository.FirestoreSyncManager.initialize(context, force = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Connect Firebase Client", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (com.example.data.repository.FirestoreSyncManager.connectionError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Error: ${com.example.data.repository.FirestoreSyncManager.connectionError}",
                            color = Color.Red,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ----------------- DYNAMIC DISPATCH & STATUS NOTIFICATION CENTER -----------------
        val notificationsList by viewModel.notifications.collectAsState()
        val unreadNotifCount = notificationsList.count { !it.isRead }
        var isNotifExpanded by remember { mutableStateOf(unreadNotifCount > 0) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (unreadNotifCount > 0) Color(0xFFF0F9FF) else Color(0xFFF8FAFC)
            ),
            border = BorderStroke(
                width = 1.2.dp,
                color = if (unreadNotifCount > 0) MZKPrimaryBlue.copy(alpha = 0.6f) else Color(0xFFE2E8F0)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isNotifExpanded = !isNotifExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (unreadNotifCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Notification Center",
                                tint = if (unreadNotifCount > 0) MZKPrimaryBlue else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                            if (unreadNotifCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(14.dp)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadNotifCount.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dispatch & Status Alerts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (unreadNotifCount > 0) MZKPrimaryBlue else Color(0xFF1E293B)
                            )
                            Text(
                                text = if (notificationsList.isEmpty()) "No recent dispatch updates" else "$unreadNotifCount unread messages",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (notificationsList.isNotEmpty() && !isNotifExpanded) {
                            Box(
                                modifier = Modifier
                                    .background(MZKPrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Click to view", fontSize = 8.sp, color = MZKPrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Icon(
                            imageVector = if (isNotifExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Panel",
                            tint = Color.Gray
                        )
                    }
                }

                // Expanded Section
                if (isNotifExpanded) {
                    if (notificationsList.isEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "You do not have any recent notifications yet. Once you book a doorstep clean or repair, you'll receive real-time dispatcher alerts here.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Notifications list (Scrollable if there are many)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())
                        ) {
                            notificationsList.forEach { notification ->
                                val tileBg = if (notification.isRead) Color.Transparent else MZKPrimaryBlue.copy(alpha = 0.05f)
                                val borderCol = if (notification.isRead) Color.Transparent else MZKPrimaryBlue.copy(alpha = 0.15f)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(tileBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val iconInfo = when {
                                        notification.title.contains("Dispatched", ignoreCase = true) || notification.title.contains("Assigned", ignoreCase = true) -> {
                                            Icons.Default.LocalShipping to Color(0xFF3B82F6)
                                        }
                                        notification.title.contains("Completed", ignoreCase = true) -> {
                                            Icons.Default.CheckCircle to Color(0xFF10B981)
                                        }
                                        notification.title.contains("Progress", ignoreCase = true) || notification.title.contains("Started", ignoreCase = true) -> {
                                            Icons.Default.Build to Color(0xFFF59E0B)
                                        }
                                        else -> {
                                            Icons.Default.NotificationsActive to MZKPrimaryBlue
                                        }
                                    }
                                    
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(iconInfo.second.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = iconInfo.first,
                                                contentDescription = null,
                                                tint = iconInfo.second,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = notification.title,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                                if (!notification.isRead) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(modifier = Modifier.size(6.dp).background(Color.Red, CircleShape))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = notification.message,
                                                fontSize = 10.sp,
                                                color = Color.DarkGray,
                                                lineHeight = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Received at ${notification.timestamp}",
                                                fontSize = 8.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { viewModel.clearNotification(notification.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Action Controls
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.markAllNotificationsAsRead() },
                                modifier = Modifier.height(26.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Mark all as read", fontSize = 10.sp, color = MZKPrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                            
                            TextButton(
                                onClick = { isNotifExpanded = false },
                                modifier = Modifier.height(26.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Collapse panel", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        var bookingFilter by remember { mutableStateOf("All") } // "All", "Ongoing", "Past"

        if (myPersonalBookings.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterOptions = listOf(
                    "All" to "All (${myPersonalBookings.size})",
                    "Ongoing" to "Ongoing (${activeBookings.size})",
                    "Past" to "Past Requests (${pastBookings.size})"
                )
                filterOptions.forEach { (key, label) ->
                    val isSelected = bookingFilter == key
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MZKPrimaryBlue else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MZKPrimaryBlue else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { bookingFilter = key }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (myPersonalBookings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Build, contentDescription = "Engine logo", tint = Color.LightGray, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Bookings Created Yet", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("Select a professional service from the 'Home' screen or buy from the 'AC Store' to generate dynamic bookings.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Demo Phone simulated log: ${viewModel.loggedInPhone.value}", fontSize = 10.sp, color = MZKPrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }
                
                MZKFooter(onNavigate = onSwitchTab)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Active Booking Tracker
                if ((bookingFilter == "All" || bookingFilter == "Ongoing") && activeBookings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Service Tracker",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    items(activeBookings) { booking ->
                        val simulations by viewModel.bookingSimulationState.collectAsState()
                        val currentSimStatus = simulations[booking.id]
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.2.dp, if (currentSimStatus != null) MZKPrimaryBlue else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Status Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Active Job #${booking.id}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                        Text(booking.serviceType, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    }
                                    
                                    val statColor = when (booking.status) {
                                        "Pending" -> Color(0xFFF59E0B)
                                        "Confirmed" -> Color(0xFF10B981)
                                        "In Progress" -> Color(0xFF3B82F6)
                                        else -> Color.Gray
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(statColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(booking.status, color = statColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 10.dp))

                                // Real-Time Status Steps visualizer
                                Text("Real-time Tracking Progress:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                
                                val steps = listOf("Registered", "Confirmed", "In-Service", "Finished")
                                val currentStepIndex = when (booking.status) {
                                    "Pending" -> 0
                                    "Confirmed" -> 1
                                    "In Progress" -> 2
                                    "Completed" -> 3
                                    else -> 0
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    steps.forEachIndexed { index, stepName ->
                                        val isActive = index <= currentStepIndex
                                        val isCurrent = index == currentStepIndex
                                        val stepColor = if (isActive) MZKPrimaryBlue else Color.LightGray
                                        val ringColor = if (isCurrent) MZKAccentOrange else stepColor
                                        
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(
                                                        if (isActive) stepColor.copy(alpha = 0.12f) else Color.Transparent,
                                                        CircleShape
                                                    )
                                                    .border(
                                                        width = if (isCurrent) 2.dp else 1.dp,
                                                        color = ringColor,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isActive && index < currentStepIndex) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = stepColor,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = (index + 1).toString(),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isActive) ringColor else Color.Gray
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stepName,
                                                fontSize = 9.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isActive) Color.DarkGray else Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        if (index < steps.size - 1) {
                                            val lineColor = if (index < currentStepIndex) MZKPrimaryBlue else Color.LightGray
                                            Box(
                                                modifier = Modifier
                                                    .height(2.dp)
                                                    .weight(0.4f)
                                                    .background(lineColor)
                                            )
                                        }
                                    }
                                }

                                // Pulsing simulation feedback alert
                                if (currentSimStatus != null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MZKPrimaryBlue.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = MZKAccentOrange
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = currentSimStatus,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MZKPrimaryBlue
                                            )
                                        }
                                    }
                                }

                                // Informational State Log
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        val statusFeed = when (booking.status) {
                                            "Pending" -> "Customer is on queue. Support coordinator is choosing the best technician available inside your PIN code."
                                            "Confirmed" -> "Technician ${booking.assignedTechnicianName} has been assigned. In-route to client site with standard cleaning and flushing inventory."
                                            "In Progress" -> "Servicing in-progress: Unit opened, washing internal jet fan and conducting gas pressure optimizations."
                                            else -> "Complete"
                                        }
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = MZKPrimaryBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = statusFeed,
                                                fontSize = 10.5.sp,
                                                color = Color.DarkGray,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Details
                                DetailIconText(Icons.Default.CalendarToday, "${booking.scheduledDate} • ${booking.scheduledTime}")
                                DetailIconText(Icons.Default.LocationOn, booking.address)
                                DetailIconText(Icons.Default.AcUnit, "Brand Selection: ${booking.acBrand}")
                                DetailIconText(Icons.Default.Payment, "Payment Channel: ${booking.paymentMethod} (${booking.paymentStatus})")

                                Divider(modifier = Modifier.padding(vertical = 10.dp))

                                // Action Items/Simulator Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Est. Charge: ₹${booking.finalPrice.toInt()}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MZKPrimaryBlue)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { showInvoiceDetail = booking },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MZKCardLight, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = "View GST Invoice", tint = Color.DarkGray, modifier = Modifier.size(15.dp))
                                        }
                                        
                                        if (currentSimStatus == null) {
                                            Button(
                                                onClick = { viewModel.simulateRealTimeTracking(booking.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MZKAccentOrange),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Simulate Live Tracker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (bookingFilter == "Ongoing" && activeBookings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No ongoing active service requests found. Book a doorstep service from the 'Home' screen or order a unit from the 'AC Store'.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }

                // Section 2: Archive & Past requests
                if (bookingFilter == "All" || bookingFilter == "Past") {
                    item {
                        Text(
                            text = "Completed Service Archives",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }

                    if (pastBookings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No archived services found. Once a job completes, active requests appear here.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(pastBookings) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Invoice MZK/2026/0${booking.id}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                            Text(booking.serviceType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("Completed", color = Color(0xFF22C55E), fontWeight = FontWeight.Black, fontSize = 10.sp)
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    DetailIconText(Icons.Default.CalendarToday, booking.scheduledDate)
                                    DetailIconText(Icons.Default.AcUnit, "Equipment: ${booking.acBrand}")
                                    
                                    if (booking.assignedTechnicianName != null) {
                                        DetailIconText(Icons.Default.Person, "Handled By: ${booking.assignedTechnicianName} (Approved)")
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Approved",
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Payment Accepted of: ${booking.paymentMethod}", 
                                                fontSize = 9.5.sp, 
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF22C55E)
                                            )
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { showInvoiceDetail = booking },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color.White, CircleShape)
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = "Invoice Details", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = { generateAndDownloadInvoicePdf(context, booking) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(MZKPrimaryBlue.copy(alpha = 0.12f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = "Download PDF Summary",
                                                    tint = MZKPrimaryBlue,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            if (booking.reviewRating == 0.0f) {
                                                Button(
                                                    onClick = { showReviewModal = booking },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MZKAccentOrange),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("Rate Work", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = "Rated", tint = MZKAccentOrange, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Rated ${booking.reviewRating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add beautiful branded footer at bottom
                item {
                    MZKFooter(onNavigate = onSwitchTab)
                }
            }
        }

        // Review dialog sheet
        if (showReviewModal != null) {
            AlertDialog(
                onDismissRequest = { showReviewModal = null },
                title = { Text("Rate Service Experience", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("We strive for premium client delivery. Please score the work done by our expert coordinator:", fontSize = 12.sp)
                        StarRatingBar(rating = reviewRating, onRatingChanged = { reviewRating = it }, starSize = 28)
                        
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            label = { Text("Feedback (Optional)") },
                            placeholder = { Text("Describe pricing transparency, technician attitude...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.rateService(showReviewModal!!.id, reviewRating, reviewText)
                            reviewText = ""
                            reviewRating = 5.0f
                            showReviewModal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue)
                    ) {
                        Text("Submit Rating")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReviewModal = null }) { Text("Cancel") }
                }
            )
        }

        // Invoice display
        if (showInvoiceDetail != null) {
            val b = showInvoiceDetail!!
            val priceBeforeGst = b.finalPrice / 1.18
            val gstPart = b.finalPrice - priceBeforeGst
            
            AlertDialog(
                onDismissRequest = { showInvoiceDetail = null },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { generateAndDownloadInvoicePdf(context, b) },
                            colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download PDF",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF", fontSize = 12.sp)
                        }
                        TextButton(onClick = { showInvoiceDetail = null }) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        Text("MZK Enterprises", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MZKPrimaryBlue)
                        Text("TAX INVOICE / RECEIPTS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        Text("GSTIN: 10MNZPK8219F1ZH", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text("Address: Dalkhola 3 Stand, Near Railway Station, PIN 733201", fontSize = 10.sp, color = Color.DarkGray)
                        Text("Phone: 8540888704 | mzkinterprises3@gmail.com", fontSize = 10.sp, color = Color.DarkGray)
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                        
                        Text("Invoice No: MZK/2026/0${b.id}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Dated: ${b.scheduledDate}", fontSize = 11.sp)
                        Text("Client Phone: ${b.customerPhone}", fontSize = 11.sp)
                        Text("Recipient Name: ${b.customerName}", fontSize = 11.sp)
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service description", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(b.serviceType, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("₹${b.finalPrice.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                        
                        // CGST SGST Details
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxable Base Amount (excluding tax):", fontSize = 10.sp)
                            Text("₹${priceBeforeGst.toInt()}", fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST @ 9%:", fontSize = 10.sp)
                            Text("₹${(gstPart / 2).toInt()}", fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST @ 9%:", fontSize = 10.sp)
                            Text("₹${(gstPart / 2).toInt()}", fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Status:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(b.paymentStatus, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (b.paymentStatus == "Paid") Color(0xFF16A34A) else Color.Red)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Outflow Paid:", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("₹${b.finalPrice.toInt()}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MZKPrimaryBlue)
                        }
                        
                        if (b.customerSignature != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Digital Signature Receipt:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                            // Draw signature coordinate simulation!
                            RenderSavedSignature(b.customerSignature, modifier = Modifier.fillMaxWidth().height(60.dp))
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DetailIconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
    }
}

// ---------------------- REFER & PROFILE TAB ----------------------
@Composable
fun CustomerProfileTab(
    viewModel: MZKViewModel,
    onTriggerLogin: () -> Unit,
    onSwitchTab: ((Int) -> Unit)? = null
) {
    var editName by remember { mutableStateOf(viewModel.loggedInName.value) }
    var editEmail by remember { mutableStateOf(viewModel.loggedInEmail.value) }
    var editPhone by remember { mutableStateOf(viewModel.loggedInPhone.value) }
    var showAddressEditor by remember { mutableStateOf(false) }
    var customAddress by remember { mutableStateOf("Dalkhola Railway Gate Market area, PIN 733201") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo and Title Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(MZKPrimaryBlue, MZKAccentBlue)),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = "User", tint = MZKPrimaryBlue, modifier = Modifier.size(34.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(viewModel.loggedInName.value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(viewModel.loggedInPhone.value, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        // Referral Card Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // Soft green background
            border = BorderStroke(1.dp, Color(0xFFDCF8C6)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Reward", tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Referral Commission Program", fontWeight = FontWeight.ExtraBold, color = Color(0xFF14532D), fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Share comfort with friends! Invite a customer to try MZK Enterprises and get ₹250 flat credited directly to your account wallet after their technician visit.",
                    fontSize = 12.sp,
                    color = Color(0xFF166534),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFBBCFBA), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("MZKREF8540", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF15803D), letterSpacing = 1.sp)
                    }
                    Button(
                        onClick = { /* Simulated Share Link trigger */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Share Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Edit Profile details form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Edit Account Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editEmail, editPhone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Save Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Service Address Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Service Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showAddressEditor = !showAddressEditor }) {
                        Text(if (showAddressEditor) "Done" else "Change", color = MZKPrimaryBlue)
                    }
                }
                
                if (showAddressEditor) {
                    OutlinedTextField(
                        value = customAddress,
                        onValueChange = { customAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(customAddress, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        // Logout Simulation Button
        TextButton(
            onClick = { onTriggerLogin() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Switch Customer Mobile via OTP Simulation", color = MZKPrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        MZKFooter(onNavigate = onSwitchTab)
    }
}


// ---------------------- OTP MOCK OVERLAY COMPOSABLE ----------------------
@Composable
fun OtpVerificationOverlay(
    initialPhone: String,
    onDismiss: () -> Unit,
    onVerified: (String, String) -> Unit
) {
    var rawPhone by remember { mutableStateOf(initialPhone) }
    var inputOtpCode by remember { mutableStateOf("") }
    var stepSentOtp by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(30) }
    var codeError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(stepSentOtp) {
        if (stepSentOtp) {
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { /* Block taps */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SMS Secure Portal Login",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MZKPrimaryBlue
                )
                
                if (!stepSentOtp) {
                    Text(
                        text = "Enter your registered Indian phone number. We will dispatch a 4-digit code instantly.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    OutlinedTextField(
                        value = rawPhone,
                        onValueChange = { rawPhone = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = {
                            if (rawPhone.length >= 10) {
                                stepSentOtp = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Verification Code")
                    }
                } else {
                    Text(
                        text = "Dispatched a test OTP to +91 $rawPhone. Enter code to log in.",
                        fontSize = 12.sp,
                        color = Color.Green,
                        textAlign = TextAlign.Center
                    )
                    
                    OutlinedTextField(
                        value = inputOtpCode,
                        onValueChange = { 
                            inputOtpCode = it
                            codeError = false
                        },
                        label = { Text("4-Digit OTP Code") },
                        placeholder = { Text("Enter 4 digits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    if (codeError) {
                        Text("Incorrect OTP code entered. (Use default code: 1234)", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = if (timerSeconds > 0) "Resend code in ${timerSeconds}s" else "Didn't receive code? Resend Code",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    
                    Button(
                        onClick = {
                            if (inputOtpCode == "1234" || inputOtpCode.length >= 4) {
                                onVerified(rawPhone, "User Mzk 2026")
                            } else {
                                codeError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm OTP Code & Enter")
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Skip / Cancel", color = Color.Gray)
                }
            }
        }
    }
}


// ---------------------- CUSTOM SERVICE BOOKING FORM DIALOG ----------------------
@Composable
fun BookingFormDialog(
    serviceType: String,
    basePrice: Double,
    viewModel: MZKViewModel,
    onDismiss: () -> Unit
) {
    var brandSelection by remember { mutableStateOf("Daikin") }
    var dateInput by remember { mutableStateOf("2026-06-15") }
    var timeSelection by remember { mutableStateOf("11:00 AM - 01:00 PM") }
    var addressInput by remember { mutableStateOf("Dalkhola Near Petrol Pump, House 34") }
    var remarksInput by remember { mutableStateOf("") }
    var paymentMethodSelection by remember { mutableStateOf("Cash on Service") }
    
    // Client-side validation states
    var nameInput by remember { mutableStateOf(viewModel.loggedInName.value.ifEmpty { "" }) }
    var phoneInput by remember { mutableStateOf(viewModel.loggedInPhone.value.ifEmpty { "" }) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    
    val categoryBrandsList = listOf("Daikin", "Voltas", "LG", "Samsung", "Blue Star", "Hitachi", "Panasonic", "Carrier")
    val timeSlots = listOf("09:00 AM - 11:00 AM", "11:00 AM - 01:00 PM", "01:00 PM - 03:00 PM", "03:00 PM - 05:00 PM", "05:00 PM - 07:00 PM")

    // Dynamic Price Estimator States
    var selectedServiceType by remember { mutableStateOf(serviceType) }
    var selectedBasePrice by remember { mutableStateOf(basePrice) }
    var selectedCapacity by remember { mutableStateOf("1.5 Ton") }
    
    var includeCopperPipe by remember { mutableStateOf(false) }
    var copperPipeMeters by remember { mutableStateOf(3) }
    var includeOutdoorStand by remember { mutableStateOf(false) }
    var includeStabilizer by remember { mutableStateOf(false) }

    val serviceOptions = remember {
        val standard = listOf(
            "AC Installation" to 1499.0,
            "AC Repair" to 999.0,
            "AC Gas Charging" to 2499.0,
            "AC Deep Cleaning" to 1199.0,
            "AC Maintenance" to 799.0
        )
        if (standard.none { it.first == serviceType }) {
            listOf(serviceType to basePrice) + standard
        } else {
            standard
        }
    }

    val capacitySurcharge = when (selectedCapacity) {
        "1.0 Ton" -> 0.0
        "1.5 Ton" -> 250.0
        "2.0 Ton" -> 500.0
        "2.5+ Ton / Cassette" -> 950.0
        else -> 0.0
    }
    
    val addonCopperPipeCharge = if (includeCopperPipe) (copperPipeMeters * 350.0) else 0.0
    val addonStandCharge = if (includeOutdoorStand) 750.0 else 0.0
    val addonStabilizerCharge = if (includeStabilizer) 1999.0 else 0.0
    
    val estimatedBaseTotal = selectedBasePrice + capacitySurcharge + addonCopperPipeCharge + addonStandCharge + addonStabilizerCharge
    val estimatedCgst = estimatedBaseTotal * 0.09
    val estimatedSgst = estimatedBaseTotal * 0.09
    val estimatedGrandTotal = estimatedBaseTotal + estimatedCgst + estimatedSgst

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = MZKPrimaryBlue, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm Doorstep Booking", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MZKPrimaryBlue)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalNameErr = if (nameInput.trim().length < 3) "Name must be at least 3 characters" else null
                    val finalPhoneErr = if (phoneInput.length != 10) "Phone must be exactly 10 digits" else null
                    val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
                    val finalDateErr = if (!dateRegex.matches(dateInput)) "Date must be in YYYY-MM-DD format (e.g., 2026-06-15)" else null
                    val finalAddressErr = if (addressInput.trim().length < 8) "Address must be at least 8 characters" else null

                    nameError = finalNameErr
                    phoneError = finalPhoneErr
                    dateError = finalDateErr
                    addressError = finalAddressErr

                    if (finalNameErr == null && finalPhoneErr == null && finalDateErr == null && finalAddressErr == null) {
                        val finalNotes = buildString {
                            if (remarksInput.trim().isNotEmpty()) {
                                append(remarksInput.trim())
                                append(" | ")
                            }
                            append("Sizing: $selectedCapacity")
                            if (includeCopperPipe) append(", Copper Pipe: ${copperPipeMeters}m")
                            if (includeOutdoorStand) append(", Outdoor Core Wall Stand")
                            if (includeStabilizer) append(", Guard Digital Stabilizer")
                        }

                        viewModel.createBooking(
                            customerName = nameInput.trim(),
                            customerPhone = phoneInput.trim(),
                            customerEmail = viewModel.loggedInEmail.value,
                            serviceType = selectedServiceType,
                            acBrand = brandSelection,
                            scheduledDate = dateInput,
                            scheduledTime = timeSelection,
                            address = addressInput.trim(),
                            notes = finalNotes,
                            price = estimatedGrandTotal,
                            paymentMethod = paymentMethodSelection
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue)
            ) {
                Text("Confirm Book Appointment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Red)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Divider()
                
                // LIVE INTERACTIVE ESTIMATE DISPLAY (Summary Quote card replaces static text)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MZKCardLight, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("⚡ Live Estimated Quote", fontWeight = FontWeight.Black, fontSize = 11.sp, color = MZKPrimaryBlue)
                            Text(selectedServiceType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Sizing adjustments applied dynamically: $selectedCapacity", fontSize = 10.sp, color = Color.LightGray)
                        }
                        Text("₹${estimatedGrandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MZKPrimaryBlue)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.DarkGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Core Cost:", fontSize = 10.sp, color = Color.LightGray)
                            Text("₹${selectedBasePrice.toInt()}", fontSize = 10.sp, color = Color.White)
                        }
                        if (capacitySurcharge > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Capacity Offset ($selectedCapacity):", fontSize = 10.sp, color = Color.LightGray)
                                Text("+₹${capacitySurcharge.toInt()}", fontSize = 10.sp, color = Color.White)
                            }
                        }
                        val addonsTotal = addonCopperPipeCharge + addonStandCharge + addonStabilizerCharge
                        if (addonsTotal > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Add-ons / Accessories:", fontSize = 10.sp, color = Color.LightGray)
                                Text("+₹${addonsTotal.toInt()}", fontSize = 10.sp, color = Color.White)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST + SGST (18%):", fontSize = 10.sp, color = Color.LightGray)
                            Text("₹${(estimatedCgst + estimatedSgst).toInt()}", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // DYNAMIC ESTIMATOR ADJUSTMENT PANEL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MZKDarkBackground.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛠️ Dynamic Sizing & Add-on Calculator", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        
                        // Service Type Selector (Allows swapping to explore other services)
                        Text("Selected Service Type:", fontSize = 10.sp, color = Color.LightGray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            serviceOptions.forEach { (name, price) ->
                                val isSelected = selectedServiceType == name
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelected) MZKPrimaryBlue else Color(0xFF1E293B),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            selectedServiceType = name
                                            selectedBasePrice = price
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        // Capacity Sizing Radio options
                        Text("AC Unit Capacity (Ton):", fontSize = 10.sp, color = Color.LightGray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val capacities = listOf("1.0 Ton", "1.5 Ton", "2.0 Ton", "2.5+ Ton / Cassette")
                            capacities.forEach { cap ->
                                val isSelected = selectedCapacity == cap
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) MZKPrimaryBlue.copy(alpha = 0.15f) else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MZKPrimaryBlue else Color(0xFF334155),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedCapacity = cap }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(cap, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        val offsetStr = when (cap) {
                                            "1.0 Ton" -> "Base"
                                            "1.5 Ton" -> "+₹250"
                                            "2.0 Ton" -> "+₹500"
                                            "2.5+ Ton / Cassette" -> "+₹950"
                                            else -> ""
                                        }
                                        Text(offsetStr, fontSize = 8.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        Divider(color = Color(0xFF334155), thickness = 0.5.dp)

                        // Optional Accessories Checkboxes
                        Text("Optional Add-on Accessories:", fontSize = 10.sp, color = Color.LightGray)
                        
                        // Add-on 1: Copper Piping
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = includeCopperPipe,
                                    onCheckedChange = { includeCopperPipe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MZKPrimaryBlue)
                                )
                                Column {
                                    Text("Copper Piping Extension", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("₹350/meter extra standard", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                            if (includeCopperPipe) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(6.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MZKPrimaryBlue, RoundedCornerShape(4.dp))
                                            .clickable { if (copperPipeMeters > 1) copperPipeMeters-- },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Text("${copperPipeMeters}m", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MZKPrimaryBlue, RoundedCornerShape(4.dp))
                                            .clickable { if (copperPipeMeters < 10) copperPipeMeters++ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        // Add-on 2: Outdoor bracket Stand
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeOutdoorStand,
                                onCheckedChange = { includeOutdoorStand = it },
                                colors = CheckboxDefaults.colors(checkedColor = MZKPrimaryBlue)
                            )
                            Column {
                                Text("Anti-Rust Outdoor Wall Bracket Stand", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Add robust structure support (₹750)", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        // Add-on 3: High-capacity stabilizer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeStabilizer,
                                onCheckedChange = { includeStabilizer = it },
                                colors = CheckboxDefaults.colors(checkedColor = MZKPrimaryBlue)
                            )
                            Column {
                                Text("Digital Voltage Stabilizer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Protect unit compressor from voltage surges (₹1999)", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("0. Contact Information", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { 
                        nameInput = it
                        nameError = if (it.trim().length < 3) "Name must be at least 3 characters" else null
                    },
                    label = { Text("Customer Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 10) {
                            phoneInput = digits
                            phoneError = if (digits.length != 10) "Phone must be exactly 10 digits" else null
                        }
                    },
                    label = { Text("Contact Phone") },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("1. Target AC Brand", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryBrandsList.forEach { brand ->
                        val isSelected = brandSelection == brand
                        InputChip(
                            selected = isSelected,
                            onClick = { brandSelection = brand },
                            label = { Text(brand, fontSize = 11.sp) }
                        )
                    }
                }

                Text("2. Scheduled Date & Slot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InputChip(
                        selected = dateInput == "2026-06-15",
                        onClick = { 
                            dateInput = "2026-06-15"
                            dateError = null
                        },
                        label = { Text("Mon 15-Jun", fontSize = 11.sp) }
                    )
                    InputChip(
                        selected = dateInput == "2026-06-16",
                        onClick = { 
                            dateInput = "2026-06-16"
                            dateError = null
                        },
                        label = { Text("Tue 16-Jun", fontSize = 11.sp) }
                    )
                    InputChip(
                        selected = dateInput == "2026-06-17",
                        onClick = { 
                            dateInput = "2026-06-17"
                            dateError = null
                        },
                        label = { Text("Wed 17-Jun", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { 
                        dateInput = it
                        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
                        dateError = if (!dateRegex.matches(it)) "Date must be in YYYY-MM-DD format (e.g., 2026-06-15)" else null
                    },
                    label = { Text("Service Date (YYYY-MM-DD)") },
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timeSlots.forEach { slot ->
                        val isSelected = timeSelection == slot
                        InputChip(
                            selected = isSelected,
                            onClick = { timeSelection = slot },
                            label = { Text(slot, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { 
                        addressInput = it
                        addressError = if (it.trim().length < 8) "Address must be at least 8 characters" else null
                    },
                    label = { Text("Doorstep Service Address") },
                    isError = addressError != null,
                    supportingText = addressError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = remarksInput,
                    onValueChange = { remarksInput = it },
                    label = { Text("Special Requests / Issue symptoms") },
                    placeholder = { Text("e.g., Ice formation, humming noise...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Text("3. Choose Settlement Method", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // light green focus block
                    border = BorderStroke(1.dp, Color(0xFFDCF8C6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Accepted",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "All Payments Accepted: UPI, Debit Card, Credit Card",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                    }
                }

                val paymentMethods = listOf(
                    "UPI / QR (Google Pay, PhonePe, Paytm)" to "UPI Payment Gateway",
                    "Debit Card (All Banks accepted)" to "Debit Card Payment",
                    "Credit Card (Slab-free EMI available)" to "Credit Card Payment",
                    "Cash on Service / After Delivery" to "Cash on Service"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    paymentMethods.forEach { (label, value) ->
                        val isSelected = paymentMethodSelection == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MZKPrimaryBlue else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    color = if (isSelected) MZKPrimaryBlue.copy(alpha = 0.05f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { paymentMethodSelection = value }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { paymentMethodSelection = value },
                                colors = RadioButtonDefaults.colors(selectedColor = MZKPrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val paymentIcon = when (value) {
                                "UPI Payment Gateway" -> Icons.Default.QrCodeScanner
                                "Debit CardPayment", "Credit Card Payment", "Debit Card Payment" -> Icons.Default.CreditCard
                                "Cash on Service" -> Icons.Default.Payments
                                else -> Icons.Default.CreditCard
                            }
                            Icon(
                                imageVector = paymentIcon,
                                contentDescription = null,
                                tint = if (isSelected) MZKPrimaryBlue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MZKPrimaryBlue else Color.DarkGray
                            )
                        }
                    }
                }
                
                Text(
                    "Note: Safe SSL-encrypted transactions powered by Razorpay. Invoices provided immediately post-checkout.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    )
}


