package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ui.theme.isDarkThemeGlobal

// Color constants resolved dynamically based on theme
val MZKPrimaryBlue: Color
    get() = if (isDarkThemeGlobal) Color(0xFF0066CC) else Color(0xFF0052A3)

val MZKAccentBlue: Color
    get() = if (isDarkThemeGlobal) Color(0xFF00BFFF) else Color(0xFF0099DD)

val MZKDarkBackground: Color
    get() = if (isDarkThemeGlobal) Color(0xFF0A0F1D) else Color(0xFFF4F6FA)

val MZKCardLight: Color
    get() = if (isDarkThemeGlobal) Color(0xFF131B2E) else Color(0xFFFFFFFF)

val MZKAccentOrange: Color
    get() = if (isDarkThemeGlobal) Color(0xFF00BFFF) else Color(0xFF0066CC)

@Composable
fun StarRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    onRatingChanged: ((Float) -> Unit)? = null,
    starSize: Int = 20
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Start) {
        val maxStars = 5
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            IconButton(
                onClick = { onRatingChanged?.invoke(i.toFloat()) },
                enabled = onRatingChanged != null,
                modifier = Modifier.size(starSize.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star $i",
                    tint = if (isSelected) MZKAccentOrange else Color.Gray,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun HandSignaturePad(
    modifier: Modifier = Modifier,
    onSaveSignature: (String) -> Unit,
    onClearPressed: () -> Unit
) {
    var signaturePoints = remember { mutableStateListOf<Offset>() }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Customer Signature Pad", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
            Row {
                TextButton(onClick = {
                    signaturePoints.clear()
                    onClearPressed()
                }) {
                    Text("Clear", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Serialize points coordinates as simple string pattern to represent drawing saved
                        if (signaturePoints.isNotEmpty()) {
                            val serialized = signaturePoints.joinToString(";") { "${it.x},${it.y}" }
                            onSaveSignature(serialized)
                        } else {
                            onSaveSignature("Signed_Blank")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MZKPrimaryBlue),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Save Signature", fontSize = 11.sp, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.Gray, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newPoint = change.position
                        // Keep within canvas approximate bounds
                        signaturePoints.add(newPoint)
                    }
                }
        ) {
            if (signaturePoints.isEmpty()) {
                Text(
                    "Draw customer signature here with finger",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                if (signaturePoints.isNotEmpty()) {
                    path.moveTo(signaturePoints[0].x, signaturePoints[0].y)
                    for (i in 1 until signaturePoints.size) {
                        // Draw continuous line segments unless drag stopped
                        path.lineTo(signaturePoints[i].x, signaturePoints[i].y)
                    }
                    drawPath(
                        path = path,
                        color = Color.Blue,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
fun RenderSavedSignature(
    serializedPoints: String,
    modifier: Modifier = Modifier
) {
    if (serializedPoints == "Signed_Blank") {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Signed digitally (No graphic details)", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }
    
    val points = remember(serializedPoints) {
        val list = mutableListOf<Offset>()
        try {
            val tokens = serializedPoints.split(";")
            for (token in tokens) {
                val coords = token.split(",")
                if (coords.size == 2) {
                    list.add(Offset(coords[0].toFloat(), coords[1].toFloat()))
                }
            }
        } catch (e: Exception) {
            // failed parsing
        }
        list
    }

    Box(
        modifier = modifier
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }
                drawPath(
                    path = path,
                    color = Color.Blue,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// Native Canvas Bar Chart Component
@Composable
fun FinanceBarChart(
    weeklyData: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = weeklyData.maxOrNull() ?: 1.0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            "Weekly Revenue Flow",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E293B)
        )
        Text(
            "Visual earnings breakdown (Inclusive of GST)",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEachIndexed { index, value ->
                val ratio = (value / maxValue).toFloat()
                val barHeight = (ratio * 130).dp
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "₹${value.toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MZKPrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight)
                            .background(
                                color = if (index % 2 == 0) MZKPrimaryBlue else MZKAccentBlue,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = labels.getOrElse(index) { "" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun MZKFooter(
    onNavigate: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MZKDarkBackground),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            // Company Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MZK ENTERPRISES",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Complete Smart Cooling & HVAC Solutions",
                        fontSize = 11.sp,
                        color = MZKAccentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .background(MZKAccentOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GSTIN: 10MNZPK8219F1ZH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MZKAccentOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))

            // Contact Info & Office Hours Side by Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1: Contact Details
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "CONTACT DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Address
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = MZKAccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dalkhola 3 Stand, Near Railway Station, PIN 733201, West Bengal, India",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }

                    // Phone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = MZKAccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+91 94343 14819",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MZKAccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "mzk03official@gmail.com",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Column 2: Hours & Support
                Column(modifier = Modifier.weight(0.9f)) {
                    Text(
                        text = "OFFICE HOURS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Hours",
                            tint = MZKAccentOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Mon - Sat: 09 AM - 08 PM",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sun: 10 AM - 04 PM\n(Emergency Support)",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                lineHeight = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SUPPORT DESK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Average response: <15 mins",
                        fontSize = 9.5.sp,
                        color = MZKAccentBlue
                    )
                }
            }

            // Quick Links Section (If navigation callback is available)
            if (onNavigate != null) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "QUICK NAVIGATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val links = listOf(
                        Triple("Book Service", 0, Icons.Default.Build),
                        Triple("Air Conditioner Store", 1, Icons.Default.ShoppingCart),
                        Triple("My Bookings", 2, Icons.Default.History),
                        Triple("My Account", 3, Icons.Default.Person)
                    )

                    links.forEach { (label, tabIndex, icon) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clickable { onNavigate(tabIndex) },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = MZKAccentBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Copyright and Disclaimers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "© 2026 MZK Enterprises. All rights reserved.",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
                Text(
                    text = "v2.5 (Stable Cloud Sync)",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
