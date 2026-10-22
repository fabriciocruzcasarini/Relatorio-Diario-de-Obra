package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CompanySettings
import com.example.data.model.RdoWithDetails
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RdoListScreen(
    rdoList: List<RdoWithDetails>,
    companySettings: CompanySettings,
    onNewRdoClick: () -> Unit,
    onEditRdoClick: (RdoWithDetails) -> Unit,
    onViewPdfClick: (RdoWithDetails) -> Unit,
    onDuplicateRdoClick: (RdoWithDetails) -> Unit,
    onDeleteRdoClick: (RdoWithDetails) -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var rdoToDelete by remember { mutableStateOf<RdoWithDetails?>(null) }

    val filteredList = remember(rdoList, searchQuery) {
        if (searchQuery.isBlank()) {
            rdoList
        } else {
            val q = searchQuery.lowercase(Locale.getDefault())
            rdoList.filter {
                it.report.obra.lowercase(Locale.getDefault()).contains(q) ||
                it.report.cliente.lowercase(Locale.getDefault()).contains(q) ||
                it.report.fiscal.lowercase(Locale.getDefault()).contains(q) ||
                it.report.dataObra.contains(q)
            }
        }
    }

    val totalM2Geral = remember(rdoList) {
        rdoList.sumOf { rdo -> rdo.productionItems.sumOf { it.totalM2 } }
    }
    val totalAprovados = remember(rdoList) {
        rdoList.count { it.report.liberacaoStatus == "APROVADO" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Diário de Obra - RDO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = companySettings.nomeEmpresa,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettingsClick,
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações da Empresa")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewRdoClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo RDO", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("new_rdo_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Metrics Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Total de RDOs",
                    value = rdoList.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Pintado",
                    value = String.format(Locale.US, "%.0f m²", totalM2Geral),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Aprovados",
                    value = "$totalAprovados/${rdoList.size}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por obra, rodovia, fiscal ou data...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_rdo_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(4.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "Nenhum relatório criado ainda" else "Nenhum RDO encontrado para a busca",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Toque em 'Novo RDO' para preencher e emitir o primeiro diário de obra com exportação em PDF.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.report.id }) { rdo ->
                        RdoCardItem(
                            rdo = rdo,
                            onEdit = { onEditRdoClick(rdo) },
                            onViewPdf = { onViewPdfClick(rdo) },
                            onDuplicate = { onDuplicateRdoClick(rdo) },
                            onDelete = { rdoToDelete = rdo }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (rdoToDelete != null) {
        AlertDialog(
            onDismissRequest = { rdoToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir RDO?") },
            text = {
                Text("Deseja realmente excluir o relatório da obra '${rdoToDelete?.report?.obra}' (${rdoToDelete?.report?.dataObra})? Os dados e o PDF serão removidos do dispositivo.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        rdoToDelete?.let { onDeleteRdoClick(it) }
                        rdoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { rdoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RdoCardItem(
    rdo: RdoWithDetails,
    onEdit: () -> Unit,
    onViewPdf: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val report = rdo.report
    val totalM2 = remember(rdo.productionItems) {
        rdo.productionItems.sumOf { it.totalM2 }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rdo_card_${report.id}")
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Obra & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.obra.ifBlank { "Sem identificação de obra" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cliente: ${report.cliente.ifBlank { "Não informado" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = report.liberacaoStatus)
            }

            Spacer(Modifier.height(8.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Data: ${report.dataObra}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Turno: ${if (report.turnoDia) "Dia" else if (report.turnoNoite) "Noite" else "Integral"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.1f m² executados", totalM2),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Fiscal: ${report.fiscal.ifBlank { "N/D" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onViewPdf,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("whatsapp_card_button_${report.id}")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PDF / WhatsApp", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("edit_card_button_${report.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Row {
                    IconButton(onClick = onDuplicate, modifier = Modifier.testTag("duplicate_button_${report.id}")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar RDO", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_button_${report.id}")) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir RDO", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg) = when (status) {
        "APROVADO" -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
        "APROVADO COM RETRABALHO" -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        else -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
