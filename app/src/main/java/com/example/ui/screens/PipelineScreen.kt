package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobApplication
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()

    // Structured roadmap stops on the Career Journey
    val journeyStations = listOf(
        JourneyStationInfo("Saved", "Discovery Stop", "Applications bookmarked for potential research.", Icons.Default.Star, Color(0xFF94A3B8)),
        JourneyStationInfo("Applied", "Landing confirmation", "Official submissions sent with documents.", Icons.Default.Send, Color(0xFF3B82F6)),
        JourneyStationInfo("Recruiter replied", "Active Dialog", "Contacts established with talent teams.", Icons.Default.Email, Color(0xFF6366F1)),
        JourneyStationInfo("Assessment", "Evaluation challenge", "Take-home code tests or technical worksheets.", Icons.Default.Lock, Color(0xFFEC4899)),
        JourneyStationInfo("Interview", "Prep Panels", "Live face-to-face sessions or phone screenings.", Icons.Default.Notifications, Color(0xFFF59E0B)),
        JourneyStationInfo("Offer", "Victory Station", "Congratulations! Employment contract offers.", Icons.Default.Favorite, Color(0xFF10B981)),
        JourneyStationInfo("Rejected", "Pivot node", "Process closed. Archive for pivot learning.", Icons.Default.Close, Color(0xFFEF4444)),
        JourneyStationInfo("Ghosted", "Dormant node", "No active communications over a prolonged cycle.", Icons.Default.Warning, Color(0xFF78716C))
    )

    var expandedStation by remember { mutableStateOf<String?>("Interview") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Career Journey Trail", fontWeight = FontWeight.Bold) },
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
        ) {
            // Header instructions
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                Text(
                    text = "ROADMAP PIPELINE STATION VIA FLUID NODES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Each job application represents an active traveler. Tap a station to view lists, log events, or guide them forward down the pipeline path.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main vertical layout of Journey Stations
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(journeyStations) { station ->
                    val isExpanded = expandedStation == station.stage
                    val stationApps = applications.filter { it.currentStatus.equals(station.stage, ignoreCase = true) }

                    JourneyStationNode(
                        info = station,
                        apps = stationApps,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedStation = if (isExpanded) null else station.stage
                        },
                        onNavigateToApp = { appId ->
                            viewModel.navigateTo(Screen.Detail(appId))
                        },
                        onStatusChange = { app, newStatus ->
                            viewModel.updateApplicationDetails(app.copy(currentStatus = newStatus, updatedAt = System.currentTimeMillis()))
                            viewModel.addTimelineEvent(
                                appId = app.id,
                                type = when (newStatus) {
                                    "Saved" -> "unknown"
                                    "Applied" -> "application_confirmation"
                                    "Recruiter replied" -> "recruiter_reply"
                                    "Assessment" -> "assessment_invitation"
                                    "Interview" -> "interview_invitation"
                                    "Offer" -> "offer"
                                    "Rejected" -> "rejection"
                                    "Ghosted" -> "ghosted"
                                    else -> "unknown"
                                },
                                summary = "Status updated to '$newStatus' via Station Journey Control.",
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

data class JourneyStationInfo(
    val stage: String,
    val textLabel: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val baseColor: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun JourneyStationNode(
    info: JourneyStationInfo,
    apps: List<JobApplication>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToApp: (Int) -> Unit,
    onStatusChange: (JobApplication, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.2.dp,
            if (isExpanded) info.baseColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journey_node_${info.stage.lowercase().replace(" ", "_")}")
    ) {
        Column {
            // Station Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggle()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(info.baseColor.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, info.baseColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = info.icon, contentDescription = null, tint = info.baseColor, modifier = Modifier.size(20.dp))
                    }

                    Column {
                        Text(
                            text = info.stage,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = info.textLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = info.baseColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(info.baseColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${apps.size} traveler" + if (apps.size == 1) "" else "s",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = info.baseColor
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Stop Details",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                    )

                    if (apps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active nodes at this journey coordinate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            apps.forEach { app ->
                                JourneyTravelerRow(
                                    app = app,
                                    stationColor = info.baseColor,
                                    onNavigate = { onNavigateToApp(app.id) },
                                    onShiftStatus = { newStatus -> onStatusChange(app, newStatus) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun JourneyTravelerRow(
    app: JobApplication,
    stationColor: Color,
    onNavigate: () -> Unit,
    onShiftStatus: (String) -> Unit
) {
    var isShiftExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { onNavigate() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Text(
                text = app.companyName,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.jobTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            OutlinedButton(
                onClick = { isShiftExpanded = true },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, stationColor.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Transfer Stop", fontSize = 11.sp, color = stationColor, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = stationColor, modifier = Modifier.size(11.dp))
                }
            }

            DropdownMenu(
                expanded = isShiftExpanded,
                onDismissRequest = { isShiftExpanded = false }
            ) {
                statusOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            isShiftExpanded = false
                            onShiftStatus(opt)
                        }
                    )
                }
            }
        }
    }
}
