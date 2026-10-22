package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CompanySettings

@Composable
fun CompanySettingsDialog(
    currentSettings: CompanySettings,
    onDismiss: () -> Unit,
    onSave: (CompanySettings) -> Unit
) {
    var nome by remember { mutableStateOf(currentSettings.nomeEmpresa) }
    var subtitulo by remember { mutableStateOf(currentSettings.subtitulo) }
    var cnpj by remember { mutableStateOf(currentSettings.cnpj) }
    var telefone by remember { mutableStateOf(currentSettings.telefone) }
    var email by remember { mutableStateOf(currentSettings.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text("Dados da Empresa no Cabeçalho", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Empresa / Razão Social") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("company_name_input")
                )
                OutlinedTextField(
                    value = subtitulo,
                    onValueChange = { subtitulo = it },
                    label = { Text("Ramo / Subtítulo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cnpj,
                    onValueChange = { cnpj = it },
                    label = { Text("CNPJ (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone / Contato (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentSettings.copy(
                            nomeEmpresa = nome.ifBlank { "Impacto Sinalização" },
                            subtitulo = subtitulo,
                            cnpj = cnpj,
                            telefone = telefone,
                            email = email
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_company_settings_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
