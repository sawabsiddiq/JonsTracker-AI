package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
fun AnalyticsScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
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
                title = { Text("Job Search Analytics", fontWeight = FontWeight.Bold) },
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
            // General Stats Metrics Card Row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONVERSION RATIOS LOG",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        RatioDisplayItem(title = "Response Rate", percentage = "$responseRate%", desc = "Non-applied status updates", modifier = Modifier.weight(1f))
                        RatioDisplayItem(title = "Interview conversion", percentage = "$interviewRate%", desc = "Advancement to panels", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RatioDisplayItem(title = "Rejection rate", percentage = "$rejectionRate%", desc = "Standard screen closures", modifier = Modifier.weight(1f))
                        RatioDisplayItem(title = "Offer conversion", percentage = "$conversionRate%", desc = "Secured employment contracts", modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PIPELINE CHART REPRESENTATION
            Text(
                text = "PIPELINE CARD DISTRIBUTION",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PipelineStageBarChart(
                saved = totalWithStage("Saved"),
                applied = totalWithStage("Applied"),
                replied = totalWithStage("Recruiter replied"),
                assessment = totalWithStage("Assessment"),
                interview = totalWithStage("Interview"),
                offer = totalWithStage("Offer"),
                rejected = totalWithStage("Rejected"),
                ghosted = totalWithStage("Ghosted")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action reminders (Ghost zones)
            if (dormantApps.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusInterview)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DORMANT APPLICATIONS (14+ DAYS GHOSTED)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusInterview
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The following applications have seen no response update for more than 14 days of submission. We recommend following up directly via email:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        dormantApps.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.companyName} (${item.jobTitle})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Follow up due!",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StatusInterview
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Top Job Sources Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOP RECRUITMENT CHANNELS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (sourcesMap.isEmpty()) {
                        Text(
                            "Audit recruitment data once search points are saved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        sourcesMap.forEachIndexed { i, source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${i + 1}. ${source.first}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${source.second} applications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // padding for menu
        }
    }
}

@Composable
fun RatioDisplayItem(
    title: String,
    percentage: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            lineHeight = 12.sp
        )
    }
}

@Composable
fun PipelineStageBarChart(
    saved: Int,
    applied: Int,
    replied: Int,
    assessment: Int,
    interview: Int,
    offer: Int,
    rejected: Int,
    ghosted: Int
) {
    val list = listOf(
        "Saved" to saved,
        "Applied" to applied,
        "Replied" to replied,
        "Test" to assessment,
        "Interview" to interview,
        "Offer" to offer,
        "Rejects" to rejected,
        "Ghosted" to ghosted
    )

    val maxVal = list.maxOfOrNull { it.second } ?: 1
    val normalizedMax = if (maxVal == 0) 1 else maxVal

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            list.forEach { stage ->
                val ratio = stage.second.toFloat() / normalizedMax
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stage.first,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.width(68.dp)
                    )

                    // Draw Horizontal Dynamic Bar Graph
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        if (stage.second > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ratio)
                                    .fillMaxHeight()
                                    .background(
                                        getStatusColor(if (stage.first == "Test") "Assessment" else if (stage.first == "Rejects") "Rejected" else stage.first),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stage.second.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
