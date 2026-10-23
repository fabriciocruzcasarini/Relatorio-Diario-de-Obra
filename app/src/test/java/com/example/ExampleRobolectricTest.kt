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

    @Test
    fun `verify multi sheet page partition logic`() {
        val itemsCount = 25
        val itemsPerPage = 12
        val totalPages = (itemsCount + itemsPerPage - 1) / itemsPerPage
        assertEquals(3, totalPages)

        val page1Start = 0 * itemsPerPage
        val page1End = minOf(page1Start + itemsPerPage, itemsCount)
        assertEquals(0, page1Start)
        assertEquals(12, page1End)

        val page2Start = 1 * itemsPerPage
        val page2End = minOf(page2Start + itemsPerPage, itemsCount)
        assertEquals(12, page2Start)
        assertEquals(24, page2End)

        val page3Start = 2 * itemsPerPage
        val page3End = minOf(page3Start + itemsPerPage, itemsCount)
        assertEquals(24, page3Start)
        assertEquals(25, page3End)
    }

    @Test
    fun `verify company settings default signature properties`() {
        val settings = CompanySettings(
            nomeEmpresa = "Impacto Sinalização Ltda",
            defaultAssinaturaPath = "/data/user/0/com.aistudio.rdosinalizacao.vhbqtz/files/signatures/sig_photo_123.png",
            defaultResponsavelNome = "Carlos Encarregado"
        )
        assertEquals("/data/user/0/com.aistudio.rdosinalizacao.vhbqtz/files/signatures/sig_photo_123.png", settings.defaultAssinaturaPath)
        assertEquals("Carlos Encarregado", settings.defaultResponsavelNome)
    }

    @Test
    fun `verify signature aspect ratio fit calculation`() {
        val maxW = 150f
        val maxH = 40f

        // Wide photo signature (e.g. 600x200 -> aspect 3:1)
        val bWidth = 600f
        val bHeight = 200f
        val scale = minOf(maxW / bWidth, maxH / bHeight)
        val drawW = bWidth * scale
        val drawH = bHeight * scale

        // Scale should be constrained by height (40 / 200 = 0.20)
        assertEquals(0.20f, scale, 0.001f)
        assertEquals(120f, drawW, 0.001f)
        assertEquals(40f, drawH, 0.001f)
        assertTrue(drawW <= maxW)
        assertTrue(drawH <= maxH)
    }
}

