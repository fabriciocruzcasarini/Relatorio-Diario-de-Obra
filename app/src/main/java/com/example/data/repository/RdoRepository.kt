package com.example.data.repository

import com.example.data.local.RdoDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class RdoRepository(private val dao: RdoDao) {

    fun getAllReports(): Flow<List<RdoWithDetails>> = dao.getAllReports()

    fun getReportById(id: Long): Flow<RdoWithDetails?> = dao.getReportById(id)

    suspend fun saveRdo(
        report: RdoReport,
        production: List<RdoProductionItem>,
        resources: List<RdoResourceItem>,
        materials: List<RdoMaterialItem>
    ): Long = dao.saveFullRdo(report, production, resources, materials)

    suspend fun updateReport(report: RdoReport) = dao.updateReport(report)

    suspend fun deleteRdo(report: RdoReport) = dao.deleteFullRdo(report)

    fun getCompanySettings(): Flow<CompanySettings?> = dao.getCompanySettings()

    suspend fun saveCompanySettings(settings: CompanySettings) = dao.saveCompanySettings(settings)
}
