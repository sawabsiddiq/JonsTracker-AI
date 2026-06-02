package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobApplication
import com.example.ui.JobTrackerViewModel
import com.example.ui.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.List

class KanbanDragAndDropState {
    var draggedApp by mutableStateOf<JobApplication?>(null)
    var dragAbsolutePosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var touchAnchorOffset by mutableStateOf(Offset.Zero)
    var targetColumn by mutableStateOf<String?>(null)
    val columnBounds = mutableStateMapOf<String, Rect>()
}

@Composable
fun rememberKanbanDragAndDropState() = remember { KanbanDragAndDropState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineScreen(viewModel: JobTrackerViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val applications by viewModel.allApplications.collectAsState()
    val pipelineStages = listOf("All", "Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
    var selectedStage by remember { mutableStateOf("All") }
    
    // Toggle between standard List View and beautiful fluid Kanban Board
    var isKanbanView by remember { mutableStateOf(false) }

    // Filtered items based on stage
    val filteredApps = if (selectedStage == "All") {
        applications
    } else {
        applications.filter { it.currentStatus.equals(selectedStage, ignoreCase = true) }
    }

    var boardLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val dragAndDropState = rememberKanbanDragAndDropState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { boardLayoutCoordinates = it }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Job Board Pipeline",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        // Segmented Layout View switch
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { isKanbanView = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (!isKanbanView) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "List View",
                                    tint = if (!isKanbanView) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { isKanbanView = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (isKanbanView) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Kanban Board",
                                    tint = if (isKanbanView) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (!isKanbanView) {
                    // Stage Selection Horizontal Pills for standard List View
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(pipelineStages) { stage ->
                            val isActive = selectedStage == stage
                            val count = if (stage == "All") applications.size else applications.count { it.currentStatus.equals(stage, ignoreCase = true) }

                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isActive) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .springClickable(haptic) { selectedStage = stage }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("pipeline_filter_${stage.lowercase().replace(" ", "_")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stage,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isActive) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Application List view
                    if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Empty Pipeline Filter",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No applications as '$selectedStage'",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredApps, key = { it.id }) { app ->
                                PipelineCard(
                                    app = app,
                                    onClick = {
                                        viewModel.navigateTo(Screen.Detail(app.id))
                                    },
                                    onStatusChange = { newStatus ->
                                        viewModel.updateApplicationDetails(app.copy(currentStatus = newStatus, updatedAt = System.currentTimeMillis()))
                                        viewModel.addTimelineEvent(
                                            appId = app.id,
                                            type = when (newStatus) {
                                                "Saved" -> "unknown"
                                                "Applied" -> "application_confirmation"
                                                "Recruiter replied" -> "recruiter_reply"
                                                "Assessment" -> "assessment_invitation"
                                                "Interview" -> "interview_invitation"
                                                "Offer" -> "offer"
                                                "Rejected" -> "rejection"
                                                "Ghosted" -> "ghosted"
                                                else -> "unknown"
                                            },
                                            summary = "Status updated manually from pipeline to '$newStatus'.",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp)) // padding for bottom menu
                            }
                        }
                    }
                } else {
                    // Beautiful drag-and-drop Kanban Board View
                    val kanbanStages = listOf("Saved", "Applied", "Interview", "Offer", "Rejected")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp, bottom = 48.dp, start = 12.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        kanbanStages.forEach { stage ->
                            // Columns status contents filter
                            val stageApps = applications.filter {
                                val kanbanMapped = when (it.currentStatus) {
                                    "Saved" -> "Saved"
                                    "Applied" -> "Applied"
                                    "Recruiter replied", "Assessment", "Interview" -> "Interview"
                                    "Offer" -> "Offer"
                                    "Rejected", "Ghosted" -> "Rejected"
                                    else -> "Saved"
                                }
                                kanbanMapped.equals(stage, ignoreCase = true)
                            }

                            val isOverCol = dragAndDropState.targetColumn == stage
                            val columnColor = when (stage) {
                                "Saved" -> MaterialTheme.colorScheme.secondary
                                "Applied" -> MaterialTheme.colorScheme.primary
                                "Interview" -> Color(0xFFF59E0B)
                                "Offer" -> Color(0xFF10B981)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Box(
                                modifier = Modifier
                                    .width(280.dp)
                                    .fillMaxHeight()
                                    .onGloballyPositioned { colCoords ->
                                        boardLayoutCoordinates?.let { rootCoords ->
                                            val offset = rootCoords.localPositionOf(colCoords, Offset.Zero)
                                            val size = colCoords.size
                                            dragAndDropState.columnBounds[stage] = Rect(
                                                offset.x,
                                                offset.y,
                                                offset.x + size.width,
                                                offset.y + size.height
                                            )
                                        }
                                    }
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isOverCol) 0.45f else 0.20f),
                                        RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isOverCol) columnColor else Color.Transparent,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .neonGlow(
                                        if (isOverCol) columnColor else columnColor.copy(alpha = 0.08f),
                                        1.dp,
                                        24.dp
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Column Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(columnColor, CircleShape)
                                            )
                                            Text(
                                                text = if (stage == "Interview") "Interviewing" else stage,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(columnColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = stageApps.size.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = columnColor
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Vertically scrollable list of Column Cards
                                    val colScrollState = rememberScrollState()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .verticalScroll(colScrollState),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (stageApps.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                                        RoundedCornerShape(16.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Drop tasks here",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                            }
                                        } else {
                                            stageApps.forEach { app ->
                                                KanbanCard(
                                                    app = app,
                                                    dragAndDropState = dragAndDropState,
                                                    boardLayoutCoordinates = boardLayoutCoordinates,
                                                    onClick = {
                                                        viewModel.navigateTo(Screen.Detail(app.id))
                                                    },
                                                    onStatusChange = { dragApp, targetStage ->
                                                        val targetStatus = when (targetStage) {
                                                            "Saved" -> "Saved"
                                                            "Applied" -> "Applied"
                                                            "Interview" -> "Interview"
                                                            "Offer" -> "Offer"
                                                            "Rejected" -> "Rejected"
                                                            else -> "Saved"
                                                        }
                                                        viewModel.updateApplicationDetails(
                                                            dragApp.copy(currentStatus = targetStatus, updatedAt = System.currentTimeMillis())
                                                        )
                                                        viewModel.addTimelineEvent(
                                                            appId = dragApp.id,
                                                            type = when (targetStatus) {
                                                                "Saved" -> "unknown"
                                                                "Applied" -> "application_confirmation"
                                                                "Interview" -> "interview_invitation"
                                                                "Offer" -> "offer"
                                                                "Rejected" -> "rejection"
                                                                else -> "unknown"
                                                            },
                                                            summary = "Status updated to '$targetStatus' via Kanban Board drag & drop.",
                                                            timestamp = System.currentTimeMillis()
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Float-rendering Dragged card Overlay
        val dragApp = dragAndDropState.draggedApp
        if (dragApp != null) {
            val priorityColor = getPriorityColor(dragApp.priority)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragAndDropState.dragAbsolutePosition.x - dragAndDropState.touchAnchorOffset.x).toInt(),
                            (dragAndDropState.dragAbsolutePosition.y - dragAndDropState.touchAnchorOffset.y).toInt()
                        )
                    }
                    .graphicsLayer(
                        scaleX = 1.05f,
                        scaleY = 1.05f,
                        rotationZ = 4f,
                        alpha = 0.90f
                    )
                    .width(260.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .cyberShadow(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.Transparent, 6.dp, 16.dp)
                    .neonGlow(MaterialTheme.colorScheme.primary, 1.dp, 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = dragApp.companyName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(priorityColor, CircleShape)
                                .align(Alignment.CenterVertically)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dragApp.jobTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun KanbanCard(
    app: JobApplication,
    dragAndDropState: KanbanDragAndDropState,
    boardLayoutCoordinates: LayoutCoordinates?,
    onClick: () -> Unit,
    onStatusChange: (JobApplication, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val priorityColor = getPriorityColor(app.priority)
    var cardLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    val isBeingDragged = dragAndDropState.draggedApp?.id == app.id

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isBeingDragged) 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isBeingDragged)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                priorityColor.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { cardLayoutCoordinates = it }
            .pointerInput(app) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragAndDropState.draggedApp = app
                        dragAndDropState.touchAnchorOffset = offset
                        cardLayoutCoordinates?.let { cardCoords ->
                            boardLayoutCoordinates?.let { rootCoords ->
                                dragAndDropState.dragAbsolutePosition = rootCoords.localPositionOf(cardCoords, Offset.Zero) + offset
                            }
                        }
                        dragAndDropState.dragOffset = Offset.Zero
                        haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAndDropState.dragOffset += dragAmount
                        dragAndDropState.dragAbsolutePosition += dragAmount
                        
                        val fingerPos = dragAndDropState.dragAbsolutePosition
                        dragAndDropState.targetColumn = dragAndDropState.columnBounds.entries.find { entry ->
                            entry.value.contains(fingerPos)
                        }?.key
                    },
                    onDragEnd = {
                        dragAndDropState.targetColumn?.let { stage ->
                            onStatusChange(app, stage)
                        }
                        dragAndDropState.draggedApp = null
                        dragAndDropState.targetColumn = null
                    },
                    onDragCancel = {
                        dragAndDropState.draggedApp = null
                        dragAndDropState.targetColumn = null
                    }
                )
            }
            .springClickable(haptic) { onClick() }
            .then(
                if (isBeingDragged) Modifier else Modifier.cyberShadow(priorityColor.copy(alpha = 0.05f), Color.Transparent, 2.dp, 16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = app.companyName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(priorityColor, CircleShape)
                        .align(Alignment.CenterVertically)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = app.jobTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (app.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = app.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun PipelineCard(
    app: JobApplication,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(app.appliedDate))

    val updateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val updatedDateString = updateFormatter.format(Date(app.updatedAt))

    val priorityColor = getPriorityColor(app.priority)
    val statusColor = getStatusColor(app.currentStatus)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.2.dp, 
            Brush.linearGradient(
                listOf(priorityColor.copy(alpha = 0.55f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .springClickable(haptic) { onClick() }
            .neonGlow(priorityColor.copy(alpha = 0.08f), 1.5.dp, 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.companyName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.jobTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Priority Badge with subtle border glow
                Box(
                    modifier = Modifier
                        .background(
                            priorityColor.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .border(1.dp, priorityColor.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = app.priority,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = priorityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extra Info Meta Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = app.location.ifEmpty { "Not specified" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Dates",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                // Interactive Stage Chip badge with ArrowDropDown
                var expanded by remember { mutableStateOf(false) }

                Box {
                    Box(
                        modifier = Modifier
                            .background(
                                statusColor.copy(alpha = 0.12f),
                                CircleShape
                            )
                            .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape)
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.currentStatus,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Edit status",
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        val statusOptions = listOf("Saved", "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted")
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    expanded = false
                                    onStatusChange(status)
                                }
                            )
                        }
                    }
                }
            }

            // Timestamp for last update
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Last update",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Last updated: $updatedDateString",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            // Quick reminder next action if defined
            if (app.nextAction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Task Reminder",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Next: ${app.nextAction}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

fun getPriorityColor(priority: String): Color {
    return when (priority) {
        "High" -> Color(0xFFEF4444)
        "Medium" -> Color(0xFFF59E0B)
        "Low" -> Color(0xFF10B981)
        else -> Color(0xFF9CA3AF)
    }
}
