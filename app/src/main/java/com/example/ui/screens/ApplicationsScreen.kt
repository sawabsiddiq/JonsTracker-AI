package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.example.data.local.JobApplication
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val priorities = listOf("All", "High", "Medium", "Low")
    val statuses = listOf("All", "Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")

    // Filter application list based on search and selected chips
    val filteredApps = remember(applications, searchQuery, selectedPriority, selectedStatus) {
        applications.filter { app ->
            val matchesSearch = app.companyName.contains(searchQuery, ignoreCase = true) || 
                                app.jobTitle.contains(searchQuery, ignoreCase = true) ||
                                app.location.contains(searchQuery, ignoreCase = true)
            
            val matchesPriority = selectedPriority == "All" || app.priority.equals(selectedPriority, ignoreCase = true)
            val matchesStatus = selectedStatus == "All" || app.currentStatus.equals(selectedStatus, ignoreCase = true)

            matchesSearch && matchesPriority && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "OFFLINE DATABASE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Applications Files",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                            color = SleekSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = SleekSecondary
                )
            )
        },
        floatingActionButton = {
            // Floating pill action button matching refined direction
            ExtendedFloatingActionButton(
                onClick = { 
                    haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    showAddDialog = true 
                },
                containerColor = SleekPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 78.dp, end = 4.dp) // shift upwards to clear floating bar beautifully
                    .testTag("add_application_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Active Node", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Entry", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Refined Claude search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search company nodes, roles, or geo-locations...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SleekPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = SleekSubtext)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("job_search_input"),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface
                )
            )

            // Filtering Row 1: Priority
            Text(
                text = "FILTER PROTOCOL: PRIORITY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp, fontSize = 9.sp),
                color = SleekSubtext,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                items(priorities) { priority ->
                    val isActive = selectedPriority == priority
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isActive) SleekPrimary.copy(alpha = 0.12f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isActive) SleekPrimary else SleekBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPriority = priority }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = priority,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isActive) SleekPrimary else SleekSecondary
                        )
                    }
                }
            }

            // Filtering Row 2: Status
            Text(
                text = "FILTER PROTOCOL: MILESTONE STATUS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp, fontSize = 9.sp),
                color = SleekSubtext,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                items(statuses) { status ->
                    val isActive = selectedStatus == status
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isActive) SleekPrimary.copy(alpha = 0.12f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isActive) SleekPrimary else SleekBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedStatus = status }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("applications_filter_${status.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isActive) SleekPrimary else SleekSecondary
                        )
                    }
                }
            }

            // Results summary header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DISCOVERED RECORDS (${filteredApps.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp, fontSize = 8.sp),
                    color = SleekSubtext
                )
            }

            // Results List using newly imported OpportunityCard
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStatePanel(
                        title = "No database matches",
                        subtitle = "Adjust your filtering protocols or query keywords to discover logged records.",
                        buttonText = "Clear Queries",
                        onButtonClick = {
                            searchQuery = ""
                            selectedPriority = "All"
                            selectedStatus = "All"
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredApps, key = { it.id }) { app ->
                        OpportunityCard(
                            app = app,
                            onClick = { viewModel.navigateTo(Screen.Detail(app.id)) }
                        )
                    }
                    item { 
                        // bottom spacer padding to guarantee scrolled items are not blocked by the bottom pill navbar
                        Spacer(modifier = Modifier.height(115.dp)) 
                    }
                }
            }
        }

        // Add application overlay using newly imported dialog from SharedUiComponents.kt
        if (showAddDialog) {
            AddApplicationDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { company, title, loc, source, status, pri, salary, note ->
                    viewModel.addApplicationManually(
                        companyName = company,
                        jobTitle = title,
                        location = loc,
                        source = source,
                        status = status,
                        priority = pri,
                        salaryRange = salary,
                        notes = note,
                        appliedDate = System.currentTimeMillis()
                    )
                    showAddDialog = false
                }
            )
        }
    }
}
