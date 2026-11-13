package com.lumen.xmlreader.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumen.xmlreader.domain.model.XmlNode
import com.lumen.xmlreader.ui.components.ErrorCardComponent
import com.lumen.xmlreader.ui.components.LoadingComponent
import com.lumen.xmlreader.ui.components.LumenFullLogo
import com.lumen.xmlreader.ui.components.LumenIcon
import com.lumen.xmlreader.ui.components.NodeDetailDialog
import com.lumen.xmlreader.ui.components.SearchBarComponent
import com.lumen.xmlreader.ui.components.XmlNodeItem
import com.lumen.xmlreader.ui.viewmodel.ReaderTab
import com.lumen.xmlreader.ui.viewmodel.ReaderUiState
import com.lumen.xmlreader.ui.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadXmlUri(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (uiState is ReaderUiState.Success) {
                        val success = uiState as ReaderUiState.Success
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = success.documentInfo.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${success.documentInfo.totalNodes} nodos • ${success.documentInfo.fileSizeFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LumenIcon(size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LUMEN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Historial de archivos"
                        )
                    }
                },
                actions = {
                    if (uiState is ReaderUiState.Success) {
                        val success = uiState as ReaderUiState.Success

                        FilledTonalButton(
                            onClick = { viewModel.exportPdf(context) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Exportar PDF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (success.selectedTab == ReaderTab.ARBOL) {
                            IconButton(onClick = { viewModel.expandAll() }) {
                                Icon(
                                    imageVector = Icons.Outlined.UnfoldMore,
                                    contentDescription = "Expandir todo"
                                )
                            }
                            IconButton(onClick = { viewModel.collapseAll() }) {
                                Icon(
                                    imageVector = Icons.Outlined.UnfoldLess,
                                    contentDescription = "Colapsar todo"
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "text/xml",
                                    "application/xml",
                                    "text/plain",
                                    "*/*"
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileOpen,
                            contentDescription = "Abrir archivo XML"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ReaderUiState.Idle -> {
                    IdleWelcomeView(
                        onOpenFileClick = {
                            filePickerLauncher.launch(
                                arrayOf("text/xml", "application/xml", "text/plain", "*/*")
                            )
                        }
                    )
                }

                is ReaderUiState.Loading -> {
                    LoadingComponent()
                }

                is ReaderUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCardComponent(
                            message = state.message,
                            line = state.line,
                            column = state.column,
                            onOpenFileClick = {
                                filePickerLauncher.launch(
                                    arrayOf("text/xml", "application/xml", "text/plain", "*/*")
                                )
                            }
                        )
                    }
                }

                is ReaderUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = state.selectedTab.ordinal,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            divider = {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        ) {
                            ReaderTab.entries.forEach { tab ->
                                Tab(
                                    selected = state.selectedTab == tab,
                                    onClick = { viewModel.selectTab(tab) },
                                    text = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                )
                            }
                        }

                        when (state.selectedTab) {
                            ReaderTab.FACTURA -> {
                                InvoiceView(
                                    invoice = state.invoiceData,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReaderTab.ARBOL -> {
                                val visibleNodes = flattenVisibleNodes(
                                    root = state.documentInfo.rootNode,
                                    expandedIds = state.expandedNodeIds,
                                    query = state.searchQuery
                                )

                                Column(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(state.documentInfo.documentType.badgeColorHex))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = state.documentInfo.documentType.label,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "${visibleNodes.size} visibles",
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        SearchBarComponent(
                                            query = state.searchQuery,
                                            onQueryChanged = { viewModel.onSearchQueryChanged(it) }
                                        )
                                    }

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        items(
                                            items = visibleNodes,
                                            key = { it.id }
                                        ) { node ->
                                            val isExpanded = state.expandedNodeIds.contains(node.id)
                                            XmlNodeItem(
                                                node = node,
                                                isExpanded = isExpanded,
                                                onToggleExpand = { viewModel.toggleNodeExpansion(node.id) },
                                                onNodeClick = { viewModel.showNodeDetails(node) },
                                                onNodeLongClick = { viewModel.showNodeDetails(node) }
                                            )
                                        }
                                    }
                                }
                            }

                            ReaderTab.CRUDO -> {
                                RawXmlView(
                                    rawXml = state.documentInfo.rawXml,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    if (state.selectedDetailNode != null) {
                        NodeDetailDialog(
                            node = state.selectedDetailNode!!,
                            onDismiss = { viewModel.dismissNodeDetails() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleWelcomeView(
    onOpenFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LumenFullLogo(iconSize = 76.dp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Abre cualquier archivo XML y examina su factura limpia, árbol interactivo o código crudo original.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenFileClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FileOpen,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Seleccionar archivo XML",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Formatos y vistas:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "• Vista Factura: Presentación comercial estilizada para CFDI 3.3 y 4.0\n• Vista Árbol: Jerarquía interactiva con namespaces y chips de atributos\n• Vista Crudo: Código XML plano con copia rápida\n• Exportación PDF: Generación nativa y envío por WhatsApp o correo",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

private fun flattenVisibleNodes(
    root: XmlNode,
    expandedIds: Set<String>,
    query: String
): List<XmlNode> {
    val result = mutableListOf<XmlNode>()
    fun traverse(node: XmlNode) {
        val matchesThis = node.matchesQuery(query)
        val matchesDescendants = nodeMatchesDescendants(node, query)

        if (query.isBlank() || matchesThis || matchesDescendants) {
            result.add(node)
        }

        if (query.isNotBlank() || expandedIds.contains(node.id)) {
            for (child in node.hijos) {
                traverse(child)
            }
        }
    }
    traverse(root)
    return result
}

private fun nodeMatchesDescendants(node: XmlNode, query: String): Boolean {
    if (query.isBlank()) return true
    for (child in node.hijos) {
        if (child.matchesQuery(query) || nodeMatchesDescendants(child, query)) {
            return true
        }
    }
    return false
}
