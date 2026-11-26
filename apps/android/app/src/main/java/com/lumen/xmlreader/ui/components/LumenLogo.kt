package com.lumen.xmlreader.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.xmlreader.R

@Composable
fun LumenIcon(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_lumen_icon),
        contentDescription = "Lumen Logo",
        modifier = modifier.size(size)
    )
}

@Composable
fun LumenFullLogo(
    iconSize: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E1B4B)
    val subtitleColor = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        LumenIcon(size = iconSize)
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "Lumen",
                fontSize = (iconSize.value * 0.44f).sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                lineHeight = (iconSize.value * 0.44f).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "XML READER",
                fontSize = (iconSize.value * 0.16f).sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = subtitleColor
            )
        }
    }
}
