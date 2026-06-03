package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.example.data.remote.ParsedEmail
import com.example.data.remote.JobExtractionResult
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInboxScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val syncResults by viewModel.gmailSyncResults.collectAsState()
    val syncing by viewModel.syncingState.collectAsState()
    val lastGmailScan by viewModel.lastGmailScan.collectAsState()

    val scanDays by viewModel.scanDays.collectAsState()
    val maxMessagesToFetch by viewModel.maxMessagesToFetch.collectAsState()
    val maxEmailsToAnalyze by viewModel.maxEmailsToAnalyze.collectAsState()

    val found by viewModel.scanProgressFound.collectAsState()
    val skipped by viewModel.scanProgressSkipped.collectAsState()
    val analyzed by viewModel.scanProgressAnalyzed.collectAsState()
    val detected by viewModel.scanProgressDetected.collectAsState()
    val limitReached by viewModel.scanLimitReached.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val secureStorageAvailable by viewModel.secureStorageAvailable.collectAsState()
    val gmailAccessToken by viewModel.gmailAccessToken.collectAsState()
    val connectedEmail by viewModel.gmailConnectedEmail.collectAsState()

    var editingItem by remember { mutableStateOf<Pair<ParsedEmail, JobExtractionResult>?>(null) }
    var showSetupInstructions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "INTELLIGENT REVIEW SYSTEM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AI Review Station",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                            color = SleekSecondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showSetupInstructions = true 
                        }, 
                        modifier = Modifier.testTag("show_setup_button")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Setup Sync Settings", tint = SleekPrimary)
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
            // High-end stats review summary block
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTELLIGENCE WAITING INGESTION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, letterSpacing = 0.4.sp),
                        color = SleekSubtext
                    )
                    Box(
                        modifier = Modifier
                            .background(SleekPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${syncResults.size} updates pending review",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = SleekPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Acknowledge, refine, or ignore career pipeline transitions harvested by Gemini from your email inboxes. No auto-writes trigger without manual clearance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekSubtext
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable review queue
            if (syncResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStatePanel(
                        title = "Your review queue is clean",
                        subtitle = "Intelligent nodes found in email scans appear here before affecting active opportunity logs."
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(syncResults, key = { it.first.messageId }) { pair ->
                        AIReviewCard(
                            email = pair.first,
                            res = pair.second,
                            onConfirm = { viewModel.confirmSyncResult(pair.first, pair.second) },
                            onIgnore = { viewModel.ignoreSyncResult(pair.first) },
                            onEdit = { editingItem = pair }
                        )
                    }
                    item { 
                        // bottom spacer padding so card items scroll past floating pill nav
                        Spacer(modifier = Modifier.height(115.dp)) 
                    }
                }
            }
        }

        // Setup Dialog modal
        if (showSetupInstructions) {
            AlertDialog(
                onDismissRequest = { showSetupInstructions = false },
                title = { Text("Gmail Intelligent Connection", fontWeight = FontWeight.Bold, color = SleekSecondary) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Link your active mailbox utilizing official Google OAuth to continuously scrape and model your interview schedules and application status changes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Connect Button representing real OAuth flow structure
                        val tokenStr = gmailAccessToken
                        Button(
                            onClick = { 
                                haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                showSetupInstructions = false
                                viewModel.startGoogleSignInFlow()
                            },
                            enabled = secureStorageAvailable && tokenStr.isNullOrEmpty(),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekPrimary,
                                disabledContainerColor = SleekPrimary.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(imageVector = if (!tokenStr.isNullOrEmpty()) Icons.Default.CheckCircle else Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!tokenStr.isNullOrEmpty()) "Connected to Google Mail" else "Connect via Google Mail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!secureStorageAvailable) {
                                "Secure storage is unavailable on this device. Gmail sync is disabled."
                            } else if (!tokenStr.isNullOrEmpty()) {
                                "Authorized as ${connectedEmail ?: "linked user"}"
                            } else {
                                "OAuth connection authorizes Read-Only query sweeps of your inbox."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!secureStorageAvailable) MaterialTheme.colorScheme.error else SleekSubtext,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSetupInstructions = false }) {
                        Text("Close", color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Editing Dialog overlay if active:
        editingItem?.let { pair ->
            EditExtractionDialog(
                currentResult = pair.second,
                onDismiss = { editingItem = null },
                onSave = { updatedResult ->
                    viewModel.confirmSyncResult(pair.first, updatedResult)
                    editingItem = null
                }
            )
        }

        IngestionProgressDialog(
            show = syncing,
            scanDays = scanDays,
            maxFetch = maxMessagesToFetch,
            maxAnalyze = maxEmailsToAnalyze,
            found = found,
            skipped = skipped,
            analyzed = analyzed,
            detected = detected,
            limitReached = limitReached,
            syncError = syncError
        )
    }
}
