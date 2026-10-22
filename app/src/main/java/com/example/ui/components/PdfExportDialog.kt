package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pdf.RdoShareHelper
import java.io.File

@Composable
fun PdfExportDialog(
    pdfFile: File,
    obraNome: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fileSizeKb = pdfFile.length() / 1024

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "RDO em PDF Pronto!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Arquivo: ${pdfFile.name}\nTamanho: ${fileSizeKb} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "O documento foi diagramado com o cabeçalho, tabelas, medições, legendas normativas de pintura e assinatura digital.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                // Botão de Envio Direto ao WhatsApp
                Button(
                    onClick = {
                        RdoShareHelper.sharePdfToWhatsApp(context, pdfFile, obraNome)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_whatsapp_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF25D366), // WhatsApp Green
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar para WhatsApp", fontWeight = FontWeight.Bold)
                }

                // Botão de Visualizar PDF
                OutlinedButton(
                    onClick = {
                        RdoShareHelper.openPdfViewer(context, pdfFile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_pdf_viewer_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Abrir e Visualizar PDF")
                }

                // Compartilhamento Geral
                TextButton(
                    onClick = {
                        RdoShareHelper.sharePdfGeneral(context, pdfFile, "Compartilhar RDO")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("share_other_apps_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Compartilhar com outros apps...")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_pdf_dialog_button")
            ) {
                Text("Fechar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
