package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()

    // Subway journey definitions
    val journeyStations = listOf(
        JourneyStationInfo("Saved", "Discovery Stop", "Bookmarked roles being analyzed.", Icons.Default.Star, StatusSaved),
        JourneyStationInfo("Applied", "Landing node", "Official documents successfully submitted.", Icons.Default.Send, StatusApplied),
        JourneyStationInfo("Recruiter replied", "Dialogue Hub", "Engaging with development and hiring teams.", Icons.Default.Email, StatusReplied),
        JourneyStationInfo("Assessment", "Evaluation coordinate", "Take-home code challanges and take-home evaluations.", Icons.Default.Lock, StatusAssessment),
        JourneyStationInfo("Interview", "Prep Panels", "Face-to-face active technical screens.", Icons.Default.Face, StatusInterview),
        JourneyStationInfo("Offer", "Victory Outpost", "Congratulations! Active contractual offers.", Icons.Default.Favorite, StatusOffer),
        JourneyStationInfo("Rejected", "Pivot Node", "Archived coordinates used to pivot criteria.", Icons.Default.Close, StatusRejected),
        JourneyStationInfo("Ghosted", "Dormant Track", "Await reactivation checkpoints on silent threads.", Icons.Default.Warning, StatusGhosted)
    )

    var expandedStation by remember { mutableStateOf<String?>("Interview") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "VISUAL ROADMAP MAP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Career Subway Trail",
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Subway Instructions header
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                Text(
                    text = "ACTIVE TRANSIT SYSTEM",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, letterSpacing = 0.4.sp),
                    color = SleekSubtext
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Observe active opportunity travelers trailing down distinct milestone coordinates. Expand any coordinates to coordinate transfer details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekSubtext
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable route with drawing behind vertical trace line to establish connected subway track visual
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .drawBehind {
                        // Drawing the clean vertical subway track background trace line
                        // The relative node coordinate is offset at horizontal start paddings (approx 18.dp offset)
                        val lineX = 18.dp.toPx()
                        drawLine(
                            color = SleekBorder,
                            start = Offset(lineX, 0f),
                            end = Offset(lineX, size.height),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
            ) {
                items(journeyStations) { station ->
                    val isExpanded = expandedStation == station.stage
                    val stationApps = applications.filter { it.currentStatus.equals(station.stage, ignoreCase = true) }

                    JourneyStationCard(
                        stationName = station.stage,
                        description = station.textLabel + ": " + station.description,
                        icon = station.icon,
                        appsCount = stationApps.size,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            expandedStation = if (isExpanded) null else station.stage
                        },
                        stationColor = station.baseColor
                    ) {
                        if (stationApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active opportunity travelers here.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekSubtext
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                stationApps.forEach { app ->
                                    SubwayTravelerRow(
                                        app = app,
                                        stationColor = station.baseColor,
                                        onNavigate = { viewModel.navigateTo(Screen.Detail(app.id)) },
                                        onShiftStatus = { newStatus ->
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
                                                summary = "Status updated to '$newStatus' via subway station control.",
                                                timestamp = System.currentTimeMillis()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item { 
                    // bottom padding spacer to clear the navigation bar
                    Spacer(modifier = Modifier.height(115.dp)) 
                }
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

@Composable
fun SubwayTravelerRow(
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
            .background(SleekNavBarBg, RoundedCornerShape(12.dp))
            .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
            .clickable { onNavigate() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Text(
                text = app.companyName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = SleekSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.jobTitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = SleekSubtext,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            OutlinedButton(
                onClick = { isShiftExpanded = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, stationColor.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Transfer", fontSize = 10.sp, color = stationColor, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = stationColor, modifier = Modifier.size(10.dp))
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
