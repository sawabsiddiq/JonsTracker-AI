package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobApplication
import com.example.data.local.JobEvent
import com.example.data.remote.DemoEmail
import com.example.data.remote.JobExtractionResult
import com.example.ui.Screen
import com.example.ui.theme.*
import androidx.compose.ui.graphics.graphicsLayer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

// --- HELPER METHODS ---

fun getPriorityColor(priority: String): Color {
    return when (priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        "low" -> Color(0xFF10B981)
        else -> Color(0xFF94A3B8)
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Saved" -> StatusSaved
        "Applied" -> StatusApplied
        "Recruiter replied" -> StatusReplied
        "Assessment" -> StatusAssessment
        "Interview" -> StatusInterview
        "Offer" -> StatusOffer
        "Rejected" -> StatusRejected
        "Ghosted" -> StatusGhosted
        else -> StatusSaved
    }
}

// --- UTILITY MODIFIER ---

@Composable
fun Modifier.tactileClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tactileScale"
    )
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
}

// --- UI CHIPS ---

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val baseColor = getStatusColor(status)
    Box(
        modifier = modifier
            .background(baseColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(1.dp, baseColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(baseColor, CircleShape)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = baseColor
            )
        }
    }
}

@Composable
fun PriorityChip(priority: String, modifier: Modifier = Modifier) {
    val color = getPriorityColor(priority)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = priority.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, letterSpacing = 0.5.sp),
            color = color
        )
    }
}

// --- REUSABLE BUTTONS ---

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = SleekPrimary,
            contentColor = Color.White,
            disabledContainerColor = SleekPrimary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SleekSecondary
        ),
        border = BorderStroke(1.2.dp, SleekBorder),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

// --- FLOATING ROUNDED PILL BOTTOM NAVIGATION ---

@Composable
fun FloatingBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1A382F2D), spotColor = Color(0x1A382F2D)),
        colors = CardDefaults.cardColors(containerColor = SleekNavBarBg),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(Screen.Today, Icons.Default.Home, "Today"),
                Triple(Screen.Applications, Icons.Default.Search, "Applications"),
                Triple(Screen.AiInbox, Icons.Default.Email, "AI Inbox"),
                Triple(Screen.Pipeline, Icons.Default.Star, "Pipeline"),
                Triple(Screen.Insights, Icons.Default.Build, "Insights")
            )

            tabs.forEach { (screen, icon, label) ->
                val isSelected = when (screen) {
                    Screen.Today -> currentScreen is Screen.Today
                    Screen.Applications -> currentScreen is Screen.Applications
                    Screen.AiInbox -> currentScreen is Screen.AiInbox
                    Screen.Pipeline -> currentScreen is Screen.Pipeline
                    Screen.Insights -> currentScreen is Screen.Insights
                    else -> false
                }

                val activeAnimProgress by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 250f),
                    label = "$label-active"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            color = SleekPrimary.copy(alpha = 0.12f * activeAnimProgress),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            SleekPrimary.copy(alpha = 0.18f * activeAnimProgress),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onNavigate(screen) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) SleekPrimary else SleekSubtext,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 9.sp
                            ),
                            color = if (isSelected) SleekPrimary else SleekSubtext
                        )
                    }
                }
            }
        }
    }
}

// --- CAREER PULSE HERO ---

@Composable
fun CareerPulseHeroCard(
    pulseTitle: String,
    pulseSubtitle: String,
    totalApps: Int,
    interviewsCount: Int,
    offersCount: Int,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse_breath")
    val orbitScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbit"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Architectural elegant sand grid lines behind panel for high-end editorial feel
                val lineSpacing = 24.dp.toPx()
                for (x in 1..4) {
                    drawLine(
                        color = SleekBorder.copy(alpha = 0.25f),
                        start = Offset(x * lineSpacing * 1.5f, 0f),
                        end = Offset(x * lineSpacing * 1.5f, size.height),
                        strokeWidth = 1f
                    )
                }
            },
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SleekPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CAREER OPERATING SYSTEM ACTIVE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                fontSize = 10.sp
                            ),
                            color = SleekPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "System Status: $pulseTitle",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
                        color = SleekSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pulseSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekSubtext
                    )
                }

                // Breathing Custom Orbit/Ring Visual
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 3.dp.toPx()
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // Static refined tracking track ring
                        drawCircle(
                            color = SleekBorder,
                            radius = size.width / 2f,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Outer glowing terra aura
                        drawCircle(
                            color = SleekPrimary.copy(alpha = 0.08f),
                            radius = (size.width / 2.3f) * orbitScale,
                            style = Stroke(width = strokeWidth * 2f)
                        )

                        // Golden active core tracker ring
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(SleekPrimary, Color(0xFFF59E0B), SleekPrimary)
                            ),
                            radius = size.width / 2.5f,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Pulse Core",
                        tint = SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = SleekBorder, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatSubBlock(num = totalApps.toString(), label = "Applications")
                StatSubBlock(num = interviewsCount.toString(), label = "Interviews Scheduled")
                StatSubBlock(num = offersCount.toString(), label = "Active Offers")
            }
        }
    }
}

