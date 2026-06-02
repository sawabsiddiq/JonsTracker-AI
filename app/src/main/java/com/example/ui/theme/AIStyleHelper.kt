package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Physics-based spring clickable modifier that gives cards and buttons tactile bouncy feedback.
 * Plays haptic feedback vibration to simulate true mechanical clicks.
 */
@Composable
fun Modifier.springClickable(
    haptic: HapticFeedback? = null,
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "springClickScale"
    )

    return this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = {
                    haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
        }
}

/**
 * 3D Paper Shadow Modifier
 * Adds a multi-layered soft parchment drop shadow to make cards look like physical literary paper.
 */
fun Modifier.cyberShadow(
    primaryColor: Color = SleekPrimary,
    secondaryColor: Color = SleekSecondary,
    offset: Dp = 4.dp,
    cornerRadius: Dp = 24.dp
): Modifier = this.drawBehind {
    val sizePx = size
    val offsetPx = offset.toPx()
    val rPx = cornerRadius.toPx()

    // Layer 1: Warm ambient card drop shadow (understated loamy grey)
    drawRoundRect(
        color = Color(0x0F382F2D),
        topLeft = Offset(offsetPx * 0.8f, offsetPx * 0.8f),
        size = sizePx,
        cornerRadius = CornerRadius(rPx, rPx)
    )

    // Layer 2: Soft warm clay aura highlight
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.04f),
        topLeft = Offset(offsetPx * 1.2f, offsetPx * 1.2f),
        size = sizePx,
        cornerRadius = CornerRadius(rPx, rPx)
    )
}

/**
 * Ambient Light Bookbinder Border
 * Generates an elegant, single-line thin linen-thread outline accent.
 */
fun Modifier.neonGlow(
    glowColor: Color = SleekPrimary,
    glowWidth: Dp = 1.dp,
    borderRadius: Dp = 24.dp
): Modifier = this.drawBehind {
    val strokeWidthPx = glowWidth.toPx()
    val rPx = borderRadius.toPx()
    
    // Thin delicate decorative border (warm wheat & clay tint)
    drawRoundRect(
        color = glowColor.copy(alpha = 0.25f),
        style = Stroke(width = strokeWidthPx),
        cornerRadius = CornerRadius(rPx, rPx)
    )
}

/**
 * AILogo
 * A gorgeous, high-fidelity 3D claymorphic vector rendering of the new resume and briefcase application icon.
 * Features a soft ambient breathing/floating animation to make the landing screen feel organic and responsive.
 */
