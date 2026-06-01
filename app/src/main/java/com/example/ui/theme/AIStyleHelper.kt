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
 * A gorgeous animated vector logo representing a scholarly brain/astrolabe blossom.
 * Features an inner pulsing warm core and very slowly revolving elegant orbital rings.
 */
@Composable
fun AILogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_logo_anim")
    
    // Core expansion pulse (calm breath)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Outer slow academic astrolabe rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rot"
    )

    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                val baseRadius = (kotlin.math.min(canvasWidth, canvasHeight) / 2f) * 0.85f

                // 1. Cozy amber/peach center background aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SleekPrimary.copy(alpha = 0.15f),
                            SleekTertiary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.4f
                    )
                )

                // 2. First rotating ring representing interconnected nodes (Warm Slate/Clay)
                drawCircle(
                    color = SleekSecondary.copy(alpha = 0.3f),
                    center = center,
                    radius = baseRadius * 0.95f,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(12f, 22f),
                            phase = rotationAngle
                        )
                    )
                )

                // 3. Second slow outer concentric ring representing a scholarly compass
                drawCircle(
                    color = SleekPrimary.copy(alpha = 0.12f),
                    center = center,
                    radius = baseRadius * 1.15f,
                    style = Stroke(
                        width = 1.dp.toPx()
                    )
                )

                // 4. Center pulsing organic flower core (Claude Peach/Clay Terracotta)
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(SleekPrimary, SleekPrimary.copy(alpha = 0.8f)),
                        start = Offset(center.x - baseRadius * 0.4f, center.y - baseRadius * 0.4f),
                        end = Offset(center.x + baseRadius * 0.4f, center.y + baseRadius * 0.4f)
                    ),
                    center = center,
                    radius = baseRadius * 0.5f * pulseScale
                )

                // 5. Exquisite sand core focal dot (warm ivory)
                drawCircle(
                    color = SleekTertiary,
                    center = center,
                    radius = baseRadius * 0.15f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Keeps the layout size perfect
    }
}