@Composable
private fun StatSubBlock(num: String, label: String) {
    Column {
        Text(
            text = num,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
            color = SleekSecondary
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = 0.3.sp),
            color = SleekSubtext
        )
    }
}

// --- TODAY ACTION MOVE CARD ---

@Composable
fun TodayActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    badgeColor: Color = SleekPrimary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tactileClick { onClick() },
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SleekNavBarBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SleekSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SleekSubtext
                )
            }
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = badgeColor
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Action Arrow",
                    tint = SleekSubtext.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- AI COMMAND SEARCH UI PLACEHOLDER ---

@Composable
fun AICommandBar(onOptionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekSurface, RoundedCornerShape(22.dp))
            .border(1.2.dp, SleekBorder, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Assist",
                tint = SleekPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Ask JobTrack AI...",
                style = MaterialTheme.typography.bodyMedium,
                color = SleekSubtext.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(SleekPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "GEMINI",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = 0.5.sp),
                    color = SleekPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val prompts = listOf(
                "Which jobs need follow-up?",
                "Summarize this week",
                "Prepare me for interview",
                "Show silent applications"
            )
            prompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .background(SleekNavBarBg, RoundedCornerShape(12.dp))
                        .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
                        .clickable { onOptionClick(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = SleekSecondary
                    )
                }
            }
        }
    }
}

// --- OPPORTUNITY FILE CARD ---

@Composable
fun OpportunityCard(app: JobApplication, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tactileClick { onClick() },
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar representation
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SleekNavBarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initialStr = app.companyName.take(1).uppercase()
                    Text(
                        text = initialStr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = SleekPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.companyName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (app.source.lowercase().contains("gmail") || app.source.lowercase().contains("sync")) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(SleekPrimary.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "AI / GMAIL",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 7.sp, letterSpacing = 0.2.sp),
                                    color = SleekPrimary
                                )
                            }
                        }
                    }
                    Text(
                        text = app.jobTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = SleekSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                PriorityChip(priority = app.priority)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = app.currentStatus)
                    if (app.location.isNotEmpty()) {
                        Text(
                            text = "• ${app.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekSubtext
                        )
                    }
                }

                // Days count / Updated
                Text(
                    text = "Updated " + getPrettyPassedTime(app.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekSubtext
                )
            }

            if (app.nextAction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SleekNavBarBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Next action: ${app.nextAction}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SleekSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// --- CONFIDENCE METER ---

@Composable
fun AIConfidenceMeter(confidence: Float) {
    val pct = (confidence * 100).toInt()
    val barColor = when {
        confidence >= 0.85f -> StatusOffer
        confidence >= 0.60f -> StatusAssessment
        else -> StatusRejected
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GEMINI RETRIEVAL CLASSIFICATION INTENT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp, letterSpacing = 0.5.sp),
                color = SleekSubtext
            )
            Text(
                text = "$pct% Confidence Score",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(SleekBorder, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(confidence)
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(3.dp))
            )
        }
    }
}

// --- DATA POINT EXTRACTOR ---

@Composable
fun ExtractionDataPoint(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, letterSpacing = 0.5.sp),
            color = SleekSubtext
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = SleekSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- VERIFICATION STYLE REVIEW CARD ---

