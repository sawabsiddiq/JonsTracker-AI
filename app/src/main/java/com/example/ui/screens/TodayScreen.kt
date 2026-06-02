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
fun TodayScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()
    val syncResults by viewModel.gmailSyncResults.collectAsState()
    val syncing by viewModel.syncingState.collectAsState()
    val lastGmailScan by viewModel.lastGmailScan.collectAsState()

    val scrollState = rememberScrollState()
    val now = System.currentTimeMillis()
    
    // --- QUERY LOGIC FOR TARGETED TODAY ACTION BLOCKS ---
    
    // 1. Follow-ups due: applications where followUpDate <= now
    val followUps = applications.filter { app ->
        app.followUpDate != null && app.followUpDate <= now
    }

    // 2. Interviews to prepare for: applications with status "Interview"
    val interviewsToPrepare = applications.filter { it.currentStatus == "Interview" }

    // 3. Silent applications: status "Applied" and no updates for more than 7 days, follow up date not set
    val sevenDaysAgo = now - (7 * 24 * 3600 * 1000L)
    val silentApplications = applications.filter { app ->
        app.currentStatus == "Applied" && 
        app.updatedAt <= sevenDaysAgo && 
        app.followUpDate == null
    }

    // 4. Recent active opportunities: top 3 recently updated applications
    val recentOpportunities = applications.sortedByDescending { it.updatedAt }.take(3)

    // --- CAREER SEARCH PULSE METRICS ---
    val totalApps = applications.size
    val activeInterviewsCount = interviewsToPrepare.size
    val offersCount = applications.count { it.currentStatus == "Offer" }
    
    val pulseTitle = when {
        offersCount > 0 -> "Superior Momentum"
        activeInterviewsCount > 2 -> "Highly Accelerated"
        activeInterviewsCount > 0 -> "Active & Promising"
        totalApps > 4 -> "Steady Tracking"
        totalApps > 0 -> "Initial Setup"
        else -> "Standby"
    }
    
    val pulseSubtitle = when {
        offersCount > 0 -> "You have secured strong employment offers. Excellent run!"
        activeInterviewsCount > 0 -> "Active interviews are in progress. Keep preparing details."
        totalApps > 0 -> "Your job search funnel is active. Keep submitting resumes."
        else -> "Submit or discover job entries to kickstart your tracking nodes."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "COMMAND CENTER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily Outpost",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                            color = SleekSecondary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.triggerDemoScan() 
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SleekPrimary),
                        modifier = Modifier.testTag("load_demo_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Simulate Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            viewModel.navigateTo(Screen.Settings)
                        },
                        modifier = Modifier.testTag("app_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SleekPrimary
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            // --- HERO CAREER PULSE ---
            CareerPulseHeroCard(
                pulseTitle = pulseTitle,
                pulseSubtitle = pulseSubtitle,
                totalApps = totalApps,
                interviewsCount = activeInterviewsCount,
                offersCount = offersCount,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // --- GMAIL/GEMINI WAITING INTEL ALERT ---
            if (syncResults.isNotEmpty()) {
                TodayActionCard(
                    title = "AI Inbox review pending",
                    subtitle = "${syncResults.size} newly parsed updates are currently waiting approval",
                    icon = Icons.Default.Email,
                    badgeText = "AI Review",
                    badgeColor = SleekPrimary,
                    onClick = { viewModel.navigateTo(Screen.AiInbox) }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // --- CURRENT WORKFLOW CRITICAL ITEMS ---
            Text(
                text = "IMMEDIATE CHECK-IN STEPS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = SleekSubtext,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Interviews list / Action Cards
            if (interviewsToPrepare.isNotEmpty()) {
                interviewsToPrepare.forEach { item ->
                    TodayActionCard(
                        title = "Prepare panel with ${item.companyName}",
                        subtitle = "Target position: ${item.jobTitle}",
                        icon = Icons.Default.Face,
                        onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Follow-ups lists / Action Cards
            if (followUps.isNotEmpty()) {
                followUps.forEach { item ->
                    TodayActionCard(
                        title = "Feedback milestone over-due",
                        subtitle = "Confirm status update with ${item.companyName}.",
                        icon = Icons.Default.Warning,
                        badgeText = "Over-due",
                        badgeColor = StatusRejected,
                        onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Silent checks / Action Cards
            if (silentApplications.isNotEmpty()) {
                silentApplications.forEach { item ->
                    TodayActionCard(
                        title = "Revive Silent Node",
                        subtitle = "${item.companyName} has been silent for over 7 days.",
                        icon = Icons.Default.ThumbUp,
                        onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // If empty today checklist
            if (interviewsToPrepare.isEmpty() && followUps.isEmpty() && silentApplications.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SleekBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, tint = StatusApplied, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            "Your check-in metrics are clean. All current opportunity nodes are healthy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- RECOGNIZED RECENT PIPELINE OUTPOSTS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATEST PIPELINE DEVELOPMENTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                    color = SleekSubtext
                )
                Text(
                    text = "Explore All",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SleekPrimary),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Pipeline) }
                )
            }

            if (recentOpportunities.isEmpty()) {
                EmptyStatePanel(
                    title = "Tracking pipeline is empty",
                    subtitle = "Manually log a prospective opportunity or sync from Google Gmail to construct your visual flow.",
                    buttonText = "Log Application Node",
                    onButtonClick = { viewModel.navigateTo(Screen.Applications) }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentOpportunities.forEach { app ->
                        OpportunityCard(app = app, onClick = { viewModel.navigateTo(Screen.Detail(app.id)) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- INTELLIGENT AI COMMAND SEARCH PANEL ---
            Text(
                text = "PRESCRIBED EXPLORATION SHORTS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = SleekSubtext,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            AICommandBar(
                onOptionClick = { tag ->
                    // Simulate smart workflow filters/navigation when options pressed
                    haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    when (tag) {
                        "Which jobs need follow-up?" -> viewModel.navigateTo(Screen.Today)
                        "Prepare me for interview" -> viewModel.navigateTo(Screen.Today)
                        "Show silent applications" -> viewModel.navigateTo(Screen.Today)
                        "Summarize this week" -> viewModel.navigateTo(Screen.Insights)
                    }
                }
            )

            // Extremely important: custom padding spacer to allow clean scrolling past the floating bar overlay
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
