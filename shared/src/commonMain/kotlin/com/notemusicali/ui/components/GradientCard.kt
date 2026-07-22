package com.notemusicali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GradientCard(
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(modifier = modifier.clip(shape)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
                .clickable(onClick = onClick)
                .padding(20.dp),
            content = content,
        )

        // Top accent line (like Remotion feature cards)
        if (accentColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.6f)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, accentColor, Color.Transparent),
                        ),
                    ),
            )
        }
    }
}
