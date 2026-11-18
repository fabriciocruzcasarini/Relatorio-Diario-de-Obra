package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig

data class AppReleaseNote(
    val version: String,
    val badge: String,
    val title: String,
    val isCurrent: Boolean,
    val highlights: List<String>
)

fun buildAppReleaseHistory(compiledVersionName: String): List<AppReleaseNote> = listOf(
    AppReleaseNote(
        version = "v$compiledVersionName",
        badge = "Atual",
        title = "Versão de Produção & Melhorias de Campo",
        isCurrent = true,
        highlights = listOf(
            "Máscara automática HH:MM com validação (00:00–23:59) e teclado numérico nos campos de horário de saída, chegada e fechamento.",
            "Formulário de novo RDO iniciado limpo (sem dados fictícios ou textos de exemplo nos campos), mantendo apenas a data atual e os dados da empresa.",
            "Tela dedicada de Dados da Empresa com Nome, Ramo/Subtítulo, Encarregado e foto única de assinatura oficial bloqueada nos RDOs.",
            "Exportação de PDF A4 expandida para 12 itens de produção por folha com paginação completa e subtotais por página.",
            "Exibição automática da versão compilada do aplicativo (BuildConfig.VERSION_NAME).",
            "Alteração do tamanho da imagem de assinatura do Encarregado",
            "Alteração no tamanho do Rodaé"
        )
    ),
    AppReleaseNote(
        version = "Base",
        badge = "Core",
        title = "Recursos Essenciais de Engenharia Viária",
        isCurrent = false,
        highlights = listOf(
            "Formulário completo em 5 abas cobrindo identificação, condições climáticas e do pavimento, produção, recursos e liberação.",
            "Cálculo automático de área demarcada (M²) com suporte a separador decimal brasileiro (vírgula ou ponto).",
            "Armazenamento local offline em banco de dados Room (SQLite) com busca, edição e duplicação de relatórios.",
            "Compartilhamento imediato do PDF oficial pelo WhatsApp via FileProvider seguro.",
            "Redução da imagem de assinatura paa 400 x 160 px em formato PNG compacto",
            "Alteração no tamanho do Rodaé"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdatesScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE
    val releaseNotes = remember(versionName) {
        buildAppReleaseHistory(versionName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Atualizações do App",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Notas da Versão v$versionName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("app_updates_back_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Versão Compilada: v$versionName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("compiled_version_banner_text")
                            )
                            Text(
                                text = "Código da compilação (Build): $versionCode",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "Sincronizado automaticamente com as configurações da compilação.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            items(releaseNotes) { note ->
                ReleaseNoteCard(note = note)
            }
        }
    }
}

@Composable
private fun ReleaseNoteCard(note: AppReleaseNote) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = if (note.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${note.version} — ${note.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (note.isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = note.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (note.isCurrent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                note.highlights.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (note.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
