package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * A beautiful, animated organic nature gradient background with floating green particles and drifting leaves.
 */
@Composable
fun BackgroundParticles(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    // Beautiful nature gradient: Misty forest morning to Linen Cream (Natural Tones)
    val gradientColors = listOf(
        Color(0xFFE8EFE5), // Misty morning sage green (light)
        Color(0xFFF8FAF6), // Clean cream linen (Natural Tones backdrop)
        Color(0xFFF1F5F0)  // Soft linen green
    )

    // Animated phase for wave/particulate drifters
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundParticles")
    
    // Smooth endless wave drifter
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    // We can define multiple particle states to float up and drift left and right
    val particles = remember {
        List(12) {
            val r = kotlin.random.Random
            ParticleState(
                xSeed = r.nextFloat(),
                ySeed = r.nextFloat(),
                speed = 0.02f + r.nextFloat() * 0.04f,
                radius = 8f + r.nextFloat() * 12f,
                alpha = 0.2f + r.nextFloat() * 0.4f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width > 0 && height > 0) {
                // 1. Draw glowing background organic ambient radial orbs
                val pulseRad = Math.toRadians(pulseAnim.toDouble()).toFloat()
                val shiftX = sin(pulseRad) * 60.dp.toPx()
                val shiftY = sin(pulseRad * 0.5f) * 40.dp.toPx()

                drawCircle(
                    color = Color(0x1F386B41), // Gentle sage-green organic accent
                    radius = width * 0.5f,
                    center = Offset(width * 0.3f + shiftX, height * 0.2f + shiftY)
                )

                drawCircle(
                    color = Color(0x14A8CDB0), // Calm background leaf glow
                    radius = width * 0.6f,
                    center = Offset(width * 0.8f - shiftX, height * 0.7f - shiftY)
                )

                // 2. Draw falling/floating animated bubbles & leaf shapes
                particles.forEach { p ->
                    // Compute dynamic vertical progress offset by pulse anim
                    val progressY = (p.ySeed + p.speed * (pulseAnim / 10f)) % 1f
                    val posX = (p.xSeed + sin(pulseRad * 2f + p.ySeed * 10f) * 0.05f) * width
                    val posY = progressY * height

                    // Draw organic particle bubble
                    drawCircle(
                        color = Color(0xFF386B41).copy(alpha = p.alpha * 0.4f),
                        radius = p.radius,
                        center = Offset(posX.toFloat(), posY.toFloat())
                    )

                    // Draw a couple of stylish floating leaf icons
                    val leafSizeX = p.radius * 2.2f
                    val leafSizeY = p.radius * 1.5f
                    if (p.radius > 15f) {
                        rotate(degrees = pulseAnim * 0.2f + (p.xSeed * 360f), pivot = Offset(posX.toFloat(), posY.toFloat())) {
                            drawOval(
                                color = Color(0x3DA8CDB0),
                                topLeft = Offset((posX - leafSizeX / 2).toFloat(), (posY - leafSizeY / 2).toFloat()),
                                size = androidx.compose.ui.geometry.Size(leafSizeX, leafSizeY)
                            )
                        }
                    }
                }
            }
        }

        // Overlay layout content
        content()
    }
}

private data class ParticleState(
    val xSeed: Float,
    val ySeed: Float,
    val speed: Float,
    val radius: Float,
    val alpha: Float
)
