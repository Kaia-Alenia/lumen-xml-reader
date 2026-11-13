package com.lumen.xmlreader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.xmlreader.domain.model.XmlNode
import com.lumen.xmlreader.ui.theme.XmlNamespaceBadgeDark
import com.lumen.xmlreader.ui.theme.XmlNamespaceBadgeLight
import com.lumen.xmlreader.ui.theme.XmlNamespaceTextDark
import com.lumen.xmlreader.ui.theme.XmlNamespaceTextLight
import com.lumen.xmlreader.ui.theme.XmlTagColorDark
import com.lumen.xmlreader.ui.theme.XmlTagColorLight

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun XmlNodeItem(
    node: XmlNode,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNodeClick: () -> Unit,
    onNodeLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val tagColor = if (isDark) XmlTagColorDark else XmlTagColorLight
    val nsBadgeBg = if (isDark) XmlNamespaceBadgeDark else XmlNamespaceBadgeLight
    val nsTextColor = if (isDark) XmlNamespaceTextDark else XmlNamespaceTextLight
    val hasChildren = node.hijos.isNotEmpty()

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "arrow_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = {
                    if (hasChildren) {
                        onToggleExpand()
                    } else {
                        onNodeClick()
                    }
                },
                onLongClick = onNodeLongClick
            )
            .padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        IndentationGuide(level = node.nivel)

        if (hasChildren) {
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = tagColor
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!node.namespacePrefix.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(nsBadgeBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = node.namespacePrefix,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = nsTextColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = node.localName,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = tagColor
                )

                if (hasChildren) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${node.hijos.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (node.atributos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val entries = node.atributos.entries.toList()
                    val previewCount = if (isExpanded) entries.size else entries.size.coerceAtMost(4)
                    for (i in 0 until previewCount) {
                        val entry = entries[i]
                        XmlAttributeChip(
                            key = entry.key,
                            value = entry.value
                        )
                    }
                    if (!isExpanded && entries.size > 4) {
                        Text(
                            text = "+${entries.size - 4} más",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            if (!node.valorTexto.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = node.valorTexto,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) 10 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
