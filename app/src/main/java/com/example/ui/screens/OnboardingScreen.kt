package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import com.example.ui.theme.AILogo
import com.example.ui.theme.cyberShadow
import com.example.ui.theme.neonGlow
import com.example.ui.theme.springClickable
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekTertiary

@Composable
fun OnboardingScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    // Entrance Animation Control state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                
                // Ambient organic clay particles representation (Dynamic visual atmosphere)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SleekPrimary.copy(alpha = 0.09f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.15f, h * 0.20f),
                        radius = w * 0.65f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SleekSecondary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.85f, h * 0.75f),
                        radius = w * 0.55f
                    )
                )
            }
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // AI Animated Cognitive Logo
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { -80 }
                        )
            ) {
                AILogo(
                    modifier = Modifier
                        .size(110.dp)
                        .neonGlow(MaterialTheme.colorScheme.primary, 3.dp, 55.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title and Subtitle with bouncy entrance
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { 50 }
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JobTrack AI",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1.8).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "An intelligent, local-first workflow tracker powered by cognitive parses.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Interactive Highlights Cards with beautiful spring transitions
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            ),
                            initialOffsetY = { 100 }
                        )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HighlightItem(
                        icon = Icons.Default.Email,
                        title = "Smart Gmail Processing",
                        desc = "Scans job applications, assessment alerts, interview schedulers & rejections securely. Deciphers dates & recruiter contacts."
                    )

                    HighlightItem(
                        icon = Icons.Default.Info,
                        title = "Local Vault & AI Parsing",
                        desc = "We parsing data directly via Gemini REST API. High-fidelity extraction on device. Your career logs remain in local SQLite."
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CTA Buttons with smooth physics-based spring tactile modifiers
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            ),
                            initialOffsetY = { 150 }
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Launch Smart Gmail Demo Box Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .cyberShadow(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, 4.dp, 24.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                            .springClickable(haptic) {
                                viewModel.scanGmail(demoMode = true)
                            }
                            .testTag("scan_gmail_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Launch Smart Gmail Demo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Configure Pipeline Manually Outlined Box Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color.Transparent, RoundedCornerShape(24.dp))
                            .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(24.dp))
                            .springClickable(haptic) {
                                viewModel.navigateTo(Screen.Dashboard)
                            }
                            .testTag("skip_to_manual_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Configure Pipeline Manually",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Consent Disclosure Card with Soft Parchment Border Glow
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .neonGlow(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), 1.dp, 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "🔒 Security Disclosure & Privacy policy",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "We prioritize your privacy. This application functions client-side. The email scans require read-only scopes. We send raw mail snippets only to Gemini API for parsing and never upload text blocks onto cloud servers. You approve all records before writing them to disk.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun HighlightItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    val haptic = LocalHapticFeedback.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .springClickable(haptic) { /* Gentle tactile pop */ }
            .cyberShadow(SleekPrimary.copy(alpha = 0.15f), SleekTertiary, 3.dp, 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
