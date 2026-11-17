package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.model.CompanySettings
import com.example.ui.components.copyUriToInternalStorage
import com.example.ui.viewmodel.RdoViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySettingsScreen(
    viewModel: RdoViewModel,
    onBack: () -> Unit,
    onOpenVersionUpdates: () -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val currentSettings by viewModel.companySettings.collectAsState()

    var nomeEmpresa by remember(currentSettings.id, currentSettings.nomeEmpresa) {
        mutableStateOf(currentSettings.nomeEmpresa)
    }
    var subtitulo by remember(currentSettings.id, currentSettings.subtitulo) {
        mutableStateOf(currentSettings.subtitulo)
    }
    var encarregadoNome by remember(currentSettings.id, currentSettings.defaultResponsavelNome) {
        mutableStateOf(currentSettings.defaultResponsavelNome)
    }
    var signaturePath by remember(currentSettings.id, currentSettings.defaultAssinaturaPath) {
        mutableStateOf(currentSettings.defaultAssinaturaPath)
    }
    var cnpj by remember(currentSettings.id, currentSettings.cnpj) {
        mutableStateOf(currentSettings.cnpj)
    }
    var telefone by remember(currentSettings.id, currentSettings.telefone) {
        mutableStateOf(currentSettings.telefone)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = copyUriToInternalStorage(context, uri)
            if (savedPath != null) {
                signaturePath = savedPath
                Toast.makeText(context, "Foto da assinatura carregada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Erro ao processar imagem da assinatura.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val sigFile = signaturePath?.let { File(it) }?.takeIf { it.exists() }
    val sigBitmap = remember(signaturePath) {
        sigFile?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dados da Empresa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cabeçalho e Assinatura Oficial do RDO",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("company_settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card informativo de sincronização
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "As informações e a assinatura definidas aqui são únicas e oficiais. Elas serão inseridas automaticamente em todos os novos RDOs e impressas no cabeçalho e rodapé do PDF.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Bloco 1: Cabeçalho da Empresa
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Identificação da Empresa",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    OutlinedTextField(
                        value = nomeEmpresa,
                        onValueChange = { nomeEmpresa = it },
                        label = { Text("Nome da Empresa *") },
                        placeholder = { Text("Ex: Impacto Sinalização Viária Ltda") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("company_name_input")
                    )

                    OutlinedTextField(
                        value = subtitulo,
                        onValueChange = { subtitulo = it },
                        label = { Text("Ramo / Subtítulo *") },
                        placeholder = { Text("Ex: Sinalização Viária Horizontal e Vertical") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("company_subtitle_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cnpj,
                            onValueChange = { cnpj = it },
                            label = { Text("CNPJ (Opcional)") },
                            placeholder = { Text("00.000.000/0001-00") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("company_cnpj_input")
                        )
                        OutlinedTextField(
                            value = telefone,
                            onValueChange = { telefone = it },
                            label = { Text("Telefone / CCO") },
                            placeholder = { Text("(11) 99999-0000") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("company_phone_input")
                        )
                    }
                }
            }

            // Bloco 2: Encarregado & Assinatura Oficial
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DriveFolderUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Encarregado & Assinatura Oficial",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    OutlinedTextField(
                        value = encarregadoNome,
                        onValueChange = { encarregadoNome = it },
                        label = { Text("Nome do Encarregado *") },
                        placeholder = { Text("Ex: Carlos Roberto Mendes (Encarregado)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manager_name_input")
                    )

                    Text(
                        text = "Arquivo da Assinatura (Única e Oficial)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (sigBitmap != null) {
                        // Exibição do preview da assinatura salva
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = sigBitmap.asImageBitmap(),
                                contentDescription = "Foto da Assinatura Oficial",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("change_signature_file_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Trocar Foto")
                            }

                            FilledTonalButton(
                                onClick = { signaturePath = null },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.testTag("remove_signature_file_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Remover")
                            }
                        }

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Assinatura oficial pronta. Bloqueada e aplicada automaticamente.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Estado sem assinatura cadastrada
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(16.dp)
                                .testTag("select_signature_file_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Toque para selecionar a foto da assinatura",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Formatos aceitos: JPG ou PNG (fundo branco nítido)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Bloco 3: Versão do App & Atualizações
            OutlinedCard(
                onClick = onOpenVersionUpdates,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_version_updates_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Atualizações da Versão",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Versão compilada: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("company_settings_version_text")
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ver notas da versão",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Botão Principal de Salvar
            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        nomeEmpresa = nomeEmpresa.ifBlank { "Impacto Sinalização" },
                        subtitulo = subtitulo.ifBlank { "Sinalização Viária Horizontal e Vertical" },
                        defaultResponsavelNome = encarregadoNome,
                        defaultAssinaturaPath = signaturePath,
                        cnpj = cnpj,
                        telefone = telefone
                    )
                    viewModel.saveCompanySettings(updated)
                    Toast.makeText(context, "Dados da empresa e assinatura salvos com sucesso!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_company_settings_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Salvar Dados da Empresa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
