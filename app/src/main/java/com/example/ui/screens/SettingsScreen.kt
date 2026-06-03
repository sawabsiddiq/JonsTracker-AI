package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val secureStorageAvailable by viewModel.secureStorageAvailable.collectAsState()

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
            // --- APPEARANCE & THEME SELECTOR ---
            Text(
                text = "APPEARANCE & RE-THEMING",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = SleekPrimary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "App Color Theme",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SleekSecondary
                    )
                    
                    val themeMode by viewModel.themeMode.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            com.example.data.local.AppThemeMode.SYSTEM to "System default",
                            com.example.data.local.AppThemeMode.LIGHT to "Light",
                            com.example.data.local.AppThemeMode.DARK to "Dark"
                        ).forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            Button(
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else SleekSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_button_${label.lowercase().replace(" ", "_")}")
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- HISTORICAL GMAIL SCAN CONTROLS ---
            Text(
                text = "HISTORICAL SCAN CONTROLS (V1)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = SleekPrimary
            )

            val scanDaysVal by viewModel.scanDays.collectAsState()
            val maxMessagesVal by viewModel.maxMessagesToFetch.collectAsState()
            val maxEmailsVal by viewModel.maxEmailsToAnalyze.collectAsState()
            val includeSpamTrashVal by viewModel.includeSpamTrash.collectAsState()
            val scanModeVal by viewModel.scanMode.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    // 1. Date Range Section
                    Column {
                        Text(
                            text = "Scan Date Range",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SleekSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(7, 15, 30, 60, 90).forEach { days ->
                                val label = when (days) {
                                    90 -> "Custom"
                                    else -> "$days days"
                                }
                                val isSelected = scanDaysVal == days
                                Button(
                                    onClick = { viewModel.setScanDays(days) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) Color.White else SleekSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Divider(color = SleekBorder)

                    // 2. Fetch Limits
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "Max fetch limit",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(50, 100, 200, 300).forEach { maxF ->
                                    val isSelected = maxMessagesVal == maxF
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { viewModel.setMaxMessagesToFetch(maxF) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$maxF",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else SleekSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Max AI scans",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(25, 50, 100).forEach { maxA ->
                                    val isSelected = maxEmailsVal == maxA
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { viewModel.setMaxEmailsToAnalyze(maxA) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$maxA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else SleekSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = SleekBorder)

                    // 3. Include Spam/Trash Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Include Spam & Trash",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Text(
                                text = "Searches extra folders via in:anywhere query",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekSubtext
                            )
                        }
                        Switch(
                            checked = includeSpamTrashVal,
                            onCheckedChange = { viewModel.setIncludeSpamTrash(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SleekPrimary
                            )
                        )
                    }

                    Divider(color = SleekBorder)

                    // 4. Scan Mode Options (Cost/Depth)
                    Column {
                        Text(
                            text = "Pre-filtering scan mode",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SleekSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Low Cost", "Balanced", "Thorough").forEach { mode ->
                                val isSelected = scanModeVal == mode
                                Button(
                                    onClick = { viewModel.setScanMode(mode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) Color.White else SleekSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(mode, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

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
                                tint = if (!token.isNullOrEmpty() && secureStorageAvailable) SleekPrimary else SleekSubtext,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gmail integration status",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekSecondary
                                )
                                Text(
                                    text = if (!secureStorageAvailable) "Secure Storage Unavailable" else if (!token.isNullOrEmpty()) "Gmail Connected & Authorized" else "Disconnected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (!secureStorageAvailable) MaterialTheme.colorScheme.error else if (!token.isNullOrEmpty()) SleekPrimary else SleekSubtext
                                )
                            }
                        }

                        if (!token.isNullOrEmpty() && secureStorageAvailable) {
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

                    if (!secureStorageAvailable) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Secure storage is unavailable on this device. Gmail sync is disabled.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!token.isNullOrEmpty() && secureStorageAvailable) {
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
                                    text = "Automated email syncer",
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
                                text = "Export local JSON",
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
                                    Toast.makeText(context, "Pipeline data copied to Clipboard!", Toast.LENGTH_SHORT).show()
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
                                text = "Delete Gmail sync history",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Text(
                                text = "Flush hash table of processed emails",
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
                            Text("Flush history", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
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
                                text = "Delete all local data",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekSecondary
                            )
                            Text(
                                text = "Erase all offline SQLite tracking nodes",
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
                            Text("Delete all", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // --- SECTION 3: Security & Privacy Policy Disclosure ---
            Text(
                text = "SECURITY & PRIVACY DISCLOSURE",
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
                        text = "🔒 Privacy Policy",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SleekPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your data privacy is our absolute priority. This application implements the following strict boundaries:\n\n" +
                                "1. LOCAL ONLY PERSISTENCE: Your credentials, email metadata, matches, and application pipeline steps are written and stored client-side in secure offline SQLite database levels. We execute no analytical tracking, telemetry, or remote user profiling.\n\n" +
                                "2. SECURE EMAIL EXTRACTION via GEMINI: To analyze and extract relevant job applications, selected email body excerpts are sent securely and directly to the Google Gemini API. These raw email contents are NOT stored on any second-party servers and are processed with zero-retention.\n\n" +
                                "3. YOUR CONTROL: No email analytics or extractions affect your active job pipeline tables without your direct review and approval in the AI Inbox.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekSubtext,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Your data and privacy are fully protected under secure locally-encrypted SQLite database rules.",
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
            title = { Text("Delete Gmail Sync History?", fontWeight = FontWeight.Bold) },
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
            title = { Text("Delete All Local Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This is an irreversible factory execution! You will lose your entire career tracker history, event intervals, applications, and logs immediately.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllApplicationData()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "All data deleted successfully.", Toast.LENGTH_LONG).show()
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
