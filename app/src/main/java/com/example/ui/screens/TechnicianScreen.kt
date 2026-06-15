package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun TechnicianScreen(
    viewModel: MZKViewModel,
    modifier: Modifier = Modifier
) {
    val technicians by viewModel.allTechnicians.collectAsState()
    val selectedId by viewModel.selectedTechnicianId.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()

    // Find active technician model
    val activeTech = technicians.find { it.id == selectedId } ?: technicians.firstOrNull()
    val myJobs = bookings.filter { it.assignedTechnicianId == selectedId }

    var technicianTab by remember { mutableStateOf(0) } // 0: Tasks, 1: Metrics & General
    var selectedJobForProcess by remember { mutableStateOf<Booking?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        // Tech Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(MZKDarkBackground, Color(0xFF1E293B))))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Technician Field Operations",
                            color = MZKAccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeTech?.name ?: "Expert Partner",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    // Profile Switcher for testing
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Switch Profile ▾", fontSize = 10.sp, color = Color.White)
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            technicians.forEach { tech ->
                                DropdownMenuItem(
                                    text = { Text("${tech.name} (${tech.rating} ★)", fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.selectedTechnicianId.value = tech.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Attendance Switch Row
                activeTech?.let { tech ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (tech.status == "Available") Color(0xFF10B981) else Color(0xFFEF4444),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Shift Status: ${tech.status}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                val nextStatus = if (tech.status == "Available") "Offline" else "Available"
                                viewModel.updateTechnicianStatus(tech.id, nextStatus)
                            }
                        ) {
                            Text(
                                text = if (tech.status == "Available") "Go Offline" else "Clock In",
                                color = MZKAccentBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Subtabs for tasks and metrics
        TabRow(
            selectedTabIndex = technicianTab,
            containerColor = Color.White,
            contentColor = MZKPrimaryBlue
        ) {
            Tab(
                selected = technicianTab == 0,
                onClick = { technicianTab = 0 },
                text = { Text("Active Assignments (${myJobs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = technicianTab == 1,
                onClick = { technicianTab = 1 },
                text = { Text("Earnings & Statistics", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        if (technicianTab == 0) {
            // Task List
            if (myJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Task, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Service Tasks Scheduled", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("When an admin assigns customer bookings to this technician, jobs of appropriate brand will be listed in real-time.", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(myJobs) { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedJobForProcess = job },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Job #${job.id}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                    
                                    val statusColor = when (job.status) {
                                        "Confirmed" -> Color(0xFF10B981) // ready
                                        "In Progress" -> Color(0xFFF59E0B) // busy
                                        "Completed" -> Color(0xFF3B82F6) // done
                                        else -> Color.DarkGray
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(job.status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(job.serviceType, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                Text("Client Name: ${job.customerName}", fontSize = 12.sp, color = Color.DarkGray)
                                Text("Address: ${job.address}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                                
                                Divider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Est Payment: ₹${job.finalPrice.toInt()}", fontWeight = FontWeight.Black, color = MZKPrimaryBlue, fontSize = 13.sp)
                                    Text("Process Task ›", fontWeight = FontWeight.Bold, color = MZKPrimaryBlue, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // General performance metrics
            activeTech?.let { tech ->
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Earnings Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MZKPrimaryBlue)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.Start) {
                            Text("Total Service Earnings", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("₹${tech.totalEarnings.toInt()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                            Text("Platform commission has been deducted. Remitted weekly.", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Jobs Executed", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                    Text("${tech.jobsCompleted}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Customer Rating", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = MZKAccentOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("${tech.rating} / 5.0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Schedule item Checklist
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Operational Quality Guidelines", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Divider()
                            CheclistRowItem("Do always wear full corporate uniform and carry valid ID card")
                            CheclistRowItem("Ensure before/after service state is recorded with clear photo proofs")
                            CheclistRowItem("Explain GST component clearly, do not demand off-market cash")
                            CheclistRowItem("Clean the floor completely post jet-cleaning water wash")
                        }
                    }
                }
            }
        }

        // Processing details overlay modal sheet
        if (selectedJobForProcess != null) {
            val job = selectedJobForProcess!!
            var beforePhotoAttached by remember { mutableStateOf(job.beforePhotoUri != null) }
            var afterPhotoAttached by remember { mutableStateOf(job.afterPhotoUri != null) }
            var base64SignatureGot by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { selectedJobForProcess = null },
                confirmButton = {
                    Button(
                        onClick = {
                            if (job.status == "Confirmed") {
                                viewModel.updateBookingStatus(job.id, "In Progress")
                            } else if (job.status == "In Progress") {
                                // Save completion records
                                viewModel.completeJob(
                                    bookingId = job.id,
                                    signatureSvgData = if (base64SignatureGot.isNotEmpty()) base64SignatureGot else "Signed_Blank",
                                    beforePhoto = if (beforePhotoAttached) "ic_ac_dirty" else null,
                                    afterPhoto = if (afterPhotoAttached) "ic_ac_clean" else null
                                )
                            }
                            selectedJobForProcess = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                        enabled = (job.status == "Confirmed" || (job.status == "In Progress" && beforePhotoAttached && afterPhotoAttached && base64SignatureGot.isNotEmpty()))
                    ) {
                        Text(
                            text = if (job.status == "Confirmed") "Verify & Start Service" else "Dispatch & Mark Completed"
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedJobForProcess = null }) { Text("Cancel") }
                },
                title = { Text("Operational Workflow: Job #${job.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Divider()
                        
                        Text("Service details: ${job.serviceType}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Brand target: ${job.acBrand}", fontSize = 12.sp)
                        Text("Address: ${job.address}", fontSize = 12.sp, color = Color.Gray)
                        Text("Customer Name: ${job.customerName} (${job.customerPhone})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        Divider()

                        // Conditional UI depending on lifecycle
                        if (job.status == "Confirmed") {
                            Text(
                                "Step 1: Check-in on Client Premises",
                                fontWeight = FontWeight.Bold,
                                color = MZKPrimaryBlue,
                                fontSize = 12.sp
                            )
                            Text(
                                "Tap 'Verify & Start Service' below once you hit the customer site. This lets the customer track you live and unlocks steps.",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        } else if (job.status == "In Progress") {
                            Text(
                                "Step 2: Collect Service Photo Evidence",
                                fontWeight = FontWeight.Bold,
                                color = MZKPrimaryBlue,
                                fontSize = 12.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { beforePhotoAttached = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (beforePhotoAttached) Color(0xFF10B981) else Color.DarkGray
                                    ),
                                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (beforePhotoAttached) "✓ Before state" else "Capture Before 📸",
                                        fontSize = 11.sp
                                    )
                                }
                                Button(
                                    onClick = { afterPhotoAttached = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (afterPhotoAttached) Color(0xFF10B981) else Color.DarkGray
                                    ),
                                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (afterPhotoAttached) "✓ After state" else "Capture After 📸",
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Divider()

                            Text(
                                "Step 3: Signature Authorization",
                                fontWeight = FontWeight.Bold,
                                color = MZKPrimaryBlue,
                                fontSize = 12.sp
                            )
                            
                            HandSignaturePad(
                                onSaveSignature = { base64SignatureGot = it },
                                onClearPressed = { base64SignatureGot = "" }
                            )
                            
                            if (base64SignatureGot.isNotEmpty()) {
                                Text("✓ Signature collected successfully", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Text("Job already completed. The customer can access invoice details in their client panel.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CheclistRowItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
    }
}
