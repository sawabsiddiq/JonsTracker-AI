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
                title = { Text("AI Inbox Review", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSetupInstructions = true }, modifier = Modifier.testTag("show_setup_button")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Setup Sync Settings")
                    }
                },
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
            // Stats card
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI-DETECTION INBOX QUEUE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${syncResults.size} updates pending approval",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verify critical details like roles, suggested milestones, or actions before they enter the career pipeline database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable column cards
            if (syncResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Inbox clean",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Inbox clear!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New job extraction results from background email scans will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.triggerDemoScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Simulate income update email", color = Color.White)
                        }
                    }
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
                        ReviewCard(
                            email = pair.first,
                            res = pair.second,
                            onConfirm = { viewModel.confirmSyncResult(pair.first, pair.second) },
                            onIgnore = { viewModel.ignoreSyncResult(pair.first) },
                            onEdit = { editingItem = pair }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
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
                title = { Text("Gmail Intelligent Sync") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Securely connect your Gmail box using OAuth to automatically discover updates to your application nodes.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Connect Button representing future OAuth flow structure
                        Button(
                            onClick = { 
                                // Simulated OAuth entry point
                                haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.saveGmailToken("ya29.mock-oauth-token-precompiled")
                                showSetupInstructions = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.AccountBox, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Gmail (Sign in with Google)", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Developer Mode toggle to hide ya29. paste from normal users
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Developer Sandbox Mode", style = MaterialTheme.typography.labelMedium)
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
                                modifier = Modifier.fillMaxWidth().testTag("token_input_field")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.saveGmailToken(tokenInput.ifEmpty { null })
                                    showSetupInstructions = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Manual Token")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSetupInstructions = false }) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
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

@Composable
fun ReviewCard(
    email: DemoEmail,
    res: JobExtractionResult,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    onEdit: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val confidencePct = (res.confidence * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Email Sender & Subject header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Sender",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = email.sender,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = email.subject,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            getStatusColor(res.applicationStatus ?: "Applied").copy(alpha = 0.15f),
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = res.applicationStatus ?: "Applied",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = getStatusColor(res.applicationStatus ?: "Applied")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            // Extraction Properties Displays
            Row(modifier = Modifier.fillMaxWidth()) {
                ExtractionDataPoint(label = "Company Name", value = res.companyName ?: "Inferring...", modifier = Modifier.weight(1f))
                ExtractionDataPoint(label = "Position Role", value = res.jobTitle ?: "Inferring...", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ExtractionDataPoint(label = "Event Type", value = res.eventType.replace("_", " ").uppercase(), modifier = Modifier.weight(1f))
                ExtractionDataPoint(label = "Action item", value = res.nextAction ?: "Await next steps", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI summary box
            if (!res.summary.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "GEMINI SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = res.summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Confidence score row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Confidence Level: $confidencePct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Decision Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.weight(1.5f).size(42.dp).testTag("confirm_extraction_button")
                ) {
                    Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1.5f).size(42.dp).testTag("edit_extraction_button")
                ) {
                    Text("Edit Specs", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onIgnore()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f).size(42.dp).testTag("ignore_extraction_button")
                ) {
                    Text("Ignore", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }
    }
}
