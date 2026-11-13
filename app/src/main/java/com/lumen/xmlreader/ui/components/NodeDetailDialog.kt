package com.lumen.xmlreader.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.xmlreader.domain.model.XmlNode
import com.lumen.xmlreader.ui.theme.AttributeKeyColor
import com.lumen.xmlreader.ui.theme.AttributeKeyColorDark
import com.lumen.xmlreader.ui.theme.AttributeValueColor
import com.lumen.xmlreader.ui.theme.AttributeValueColorDark
import com.lumen.xmlreader.ui.theme.XmlTagColorDark
import com.lumen.xmlreader.ui.theme.XmlTagColorLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NodeDetailDialog(
    node: XmlNode,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val tagColor = if (isDark) XmlTagColorDark else XmlTagColorLight
    val keyColor = if (isDark) AttributeKeyColorDark else AttributeKeyColor
    val valColor = if (isDark) AttributeValueColorDark else AttributeValueColor
    val boxBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val boxBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "<${node.nombre}>",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = tagColor,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Nivel: ${node.nivel} | Hijos directos: ${node.hijos.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        copyToClipboard(context, "Nodo ${node.nombre}", nodeToSummaryString(node))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar contenido"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!node.valorTexto.isNullOrBlank()) {
                    Text(
                        text = "Valor de texto:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(boxBg)
                            .border(1.dp, boxBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = node.valorTexto,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (node.atributos.isNotEmpty()) {
                    Text(
                        text = "Atributos (${node.atributos.size}):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        node.atributos.forEach { (k, v) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(boxBg)
                                    .border(0.5.dp, boxBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = k,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = keyColor,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = v,
                                        fontFamily = FontFamily.Monospace,
                                        color = valColor,
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, k, v)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copiar valor",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tagColor
                )
            ) {
                Text(text = "Cerrar")
            }
        }
    )
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
}

private fun nodeToSummaryString(node: XmlNode): String {
    val sb = StringBuilder()
    sb.append("<").append(node.nombre)
    node.atributos.forEach { (k, v) ->
        sb.append(" ").append(k).append("=\"").append(v).append("\"")
    }
    if (node.valorTexto.isNullOrBlank() && node.hijos.isEmpty()) {
        sb.append(" />")
    } else {
        sb.append(">")
        if (!node.valorTexto.isNullOrBlank()) {
            sb.append(node.valorTexto)
        }
        if (node.hijos.isNotEmpty()) {
            sb.append("\n  <!-- ").append(node.hijos.size).append(" nodos hijos -->\n")
        }
        sb.append("</").append(node.nombre).append(">")
    }
    return sb.toString()
}
