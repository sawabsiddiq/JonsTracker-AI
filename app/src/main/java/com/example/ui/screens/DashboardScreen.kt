package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.local.JobApplication
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()
    val processedCount by viewModel.processedEmails.collectAsState()
    val syncing by viewModel.syncingState.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val lastGmailScan by viewModel.lastGmailScan.collectAsState()
    val liveToken by viewModel.gmailAccessToken.collectAsState()

    val lastScanFormatted = remember(lastGmailScan) {
        if (lastGmailScan == 0L) {
            "Never synced"
        } else {
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            "Synced: " + formatter.format(Date(lastGmailScan))
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showScanInstructions by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Key Stats Calculation
    val totalApps = applications.size
    val activeInterviews = applications.count { it.currentStatus == "Interview" }
    val activeOffers = applications.count { it.currentStatus == "Offer" }
    val pendingReplies = applications.count { it.currentStatus == "Applied" || it.currentStatus == "Recruiter replied" }
    val rejections = applications.count { it.currentStatus == "Rejected" }

    // Applied this week (last 7 days)
    val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 3600 * 1000L)
    val appliedThisWeek = applications.count { it.appliedDate >= oneWeekAgo }

    val syncResults by viewModel.gmailSyncResults.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp) // space for bottom navigation
        ) {
            // Welcome Header Block (Sleek Interface Flat Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AILogo(
                            modifier = Modifier.size(44.dp)
                        )
                        Column {
                            Text(
                                text = "JobTrack AI",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tracking ${totalApps} nodes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Direct Gmail Scanner button
                        if (syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                                    .clip(CircleShape)
                                    .springClickable(haptic) { showScanInstructions = true }
                                    .testTag("quick_sync_icon"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Manual Scanning trigger",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Sleek Interface JD Badge Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                .neonGlow(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), 1.5.dp, 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Visually integrated Sync Status indicator & quick Sync Now action
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .neonGlow(
                        glowColor = if (syncing) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                        borderRadius = 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (lastGmailScan == 0L) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Sync status icon",
                                        tint = if (lastGmailScan == 0L) MaterialTheme.colorScheme.outline
                                               else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (lastGmailScan == 0L) Color.Gray
                                            else Color(0xFF4CAF50),
                                            CircleShape
                                        )
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (syncing) "Syncing messages..." else "Gmail AI Processor",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = lastScanFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .background(
                                if (syncing) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .springClickable(haptic) {
                                if (!syncing) {
                                    viewModel.scanGmail(demoMode = liveToken.isNullOrBlank())
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("sync_now_header_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Sync Now",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Sync status/error/notice panel
            syncError?.let { msg ->
                val isError = msg.contains("error", ignoreCase = true) || msg.contains("fail", ignoreCase = true) || msg.contains("invalid", ignoreCase = true)
                val isSuccess = msg.contains("reset", ignoreCase = true) || msg.contains("clean", ignoreCase = true) || msg.contains("fully synced", ignoreCase = true) || msg.contains("secure", ignoreCase = true) || msg.contains("analyzed", ignoreCase = true)

                val containerColor = when {
                    isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
                    isSuccess -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
                val borderColor = when {
                    isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                    isSuccess -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                }
                val icon = when {
                    isError -> Icons.Default.Warning
                    isSuccess -> Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                }
                val tintColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isSuccess -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
                val textColor = when {
                    isError -> MaterialTheme.colorScheme.onErrorContainer
                    isSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Notification",
                                tint = tintColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Under information messages, play with an inline interactive "Reset Simulation Logs" triggers
                        if (!isError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = borderColor.copy(alpha = 0.3f), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { viewModel.resetSyncLogs() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset scan metadata",
                                        tint = tintColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reset Sync Demo",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tintColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // High Precision Primary Metrics (2x2 Grid with sleek styling)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "TOTAL HUNTED",
                        value = totalApps.toString(),
                        subtitle = "Applications saved",
                        icon = Icons.Default.Home,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "INTERVIEWS",
                        value = activeInterviews.toString(),
                        subtitle = "Active panels",
                        icon = Icons.Default.PlayArrow,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "OFFERS RECEIVED",
                        value = activeOffers.toString(),
                        subtitle = "Crushed path",
                        icon = Icons.Default.CheckCircle,
                        color = StatusOffer,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "SENT THIS WEEK",
                        value = appliedThisWeek.toString(),
                        subtitle = "+$appliedThisWeek application",
                        icon = Icons.Default.Star,
                        color = StatusApplied,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Secondary Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MiniStatItem(label = "Waiting Reply", count = pendingReplies, color = StatusApplied)
                MiniStatItem(label = "Rejections", count = rejections, color = StatusRejected)
                MiniStatItem(label = "Synced Emails", count = processedCount.size, color = StatusOffer)
            }

            // Sleek Interface Gmail Smart Sync CTA Banner with 3D Paper Shadow & Peach gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .cyberShadow(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, 5.dp, 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gmail Smart Sync",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (syncResults.isNotEmpty()) "${syncResults.size} new updates found by Gemini AI" else "7 new updates parsed by Gemini",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.GmailSyncReview) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Review",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recent Applications Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATEST LOGGED",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "See Pipeline",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Pipeline) }
                )
            }

            if (applications.isEmpty()) {
                // Empty state card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty Pipeline",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your pipeline is clean & waiting.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Sync job-related emails using Gemini parser or tap the + button below to log manually.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // Show latest 3 applications
                applications.take(3).forEach { app ->
                    RecentAppCard(
                        app = app,
                        onClick = {
                            viewModel.navigateTo(Screen.Detail(app.id))
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateApplicationDetails(app.copy(currentStatus = newStatus))
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
                                summary = "Status updated manually from dashboard to '$newStatus'.",
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Floating manual addition button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 24.dp)
                .size(56.dp)
                .cyberShadow(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, 4.dp, 24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .springClickable(haptic) { showAddDialog = true }
                .testTag("add_manual_app_fab"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add manual application log",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // 1. Manual Application entry dialog:
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

        // 2. Scan Gmail dialog / choice selector:
        if (showScanInstructions) {
            val liveToken by viewModel.gmailAccessToken.collectAsState()
            val autoSync by viewModel.autoSyncEnabled.collectAsState()
            var tokenInput by remember(liveToken) { mutableStateOf(liveToken ?: "") }
            var showHelpInstructions by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showScanInstructions = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Gmail Intelligent Sync Setup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Sync and scan real Gmail conversations of the last 15 days using our advanced Gemini classification models.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        // Clean step-by-step assistant box for better UX
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (showHelpInstructions)
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (showHelpInstructions)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHelpInstructions = !showHelpInstructions }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Setup Instructions (Live Gmail)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showHelpInstructions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Instructions",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (showHelpInstructions) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Since the browser streaming emulator runs in a sandbox and does not support Google custom sign-in popups, please supply a temporary Access Token:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val steps = listOf(
                                        "1. Navigate to: developers.google.com/oauthplayground",
                                        "2. Under Step 1, select 'Gmail API v1' and check: https://www.googleapis.com/auth/gmail.readonly",
                                        "3. Click 'Authorize APIs' & authorize your Gmail account,",
                                        "4. Click 'Exchange authorization code' & copy the 'Access Token' (starts with ya29.)"
                                    )

                                    steps.forEach { step ->
                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Click to view 4 simple steps to connect your actual inbox.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Text(
                            "Google Account Authorization Token:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            placeholder = { Text("Paste 'ya29...' access token here.") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gmail_oauth_token_input"),
                            trailingIcon = {
                                if (liveToken != null) {
                                    IconButton(
                                        onClick = {
                                            viewModel.clearGmailToken()
                                            tokenInput = ""
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Disconnect Account",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        )

                        if (!liveToken.isNullOrEmpty()) {
                            Text(
                                "✓ Connected to Google Services",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusOffer
                            )
                        } else {
                            Text(
                                "Using sandbox presets if empty. Connect above to scan live.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Real-Time Auto-Sync Events",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Continuously analyze upcoming email updates of jobs automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Switch(
                                checked = autoSync,
                                onCheckedChange = { viewModel.setAutoSync(it) },
                                modifier = Modifier.testTag("auto_sync_switch")
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showScanInstructions = false
                            if (tokenInput.isNotBlank()) {
                                viewModel.saveGmailToken(tokenInput.trim())
                            } else {
                                viewModel.scanGmail(demoMode = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (tokenInput.isNotBlank()) "Authenticate & Scan Live" else "Scan Sandbox Presets", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScanInstructions = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.2.dp, 
            Brush.linearGradient(
                listOf(color.copy(alpha = 0.7f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            )
        ),
        modifier = modifier
            .height(135.dp)
            .springClickable(haptic) { /* Gentle tactile pop */ }
            .neonGlow(color.copy(alpha = 0.12f), 1.5.dp, 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MiniStatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun RecentAppCard(
    app: JobApplication,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(app.appliedDate))

    val updateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val updatedDateString = updateFormatter.format(Date(app.updatedAt))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .springClickable(haptic) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.companyName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.jobTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Applied $dateString",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Last updated $updatedDateString",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Interactive Status chip badge with ArrowDropDown
            var expanded by remember { mutableStateOf(false) }

            Box {
                Box(
                    modifier = Modifier
                        .background(
                            getStatusColor(app.currentStatus).copy(alpha = 0.15f),
                            CircleShape
                        )
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.currentStatus,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = getStatusColor(app.currentStatus)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Edit status",
                            tint = getStatusColor(app.currentStatus),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                expanded = false
                                onStatusChange(status)
                            }
                        )
                    }
                }
            }
        }
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
        title = { Text("Log Job Application") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name (*)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_company_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title (*)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_role_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Remote, NY)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Job Source (e.g. LinkedIn, Recruiter)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Status Dropdown selector
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

                Spacer(modifier = Modifier.height(8.dp))

                // Priority Dropdown selector
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

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary / Range (e.g. £80k, \$140k)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (company.isNotEmpty() && title.isNotEmpty()) onAdd(company, title, location, source, status, priority, salary, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Log Application", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
