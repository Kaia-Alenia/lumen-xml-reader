package com.lumen.xmlreader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lumen.xmlreader.ui.theme.GuideLineColorDark
import com.lumen.xmlreader.ui.theme.GuideLineColorLight

@Composable
fun IndentationGuide(
    level: Int,
    modifier: Modifier = Modifier
) {
    if (level <= 0) return

    val isDark = isSystemInDarkTheme()
    val guideColor: Color = if (isDark) GuideLineColorDark else GuideLineColorLight
    val stepWidth = 16.dp

    Row(modifier = modifier) {
        for (i in 0 until level) {
            Canvas(
                modifier = Modifier
                    .width(stepWidth)
                    .fillMaxHeight()
                    .height(36.dp)
            ) {
                val x = size.width / 2
                drawLine(
                    color = guideColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}
