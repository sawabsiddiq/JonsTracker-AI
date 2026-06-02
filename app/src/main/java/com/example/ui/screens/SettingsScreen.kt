package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val token by viewModel.gmailAccessToken.collectAsState()
    val isAutoSync by viewModel.autoSyncEnabled.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSyncClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PREFERENCES & PRIVACY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Settings Vault",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                            color = SleekSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: Gmail Connection Status ---
            Text(
                text = "CONNECTION PREFERENCES",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = SleekPrimary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (!token.isNullOrEmpty()) SleekPrimary else SleekSubtext,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gmail Syncer Status",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekSecondary
                                )
                                Text(
                                    text = if (!token.isNullOrEmpty()) "Credentials Authorized" else "Disconnected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (!token.isNullOrEmpty()) SleekPrimary else SleekSubtext
                                )
                            }
                        }

                        if (!token.isNullOrEmpty()) {
                            Button(
                                onClick = {
                                    haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.clearGmailToken()
                                    Toast.makeText(context, "Gmail integration disconnected successfully.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("disconnect_gmail_button")
                            ) {
                                Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    if (!token.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = SleekBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Automated Syncing Nodes",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = SleekSecondary
                                )
                                Text(
                                    text = "Periodic background email sweep (15 min)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekSubtext
                                )
                            }

                            Switch(
                                checked = isAutoSync,
                                onCheckedChange = { viewModel.setAutoSync(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SleekPrimary
                                ),
                                modifier = Modifier.testTag("auto_sync_toggle")
                            )
                        }
                    }
                }
            }

            // --- SECTION 2: Data Preservation & Cleanup ---
            Text(
                text = "DATA ENGINES OPERATIONS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = SleekPrimary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Export Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Export Pipeline Logs",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                              )
                            Text(
                                text = "Dump system applications to JSON",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekSubtext
                            )
                        }

                        IconButton(
                            onClick = {
                                haptic?.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.exportData { jsonString ->
                                    clipboard.setText(AnnotatedString(jsonString))
                                    Toast.makeText(context, "Pipeline copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .background(SleekPrimary.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .testTag("export_data_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export Data", tint = SleekPrimary)
                        }
                    }

                    Divider(color = SleekBorder)

                    // Clear Sync Logs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Wipe Scrapes Logs",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Text(
                                text = "Flush analyzed email hash tags",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekSubtext
                            )
                        }

                        Button(
                            onClick = { showSyncClearDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("clear_sync_logs_button")
                        ) {
                            Text("Wipe Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Divider(color = SleekBorder)

                    // Wipe All Database Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Factory Purge Database",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Text(
                                text = "Erase all SQLite tracker tables",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekSubtext
                            )
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("wipe_database_button")
                        ) {
                            Text("Wipe SQLite", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // --- SECTION 3: Security & Privacy Policy Disclosure ---
            Text(
                text = "COGNITIVE VAULT DISCLOSURE",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = SleekPrimary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🛡️ Client-First Absolute Isolation Policy",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SleekPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. LOCAL ONLY PERSISTENCE: Your credentials, email metadata, matches, and pipeline steps are written and stored client-side in secure SQLite DB layers. We execute no telemetry.\n\n" +
                                "2. GMAIL BOUNDS: The email ingestion module requests localized read-only scopes. No auto-forwarders or background relays are initialized.\n\n" +
                                "3. ZERO-RETENTION COGNITIVE PARSING: Live token extractions are formatted client-side and analyzed directly via encrypted REST API calls to the Google Gemini endpoint. At no point do we retain, pipeline, or feed inputs into third-party vector weights.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekSubtext,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Full terms and secure encryption rules correspond to the official local sandboxed SDK protocol.",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SleekPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(115.dp))
        }
    }

    // Modal dialog for clearing Sync logs
    if (showSyncClearDialog) {
        AlertDialog(
            onDismissRequest = { showSyncClearDialog = false },
            title = { Text("Wipe Ingestion Sync History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear the history of all processed emails in your local database. Future scans may reconsider previously parsed messages.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGmailSyncHistory()
                        showSyncClearDialog = false
                        Toast.makeText(context, "Sync logs cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Wipe", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal dialog for complete wipe
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Erase All Tracking Node Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This is an irreversible factory execution! You will lose your entire career tracker history, event intervals, applications, and logs immediately.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllApplicationData()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "SQLite database purged", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("FACTORY WIPE ALL", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
