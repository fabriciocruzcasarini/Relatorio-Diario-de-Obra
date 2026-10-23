package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.components.SignaturePad
import com.example.ui.viewmodel.RdoViewModel
import com.example.util.FormatUtils
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RdoFormScreen(
    viewModel: RdoViewModel,
    onBack: () -> Unit,
    onPdfGenerated: (File) -> Unit
) {
    BackHandler { onBack() }

    val report by viewModel.currentReport.collectAsState()
    val productionItems by viewModel.productionItems.collectAsState()
    val resourceItems by viewModel.resourceItems.collectAsState()
    val materialItems by viewModel.materialItems.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()

    val tabTitles = listOf("1. Identificação", "2. Condições", "3. Produção", "4. Recursos", "5. Assinatura")

    var showSavedSnackbar by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    fun validateAndProceed(onSuccess: () -> Unit) {
        hasAttemptedSubmit = true
        val missing = mutableListOf<String>()
        if (report.obra.isBlank()) missing.add("Nome da Obra")
        if (report.cliente.isBlank()) missing.add("Cliente / Concessionária")
        if (report.fiscal.isBlank()) missing.add("Responsável / Fiscal")
        if (report.dataObra.isBlank()) missing.add("Data da Obra")

        if (missing.isNotEmpty()) {
            validationError = "Por favor preencha os campos obrigatórios: ${missing.joinToString(", ")}"
            viewModel.setActiveTab(0) // Focus back to Tab 0
        } else {
            validationError = null
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (report.id == 0L) "Novo RDO" else "Editar RDO #${report.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = report.obra.ifBlank { "Sinalização Horizontal" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("form_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveReport {
                                showSavedSnackbar = true
                            }
                        },
                        modifier = Modifier.testTag("save_rdo_action_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Salvar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeTab > 0) {
                        OutlinedButton(
                            onClick = { viewModel.setActiveTab(activeTab - 1) },
                            modifier = Modifier.testTag("prev_tab_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            Text("Anterior")
                        }
                    } else {
                        Spacer(Modifier.width(80.dp))
                    }

                    if (activeTab < tabTitles.size - 1) {
                        Button(
                            onClick = { viewModel.setActiveTab(activeTab + 1) },
                            modifier = Modifier.testTag("next_tab_button")
                        ) {
                            Text("Próximo")
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = {
                                validateAndProceed {
                                    viewModel.generatePdf { file ->
                                        onPdfGenerated(file)
                                    }
                                }
                            },
                            enabled = !isGeneratingPdf,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("generate_pdf_button")
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Gerando PDF...")
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Gerar PDF & WhatsApp", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { viewModel.setActiveTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }

            // Error validation alert
            if (validationError != null) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = validationError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF991B1B)
                            )
                        }
                        IconButton(onClick = { validationError = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color(0xFF991B1B), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (showSavedSnackbar) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RDO salvo com sucesso no banco de dados local!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF166534)
                        )
                        IconButton(onClick = { showSavedSnackbar = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    0 -> TabIdentificacao(
                        report = report,
                        onUpdate = viewModel::updateReport,
                        hasAttemptedSubmit = hasAttemptedSubmit
                    )
                    1 -> TabCondicoes(report = report, onUpdate = viewModel::updateReport)
                    2 -> TabProducao(
                        items = productionItems,
                        report = report,
                        onUpdateReport = viewModel::updateReport,
                        onAddItem = viewModel::addProductionItem,
                        onUpdateItem = viewModel::updateProductionItem,
                        onRemoveItem = viewModel::removeProductionItem
                    )
                    3 -> TabRecursosMateriais(
                        resources = resourceItems,
                        materials = materialItems,
                        onAddResource = viewModel::addResourceItem,
                        onUpdateResource = viewModel::updateResourceItem,
                        onRemoveResource = viewModel::removeResourceItem,
                        onAddMaterial = viewModel::addMaterialItem,
                        onUpdateMaterial = viewModel::updateMaterialItem,
                        onRemoveMaterial = viewModel::removeMaterialItem
                    )
                    4 -> TabLiberacaoAssinatura(
                        report = report,
                        onUpdate = viewModel::updateReport,
                        onSignatureSaved = viewModel::setSignaturePath,
                        onGeneratePdf = {
                            validateAndProceed {
                                viewModel.generatePdf { file ->
                                    onPdfGenerated(file)
                                }
                            }
                        },
                        isGenerating = isGeneratingPdf
                    )
                }
            }
        }
    }
}

