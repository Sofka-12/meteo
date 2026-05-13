package com.example.meteopipli.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GlowRing(
    level: String, // GREEN, YELLOW, RED
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val baseColor = when (level) {
        "GREEN" -> Color(0xFF00B894)
        "YELLOW" -> Color(0xFFFDCB6E)
        "RED" -> Color(0xFFFF7675)
        else -> Color(0xFF00B894)
    }

    val glowColor = baseColor.copy(alpha = 0.4f * pulse)
    val centerColor = baseColor.copy(alpha = 0.9f)

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Внешнее свечение
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = glowColor,
                radius = size.width / 2 * (0.85f + pulse * 0.15f),
                style = Stroke(width = 8f)
            )
        }

        // Основной круг
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(centerColor, baseColor),
                        radius = 1f
                    )
                )
        )
    }
}