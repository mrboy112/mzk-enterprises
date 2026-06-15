package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Technician
import com.example.ui.viewmodel.MZKViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MZKViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.allBookings.collectAsState()
    val technicians by viewModel.allTechnicians.collectAsState()
    
    var adminTab by remember { mutableStateOf(0) } // 0: Analytics, 1: Bookings, 2: Technicians, 3: GST Ledger
    var showAssignModal by remember { mutableStateOf<Booking?>(null) }
    var selectedStatusFilter by remember { mutableStateOf("All") } // All, Pending, Confirmed, Completed, Cancelled

    Column(modifier = modifier.fillMaxSize().background(MZKDarkBackground)) {
        // Admin Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MZKCardLight)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MZK Enterprises ERP System",
                    color = MZKAccentBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Executive Administration Control",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF22C55E).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFF22C55E), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Live Server Active", color = Color(0xFF22C55E), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Navigation Tabs M3 style
        TabRow(
            selectedTabIndex = adminTab,
            containerColor = Color.White,
            contentColor = MZKPrimaryBlue
        ) {
            Tab(selected = adminTab == 0, onClick = { adminTab = 0 }, icon = { Icon(Icons.Default.Analytics, contentDescription = "Charts", modifier = Modifier.size(20.dp)) }, text = { Text("Metrics", fontSize = 10.sp, fontWeight = FontWeight.Bold) } )
            Tab(selected = adminTab == 1, onClick = { adminTab = 1 }, icon = { Icon(Icons.Default.Assignment, contentDescription = "Bookings", modifier = Modifier.size(20.dp)) }, text = { Text("Bookings", fontSize = 10.sp, fontWeight = FontWeight.Bold) } )
            Tab(selected = adminTab == 2, onClick = { adminTab = 2 }, icon = { Icon(Icons.Default.Engineering, contentDescription = "Staff", modifier = Modifier.size(20.dp)) }, text = { Text("Staff Control", fontSize = 10.sp, fontWeight = FontWeight.Bold) } )
            Tab(selected = adminTab == 3, onClick = { adminTab = 3 }, icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "GST Ledger", modifier = Modifier.size(20.dp)) }, text = { Text("GST Reports", fontSize = 10.sp, fontWeight = FontWeight.Bold) } )
        }

        when (adminTab) {
            0 -> AdminMetricsTab(bookings, technicians)
            1 -> AdminBookingsTab(
                bookings = bookings,
                selectedFilter = selectedStatusFilter,
                onFilterChanged = { selectedStatusFilter = it },
                onAssignTriggered = { showAssignModal = it }
            )
            2 -> AdminTechniciansTab(technicians)
            3 -> AdminGstTab(bookings)
        }

        // Technician Assignment Modal Dialogue
        if (showAssignModal != null) {
            val booking = showAssignModal!!
            AlertDialog(
                onDismissRequest = { showAssignModal = null },
                title = { Text("Assign Partner to Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Choose from our registered, active service operatives for: ${booking.serviceType} (${booking.acBrand})",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        
                        Divider()

                        LazyColumn(
                            modifier = Modifier.height(200.dp).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(technicians) { tech ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.assignTechnician(booking.id, tech.id, tech.name)
                                            showAssignModal = null
                                        }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Current: ${tech.status}", fontSize = 11.sp, color = if (tech.status == "Available") Color(0xFF10B981) else Color.Red)
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = MZKAccentOrange, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("${tech.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Select ›", color = MZKPrimaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAssignModal = null }) { Text("Close") }
                }
            )
        }
    }
}

