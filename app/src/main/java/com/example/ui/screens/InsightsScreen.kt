package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

    // Ratios & Rates
    val responseCount = applications.count {
        it.currentStatus != "Applied" && it.currentStatus != "Saved" && it.currentStatus != "Ghosted"
    }
    val responseRate = if (total > 0) (responseCount.toFloat() / total * 100).toInt() else 0
    val interviewRate = if (total > 0) (totalWithStage("Interview").toFloat() / total * 100).toInt() else 0
    val rejectionRate = if (total > 0) (totalWithStage("Rejected").toFloat() / total * 100).toInt() else 0
    val conversionRate = if (total > 0) ((totalWithStage("Offer").toFloat() / total) * 100).toInt() else 0

    // Top sources
    val sourcesMap = applications.groupBy { it.source }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    // Ghost zones (No update for 14d+ and status = Applied or Recruiter Replied)
    val fourteenDaysAgo = System.currentTimeMillis() - (14 * 24 * 3600 * 1000L)
    val dormantApps = applications.filter {
        (it.currentStatus == "Applied" || it.currentStatus == "Recruiter replied") &&
                it.updatedAt <= fourteenDaysAgo
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Career Insights", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {
            // Summary Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CURRENT FUNNEL CONVERSIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ConversionItem(title = "Response Rate", value = "$responseRate%", modifier = Modifier.weight(1f))
                        ConversionItem(title = "Interview conversion", value = "$interviewRate%", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ConversionItem(title = "Offer Conversion", value = "$conversionRate%", modifier = Modifier.weight(1f))
                        ConversionItem(title = "Active Nodes", value = "$total tracked", modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- STRATEGIC ADVICE ---
            Text(
                text = "TAILORED CAREER COUNSELING",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Advice Card 1: Response Optimization
            AdviceCard(
                title = "Pipeline Conversions Coaching",
                description = if (responseRate < 25) {
                    "Your response rate is currently at $responseRate%. Consider customizing major skills on your resume profile. Ensure standard ATS terms match the keywords in active software or product descriptions to boost recruiter scan rate by up to 35%."
                } else {
                    "Your response rate is high! ($responseRate%). Capitalize on this momentum by requesting early 1-on-1 team descriptions during screening, keeping the conversational touch authentic."
                },
                icon = Icons.Default.ThumbUp,
                color = Color(0xFF6366F1)
            )

            // Advice Card 2: Interviewing Strategy
            AdviceCard(
                title = "Interview prep strategies",
                description = if (interviewRate == 0) {
                    "Your panel conversion stands at 0%. Try expanding your discovery loop. Saving and applying to 4 more high-relevance roles this week increases the geometric possibility of securing interview invites."
                } else {
                    "With a solid interviewer footprint ($interviewRate%), prioritize mock presentation reviews! Deeply research the company's tech stacks, and prepare 3 clean STAR method stories addressing software architectural scaling."
                },
                icon = Icons.Default.PlayArrow,
                color = Color(0xFFF59E0B)
            )

            // Advice Card 3: Channel Optimizers
            if (sourcesMap.isNotEmpty()) {
                val bestSource = sourcesMap.first().first
                AdviceCard(
                    title = "Optimal Recruitment Channels",
                    description = "Your highest frequency channel is '$bestSource' with ${sourcesMap.first().second} applications. Since '$bestSource' holds excellent traction, double down there! Consider setting daily alert notifications or direct-messaging hiring managers directly on that channel to trigger interviews.",
                    icon = Icons.Default.Check,
                    color = Color(0xFF10B981)
                )
            }

            // Advice Card 4: Proactive Response Booster (Ghost Zone Actions)
            if (dormantApps.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusInterview)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GHOST STATIONS REMEDIATION ADVICE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusInterview
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "You have ${dormantApps.size} applications silent for over 14 days of submission. We strongly recommend sending a brief follow-up email inquiring about their current hiring timeframes. This simple touch point can pull silent items back into recruiter view:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        dormantApps.take(3).forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                                    .padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "${item.companyName} — ${item.jobTitle}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Suggested check-in: 'Hi team, checking in on my ${item.jobTitle} submission. I remain very enthusiastic about the work you do. I'd love to update you on my recent progress if a slot is open.'",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom menu
        }
    }
}

@Composable
fun ConversionItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AdviceCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