@Composable
fun AIReviewCard(
    email: DemoEmail,
    res: JobExtractionResult,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Intelligent classified banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SleekPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI DISCOVERY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp, letterSpacing = 0.5.sp),
                            color = SleekPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sender: " + email.sender.substringBefore("<").trim(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = SleekSecondary
                    )
                }
                StatusChip(status = res.applicationStatus ?: "Applied")
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = SleekBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Large Hero Details
            Text(
                text = res.companyName ?: "Inferring Company...",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                color = SleekSecondary
            )
            Text(
                text = res.jobTitle ?: "Inferring Position...",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = SleekSubtext
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verification vertical specifications layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SleekNavBarBg, RoundedCornerShape(14.dp))
                    .border(1.dp, SleekBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ExtractionDataPoint(label = "Suggested Milestone", value = res.eventType.replace("_", " ").uppercase(), modifier = Modifier.weight(1f))
                        ExtractionDataPoint(label = "Prescribed Action", value = res.nextAction ?: "Await further follow up", modifier = Modifier.weight(1f))
                    }
                    if (!res.summary.isNullOrBlank()) {
                        Divider(color = SleekBorder.copy(alpha = 0.6f))
                        Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
                            Text(
                                text = "GEMINI EXTRACTION SUMMARY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, letterSpacing = 0.5.sp),
                                color = SleekSubtext
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = res.summary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SleekSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            AIConfidenceMeter(confidence = res.confidence)
            Spacer(modifier = Modifier.height(18.dp))

            // Action Station Flow buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f).height(42.dp).testTag("confirm_extraction_button")
                ) {
                    Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onEdit,
                    border = BorderStroke(1.dp, SleekBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekSecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).height(42.dp).testTag("edit_extraction_button")
                ) {
                    Text("Edit Specs", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                TextButton(
                    onClick = onIgnore,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.9f).height(42.dp).testTag("ignore_extraction_button")
                ) {
                    Text("Ignore", color = StatusRejected, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

// --- JOURNEY SUBWAY LINE STATION CARD ---

@Composable
fun JourneyStationCard(
    stationName: String,
    description: String,
    icon: ImageVector,
    appsCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    stationColor: Color = SleekPrimary,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onToggleExpand() }
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subway Node Icon/Anchor Dot
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(stationColor.copy(alpha = 0.12f), CircleShape)
                    .border(2.dp, stationColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = stationColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SleekSecondary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SleekSubtext
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(SleekNavBarBg, CircleShape)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appsCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = SleekSecondary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = SleekSubtext,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expanded applications inside station
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 14.dp) // align right to the vertical subway track line
                    .border(BorderStroke(1.2.dp, SleekBorder.copy(alpha = 0.7f)), RoundedCornerShape(16.dp))
                    .background(SleekSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

// --- TIMELINE EVENT CARD (CASE FILE) ---

@Composable
fun TimelineEventCard(
    event: JobEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(event.eventDate))
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekNavBarBg),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val status = mapEventToStatusLabel(event.eventType)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(getStatusColor(status), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.eventType.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 0.5.sp),
                        color = SleekSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekSubtext
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Event", tint = StatusRejected, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodySmall,
                color = SleekSecondary
            )
            if (event.extractedByAi) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SleekPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI DISCOVERED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 7.sp),
                            color = SleekPrimary
                        )
                    }
                    if (event.rawSnippet.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Snippet: \"${event.rawSnippet.take(45)}...\"",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SleekSubtext,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun mapEventToStatusLabel(type: String): String {
    return when (type) {
        "irrelevant" -> "Saved"
        "application_confirmation" -> "Applied"
        "recruiter_reply" -> "Recruiter replied"
        "assessment_invitation" -> "Assessment"
        "interview_invitation", "interview_reschedule" -> "Interview"
        "offer" -> "Offer"
        "rejection" -> "Rejected"
        "follow_up_needed" -> "Ghosted"
        else -> "Saved"
    }
}

// --- INSIGHT METRIC & ADVICE ---

@Composable
fun InsightMetricCard(
    label: String,
    value: String,
    subtitle: String,
    cardColor: Color = SleekPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, letterSpacing = 0.5.sp),
                color = SleekSubtext
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 24.sp),
                color = cardColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = SleekSubtext
            )
        }
    }
}

@Composable
fun AdviceCard(
    adviceText: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekNavBarBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, SleekBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SleekPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = adviceText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SleekSecondary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = SleekSubtext
                )
            }
        }
    }
}

// --- REFINED EMPTY STATE ASSEMBLY ---

@Composable
fun EmptyStatePanel(
    title: String,
    subtitle: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SleekNavBarBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    tint = SleekPrimary,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = SleekSecondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SleekSubtext,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
            if (buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                PrimaryActionButton(text = buttonText, onClick = onButtonClick)
            }
        }
    }
}

// --- UTILITIES ---

