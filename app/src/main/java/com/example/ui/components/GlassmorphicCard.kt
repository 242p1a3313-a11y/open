package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A beautiful Nature-themed translucent glassmorphic panel with clear highlight borders.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    padding: Dp = 16.dp,
    contentColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            // Clean semi-translucent crisp white background for the Natural Tones aesthetic
            .background(Color(0xF0FFFFFF))
            // Sage-border clean color #DDE5DB
            .border(
                width = 1.2.dp,
                color = Color(0xFFDDE5DB),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(padding),
        content = content
    )
}
