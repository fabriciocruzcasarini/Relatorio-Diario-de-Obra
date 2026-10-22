package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.RdoWithDetails
import com.example.pdf.RdoPdfGenerator
import com.example.ui.components.CompanySettingsDialog
import com.example.ui.components.PdfExportDialog
import com.example.ui.screens.RdoFormScreen
import com.example.ui.screens.RdoListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RdoViewModel
import java.io.File

sealed interface Screen {
    object List : Screen
    object Form : Screen
}

class MainActivity : ComponentActivity() {

    private val viewModel: RdoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
                    var pdfDialogTarget by remember { mutableStateOf<Pair<File, String>?>(null) }
                    var showSettingsDialog by remember { mutableStateOf(false) }

                    val rdoList by viewModel.rdoList.collectAsState()
                    val companySettings by viewModel.companySettings.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.List -> {
                                RdoListScreen(
                                    rdoList = rdoList,
                                    companySettings = companySettings,
                                    onNewRdoClick = {
                                        viewModel.startNewRdo()
                                        currentScreen = Screen.Form
                                    },
                                    onEditRdoClick = { rdo ->
                                        viewModel.loadRdoForEdit(rdo)
                                        currentScreen = Screen.Form
                                    },
                                    onViewPdfClick = { rdo ->
                                        // Open existing or generate on demand
                                        val existing = rdo.report.pdfGeradoPath?.let { File(it) }?.takeIf { it.exists() }
                                        if (existing != null) {
                                            pdfDialogTarget = Pair(existing, rdo.report.obra)
                                        } else {
                                            val generated = RdoPdfGenerator.generatePdf(this@MainActivity, rdo, companySettings)
                                            viewModel.updateReport { it.copy(pdfGeradoPath = generated.absolutePath) }
                                            pdfDialogTarget = Pair(generated, rdo.report.obra)
                                        }
                                    },
                                    onDuplicateRdoClick = { rdo ->
                                        viewModel.duplicateRdo(rdo)
                                    },
                                    onDeleteRdoClick = { rdo ->
                                        viewModel.deleteRdo(rdo)
                                    },
                                    onOpenSettingsClick = {
                                        showSettingsDialog = true
                                    }
                                )
                            }
                            Screen.Form -> {
                                RdoFormScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.List },
                                    onPdfGenerated = { file ->
                                        pdfDialogTarget = Pair(file, viewModel.currentReport.value.obra)
                                    }
                                )
                            }
                        }

                        // PDF Export & WhatsApp Dialog
                        pdfDialogTarget?.let { (file, obraNome) ->
                            PdfExportDialog(
                                pdfFile = file,
                                obraNome = obraNome,
                                onDismiss = { pdfDialogTarget = null }
                            )
                        }

                        // Company Settings Dialog
                        if (showSettingsDialog) {
                            CompanySettingsDialog(
                                currentSettings = companySettings,
                                onDismiss = { showSettingsDialog = false },
                                onSave = { updated ->
                                    viewModel.saveCompanySettings(updated)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
