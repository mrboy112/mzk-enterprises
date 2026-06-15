package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MZKViewModel
import com.example.ui.viewmodel.UserRole

import com.example.ui.theme.isDarkThemeGlobal
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    viewModel: MZKViewModel,
    modifier: Modifier = Modifier
) {
    val activeRole by viewModel.currentRole.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                color = MZKDarkBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 10.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MZK Enterprises Portal",
                                color = if (isDarkThemeGlobal) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Multi-Persona Simulation System",
                                color = MZKAccentBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Theme Toggle Button
                            IconButton(
                                onClick = { isDarkThemeGlobal = !isDarkThemeGlobal },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MZKCardLight, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = if (isDarkThemeGlobal) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                    contentDescription = "Toggle Theme Mode",
                                    tint = if (isDarkThemeGlobal) Color(0xFFFFCC00) else Color(0xFF0066CC),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // PIN Info box
                            Box(
                                modifier = Modifier
                                    .background(MZKCardLight, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "PIN 733201",
                                    color = if (isDarkThemeGlobal) Color.White else Color(0xFF0066CC),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Unified Segmented Swapper Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MZKCardLight, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Customer Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activeRole == UserRole.CUSTOMER) MZKPrimaryBlue else Color.Transparent)
                                .clickable { viewModel.currentRole.value = UserRole.CUSTOMER }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (activeRole == UserRole.CUSTOMER) Color.White else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Customer",
                                    color = if (activeRole == UserRole.CUSTOMER) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Technician Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activeRole == UserRole.TECHNICIAN) MZKPrimaryBlue else Color.Transparent)
                                .clickable { viewModel.currentRole.value = UserRole.TECHNICIAN }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = if (activeRole == UserRole.TECHNICIAN) Color.White else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Technician",
                                    color = if (activeRole == UserRole.TECHNICIAN) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Admin ERP Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activeRole == UserRole.ADMIN) MZKPrimaryBlue else Color.Transparent)
                                .clickable { viewModel.currentRole.value = UserRole.ADMIN }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    Icons.Default.BusinessCenter,
                                    contentDescription = null,
                                    tint = if (activeRole == UserRole.ADMIN) Color.White else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Admin ERP",
                                    color = if (activeRole == UserRole.ADMIN) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = activeRole,
                transitionSpec = {
                    fadeIn() with fadeOut()
                }
            ) { role ->
                when (role) {
                    UserRole.CUSTOMER -> CustomerScreen(viewModel = viewModel)
                    UserRole.TECHNICIAN -> TechnicianScreen(viewModel = viewModel)
                    UserRole.ADMIN -> AdminScreen(viewModel = viewModel)
                }
            }
        }
    }
}
