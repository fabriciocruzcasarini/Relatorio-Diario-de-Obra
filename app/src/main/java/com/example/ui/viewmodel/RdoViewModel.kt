package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.RdoRepository
import com.example.pdf.RdoPdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RdoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RdoRepository
    val rdoList: StateFlow<List<RdoWithDetails>>
    val companySettings: StateFlow<CompanySettings>

    // Form editing state
    private val _currentReport = MutableStateFlow(createEmptyReport())
    val currentReport: StateFlow<RdoReport> = _currentReport.asStateFlow()

    private val _productionItems = MutableStateFlow<List<RdoProductionItem>>(emptyList())
    val productionItems: StateFlow<List<RdoProductionItem>> = _productionItems.asStateFlow()

    private val _resourceItems = MutableStateFlow<List<RdoResourceItem>>(emptyList())
    val resourceItems: StateFlow<List<RdoResourceItem>> = _resourceItems.asStateFlow()

    private val _materialItems = MutableStateFlow<List<RdoMaterialItem>>(emptyList())
    val materialItems: StateFlow<List<RdoMaterialItem>> = _materialItems.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = RdoRepository(db.rdoDao())

        rdoList = repository.getAllReports()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        companySettings = repository.getCompanySettings()
            .map { it ?: CompanySettings() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanySettings())
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun startNewRdo() {
        val empty = createEmptyReport()
        val defaultSig = companySettings.value.defaultAssinaturaPath?.takeIf { File(it).exists() }
        val defaultName = companySettings.value.defaultResponsavelNome.takeIf { it.isNotBlank() }

        _currentReport.value = empty.copy(
            assinaturaBitmapPath = defaultSig,
            responsavelNome = defaultName ?: ""
        )
        _productionItems.value = emptyList()
        _resourceItems.value = emptyList()
        _materialItems.value = emptyList()
        _activeTab.value = 0
        _generatedPdfFile.value = null
    }

    fun loadRdoForEdit(rdoWithDetails: RdoWithDetails) {
        _currentReport.value = rdoWithDetails.report
        _productionItems.value = rdoWithDetails.productionItems
        _resourceItems.value = rdoWithDetails.resourceItems
        _materialItems.value = rdoWithDetails.materialItems
        _activeTab.value = 0
        _generatedPdfFile.value = rdoWithDetails.report.pdfGeradoPath?.let { File(it) }?.takeIf { it.exists() }
    }

    fun updateReport(update: (RdoReport) -> RdoReport) {
        _currentReport.value = update(_currentReport.value)
    }

    // Production items management
    fun addProductionItem() {
        val current = _productionItems.value.toMutableList()
        current.add(
            RdoProductionItem(
                servicoExecutado = "LBO",
                rodovia = _currentReport.value.obra.takeIf { it.isNotBlank() } ?: "Rodovia",
                kmInicial = "",
                kmFinal = "",
                quantFaixas = 1,
                comprimentoM = 0.0,
                larguraM = 0.15,
                totalM2 = 0.0,
                cor = "BRANCO",
                espessuraMm = "0.6"
            )
        )
        _productionItems.value = current
    }

    fun updateProductionItem(index: Int, item: RdoProductionItem) {
        val current = _productionItems.value.toMutableList()
        if (index in current.indices) {
            // Auto compute total M2 if needed
            val computedM2 = item.quantFaixas * item.comprimentoM * item.larguraM
            val finalItem = if (item.totalM2 == 0.0 && computedM2 > 0) {
                item.copy(totalM2 = (Math.round(computedM2 * 100.0) / 100.0))
            } else item

            current[index] = finalItem
            _productionItems.value = current
        }
    }

    fun removeProductionItem(index: Int) {
        val current = _productionItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _productionItems.value = current
        }
    }

    // Resource items management
    fun addResourceItem() {
        val current = _resourceItems.value.toMutableList()
        current.add(RdoResourceItem())
        _resourceItems.value = current
    }

    fun updateResourceItem(index: Int, item: RdoResourceItem) {
        val current = _resourceItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = item
            _resourceItems.value = current
        }
    }

    fun removeResourceItem(index: Int) {
        val current = _resourceItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _resourceItems.value = current
        }
    }

    // Material items management
    fun addMaterialItem() {
        val current = _materialItems.value.toMutableList()
        current.add(RdoMaterialItem())
        _materialItems.value = current
    }

    fun updateMaterialItem(index: Int, item: RdoMaterialItem) {
        val current = _materialItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = item
            _materialItems.value = current
        }
    }

    fun removeMaterialItem(index: Int) {
        val current = _materialItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _materialItems.value = current
        }
    }

    fun setSignaturePath(path: String?) {
        _currentReport.value = _currentReport.value.copy(assinaturaBitmapPath = path)
    }

    fun setDefaultSignature(path: String, name: String = "") {
        viewModelScope.launch {
            val current = companySettings.value
            val updated = current.copy(
                defaultAssinaturaPath = path,
                defaultResponsavelNome = if (name.isNotBlank()) name else current.defaultResponsavelNome
            )
            repository.saveCompanySettings(updated)
        }
    }

    fun saveReport(onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val reportId = repository.saveRdo(
                _currentReport.value,
                _productionItems.value,
                _resourceItems.value,
                _materialItems.value
            )
            _currentReport.value = _currentReport.value.copy(id = reportId)
            onSaved(reportId)
        }
    }

    fun generatePdf(onComplete: (File) -> Unit) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                // Ensure saved first
                val reportId = repository.saveRdo(
                    _currentReport.value,
                    _productionItems.value,
                    _resourceItems.value,
                    _materialItems.value
                )
                val updatedReport = _currentReport.value.copy(id = reportId)
                _currentReport.value = updatedReport

                val rdoDetails = RdoWithDetails(
                    report = updatedReport,
                    productionItems = _productionItems.value,
                    resourceItems = _resourceItems.value,
                    materialItems = _materialItems.value
                )

                val file = RdoPdfGenerator.generatePdf(
                    getApplication(),
                    rdoDetails,
                    companySettings.value
                )

                // Update report with generated pdf path
                repository.updateReport(updatedReport.copy(pdfGeradoPath = file.absolutePath))
                _currentReport.value = _currentReport.value.copy(pdfGeradoPath = file.absolutePath)
                _generatedPdfFile.value = file

                onComplete(file)
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun deleteRdo(rdoWithDetails: RdoWithDetails) {
        viewModelScope.launch {
            repository.deleteRdo(rdoWithDetails.report)
            // Delete associated PDF if exists
            rdoWithDetails.report.pdfGeradoPath?.let {
                val f = File(it)
                if (f.exists()) f.delete()
            }
        }
    }

    fun duplicateRdo(rdoWithDetails: RdoWithDetails) {
        viewModelScope.launch {
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val duplicatedReport = rdoWithDetails.report.copy(
                id = 0,
                dataCriacao = System.currentTimeMillis(),
                dataObra = today,
                assinaturaBitmapPath = null,
                pdfGeradoPath = null
            )
            repository.saveRdo(
                duplicatedReport,
                rdoWithDetails.productionItems.map { it.copy(id = 0) },
                rdoWithDetails.resourceItems.map { it.copy(id = 0) },
                rdoWithDetails.materialItems.map { it.copy(id = 0) }
            )
        }
    }

    fun saveCompanySettings(settings: CompanySettings) {
        viewModelScope.launch {
            repository.saveCompanySettings(settings)
        }
    }

    private fun createEmptyReport(): RdoReport {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        return RdoReport(
            dataObra = today,
            turnoDia = false,
            pavimentoSeco = false,
            contaminacoes = "",
            tempoAberto = false,
            choveuAposPintura = "",
            metodoAcrilicaSolvente = false,
            metodoMecanico = false,
            liberacaoStatus = "APROVADO"
        )
    }
}
