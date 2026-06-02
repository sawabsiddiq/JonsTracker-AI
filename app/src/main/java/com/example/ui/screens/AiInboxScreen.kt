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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.example.data.remote.DemoEmail
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

    var editingItem by remember { mutableStateOf<Pair<DemoEmail, JobExtractionResult>?>(null) }
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
                        subtitle = "Intelligent nodes found in email scans appear here before affecting active opportunity logs.",
                        buttonText = "Simulate New Inbox Email",
                        onButtonClick = { viewModel.triggerDemoScan() }
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
            val liveToken by viewModel.gmailAccessToken.collectAsState()
            val autoSync by viewModel.autoSyncEnabled.collectAsState()
            var tokenInput by remember(liveToken) { mutableStateOf(liveToken ?: "") }
            var isDeveloperMode by remember { mutableStateOf(false) }

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

                        // Connect Button representing future OAuth flow structure
                        Button(
                            onClick = { 
                                haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.saveGmailToken("ya29.mock-oauth-token-precompiled")
                                showSetupInstructions = false
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect via Google Calendar/Gmail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Developer Mode toggle to hide ya29. paste from normal users
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Developer Sandbox Mode", style = MaterialTheme.typography.labelMedium, color = SleekSecondary)
                            Switch(
                                checked = isDeveloperMode,
                                onCheckedChange = { isDeveloperMode = it },
                                modifier = Modifier.testTag("developer_mode_switch")
                            )
                        }

                        if (isDeveloperMode) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = tokenInput,
                                onValueChange = { tokenInput = it },
                                label = { Text("Manual OAuth Token (ya29...)") },
                                trailingIcon = {
                                    if (tokenInput.isNotEmpty()) {
                                        IconButton(onClick = { tokenInput = "" }) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("token_input_field")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.saveGmailToken(tokenInput.ifEmpty { null })
                                    showSetupInstructions = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Sandbox Token", fontWeight = FontWeight.Bold)
                            }
                        }
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
    }
}