@Composable
fun AILogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "app_logo_anim")
    
    // Gentle premium floating hover action
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_hover"
    )

    // Breathing scale pulse
    val rawScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationY = hoverY * density
                scaleX = rawScale
                scaleY = rawScale
            }
            .drawBehind {
                val w = this.size.width
                val h = this.size.height
                val center = Offset(w / 2f, h / 2f)

                // Draw premium rounded soft-beige outer plate background (replicates app icon container)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFEFDFB), Color(0xFFFAF5EB), Color(0xFFF3EBE0)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    size = this.size,
                    cornerRadius = CornerRadius(w * 0.28f, h * 0.28f)
                )

                // Soft outer ambient drop shadow for the card folder structure
                drawRoundRect(
                    color = Color(0x14100D0B),
                    topLeft = Offset(w * 0.22f, h * 0.32f),
                    size = Size(w * 0.52f, h * 0.50f),
                    cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
                )

                // 1. Folder outer back backing (color: #DCD0C2)
                drawRoundRect(
                    color = Color(0xFFDCD0C2),
                    topLeft = Offset(w * 0.24f, h * 0.30f),
                    size = Size(w * 0.50f, h * 0.48f),
                    cornerRadius = CornerRadius(w * 0.06f, h * 0.06f)
                )

                // 2. Orange Tab Sheet peeking out (color: #DF845C)
                drawRoundRect(
                    color = Color(0xFFDF845C),
                    topLeft = Offset(w * 0.28f, h * 0.27f),
                    size = Size(w * 0.48f, h * 0.48f),
                    cornerRadius = CornerRadius(w * 0.05f, h * 0.05f)
                )

                // 3. Primary White Resume Document Card (color: #FFFFFF)
                val docWidth = w * 0.44f
                val docHeight = h * 0.52f
                val docLeft = w * 0.28f
                val docTop = h * 0.21f
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(docLeft, docTop),
                    size = Size(docWidth, docHeight),
                    cornerRadius = CornerRadius(w * 0.05f, h * 0.05f)
                )

                // Inside Resume: Terracotta profile avatar circle (color: #D2744E)
                val avatarRadius = docWidth * 0.15f
                val avatarCenter = Offset(docLeft + docWidth * 0.26f, docTop + docHeight * 0.20f)
                drawCircle(
                    color = Color(0xFFD2744E),
                    center = avatarCenter,
                    radius = avatarRadius
                )
                // Head profile outline
                drawCircle(
                    color = Color.White,
                    center = Offset(avatarCenter.x, avatarCenter.y - avatarRadius * 0.15f),
                    radius = avatarRadius * 0.35f
                )
                // Shoulders curve
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(avatarCenter.x - avatarRadius * 0.60f, avatarCenter.y + avatarRadius * 0.35f),
                    size = Size(avatarRadius * 1.2f, avatarRadius * 0.6f),
                    cornerRadius = CornerRadius(avatarRadius * 0.2f, avatarRadius * 0.2f)
                )

                // Header metadata lines (color: #B7AB9D)
                drawLine(
                    color = Color(0xFFB7AB9D),
                    start = Offset(docLeft + docWidth * 0.52f, docTop + docHeight * 0.17f),
                    end = Offset(docLeft + docWidth * 0.84f, docTop + docHeight * 0.17f),
                    strokeWidth = w * 0.015f
                )
                drawLine(
                    color = Color(0xFFB7AB9D),
                    start = Offset(docLeft + docWidth * 0.52f, docTop + docHeight * 0.25f),
                    end = Offset(docLeft + docWidth * 0.74f, docTop + docHeight * 0.25f),
                    strokeWidth = w * 0.015f
                )

                // Checklist row 1: orange circle check and line
                val checkY1 = docTop + docHeight * 0.46f
                drawCircle(
                    color = Color(0xFFD2744E),
                    center = Offset(docLeft + docWidth * 0.24f, checkY1),
                    radius = docWidth * 0.08f
                )
                // Small micro check indicator
                drawLine(
                    color = Color.White,
                    start = Offset(docLeft + docWidth * 0.20f, checkY1),
                    end = Offset(docLeft + docWidth * 0.24f, checkY1 + docHeight * 0.025f),
                    strokeWidth = w * 0.008f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(docLeft + docWidth * 0.24f, checkY1 + docHeight * 0.025f),
                    end = Offset(docLeft + docWidth * 0.29f, checkY1 - docHeight * 0.02f),
                    strokeWidth = w * 0.008f
                )
                drawLine(
                    color = Color(0xFFB7AB9D),
                    start = Offset(docLeft + docWidth * 0.40f, checkY1),
                    end = Offset(docLeft + docWidth * 0.85f, checkY1),
                    strokeWidth = w * 0.015f
                )

                // Checklist row 2: orange circle check and line
                val checkY2 = docTop + docHeight * 0.64f
                drawCircle(
                    color = Color(0xFFD2744E),
                    center = Offset(docLeft + docWidth * 0.24f, checkY2),
                    radius = docWidth * 0.08f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(docLeft + docWidth * 0.20f, checkY2),
                    end = Offset(docLeft + docWidth * 0.24f, checkY2 + docHeight * 0.025f),
                    strokeWidth = w * 0.008f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(docLeft + docWidth * 0.24f, checkY2 + docHeight * 0.025f),
                    end = Offset(docLeft + docWidth * 0.29f, checkY2 - docHeight * 0.02f),
                    strokeWidth = w * 0.008f
                )
                drawLine(
                    color = Color(0xFFB7AB9D),
                    start = Offset(docLeft + docWidth * 0.40f, checkY2),
                    end = Offset(docLeft + docWidth * 0.85f, checkY2),
                    strokeWidth = w * 0.015f
                )

                // Checklist row 3: unchecked biscuit bullet and line
                val checkY3 = docTop + docHeight * 0.82f
                drawCircle(
                    color = Color(0xFFEADFD2),
                    center = Offset(docLeft + docWidth * 0.24f, checkY3),
                    radius = docWidth * 0.08f
                )
                drawLine(
                    color = Color(0xFFB7AB9D),
                    start = Offset(docLeft + docWidth * 0.40f, checkY3),
                    end = Offset(docLeft + docWidth * 0.70f, checkY3),
                    strokeWidth = w * 0.015f
                )

                // 4. Claymorphic front pocket lip (color: #C6B49E)
                // Draw bottom portion covering bottom base of folders/resume
                drawRoundRect(
                    color = Color(0xFFC6B49E),
                    topLeft = Offset(w * 0.21f, h * 0.62f),
                    size = Size(w * 0.58f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.05f, h * 0.05f)
                )

                // 5. Cute executive briefcase overlay (color: #453B34)
                val caseWidth = w * 0.32f
                val caseHeight = h * 0.23f
                val caseLeft = w * 0.49f
                val caseTop = h * 0.58f

                // Draw briefcase thin handle
                drawRoundRect(
                    color = Color(0xFF453B34),
                    topLeft = Offset(caseLeft + caseWidth * 0.35f, caseTop - h * 0.035f),
                    size = Size(caseWidth * 0.30f, h * 0.05f),
                    cornerRadius = CornerRadius(w * 0.015f, h * 0.015f),
                    style = Stroke(width = w * 0.018f)
                )

                // Draw briefcase body
                drawRoundRect(
                    color = Color(0xFF453B34),
                    topLeft = Offset(caseLeft, caseTop),
                    size = Size(caseWidth, caseHeight),
                    cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
                )

                // Draw golden lock accent plate (color: #E6A573)
                drawRoundRect(
                    color = Color(0xFFE6A573),
                    topLeft = Offset(caseLeft + caseWidth * 0.40f, caseTop + caseHeight * 0.40f),
                    size = Size(caseWidth * 0.20f, caseHeight * 0.25f),
                    cornerRadius = CornerRadius(w * 0.015f, h * 0.015f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // High fidelity vector boundaries
    }
}