// ------------------- TAB 1: IDENTIFICAÇÃO -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabIdentificacao(
    report: RdoReport,
    onUpdate: ((RdoReport) -> RdoReport) -> Unit,
    hasAttemptedSubmit: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = FormatUtils.formatDateToBrazilian(millis)
                        onUpdate { it.copy(dataObra = formatted) }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "Dados Principais da Obra") {
            OutlinedTextField(
                value = report.obra,
                onValueChange = { v -> onUpdate { it.copy(obra = v) } },
                label = { Text("Nome da Obra / Rodovia *") },
                placeholder = { Text("Ex: SP-330 Rodovia Anhanguera") },
                singleLine = true,
                isError = hasAttemptedSubmit && report.obra.isBlank(),
                supportingText = if (hasAttemptedSubmit && report.obra.isBlank()) {
                    { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth().testTag("input_obra")
            )
            OutlinedTextField(
                value = report.cliente,
                onValueChange = { v -> onUpdate { it.copy(cliente = v) } },
                label = { Text("Cliente / Concessionária *") },
                placeholder = { Text("Ex: ARTESP / CCR AutoBAn") },
                singleLine = true,
                isError = hasAttemptedSubmit && report.cliente.isBlank(),
                supportingText = if (hasAttemptedSubmit && report.cliente.isBlank()) {
                    { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth().testTag("input_cliente")
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.dataObra,
                    onValueChange = { v -> onUpdate { it.copy(dataObra = v) } },
                    label = { Text("Data da Obra *") },
                    placeholder = { Text("DD/MM/AAAA") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }, modifier = Modifier.testTag("open_date_picker_button")) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar data")
                        }
                    },
                    singleLine = true,
                    isError = hasAttemptedSubmit && report.dataObra.isBlank(),
                    modifier = Modifier.weight(1.2f).testTag("input_data_obra")
                )
                OutlinedTextField(
                    value = report.veiculoKm,
                    onValueChange = { v -> onUpdate { it.copy(veiculoKm = v) } },
                    label = { Text("Veículo KM") },
                    placeholder = { Text("Ex: 142.500") },
                    singleLine = true,
                    modifier = Modifier.weight(0.8f).testTag("input_veiculo_km")
                )
            }
        }

        SectionCard(title = "Equipe de Fiscalização & Turno") {
            OutlinedTextField(
                value = report.fiscal,
                onValueChange = { v -> onUpdate { it.copy(fiscal = v) } },
                label = { Text("Responsável / Fiscal da Obra *") },
                singleLine = true,
                isError = hasAttemptedSubmit && report.fiscal.isBlank(),
                supportingText = if (hasAttemptedSubmit && report.fiscal.isBlank()) {
                    { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth().testTag("input_fiscal")
            )

            Text("Turno de Operação:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onUpdate { it.copy(turnoDia = !it.turnoDia) } }
                ) {
                    Checkbox(
                        checked = report.turnoDia,
                        onCheckedChange = { v -> onUpdate { it.copy(turnoDia = v) } },
                        modifier = Modifier.testTag("chk_turno_dia")
                    )
                    Text("DIA")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onUpdate { it.copy(turnoNoite = !it.turnoNoite) } }
                ) {
                    Checkbox(
                        checked = report.turnoNoite,
                        onCheckedChange = { v -> onUpdate { it.copy(turnoNoite = v) } },
                        modifier = Modifier.testTag("chk_turno_noite")
                    )
                    Text("NOITE")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.horaSaida,
                    onValueChange = { v -> onUpdate { it.copy(horaSaida = v) } },
                    label = { Text("Horário Saída") },
                    placeholder = { Text("07:30") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_hora_saida")
                )
                OutlinedTextField(
                    value = report.horaChegada,
                    onValueChange = { v -> onUpdate { it.copy(horaChegada = v) } },
                    label = { Text("Horário Chegada") },
                    placeholder = { Text("17:45") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_hora_chegada")
                )
            }
        }

        SectionCard(title = "Controle de Fechamento de Faixa") {
            OutlinedTextField(
                value = report.fechamentoLocal,
                onValueChange = { v -> onUpdate { it.copy(fechamentoLocal = v) } },
                label = { Text("Local de Execução") },
                placeholder = { Text("Ex: KM 102 ao 106 - Pista Norte") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_fechamento_local")
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.fechamentoContato,
                    onValueChange = { v -> onUpdate { it.copy(fechamentoContato = v) } },
                    label = { Text("Contato / CCO") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f).testTag("input_fechamento_contato")
                )
                OutlinedTextField(
                    value = report.fechamentoHorario,
                    onValueChange = { v -> onUpdate { it.copy(fechamentoHorario = v) } },
                    label = { Text("Horário") },
                    singleLine = true,
                    modifier = Modifier.weight(0.8f).testTag("input_fechamento_horario")
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.fechamentoLiberadoPor,
                    onValueChange = { v -> onUpdate { it.copy(fechamentoLiberadoPor = v) } },
                    label = { Text("Liberado Por") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f).testTag("input_fechamento_liberado")
                )
                OutlinedTextField(
                    value = report.fechamentoHorarioFim,
                    onValueChange = { v -> onUpdate { it.copy(fechamentoHorarioFim = v) } },
                    label = { Text("Horário Fech.") },
                    singleLine = true,
                    modifier = Modifier.weight(0.8f).testTag("input_fechamento_fim")
                )
            }
        }
    }
}

