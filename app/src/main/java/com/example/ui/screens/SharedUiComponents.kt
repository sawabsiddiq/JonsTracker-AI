package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.JobExtractionResult
import com.example.ui.theme.*

fun getPriorityColor(priority: String): Color {
    return when (priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        "low" -> Color(0xFF10B981)
        else -> Color(0xFF94A3B8)
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Saved" -> StatusSaved
        "Applied" -> StatusApplied
        "Recruiter replied" -> StatusReplied
        "Assessment" -> StatusAssessment
        "Interview" -> StatusInterview
        "Offer" -> StatusOffer
        "Rejected" -> StatusRejected
        "Ghosted" -> StatusGhosted
        else -> StatusSaved
    }
}

@Composable
fun ExtractionDataPoint(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.5.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplicationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, String, String) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Manual") }
    var status by remember { mutableStateOf("Applied") }
    var priority by remember { mutableStateOf("Medium") }
    var salary by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
    val priorityOptions = listOf("High", "Medium", "Low")

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Job Application", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name (*)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_company_input")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title (*)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_role_input")
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Remote, NY)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Job Source (e.g. LinkedIn, Recruiter)") },
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
                        label = { Text("Application Status") },
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
                        label = { Text("Priority Level") },
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
                    label = { Text("Salary / Range (e.g. £80k, $140k)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (company.isNotEmpty() && title.isNotEmpty()) {
                        onAdd(company, title, location, source, status, priority, salary, notes) 
                    }
                }
            ) {
                Text("Discard/Save", color = Color.White)
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
fun EditExtractionDialog(
    currentResult: JobExtractionResult,
    onDismiss: () -> Unit,
    onSave: (JobExtractionResult) -> Unit
) {
    var company by remember { mutableStateOf(currentResult.companyName ?: "") }
    var role by remember { mutableStateOf(currentResult.jobTitle ?: "") }
    var eventType by remember { mutableStateOf(currentResult.eventType) }
    var status by remember { mutableStateOf(currentResult.applicationStatus ?: "Applied") }
    var action by remember { mutableStateOf(currentResult.nextAction ?: "") }
    var summary by remember { mutableStateOf(currentResult.summary ?: "") }

    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Extraction Specifications", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_diag_company_input")
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_diag_role_input")
                )

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status,
                        onValueChange = {},
                        label = { Text("Suggested Status") },
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

                OutlinedTextField(
                    value = eventType,
                    onValueChange = { eventType = it },
                    label = { Text("Event Type (e.g. interview_invitation)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Next Action Event") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary Details") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        JobExtractionResult(
                            isJobRelated = true,
                            confidence = currentResult.confidence,
                            companyName = company.ifEmpty { null },
                            jobTitle = role.ifEmpty { null },
                            applicationStatus = status,
                            eventType = eventType,
                            eventDate = currentResult.eventDate,
                            nextAction = action.ifEmpty { null },
                            summary = summary.ifEmpty { null }
                        )
                    )
                }
            ) {
                Text("Apply Specs", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
