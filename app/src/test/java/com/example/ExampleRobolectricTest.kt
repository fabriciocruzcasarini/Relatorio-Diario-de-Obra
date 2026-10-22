package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Relatório Diário de Obra", appName)
    }

    @Test
    fun `verify brazilian decimal parsing with comma and dot`() {
        assertEquals(1.5, FormatUtils.parseBrazilianDecimal("1,5"), 0.001)
        assertEquals(0.15, FormatUtils.parseBrazilianDecimal("0,15"), 0.001)
        assertEquals(1250.5, FormatUtils.parseBrazilianDecimal("1250,5"), 0.001)
        assertEquals(1250.5, FormatUtils.parseBrazilianDecimal("1.250,50"), 0.001)
        assertEquals(300.0, FormatUtils.parseBrazilianDecimal("300"), 0.001)
        assertEquals(0.0, FormatUtils.parseBrazilianDecimal(""), 0.001)
    }

    @Test
    fun `verify production area calculation`() {
        val faixas = 2
        val comprimento = 1500.0
        val largura = 0.15
        val totalM2 = faixas * comprimento * largura
        assertEquals(450.0, totalM2, 0.001)
    }

    @Test
    fun `verify report data model defaults`() {
        val report = RdoReport(
            obra = "SP-330 Rodovia Anhanguera",
            cliente = "ARTESP / CCR AutoBAn",
            dataObra = "21/10/2026",
            fiscal = "Eng. Marcos Roberto"
        )
        assertEquals("SP-330 Rodovia Anhanguera", report.obra)
        assertEquals("ARTESP / CCR AutoBAn", report.cliente)
        assertTrue(report.turnoDia)
        assertEquals("APROVADO", report.liberacaoStatus)
    }

    @Test
    fun `verify production items summation`() {
        val items = listOf(
            RdoProductionItem(totalM2 = 525.0),
            RdoProductionItem(totalM2 = 174.9),
            RdoProductionItem(totalM2 = 150.0)
        )
        val grandTotal = items.sumOf { it.totalM2 }
        assertEquals(849.9, grandTotal, 0.01)
    }
}