// ------------------- TAB 2: CONDIÇÕES & CLIMA -------------------
@Composable
private fun TabCondicoes(
    report: RdoReport,
    onUpdate: ((RdoReport) -> RdoReport) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "Condições do Pavimento") {
            OutlinedTextField(
                value = report.tipoPavimento,
                onValueChange = { v -> onUpdate { it.copy(tipoPavimento = v) } },
                label = { Text("Tipo de Pavimento") },
                placeholder = { Text("Ex: CBUQ, Concreto, Microrrevestimento") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_tipo_pavimento")
            )

            Text("Umidade do Pavimento:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    onUpdate { it.copy(pavimentoSeco = true, pavimentoUmido = false, pavimentoMolhado = false) }
                }) {
                    RadioButton(
                        selected = report.pavimentoSeco,
                        onClick = { onUpdate { it.copy(pavimentoSeco = true, pavimentoUmido = false, pavimentoMolhado = false) } }
                    )
                    Text("Seco")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    onUpdate { it.copy(pavimentoSeco = false, pavimentoUmido = true, pavimentoMolhado = false) }
                }) {
                    RadioButton(
                        selected = report.pavimentoUmido,
                        onClick = { onUpdate { it.copy(pavimentoSeco = false, pavimentoUmido = true, pavimentoMolhado = false) } }
                    )
                    Text("Úmido")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    onUpdate { it.copy(pavimentoSeco = false, pavimentoUmido = false, pavimentoMolhado = true) }
                }) {
                    RadioButton(
                        selected = report.pavimentoMolhado,
                        onClick = { onUpdate { it.copy(pavimentoSeco = false, pavimentoUmido = false, pavimentoMolhado = true) } }
                    )
                    Text("Molhado")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.repinturaSobre,
                    onValueChange = { v -> onUpdate { it.copy(repinturaSobre = v) } },
                    label = { Text("Repintura Sobre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = report.aderenciaPinturaAntiga,
                    onValueChange = { v -> onUpdate { it.copy(aderenciaPinturaAntiga = v) } },
                    label = { Text("Aderência Pintura Antiga") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.diasSemChuva,
                    onValueChange = { v -> onUpdate { it.copy(diasSemChuva = v) } },
                    label = { Text("Dias Sem Chuva") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = report.pavimentoNovo,
                        onCheckedChange = { v -> onUpdate { it.copy(pavimentoNovo = v) } }
                    )
                    Text("Pavimento Novo?")
                }
            }

            Text("Contaminações na Pista:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("BOM", "REGULAR", "RUIM").forEach { option ->
                    FilterChip(
                        selected = report.contaminacoes == option,
                        onClick = { onUpdate { it.copy(contaminacoes = option) } },
                        label = { Text(option) }
                    )
                }
            }
        }

        SectionCard(title = "Condições Climáticas & Aplicação") {
            Text("Tempo:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = report.tempoAberto,
                    onClick = { onUpdate { it.copy(tempoAberto = true, tempoNublado = false, tempoChuva = false) } },
                    label = { Text("Aberto / Ensolarado") }
                )
                FilterChip(
                    selected = report.tempoNublado,
                    onClick = { onUpdate { it.copy(tempoAberto = false, tempoNublado = true, tempoChuva = false) } },
                    label = { Text("Nublado") }
                )
                FilterChip(
                    selected = report.tempoChuva,
                    onClick = { onUpdate { it.copy(tempoAberto = false, tempoNublado = false, tempoChuva = true) } },
                    label = { Text("Chuva") }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.tempAmbiente,
                    onValueChange = { v -> onUpdate { it.copy(tempAmbiente = v) } },
                    label = { Text("Temp. Ambiente") },
                    placeholder = { Text("Ex: 28°C") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = report.umidadeRelativa,
                    onValueChange = { v -> onUpdate { it.copy(umidadeRelativa = v) } },
                    label = { Text("Umidade Relativa") },
                    placeholder = { Text("Ex: 55% (Máx 65%)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.pontoOrvalho,
                    onValueChange = { v -> onUpdate { it.copy(pontoOrvalho = v) } },
                    label = { Text("Ponto de Orvalho") },
                    placeholder = { Text("Ex: 17°C") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = report.tempPavimento,
                    onValueChange = { v -> onUpdate { it.copy(tempPavimento = v) } },
                    label = { Text("Temp. Pavimento") },
                    placeholder = { Text("Ex: 34°C") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = report.liberacaoTrafegoMin,
                    onValueChange = { v -> onUpdate { it.copy(liberacaoTrafegoMin = v) } },
                    label = { Text("Lib. Tráfego (min)") },
                    placeholder = { Text("Ex: 30") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = report.choveuAposPintura,
                    onValueChange = { v -> onUpdate { it.copy(choveuAposPintura = v) } },
                    label = { Text("Choveu Após?") },
                    placeholder = { Text("Ex: NÃO") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ------------------- TAB 3: PRODUÇÃO & SERVIÇOS -------------------
@Composable
private fun TabProducao(
    items: List<RdoProductionItem>,
    report: RdoReport,
    onUpdateReport: ((RdoReport) -> RdoReport) -> Unit,
    onAddItem: () -> Unit,
    onUpdateItem: (Int, RdoProductionItem) -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    val totalM2Geral = remember(items) { items.sumOf { it.totalM2 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "Tipo de Tinta & Método de Aplicação") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MethodCheckbox(
                    title = "Base Água",
                    checked = report.metodoBaseAgua,
                    onChecked = { onUpdateReport { it.copy(metodoBaseAgua = !it.metodoBaseAgua) } },
                    modifier = Modifier.weight(1f)
                )
                MethodCheckbox(
                    title = "Acrílica Solvente",
                    checked = report.metodoAcrilicaSolvente,
                    onChecked = { onUpdateReport { it.copy(metodoAcrilicaSolvente = !it.metodoAcrilicaSolvente) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MethodCheckbox(
                    title = "Plástica a Frio",
                    checked = report.metodoPlasticaAFrio,
                    onChecked = { onUpdateReport { it.copy(metodoPlasticaAFrio = !it.metodoPlasticaAFrio) } },
                    modifier = Modifier.weight(1f)
                )
                MethodCheckbox(
                    title = "Laminado",
                    checked = report.metodoLaminado,
                    onChecked = { onUpdateReport { it.copy(metodoLaminado = !it.metodoLaminado) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MethodCheckbox(
                    title = "Manual",
                    checked = report.metodoManual,
                    onChecked = { onUpdateReport { it.copy(metodoManual = !it.metodoManual) } },
                    modifier = Modifier.weight(1f)
                )
                MethodCheckbox(
                    title = "Mecânico",
                    checked = report.metodoMecanico,
                    onChecked = { onUpdateReport { it.copy(metodoMecanico = !it.metodoMecanico) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MethodCheckbox(
                    title = "Termo Extrudado",
                    checked = report.metodoTermExtrudado,
                    onChecked = { onUpdateReport { it.copy(metodoTermExtrudado = !it.metodoTermExtrudado) } },
                    modifier = Modifier.weight(1f)
                )
                MethodCheckbox(
                    title = "Hot Spray",
                    checked = report.metodoHotSpray,
                    onChecked = { onUpdateReport { it.copy(metodoHotSpray = !it.metodoHotSpray) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MethodCheckbox(
                    title = "Termo Relevo",
                    checked = report.metodoTermRelevo,
                    onChecked = { onUpdateReport { it.copy(metodoTermRelevo = !it.metodoTermRelevo) } },
                    modifier = Modifier.weight(1f)
                )
                MethodCheckbox(
                    title = "Remoção Fogo/Fresca",
                    checked = report.remocaoFresca || report.remocaoFogo,
                    onChecked = { onUpdateReport { it.copy(remocaoFresca = !it.remocaoFresca) } },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = report.metodoOutros,
                onValueChange = { v -> onUpdateReport { it.copy(metodoOutros = v) } },
                label = { Text("Outros Métodos / Observação") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionCard(title = "Relatório de Produção (Trechos Executados)") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL EXECUTADO:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (items.size > 7) {
                            Text(
                                text = "${items.size} trechos registrados (será gerado anexo de continuação)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = String.format(Locale.US, "%.2f M²", totalM2Geral),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items.forEachIndexed { index, item ->
                ProductionItemCard(
                    index = index,
                    item = item,
                    onUpdate = { updated -> onUpdateItem(index, updated) },
                    onRemove = { onRemoveItem(index) }
                )
            }

            Button(
                onClick = onAddItem,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_production_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Adicionar Linha de Produção")
            }
        }
    }
}

@Composable
private fun ProductionItemCard(
    index: Int,
    item: RdoProductionItem,
    onUpdate: (RdoProductionItem) -> Unit,
    onRemove: () -> Unit
) {
    var compText by remember(item.id, index) {
        mutableStateOf(if (item.comprimentoM > 0) FormatUtils.formatDecimal(item.comprimentoM, 1) else "")
    }
    var largText by remember(item.id, index) {
        mutableStateOf(if (item.larguraM > 0) FormatUtils.formatDecimal(item.larguraM, 2) else "")
    }
    var faixasText by remember(item.id, index) {
        mutableStateOf(item.quantFaixas.toString())
    }
    var totalM2Text by remember(item.id, index) {
        mutableStateOf(if (item.totalM2 > 0) FormatUtils.formatDecimal(item.totalM2, 2) else "")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trecho #${index + 1}: ${item.servicoExecutado}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.servicoExecutado,
                    onValueChange = { onUpdate(item.copy(servicoExecutado = it)) },
                    label = { Text("Serviço (ex: LBO, LMS-1)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )
                OutlinedTextField(
                    value = item.rodovia,
                    onValueChange = { onUpdate(item.copy(rodovia = it)) },
                    label = { Text("Rodovia") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.kmInicial,
                    onValueChange = { onUpdate(item.copy(kmInicial = it)) },
                    label = { Text("KM Inicial") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = item.kmFinal,
                    onValueChange = { onUpdate(item.copy(kmFinal = it)) },
                    label = { Text("KM Final") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = faixasText,
                    onValueChange = { v ->
                        faixasText = v
                        val faixas = v.toIntOrNull() ?: 1
                        val comp = FormatUtils.parseBrazilianDecimal(compText)
                        val larg = FormatUtils.parseBrazilianDecimal(largText)
                        val m2 = Math.round(faixas * comp * larg * 100.0) / 100.0
                        totalM2Text = if (m2 > 0) FormatUtils.formatDecimal(m2, 2) else ""
                        onUpdate(item.copy(quantFaixas = faixas, totalM2 = m2))
                    },
                    label = { Text("Qtd Faixas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compText,
                    onValueChange = { v ->
                        compText = v
                        val comp = FormatUtils.parseBrazilianDecimal(v)
                        val faixas = faixasText.toIntOrNull() ?: 1
                        val larg = FormatUtils.parseBrazilianDecimal(largText)
                        val m2 = Math.round(faixas * comp * larg * 100.0) / 100.0
                        totalM2Text = if (m2 > 0) FormatUtils.formatDecimal(m2, 2) else ""
                        onUpdate(item.copy(comprimentoM = comp, totalM2 = m2))
                    },
                    label = { Text("Comprimento (M)") },
                    placeholder = { Text("ex: 1500,5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = largText,
                    onValueChange = { v ->
                        largText = v
                        val larg = FormatUtils.parseBrazilianDecimal(v)
                        val comp = FormatUtils.parseBrazilianDecimal(compText)
                        val faixas = faixasText.toIntOrNull() ?: 1
                        val m2 = Math.round(faixas * comp * larg * 100.0) / 100.0
                        totalM2Text = if (m2 > 0) FormatUtils.formatDecimal(m2, 2) else ""
                        onUpdate(item.copy(larguraM = larg, totalM2 = m2))
                    },
                    label = { Text("Largura (M)") },
                    placeholder = { Text("ex: 0,15") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1.1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = totalM2Text,
                    onValueChange = { v ->
                        totalM2Text = v
                        val m2 = FormatUtils.parseBrazilianDecimal(v)
                        onUpdate(item.copy(totalM2 = m2))
                    },
                    label = { Text("Total M² (Auto/Ajuste)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )
                OutlinedTextField(
                    value = item.cor,
                    onValueChange = { onUpdate(item.copy(cor = it)) },
                    label = { Text("Cor") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = item.espessuraMm,
                    onValueChange = { onUpdate(item.copy(espessuraMm = it)) },
                    label = { Text("Esp. (mm)") },
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

// ------------------- TAB 4: RECURSOS & MATERIAIS -------------------
@Composable
private fun TabRecursosMateriais(
    resources: List<RdoResourceItem>,
    materials: List<RdoMaterialItem>,
    onAddResource: () -> Unit,
    onUpdateResource: (Int, RdoResourceItem) -> Unit,
    onRemoveResource: (Int) -> Unit,
    onAddMaterial: () -> Unit,
    onUpdateMaterial: (Int, RdoMaterialItem) -> Unit,
    onRemoveMaterial: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "Recursos (Equipe & Frota)") {
            resources.forEachIndexed { index, res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recurso #${index + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onRemoveResource(index) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        OutlinedTextField(
                            value = res.equipe,
                            onValueChange = { onUpdateResource(index, res.copy(equipe = it)) },
                            label = { Text("Equipe (Pintores, Encarregados, Motoristas)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = res.veiculos,
                                onValueChange = { onUpdateResource(index, res.copy(veiculos = it)) },
                                label = { Text("Veículo / Caminhão") },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )
                            OutlinedTextField(
                                value = res.placa,
                                onValueChange = { onUpdateResource(index, res.copy(placa = it)) },
                                label = { Text("Placa") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = res.equipamentosMedicao,
                            onValueChange = { onUpdateResource(index, res.copy(equipamentosMedicao = it)) },
                            label = { Text("Equipamentos / Instrumentos de Medição") },
                            placeholder = { Text("Ex: Espessímetro Digital, Higrômetro") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            OutlinedButton(onClick = onAddResource, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Adicionar Recurso / Veículo")
            }
        }

        SectionCard(title = "Materiais Utilizados (Tintas, Esferas, Lotes)") {
            materials.forEachIndexed { index, mat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Material #${index + 1}: ${mat.descricao.ifBlank { "Insumo" }}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onRemoveMaterial(index) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mat.quantidade,
                                onValueChange = { onUpdateMaterial(index, mat.copy(quantidade = it)) },
                                label = { Text("Qtd") },
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                            OutlinedTextField(
                                value = mat.unidade,
                                onValueChange = { onUpdateMaterial(index, mat.copy(unidade = it)) },
                                label = { Text("Unid.") },
                                placeholder = { Text("GL/SC/KG") },
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                            OutlinedTextField(
                                value = mat.marca,
                                onValueChange = { onUpdateMaterial(index, mat.copy(marca = it)) },
                                label = { Text("Marca") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                        OutlinedTextField(
                            value = mat.descricao,
                            onValueChange = { onUpdateMaterial(index, mat.copy(descricao = it)) },
                            label = { Text("Descrição do Material") },
                            placeholder = { Text("Ex: Tinta Acrílica Solvente Branca Tipo I") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mat.fabricacao,
                                onValueChange = { onUpdateMaterial(index, mat.copy(fabricacao = it)) },
                                label = { Text("Fabricação") },
                                placeholder = { Text("MM/AAAA") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mat.validade,
                                onValueChange = { onUpdateMaterial(index, mat.copy(validade = it)) },
                                label = { Text("Validade") },
                                placeholder = { Text("MM/AAAA") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mat.lote,
                                onValueChange = { onUpdateMaterial(index, mat.copy(lote = it)) },
                                label = { Text("Lote") },
                                singleLine = true,
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }
            }

            OutlinedButton(onClick = onAddMaterial, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Adicionar Insumo / Material")
            }
        }
    }
}

// ------------------- TAB 5: LIBERAÇÃO & ASSINATURA -------------------
@Composable
private fun TabLiberacaoAssinatura(
    report: RdoReport,
    onUpdate: ((RdoReport) -> RdoReport) -> Unit,
    onSignatureSaved: (String) -> Unit,
    onGeneratePdf: () -> Unit,
    isGenerating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "Observações Gerais da Obra") {
            OutlinedTextField(
                value = report.observacoes,
                onValueChange = { v -> onUpdate { it.copy(observacoes = v) } },
                label = { Text("Ocorrências, interferências e observações de campo") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth().testTag("input_observacoes")
            )
        }

        SectionCard(title = "Liberação da Obra (Parecer do Fiscal)") {
            Text("Status de Aprovação:", style = MaterialTheme.typography.labelMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "APROVADO",
                    "APROVADO COM RETRABALHO",
                    "REPROVADO"
                ).forEach { status ->
                    FilterChip(
                        selected = report.liberacaoStatus == status,
                        onClick = { onUpdate { it.copy(liberacaoStatus = status) } },
                        label = {
                            Text(
                                text = if (status == "APROVADO COM RETRABALHO") "APROV. C/ RETRAB." else status,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = report.responsavelNome,
                onValueChange = { v -> onUpdate { it.copy(responsavelNome = v) } },
                label = { Text("Nome do Responsável / Fiscal Assinante") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_responsavel_nome")
            )
        }

        SectionCard(title = "Assinatura Digital em Tela") {
            SignaturePad(
                initialSignaturePath = report.assinaturaBitmapPath,
                onSignatureSaved = onSignatureSaved
            )
        }

        Button(
            onClick = onGeneratePdf,
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_generate_pdf_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Processando Relatório...")
            } else {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Emitir RDO em PDF & WhatsApp", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            content()
        }
    }
}

@Composable
private fun MethodCheckbox(
    title: String,
    checked: Boolean,
    onChecked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onChecked() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onChecked() })
        Text(title, style = MaterialTheme.typography.bodySmall)
    }
}
