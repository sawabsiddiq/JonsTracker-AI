package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobApplication
import com.example.data.local.JobEvent
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    applicationId: Int,
    viewModel: JobTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()
    val app = applications.find { it.id == applicationId }

    if (app == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Logged Application not found.", style = MaterialTheme.typography.titleMedium, color = SleekSecondary)
        }
        return
    }

    val eventsList by viewModel.getEventsForApplication(applicationId).collectAsState(initial = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "OPPORTUNITY DOSSIER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = app.companyName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                            color = SleekSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            viewModel.navigateBack() 
                        }, 
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Return", tint = SleekPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showEditDialog = true 
                        }, 
                        modifier = Modifier.testTag("detail_edit_app_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Specs", tint = SleekPrimary)
                    }
                    IconButton(
                        onClick = { 
                            haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.deleteApplication(app) 
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge Case File", tint = StatusRejected)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = SleekSecondary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            
            // --- HERO DOSSIER CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, SleekBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.jobTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
                                color = SleekSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = app.location.ifEmpty { "Location unspecified" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekSubtext
                                )
                            }
                        }

                        StatusChip(status = app.currentStatus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SleekBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Specs Matrix
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailMetaItem(label = "Source", value = app.source, icon = Icons.Default.Info, modifier = Modifier.weight(1.5f))
                        DetailMetaItem(label = "Priority Level", value = app.priority, icon = Icons.Default.Star, valueColor = getPriorityColor(app.priority), modifier = Modifier.weight(1.2f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailMetaItem(label = "Salary Range", value = app.salaryRange.ifEmpty { "Unspecified" }, icon = Icons.Default.ShoppingCart, modifier = Modifier.weight(1.5f))
                        DetailMetaItem(label = "Next Action Step", value = app.nextAction.ifEmpty { "Await response" }, icon = Icons.Default.PlayArrow, modifier = Modifier.weight(1.2f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val fullDateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        val updatedFull = fullDateFormatter.format(Date(app.updatedAt))
                        DetailMetaItem(label = "Last Synchronized", value = updatedFull, icon = Icons.Default.Refresh, modifier = Modifier.weight(1.5f))
                    }

                    // JD / Job URL
                    if (app.jobUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SleekNavBarBg, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "JD", tint = SleekPrimary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = app.jobUrl,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = SleekPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Personal specs Notes
                    if (app.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "OFFICIAL DOSSIER ANNOTATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, fontSize = 8.sp),
                            color = SleekSubtext
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SleekNavBarBg, RoundedCornerShape(14.dp))
                                .border(1.dp, SleekBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = app.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- EVIDENCE / TIMELINE STATION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPLICATION TIMELINE ACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                    color = SleekSubtext
                )

                Button(
                    onClick = { 
                        haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        showAddEventDialog = true 
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    modifier = Modifier.height(30.dp).testTag("add_milestone_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Milestone", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeline Event Card listing
            if (eventsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Initialize tracking by logging a timeline milestone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekSubtext
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    eventsList.forEach { event ->
                        TimelineEventCard(
                            event = event,
                            onDelete = { 
                                haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.deleteEventById(event.id) 
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Dialog: Edit Application Specs
        if (showEditDialog) {
            EditApplicationDialog(
                app = app,
                onDismiss = { showEditDialog = false },
                onSave = { updatedApp ->
                    viewModel.updateApplicationDetails(updatedApp)
                    showEditDialog = false
                }
            )
        }

        // Dialog: Add Custom Milestone Event
        if (showAddEventDialog) {
            AddMilestoneDialog(
                onDismiss = { showAddEventDialog = false },
                onAdd = { type, summary ->
                    viewModel.addTimelineEvent(app.id, type, summary, System.currentTimeMillis())
                    showAddEventDialog = false
                }
            )
        }
    }
}

@Composable
fun DetailMetaItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = SleekSecondary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(SleekNavBarBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SleekPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 7.sp, letterSpacing = 0.3.sp),
                color = SleekSubtext
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApplicationDialog(
    app: JobApplication,
    onDismiss: () -> Unit,
    onSave: (JobApplication) -> Unit
) {
    var company by remember { mutableStateOf(app.companyName) }
    var title by remember { mutableStateOf(app.jobTitle) }
    var location by remember { mutableStateOf(app.location) }
    var source by remember { mutableStateOf(app.source) }
    var status by remember { mutableStateOf(app.currentStatus) }
    var priority by remember { mutableStateOf(app.priority) }
    var salary by remember { mutableStateOf(app.salaryRange) }
    var notes by remember { mutableStateOf(app.notes) }
    var url by remember { mutableStateOf(app.jobUrl) }
    var action by remember { mutableStateOf(app.nextAction) }

    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
    var statusExpanded by remember { mutableStateOf(false) }

    val priorityOptions = listOf("High", "Medium", "Low")
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Refine Dossier details", fontWeight = FontWeight.ExtraBold, color = SleekSecondary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_input")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth().testTag("edit_role_input")
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status,
                        onValueChange = {},
                        label = { Text("Status Coordinate") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    status = selection
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = priority,
                        onValueChange = {},
                        label = { Text("Priority") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        priorityOptions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    priority = selection
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary Range") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Job Link URL") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Prescribed Next Action") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Personal Annotations") },
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        app.copy(
                            companyName = company,
                            jobTitle = title,
                            location = location,
                            source = source,
                            currentStatus = status,
                            priority = priority,
                            salaryRange = salary,
                            jobUrl = url,
                            nextAction = action,
                            notes = notes,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Apply Refinements", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SleekPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var type by remember { mutableStateOf("interview_invitation") }
    var summary by remember { mutableStateOf("") }

    val options = listOf(
        "application_confirmation" to "Applied Confirmation",
        "recruiter_reply" to "Recruiter Replied",
        "assessment_invitation" to "Assessment Challenge",
        "interview_invitation" to "Interview Invitation",
        "interview_reschedule" to "Interview Reschedule",
        "offer" to "Job Offer Contract",
        "rejection" to "Rejection Node",
        "ghosted" to "Ghosted Thread"
    )
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track New Milestone", fontWeight = FontWeight.ExtraBold, color = SleekSecondary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val currentLabel = options.find { it.first == type }?.second ?: type
                    OutlinedTextField(
                        readOnly = true,
                        value = currentLabel,
                        onValueChange = {},
                        label = { Text("Milestone Category") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.second) },
                                onClick = {
                                    type = item.first
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Timeline Details / Summary") },
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekPrimary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_milestone_desc")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (summary.isNotEmpty()) onAdd(type, summary) },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_milestone_button")
            ) {
                Text("Log Milestone", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SleekPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