private fun getPrettyPassedTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days d ago"
        hours > 0 -> "$hours h ago"
        minutes > 0 -> "$minutes m ago"
        else -> "Just now"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplicationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, String, String) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Manual") }
    var status by remember { mutableStateOf("Applied") }
    var priority by remember { mutableStateOf("Medium") }
    var salary by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
    val priorityOptions = listOf("High", "Medium", "Low")

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Job Application", fontWeight = FontWeight.ExtraBold, color = SleekSecondary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name (*)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_company_input")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title (*)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_role_input")
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Remote, NY)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Job Source (e.g. LinkedIn, Recruiter)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status,
                        onValueChange = {},
                        label = { Text("Application Status") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    status = selection
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = priority,
                        onValueChange = {},
                        label = { Text("Priority Level") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        priorityOptions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    priority = selection
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary / Range (e.g. £80k, $140k)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (company.isNotEmpty() && title.isNotEmpty()) {
                        onAdd(company, title, location, source, status, priority, salary, notes) 
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Entry", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SleekPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExtractionDialog(
    currentResult: JobExtractionResult,
    onDismiss: () -> Unit,
    onSave: (JobExtractionResult) -> Unit
) {
    var company by remember { mutableStateOf(currentResult.companyName ?: "") }
    var role by remember { mutableStateOf(currentResult.jobTitle ?: "") }
    var eventType by remember { mutableStateOf(currentResult.eventType) }
    var status by remember { mutableStateOf(currentResult.applicationStatus ?: "Applied") }
    var action by remember { mutableStateOf(currentResult.nextAction ?: "") }
    var summary by remember { mutableStateOf(currentResult.summary ?: "") }

    val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Extraction Specifications", fontWeight = FontWeight.ExtraBold, color = SleekSecondary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_diag_company_input")
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Job Title") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_diag_role_input")
                )

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status,
                        onValueChange = {},
                        label = { Text("Suggested Status") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    status = selection
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = eventType,
                    onValueChange = { eventType = it },
                    label = { Text("Event Type (e.g. interview_invitation)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Next Action Event") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary Details") },
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        JobExtractionResult(
                            isJobRelated = true,
                            confidence = currentResult.confidence,
                            companyName = company.ifEmpty { null },
                            jobTitle = role.ifEmpty { null },
                            applicationStatus = status,
                            eventType = eventType,
                            eventDate = currentResult.eventDate,
                            nextAction = action.ifEmpty { null },
                            summary = summary.ifEmpty { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Apply Specs", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SleekPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun IngestionProgressDialog(
    show: Boolean,
    scanDays: Int,
    maxFetch: Int,
    maxAnalyze: Int,
    found: Int,
    skipped: Int,
    analyzed: Int,
    detected: Int,
    limitReached: Boolean,
    syncError: String?,
    onDismiss: () -> Unit = {}
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = { /* user cannot dismiss manually */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        confirmButton = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = SleekPrimary,
                    strokeWidth = 2.5.dp
                )
                Text(
                    text = "Syncing ATS Nodes...",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = SleekSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pre-scan message with configured bounds
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SleekPrimary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "JobTrack AI will scan up to $maxFetch emails from the past $scanDays days. Only likely job-related emails will be analyzed by Gemini. Results will wait in AI Inbox for approval.",
                        fontSize = 11.sp,
                        color = SleekPrimary,
                        lineHeight = 15.sp
                    )
                }

                // Rolling Progress Stats
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📩 Emails Found:", fontSize = 12.sp, color = SleekSecondary, fontWeight = FontWeight.Bold)
                        Text("$found / $maxFetch", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.ExtraBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("⏩ Emails Skipped:", fontSize = 12.sp, color = SleekSecondary)
                        Text("$skipped", fontSize = 12.sp, color = SleekSubtext)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🧠 AI Scans Executed:", fontSize = 12.sp, color = SleekSecondary, fontWeight = FontWeight.Bold)
                        Text("$analyzed / $maxAnalyze", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.ExtraBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("⚡ Career Transitions Found:", fontSize = 12.sp, color = SleekSecondary, fontWeight = FontWeight.Bold)
                        Text("$detected", fontSize = 12.sp, color = StatusOffer, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Progress Indicator Bar
                val fraction = if (maxAnalyze > 0) analyzed.toFloat() / maxAnalyze.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { fraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SleekPrimary,
                    trackColor = SleekBorder
                )

                if (limitReached || syncError != null) {
                    Text(
                        text = syncError ?: "Scan paused because selected limit was reached.",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Retrieving and parsing secure pipeline intervals...",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekSubtext,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
