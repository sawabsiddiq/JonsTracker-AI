package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.ui.theme.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobApplication
import com.example.ui.JobTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val applications by viewModel.allApplications.collectAsState()

    val total = applications.size
    val totalWithStage = { stage: String -> applications.count { it.currentStatus.equals(stage, ignoreCase = true) } }

    // Funnel diagnostics
    val responseCount = applications.count {
        it.currentStatus != "Applied" && it.currentStatus != "Saved" && it.currentStatus != "Ghosted"
    }
    val responseRate = if (total > 0) (responseCount.toFloat() / total * 100).toInt() else 0
    val interviewRate = if (total > 0) (totalWithStage("Interview").toFloat() / total * 100).toInt() else 0
    val rejectionRate = if (total > 0) (totalWithStage("Rejected").toFloat() / total * 100).toInt() else 0
    val conversionRate = if (total > 0) ((totalWithStage("Offer").toFloat() / total) * 100).toInt() else 0

    // Discover maximum channels
    val sourcesMap = applications.groupBy { it.source }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    // Ghost zones diagnostics
    val fourteenDaysAgo = System.currentTimeMillis() - (14 * 24 * 3600 * 1000L)
    val dormantApps = applications.filter {
        (it.currentStatus == "Applied" || it.currentStatus == "Recruiter replied") &&
                it.updatedAt <= fourteenDaysAgo
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "OFFICIAL ANALYTICS PORTAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Funnel Insights",
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            
            // --- CONVERSIONS LEADERBOARD GRID ---
            Text(
                text = "ACTIVE FUNNEL STATISTICS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = SleekSubtext,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightMetricCard(
                            label = "Response Rate",
                            value = "$responseRate%",
                            subtitle = "funnel traction",
                            modifier = Modifier.weight(1f)
                        )
                        InsightMetricCard(
                            label = "Interview conversion",
                            value = "$interviewRate%",
                            subtitle = "interview trace",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightMetricCard(
                            label = "Offer Success Rate",
                            value = "$conversionRate%",
                            subtitle = "final rate",
                            modifier = Modifier.weight(1f)
                        )
                        InsightMetricCard(
                            label = "Total Career Nodes",
                            value = "$total tracked",
                            subtitle = "entries cataloged",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- TAILORED STRATEGIC ADVICE PANELS ---
            Text(
                text = "TAILORED STRATEGIC ADVICE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = SleekSubtext,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Advice 1: Response Rate optimizer
            AdviceCard(
                adviceText = "Friction & ATS Profile Diagnostic",
                subtitle = if (responseRate < 25) {
                    "Your response coordinate resolves at $responseRate%. Consider auditing major skills on your resume profile. Aligning exact industry keywords with recruiter descriptors can trigger up to a 35% response uptick."
                } else {
                    "Your response coordinate is excellent ($responseRate%)! Maintain pipeline pressure by booking screening sync calls promptly and scheduling technical prep sessions."
                },
                icon = Icons.Default.Info
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Advice 2: Pre Interview tactics
            AdviceCard(
                adviceText = "Interviewing Execution Plan",
                subtitle = if (interviewRate == 0) {
                    "Interview conversion registers at 0%. Try expanding discovery. Submitting entries to 3 more premium matches this cycle geometrically raises technical callback probability."
                } else {
                    "With active interviews in progress ($interviewRate%), refine your STAR architecture stories focusing on cloud scalability, project ownership limits, and personal leadership metrics."
                },
                icon = Icons.Default.PlayArrow
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Advice 3: Referral Channels
            if (sourcesMap.isNotEmpty()) {
                val bestSource = sourcesMap.first().first
                AdviceCard(
                    adviceText = "Optimal Discovery Channels",
                    subtitle = "The source '$bestSource' yields the most engagement trace with ${sourcesMap.first().second} submissions. Double-down here. Formulating direct touchpoints with teams inside '$bestSource' can bypass normal queue blocks.",
                    icon = Icons.Default.Check
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Ghost status alarms remediation
            if (dormantApps.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekNavBarBg),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, StatusInterview.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusInterview, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DORMANT TRAVELLER RETRIEVALS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                                color = StatusInterview
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "You currently have ${dormantApps.size} opportunity nodes silent for more than 14 days. Re-initiate contact to clear dormant pipeline items:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        dormantApps.take(2).forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SleekSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${item.companyName} — ${item.jobTitle}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = SleekSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Polite follow-up template: 'Hi team, checking in on my ${item.jobTitle} submission. I remain very interested in the vision at ${item.companyName} and would love to catch up if a tech slot opens.'",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = SleekSubtext
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // critical bottom padding scroll allowance past floating bottom nav
            Spacer(modifier = Modifier.height(115.dp))
        }
    }
}
