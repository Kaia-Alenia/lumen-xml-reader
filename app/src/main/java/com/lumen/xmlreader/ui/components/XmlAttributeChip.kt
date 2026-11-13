package com.lumen.xmlreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.xmlreader.ui.theme.AttributeKeyColor
import com.lumen.xmlreader.ui.theme.AttributeKeyColorDark
import com.lumen.xmlreader.ui.theme.AttributeValueColor
import com.lumen.xmlreader.ui.theme.AttributeValueColorDark

@Composable
fun XmlAttributeChip(
    key: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val keyColor = if (isDark) AttributeKeyColorDark else AttributeKeyColor
    val valColor = if (isDark) AttributeValueColorDark else AttributeValueColor
    val bgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            color = keyColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "=\"",
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = valColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
        Text(
            text = "\"",
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
