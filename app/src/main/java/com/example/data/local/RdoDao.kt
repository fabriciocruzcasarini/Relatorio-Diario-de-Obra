package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RdoDao {

    @Transaction
    @Query("SELECT * FROM rdo_reports ORDER BY id DESC")
    fun getAllReports(): Flow<List<RdoWithDetails>>

    @Transaction
    @Query("SELECT * FROM rdo_reports WHERE id = :id")
    fun getReportById(id: Long): Flow<RdoWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: RdoReport): Long

    @Update
    suspend fun updateReport(report: RdoReport)

    @Delete
    suspend fun deleteReport(report: RdoReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionItems(items: List<RdoProductionItem>)

    @Query("DELETE FROM rdo_production_items WHERE reportId = :reportId")
    suspend fun deleteProductionItems(reportId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResourceItems(items: List<RdoResourceItem>)

    @Query("DELETE FROM rdo_resource_items WHERE reportId = :reportId")
    suspend fun deleteResourceItems(reportId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialItems(items: List<RdoMaterialItem>)

    @Query("DELETE FROM rdo_material_items WHERE reportId = :reportId")
    suspend fun deleteMaterialItems(reportId: Long)

    @Query("SELECT * FROM company_settings WHERE id = 1")
    fun getCompanySettings(): Flow<CompanySettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCompanySettings(settings: CompanySettings)

    @Transaction
    suspend fun saveFullRdo(
        report: RdoReport,
        production: List<RdoProductionItem>,
        resources: List<RdoResourceItem>,
        materials: List<RdoMaterialItem>
    ): Long {
        val reportId = if (report.id == 0L) {
            insertReport(report)
        } else {
            updateReport(report)
            report.id
        }

        deleteProductionItems(reportId)
        val prodWithId = production.map { it.copy(reportId = reportId) }
        insertProductionItems(prodWithId)

        deleteResourceItems(reportId)
        val resWithId = resources.map { it.copy(reportId = reportId) }
        insertResourceItems(resWithId)

        deleteMaterialItems(reportId)
        val matWithId = materials.map { it.copy(reportId = reportId) }
        insertMaterialItems(matWithId)

        return reportId
    }

    @Transaction
    suspend fun deleteFullRdo(report: RdoReport) {
        deleteProductionItems(report.id)
        deleteResourceItems(report.id)
        deleteMaterialItems(report.id)
        deleteReport(report)
    }
}