// ---------------------- EXECUTIVE METRICS TAB ----------------------
@Composable
fun AdminMetricsTab(
    bookings: List<Booking>,
    technicians: List<Technician>
) {
    val totalRevenue = bookings.filter { it.status == "Completed" }.sumOf { it.finalPrice }
    val pendingCount = bookings.filter { it.status == "Pending" }.size
    val activeCount = bookings.filter { it.status == "Confirmed" || it.status == "In Progress" }.size
    val totalTickets = bookings.size

    val avgRating = if (bookings.filter { it.reviewRating > 0f }.isNotEmpty()) {
        bookings.filter { it.reviewRating > 0f }.map { it.reviewRating }.average()
    } else {
        4.8
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High level KPI Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiMetricBox(
                title = "Total Revenue",
                value = "₹${totalRevenue.toInt()}",
                subtitle = "Room-backed logs",
                color = MZKPrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            KpiMetricBox(
                title = "Trust Rating",
                value = "${String.format("%.1f", avgRating)} ★",
                subtitle = "Customer Feedback",
                color = MZKAccentOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiMetricBox(
                title = "Action Needed",
                value = "$pendingCount",
                subtitle = "Unassigned Tasks",
                color = Color.Red,
                modifier = Modifier.weight(1f)
            )
            KpiMetricBox(
                title = "Active Pipeline",
                value = "$activeCount",
                subtitle = "Jobs Excuting",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        // Render Native Canvas Bar Chart!
        FinanceBarChart(
            weeklyData = listOf(totalRevenue * 0.2, totalRevenue * 0.15, totalRevenue * 0.35, totalRevenue * 0.3),
            labels = listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4")
        )

        // General business summary details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MZKCardLight),
            border = BorderStroke(1.dp, Color(0xFF222222))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dalkhola Core Operations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Divider()
                Text("Company Partner: MZK Enterprises", fontSize = 12.sp, color = Color.LightGray)
                Text("Operational limits: 15km circumference from Dalkhola 3 Stand", fontSize = 12.sp, color = Color.LightGray)
                Text("GST Tax Identification Number: 10MNZPK8219F1ZH", fontSize = 12.sp, color = Color.LightGray)
                Text("Email Support: mzkinterprises3@gmail.com", fontSize = 12.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun KpiMetricBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

// ---------------------- ADMIN BOOKINGS TAB ----------------------
@Composable
fun AdminBookingsTab(
    bookings: List<Booking>,
    selectedFilter: String,
    onFilterChanged: (String) -> Unit,
    onAssignTriggered: (Booking) -> Unit
) {
    val statesList = listOf("All", "Pending", "Confirmed", "In Progress", "Completed")

    Column(modifier = Modifier.fillMaxSize()) {
        // Status filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 10.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statesList.forEach { state ->
                val isSelected = selectedFilter == state
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MZKPrimaryBlue else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onFilterChanged(state) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = state,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.DarkGray
                    )
                }
            }
        }

        val parsedList = bookings.filter {
            selectedFilter == "All" || it.status.equals(selectedFilter, ignoreCase = true)
        }

        if (parsedList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Bookings with state: $selectedFilter", fontWeight = FontWeight.Bold, color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(parsedList) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Ticket #${booking.id}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                    Text(booking.serviceType, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                                
                                val bookingStatusColor = when (booking.status) {
                                    "Pending" -> Color(0xFFF59E0B)
                                    "Confirmed" -> Color(0xFF10B981)
                                    "Completed" -> Color(0xFF3B82F6)
                                    else -> Color.DarkGray
                                }
                                Box(
                                    modifier = Modifier
                                        .background(bookingStatusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(booking.status, fontSize = 10.sp, color = bookingStatusColor, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text("Customer: ${booking.customerName} (${booking.customerPhone})", fontSize = 12.sp, color = Color.DarkGray)
                            Text("Address: ${booking.address}", fontSize = 12.sp, color = Color.Gray)
                            
                            if (booking.assignedTechnicianName != null) {
                                Text("Operative Assigned: ${booking.assignedTechnicianName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MZKPrimaryBlue)
                            } else {
                                Text("❌ No operative partner assigned yet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Revenue: ₹${booking.finalPrice.toInt()}", fontWeight = FontWeight.Black, color = MZKPrimaryBlue, fontSize = 14.sp)
                                
                                if (booking.status == "Pending") {
                                    Button(
                                        onClick = { onAssignTriggered(booking) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Assign Staff Partners 🛠️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("Dispatch Confirmed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- TECHNICIANS PARTNERS DIRECTORY ----------------------
@Composable
fun AdminTechniciansTab(
    technicians: List<Technician>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(technicians) { tech ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MZKCardLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = MZKPrimaryBlue, modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                            Text("Mobile: ${tech.phone}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (tech.status == "Available") Color(0xFF10B981) else Color.Red,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Status: ${tech.status}",
                                    fontSize = 11.sp,
                                    color = if (tech.status == "Available") Color(0xFF10B981) else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Payout: ₹${tech.totalEarnings.toInt()}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MZKPrimaryBlue)
                        Text("${tech.jobsCompleted} Services", fontSize = 11.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MZKAccentOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${tech.rating} Score", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- GST TAXATION & FINANCIAL AGGREGATE LEDGER ----------------------
@Composable
fun AdminGstTab(
    bookings: List<Booking>
) {
    val completedBookings = bookings.filter { it.status == "Completed" }
    val totalVolume = completedBookings.sumOf { it.finalPrice }
    val priceBeforeTax = totalVolume / 1.18
    val gstOwedTotal = totalVolume - priceBeforeTax
    val cgstPortion = gstOwedTotal / 2
    val sgstPortion = gstOwedTotal / 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Corporate Heading
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MZKPrimaryBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MZK Enterprises GST Registry", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                Text("Authorized Corporate License: 10MNZPK8219F1ZH", fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text("Registered headquarters: Dalkhola 3 Stand, Near Railway Station, PIN 733201", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }

        // Ledger Figures
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Indian GST Tax Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Divider()

                TaxLedgerRow("Gross Taxed Revenue (Inclusive):", "₹${totalVolume.toInt()}")
                TaxLedgerRow("Taxable Net base amount (100%):", "₹${priceBeforeTax.toInt()}")
                TaxLedgerRow("Output CGST @ 9% Portion:", "₹${cgstPortion.toInt()}")
                TaxLedgerRow("Output SGST @ 9% Portion:", "₹${sgstPortion.toInt()}")
                
                Divider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Compiled GST Liability Due:", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Text("₹${gstOwedTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.Red)
                }
            }
        }

        Button(
            onClick = { /* Download trigger simulated */ },
            colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Official GST Return Report (CSV)")
        }
    }
}

@Composable
fun TaxLedgerRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.DarkGray)
        Text(amount, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
    }
}
