package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobApplication
import com.example.data.local.JobEvent
import com.example.ui.JobTrackerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    applicationId: Int,
    viewModel: JobTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val applications by viewModel.allApplications.collectAsState()
    val app = applications.find { it.id == applicationId }

    if (app == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Logged Application not found.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val eventsList by viewModel.getEventsForApplication(applicationId).collectAsState(initial = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.companyName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }, modifier = Modifier.testTag("detail_edit_app_button")) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit application specs")
                    }
                    IconButton(onClick = { viewModel.deleteApplication(app) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge application logs", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // General Information Header Card (Sleek Interface Styled)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = app.jobTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = app.location.ifEmpty { "Remote / Not configured" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Static status Pill
                        Box(
                            modifier = Modifier
                                .background(
                                    getStatusColor(app.currentStatus).copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = app.currentStatus,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = getStatusColor(app.currentStatus)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Meta Details Grid
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailMetaItem(label = "Source", value = app.source, icon = Icons.Default.Search, modifier = Modifier.weight(1f))
                        DetailMetaItem(label = "Priority", value = app.priority, icon = Icons.Default.Star, valueColor = getPriorityColor(app.priority), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailMetaItem(label = "Salary", value = app.salaryRange.ifEmpty { "Not stated" }, icon = Icons.Default.Star, modifier = Modifier.weight(1f))
                        DetailMetaItem(label = "Follow Up", value = if (app.nextAction.isNotEmpty()) app.nextAction else "No tasks set", icon = Icons.Default.Done, modifier = Modifier.weight(1f))
                    }

                    if (app.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    if (app.jobUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "JD URL", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = app.jobUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                modifier = Modifier.clickable { /* open native browser */ }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Timeline Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPLICATION MILESTONE TIMELINE",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )

                Button(
                    onClick = { showAddEventDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(32.dp).testTag("add_milestone_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Milestone", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vertical Milestone Timeline List
            if (eventsList.isEmpty()) {
                Text(
                    "No tracked milestone updates yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                eventsList.forEachIndexed { index, event ->
                    TimelineEventRow(
                        event = event,
                        isLast = index == eventsList.size - 1,
                        onDelete = { viewModel.deleteEventById(event.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
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
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )
        }
    }
}

@Composable
fun TimelineEventRow(
    event: JobEvent,
    isLast: Boolean,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(event.eventDate))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Dot and Line connector left column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Circle Dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(getStatusColor(mapEventToStatusLabel(event.eventType)), CircleShape)
            )

            // Vertical line connector
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content card column
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.eventType.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = getStatusColor(mapEventToStatusLabel(event.eventType))
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (event.extractedByAi) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI parsing mark",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gemini AI Synced (${(event.confidence * 100).toInt()}% conf)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun mapEventToStatusLabel(event: String): String {
    return when (event) {
        "application_confirmation" -> "Applied"
        "recruiter_reply" -> "Recruiter replied"
        "assessment_invitation" -> "Assessment"
        "interview_invitation", "interview_reschedule" -> "Interview"
        "offer" -> "Offer"
        "rejection" -> "Rejected"
        "ghosted" -> "Ghosted"
        else -> "Saved"
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Application specs") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_role_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status,
                        onValueChange = {},
                        label = { Text("Pipelines State") },
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

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Next follow-up action") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary Range") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Job Board Listing Link") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Description/Notes") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
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
                            notes = notes,
                            jobUrl = url,
                            nextAction = action
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Details", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
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
    val options = listOf<Pair<String, String>>(
        "application_confirmation" to "Application Confirmation Sent",
        "recruiter_reply" to "Recruiter Replied back",
        "assessment_invitation" to "Assessment Link received",
        "interview_invitation" to "Interview Schedule Call Offer",
        "offer" to "Official Job Offer Received!",
        "rejection" to "Rejection Notice (Not advancing)",
        "ghosted" to "Ghosted (No response 14d+)"
    )

    var selectedType by remember { mutableStateOf("interview_invitation") }
    var summary by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Milestone") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = options.find { it.first == selectedType }?.second ?: selectedType,
                        onValueChange = {},
                        label = { Text("Milestone Milestone") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Event summary (e.g. 'Intro call with Sarah')") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (summary.isNotEmpty()) onAdd(selectedType, summary) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Milestone", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
