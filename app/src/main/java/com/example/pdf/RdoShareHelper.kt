package com.example.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object RdoShareHelper {

    fun getFileUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun sharePdfToWhatsApp(context: Context, file: File, obraNome: String = "") {
        if (!file.exists()) {
            Toast.makeText(context, "Arquivo PDF não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = getFileUri(context, file)

        // Try standard WhatsApp first, then WhatsApp Business, or fall back to system chooser
        val message = "Segue em anexo o Relatório Diário de Obra (RDO)" +
                if (obraNome.isNotBlank()) " - $obraNome." else "."

        val waIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
        }

        try {
            context.startActivity(waIntent)
        } catch (e: Exception) {
            // Try WhatsApp Business
            val waBusinessIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                `package` = "com.whatsapp.w4b"
            }
            try {
                context.startActivity(waBusinessIntent)
            } catch (e2: Exception) {
                // Fall back to general chooser
                sharePdfGeneral(context, file, "Compartilhar RDO via WhatsApp / Aplicativo")
            }
        }
    }

    fun sharePdfGeneral(context: Context, file: File, title: String = "Compartilhar RDO") {
        if (!file.exists()) {
            Toast.makeText(context, "Arquivo PDF não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = getFileUri(context, file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Diário de Obra (RDO)")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o Relatório Diário de Obra (RDO).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title)
        context.startActivity(chooser)
    }

    fun openPdfViewer(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "Arquivo PDF não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = getFileUri(context, file)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Nenhum visualizador de PDF encontrado no dispositivo.", Toast.LENGTH_LONG).show()
        }
    }
}
