package com.example.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object RdoPdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 points (72 dpi)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 18f
    private const val MARGIN_RIGHT = 577f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT

    fun generatePdf(
        context: Context,
        reportWithDetails: RdoWithDetails,
        settings: CompanySettings?
    ): File {
        val document = PdfDocument()

        val linePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            isAntiAlias = true
        }

        val thinLinePaint = Paint().apply {
            color = Color.rgb(180, 180, 180)
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            isAntiAlias = true
        }

        val headerFillPaint = Paint().apply {
            color = Color.rgb(225, 230, 235)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val subheaderFillPaint = Paint().apply {
            color = Color.rgb(238, 241, 245)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textBold = Paint().apply {
            color = Color.BLACK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textRegular = Paint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textSmall = Paint().apply {
            color = Color.rgb(50, 50, 50)
            textSize = 6f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textCenterBold = Paint(textBold).apply { textAlign = Paint.Align.CENTER }

        val report = reportWithDetails.report
        val companyName = settings?.nomeEmpresa?.takeIf { it.isNotBlank() } ?: "Impacto Sinalização"
        val allProdItems = reportWithDetails.productionItems
        val grandTotalM2 = allProdItems.sumOf { it.totalM2 }

        // ======================== PAGE 1: MAIN RDO SHEET ========================
        val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(page1Info)
        val canvas1 = page1.canvas
        var currentY = 18f

        // Outer Border
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, PAGE_HEIGHT - 18f, linePaint)

        // 1. HEADER (Logo + Title)
        val headerH = 28f
        val logoWidth = 110f
        canvas1.drawRect(MARGIN_LEFT + logoWidth, currentY, MARGIN_RIGHT, currentY + headerH, headerFillPaint)
        canvas1.drawLine(MARGIN_LEFT + logoWidth, currentY, MARGIN_LEFT + logoWidth, currentY + headerH, linePaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + headerH, MARGIN_RIGHT, currentY + headerH, linePaint)

        val logoPaint = Paint().apply {
            color = Color.rgb(180, 83, 9)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            isAntiAlias = true
        }
        canvas1.drawText(companyName, MARGIN_LEFT + 6f, currentY + 16f, logoPaint)
        val logoSub = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 5.5f
            isAntiAlias = true
        }
        canvas1.drawText("Sinalização Viária", MARGIN_LEFT + 6f, currentY + 24f, logoSub)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val titleX = (MARGIN_LEFT + logoWidth + MARGIN_RIGHT) / 2f
        canvas1.drawText("DIÁRIO DE OBRA - SINALIZAÇÃO HORIZONTAL", titleX, currentY + 18f, titlePaint)
        currentY += headerH

        // 2. CLIENTE | OBRA | VEÍCULO KM
        val row1H = 15f
        canvas1.drawLine(MARGIN_LEFT, currentY + row1H, MARGIN_RIGHT, currentY + row1H, linePaint)
        canvas1.drawText("CLIENTE: ", MARGIN_LEFT + 4f, currentY + 11f, textBold)
        canvas1.drawText(report.cliente, MARGIN_LEFT + 42f, currentY + 11f, textRegular)

        val colObraX = MARGIN_LEFT + 220f
        canvas1.drawLine(colObraX, currentY, colObraX, currentY + row1H, linePaint)
        canvas1.drawText("OBRA: ", colObraX + 4f, currentY + 11f, textBold)
        canvas1.drawText(report.obra, colObraX + 32f, currentY + 11f, textRegular)

        val colKmX = MARGIN_LEFT + 410f
        canvas1.drawLine(colKmX, currentY, colKmX, currentY + row1H, linePaint)
        canvas1.drawText("VEÍCULO KM: ", colKmX + 4f, currentY + 11f, textBold)
        canvas1.drawText(report.veiculoKm, colKmX + 54f, currentY + 11f, textRegular)
        currentY += row1H

        // 3. RESP./FISCAL | TURNO | SAÍDA | CHEGADA
        val row2H = 15f
        canvas1.drawLine(MARGIN_LEFT, currentY + row2H, MARGIN_RIGHT, currentY + row2H, linePaint)
        canvas1.drawText("RESP./FISCAL: ", MARGIN_LEFT + 4f, currentY + 11f, textBold)
        canvas1.drawText(report.fiscal, MARGIN_LEFT + 60f, currentY + 11f, textRegular)

        val colTurnoX = MARGIN_LEFT + 230f
        canvas1.drawLine(colTurnoX, currentY, colTurnoX, currentY + row2H, linePaint)
        drawCheckbox(canvas1, colTurnoX + 6f, currentY + 4f, report.turnoDia, linePaint)
        canvas1.drawText("DIA", colTurnoX + 16f, currentY + 11f, textBold)
        drawCheckbox(canvas1, colTurnoX + 42f, currentY + 4f, report.turnoNoite, linePaint)
        canvas1.drawText("NOITE", colTurnoX + 52f, currentY + 11f, textBold)

        val colSaidaX = MARGIN_LEFT + 360f
        canvas1.drawLine(colSaidaX, currentY, colSaidaX, currentY + row2H, linePaint)
        canvas1.drawText("SAÍDA: ", colSaidaX + 4f, currentY + 11f, textBold)
        canvas1.drawText(report.horaSaida, colSaidaX + 32f, currentY + 11f, textRegular)

        val colChegadaX = MARGIN_LEFT + 445f
        canvas1.drawLine(colChegadaX, currentY, colChegadaX, currentY + row2H, linePaint)
        canvas1.drawText("CHEGADA: ", colChegadaX + 4f, currentY + 11f, textBold)
        canvas1.drawText(report.horaChegada, colChegadaX + 46f, currentY + 11f, textRegular)
        currentY += row2H

        // 4. CONTROLE DE FECHAMENTO
        val fechamentoHeaderH = 12f
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + fechamentoHeaderH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + fechamentoHeaderH, MARGIN_RIGHT, currentY + fechamentoHeaderH, linePaint)
        canvas1.drawText("CONTROLE DE FECHAMENTO", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += fechamentoHeaderH

        val fechRowH = 12f
        val colLocalW = 140f
        val colContatoW = 100f
        val colHoraW = 60f
        val colLiberadoW = 140f

        var fx = MARGIN_LEFT
        canvas1.drawText("LOCAL DE EXECUÇÃO:", fx + 3f, currentY + 9f, textSmall)
        fx += colLocalW
        canvas1.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
        canvas1.drawText("CONTATO:", fx + 3f, currentY + 9f, textSmall)
        fx += colContatoW
        canvas1.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
        canvas1.drawText("HORÁRIO:", fx + 3f, currentY + 9f, textSmall)
        fx += colHoraW
        canvas1.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
        canvas1.drawText("LIBERADO POR:", fx + 3f, currentY + 9f, textSmall)
        fx += colLiberadoW
        canvas1.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
        canvas1.drawText("HORÁRIO DE FECHAMENTO:", fx + 3f, currentY + 9f, textSmall)

        canvas1.drawLine(MARGIN_LEFT, currentY + fechRowH, MARGIN_RIGHT, currentY + fechRowH, thinLinePaint)
        currentY += fechRowH

        fx = MARGIN_LEFT
        canvas1.drawText(report.fechamentoLocal, fx + 3f, currentY + 9f, textRegular)
        fx += colLocalW
        canvas1.drawText(report.fechamentoContato, fx + 3f, currentY + 9f, textRegular)
        fx += colContatoW
        canvas1.drawText(report.fechamentoHorario, fx + 3f, currentY + 9f, textRegular)
        fx += colHoraW
        canvas1.drawText(report.fechamentoLiberadoPor, fx + 3f, currentY + 9f, textRegular)
        fx += colLiberadoW
        canvas1.drawText(report.fechamentoHorarioFim, fx + 3f, currentY + 9f, textRegular)

        canvas1.drawLine(MARGIN_LEFT, currentY + fechRowH, MARGIN_RIGHT, currentY + fechRowH, linePaint)
        currentY += fechRowH

        // 5. BANNER: PAVIMENTO
        val bannerH = 12f
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
        canvas1.drawText("PAVIMENTO", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += bannerH

        val pavRow1H = 14f
        canvas1.drawLine(MARGIN_LEFT, currentY + pavRow1H, MARGIN_RIGHT, currentY + pavRow1H, linePaint)
        canvas1.drawText("TIPO DE PAVIMENTO:", MARGIN_LEFT + 3f, currentY + 10f, textBold)
        canvas1.drawText(report.tipoPavimento, MARGIN_LEFT + 85f, currentY + 10f, textRegular)

        val colSecoX = MARGIN_LEFT + 180f
        drawCheckbox(canvas1, colSecoX, currentY + 3f, report.pavimentoSeco, linePaint)
        canvas1.drawText("SECO", colSecoX + 9f, currentY + 10f, textSmall)
        val colUmidoX = colSecoX + 38f
        drawCheckbox(canvas1, colUmidoX, currentY + 3f, report.pavimentoUmido, linePaint)
        canvas1.drawText("ÚMIDO", colUmidoX + 9f, currentY + 10f, textSmall)
        val colMolhX = colUmidoX + 42f
        drawCheckbox(canvas1, colMolhX, currentY + 3f, report.pavimentoMolhado, linePaint)
        canvas1.drawText("MOLHADO", colMolhX + 9f, currentY + 10f, textSmall)

        val colRepintX = MARGIN_LEFT + 315f
        canvas1.drawLine(colRepintX, currentY, colRepintX, currentY + pavRow1H, linePaint)
        drawCheckbox(canvas1, colRepintX + 4f, currentY + 3f, report.repinturaSobre.isNotBlank(), linePaint)
        canvas1.drawText("REPINTURA SOBRE: " + report.repinturaSobre, colRepintX + 14f, currentY + 10f, textSmall)

        val colAderX = MARGIN_LEFT + 425f
        canvas1.drawLine(colAderX, currentY, colAderX, currentY + pavRow1H, linePaint)
        canvas1.drawText("ADERÊNCIA PINTURA ANTIGA: " + report.aderenciaPinturaAntiga, colAderX + 3f, currentY + 10f, textSmall)
        currentY += pavRow1H

        val pavRow2H = 14f
        canvas1.drawLine(MARGIN_LEFT, currentY + pavRow2H, MARGIN_RIGHT, currentY + pavRow2H, linePaint)
        canvas1.drawText("DIAS SEM CHUVA: " + report.diasSemChuva, MARGIN_LEFT + 3f, currentY + 10f, textSmall)

        val colContamX = MARGIN_LEFT + 100f
        canvas1.drawLine(colContamX, currentY, colContamX, currentY + pavRow2H, linePaint)
        canvas1.drawText("CONTAMINAÇÕES:", colContamX + 3f, currentY + 10f, textSmall)
        drawCheckbox(canvas1, colContamX + 75f, currentY + 3f, report.contaminacoes == "BOM", linePaint)
        canvas1.drawText("BOM", colContamX + 85f, currentY + 10f, textSmall)
        drawCheckbox(canvas1, colContamX + 110f, currentY + 3f, report.contaminacoes == "REGULAR", linePaint)
        canvas1.drawText("REGULAR", colContamX + 120f, currentY + 10f, textSmall)
        drawCheckbox(canvas1, colContamX + 160f, currentY + 3f, report.contaminacoes == "RUIM", linePaint)
        canvas1.drawText("RUIM", colContamX + 170f, currentY + 10f, textSmall)

        val colPavNovoX = MARGIN_LEFT + 330f
        canvas1.drawLine(colPavNovoX, currentY, colPavNovoX, currentY + pavRow2H, linePaint)
        drawCheckbox(canvas1, colPavNovoX + 6f, currentY + 3f, report.pavimentoNovo, linePaint)
        canvas1.drawText("PAVIMENTO NOVO?", colPavNovoX + 16f, currentY + 10f, textSmall)
        currentY += pavRow2H

        // 6. BANNER: CONDIÇÕES DE APLICAÇÃO
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
        canvas1.drawText("CONDIÇÕES DE APLICAÇÃO", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += bannerH

        val climaRow1H = 14f
        canvas1.drawLine(MARGIN_LEFT, currentY + climaRow1H, MARGIN_RIGHT, currentY + climaRow1H, linePaint)
        canvas1.drawText("TEMPO:", MARGIN_LEFT + 3f, currentY + 10f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 35f, currentY + 3f, report.tempoAberto, linePaint)
        canvas1.drawText("ABERTO", MARGIN_LEFT + 44f, currentY + 10f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 78f, currentY + 3f, report.tempoNublado, linePaint)
        canvas1.drawText("NUBLADO", MARGIN_LEFT + 87f, currentY + 10f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 126f, currentY + 3f, report.tempoChuva, linePaint)
        canvas1.drawText("CHUVA", MARGIN_LEFT + 135f, currentY + 10f, textSmall)

        val colTempAmbX = MARGIN_LEFT + 172f
        canvas1.drawLine(colTempAmbX, currentY, colTempAmbX, currentY + climaRow1H, linePaint)
        canvas1.drawText("TEMP. AMBIENTE: " + report.tempAmbiente, colTempAmbX + 3f, currentY + 10f, textSmall)

        val colUmidX = MARGIN_LEFT + 265f
        canvas1.drawLine(colUmidX, currentY, colUmidX, currentY + climaRow1H, linePaint)
        canvas1.drawText("UMIDADE RELATIVA: " + report.umidadeRelativa + " (ATÉ 65%)", colUmidX + 3f, currentY + 10f, textSmall)

        val colOrvalX = MARGIN_LEFT + 425f
        canvas1.drawLine(colOrvalX, currentY, colOrvalX, currentY + climaRow1H, linePaint)
        canvas1.drawText("PONTO DE ORVALHO: " + report.pontoOrvalho, colOrvalX + 3f, currentY + 10f, textSmall)
        currentY += climaRow1H

        val climaRow2H = 14f
        canvas1.drawLine(MARGIN_LEFT, currentY + climaRow2H, MARGIN_RIGHT, currentY + climaRow2H, linePaint)
        canvas1.drawText("TEMP. PAVIMENTO: " + report.tempPavimento, MARGIN_LEFT + 3f, currentY + 10f, textSmall)

        val colLibTrafX = MARGIN_LEFT + 165f
        canvas1.drawLine(colLibTrafX, currentY, colLibTrafX, currentY + climaRow2H, linePaint)
        canvas1.drawText("LIB. TRÁFEGO: " + report.liberacaoTrafegoMin + " min.", colLibTrafX + 3f, currentY + 10f, textSmall)

        val colChovAposX = MARGIN_LEFT + 285f
        canvas1.drawLine(colChovAposX, currentY, colChovAposX, currentY + climaRow2H, linePaint)
        canvas1.drawText("CHOVEU APÓS PINTURA: " + report.choveuAposPintura, colChovAposX + 3f, currentY + 10f, textSmall)
        currentY += climaRow2H

        // 7. BANNER: RELATÓRIO DE PRODUÇÃO
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
        val prodBannerTitle = if (allProdItems.size > 7) "RELATÓRIO DE PRODUÇÃO (PARTE 1 - VIDE ANEXO)" else "RELATÓRIO DE PRODUÇÃO"
        canvas1.drawText(prodBannerTitle, (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += bannerH

        val metRowH = 12f
        drawCheckbox(canvas1, MARGIN_LEFT + 6f, currentY + 2f, report.metodoBaseAgua, linePaint)
        canvas1.drawText("BASE ÁGUA", MARGIN_LEFT + 16f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 75f, currentY + 2f, report.metodoAcrilicaSolvente, linePaint)
        canvas1.drawText("ACRÍLICA SOLVENTE", MARGIN_LEFT + 85f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 175f, currentY + 2f, report.metodoPlasticaAFrio, linePaint)
        canvas1.drawText("PLÁSTICA A FRIO", MARGIN_LEFT + 185f, currentY + 9f, textSmall)

        canvas1.drawText("TIPO:", MARGIN_LEFT + 265f, currentY + 9f, textBold)

        drawCheckbox(canvas1, MARGIN_LEFT + 315f, currentY + 2f, report.metodoLaminado, linePaint)
        canvas1.drawText("LAMINADO", MARGIN_LEFT + 325f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 375f, currentY + 2f, report.remocaoFresca || report.remocaoFogo, linePaint)
        canvas1.drawText("REMOÇÃO", MARGIN_LEFT + 385f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 425f, currentY + 2f, report.remocaoFresca, linePaint)
        canvas1.drawText("FRESCA", MARGIN_LEFT + 434f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 463f, currentY + 2f, report.remocaoFogo, linePaint)
        canvas1.drawText("FOGO", MARGIN_LEFT + 472f, currentY + 9f, textSmall)

        canvas1.drawLine(MARGIN_LEFT, currentY + metRowH, MARGIN_RIGHT, currentY + metRowH, thinLinePaint)
        currentY += metRowH

        drawCheckbox(canvas1, MARGIN_LEFT + 6f, currentY + 2f, report.metodoManual, linePaint)
        canvas1.drawText("MANUAL", MARGIN_LEFT + 16f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 75f, currentY + 2f, report.metodoMecanico, linePaint)
        canvas1.drawText("MECÂNICO", MARGIN_LEFT + 85f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 175f, currentY + 2f, report.metodoTermExtrudado, linePaint)
        canvas1.drawText("TERM EXTRUD.", MARGIN_LEFT + 185f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 250f, currentY + 2f, report.metodoHotSpray, linePaint)
        canvas1.drawText("HOT SPRAY", MARGIN_LEFT + 260f, currentY + 9f, textSmall)
        drawCheckbox(canvas1, MARGIN_LEFT + 315f, currentY + 2f, report.metodoTermRelevo, linePaint)
        canvas1.drawText("TERM RELEVO", MARGIN_LEFT + 325f, currentY + 9f, textSmall)

        canvas1.drawText("OUTROS: " + report.metodoOutros, MARGIN_LEFT + 400f, currentY + 9f, textSmall)
        canvas1.drawLine(MARGIN_LEFT, currentY + metRowH, MARGIN_RIGHT, currentY + metRowH, linePaint)
        currentY += metRowH

        // Production Table
        val prodHeaderH = 18f
        val colW = floatArrayOf(90f, 45f, 40f, 40f, 45f, 55f, 45f, 50f, 45f, 48f)
        val colTitles = arrayOf(
            "SERVIÇO EXECUTADO", "RODOVIA", "KM\nINICIAL", "KM\nFINAL",
            "QUANT. DE\nFAIXAS", "COMPRIMENTO\n(M)", "LARGURA\n(M)", "TOTAL\n(M2)",
            "COR", "ESPESSURA\n(MM)"
        )

        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + prodHeaderH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + prodHeaderH, MARGIN_RIGHT, currentY + prodHeaderH, linePaint)

        var cx = MARGIN_LEFT
        val colCenterSmall = Paint(textSmall).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 5.5f
        }

        for (i in colW.indices) {
            val lines = colTitles[i].split("\n")
            if (lines.size == 1) {
                canvas1.drawText(lines[0], cx + colW[i] / 2f, currentY + 11f, colCenterSmall)
            } else {
                canvas1.drawText(lines[0], cx + colW[i] / 2f, currentY + 7f, colCenterSmall)
                canvas1.drawText(lines[1], cx + colW[i] / 2f, currentY + 14f, colCenterSmall)
            }
            cx += colW[i]
            if (i < colW.size - 1) {
                canvas1.drawLine(cx, currentY, cx, currentY + prodHeaderH, linePaint)
            }
        }
        currentY += prodHeaderH

        // First 7 rows on Page 1
        val page1ItemsCount = 7
        val prodRowH = 13f

        for (r in 0 until page1ItemsCount) {
            val item = allProdItems.getOrNull(r)
            canvas1.drawLine(MARGIN_LEFT, currentY + prodRowH, MARGIN_RIGHT, currentY + prodRowH, thinLinePaint)

            var rowX = MARGIN_LEFT
            for (i in colW.indices) {
                if (item != null) {
                    val text = when (i) {
                        0 -> item.servicoExecutado
                        1 -> item.rodovia
                        2 -> item.kmInicial
                        3 -> item.kmFinal
                        4 -> item.quantFaixas.toString()
                        5 -> if (item.comprimentoM > 0) String.format(Locale.US, "%.1f", item.comprimentoM) else ""
                        6 -> if (item.larguraM > 0) String.format(Locale.US, "%.2f", item.larguraM) else ""
                        7 -> if (item.totalM2 > 0) String.format(Locale.US, "%.2f", item.totalM2) else ""
                        8 -> item.cor
                        9 -> item.espessuraMm
                        else -> ""
                    }
                    if (i == 0 || i == 1) {
                        canvas1.drawText(text, rowX + 2f, currentY + 9f, textSmall)
                    } else {
                        canvas1.drawText(text, rowX + colW[i] / 2f, currentY + 9f, colCenterSmall)
                    }
                }
                rowX += colW[i]
                if (i < colW.size - 1) {
                    canvas1.drawLine(rowX, currentY, rowX, currentY + prodRowH, thinLinePaint)
                }
            }
            currentY += prodRowH
        }

        // TOTAL EXECUTADO Row
        val totalRowH = 14f
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + totalRowH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + totalRowH, MARGIN_RIGHT, currentY + totalRowH, linePaint)

        val textTotalPaint = Paint(textBold).apply { textAlign = Paint.Align.RIGHT }
        val labelTotal = if (allProdItems.size > 7) "TOTAL GERAL CONSOLIDADO: " else "TOTAL EXECUTADO: "
        val totalStr = String.format(Locale.US, "%.2f M²", grandTotalM2)

        // 1. Desenha o valor numérico na extremidade direita
        canvas1.drawText(totalStr, MARGIN_RIGHT - 10f, currentY + 10f, textTotalPaint)

        // 2. Calcula a largura do valor numérico para posicionar o rótulo logo antes dele (com 15f de espaço)
        val totalStrWidth = textTotalPaint.measureText(totalStr)
        canvas1.drawText(labelTotal, MARGIN_RIGHT - 10f - totalStrWidth - 15f, currentY + 10f, textTotalPaint)

        currentY += totalRowH

        // 8. BANNER: RECURSOS
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
        canvas1.drawText("RECURSOS", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += bannerH

        val recColW = floatArrayOf(180f, 90f, 80f, CONTENT_WIDTH - 350f)
        val recColTitles = arrayOf("EQUIPE", "VEÍCULOS", "PLACA", "EQUIPAMENTOS/INSTRUMENTOS DE MEDIÇÃO")
        val recHeaderH = 12f
        canvas1.drawLine(MARGIN_LEFT, currentY + recHeaderH, MARGIN_RIGHT, currentY + recHeaderH, linePaint)

        cx = MARGIN_LEFT
        for (i in recColW.indices) {
            canvas1.drawText(recColTitles[i], cx + recColW[i] / 2f, currentY + 9f, colCenterSmall)
            cx += recColW[i]
            if (i < recColW.size - 1) {
                canvas1.drawLine(cx, currentY, cx, currentY + recHeaderH, linePaint)
            }
        }
        currentY += recHeaderH

        val recRowsCount = 3
        val recRowH = 12f
        val resourceItems = reportWithDetails.resourceItems
        for (r in 0 until recRowsCount) {
            val item = resourceItems.getOrNull(r)
            canvas1.drawLine(MARGIN_LEFT, currentY + recRowH, MARGIN_RIGHT, currentY + recRowH, thinLinePaint)
            var rx = MARGIN_LEFT
            for (i in recColW.indices) {
                if (item != null) {
                    val text = when (i) {
                        0 -> item.equipe
                        1 -> item.veiculos
                        2 -> item.placa
                        3 -> item.equipamentosMedicao
                        else -> ""
                    }
                    canvas1.drawText(text, rx + 4f, currentY + 9f, textSmall)
                }
                rx += recColW[i]
                if (i < recColW.size - 1) {
                    canvas1.drawLine(rx, currentY, rx, currentY + recRowH, thinLinePaint)
                }
            }
            currentY += recRowH
        }

        // 9. BANNER: MATERIAIS
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
        canvas1.drawText("MATERIAIS", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += bannerH

        val matColW = floatArrayOf(45f, 40f, 75f, 215f, 60f, 60f, 68f)
        val matColTitles = arrayOf("QUANT.", "UNID.", "MARCA", "DESCRIÇÃO", "FAB.", "VAL.", "LOTE")
        val matHeaderH = 12f
        canvas1.drawLine(MARGIN_LEFT, currentY + matHeaderH, MARGIN_RIGHT, currentY + matHeaderH, linePaint)

        cx = MARGIN_LEFT
        for (i in matColW.indices) {
            canvas1.drawText(matColTitles[i], cx + matColW[i] / 2f, currentY + 9f, colCenterSmall)
            cx += matColW[i]
            if (i < matColW.size - 1) {
                canvas1.drawLine(cx, currentY, cx, currentY + matHeaderH, linePaint)
            }
        }
        currentY += matHeaderH

        val matRowsCount = 3
        val matRowH = 12f
        val materialItems = reportWithDetails.materialItems
        for (r in 0 until matRowsCount) {
            val item = materialItems.getOrNull(r)
            canvas1.drawLine(MARGIN_LEFT, currentY + matRowH, MARGIN_RIGHT, currentY + matRowH, thinLinePaint)
            var mx = MARGIN_LEFT
            for (i in matColW.indices) {
                if (item != null) {
                    val text = when (i) {
                        0 -> item.quantidade
                        1 -> item.unidade
                        2 -> item.marca
                        3 -> item.descricao
                        4 -> item.fabricacao
                        5 -> item.validade
                        6 -> item.lote
                        else -> ""
                    }
                    if (i == 3) {
                        canvas1.drawText(text, mx + 4f, currentY + 9f, textSmall)
                    } else {
                        canvas1.drawText(text, mx + matColW[i] / 2f, currentY + 9f, colCenterSmall)
                    }
                }
                mx += matColW[i]
                if (i < matColW.size - 1) {
                    canvas1.drawLine(mx, currentY, mx, currentY + matRowH, thinLinePaint)
                }
            }
            currentY += matRowH
        }

        // 10. BANNER: OBSERVAÇÕES
        canvas1.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
        canvas1.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
        canvas1.drawText("OBSERVAÇÕES", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
        currentY += bannerH

        val obsBoxH = 34f
        canvas1.drawLine(MARGIN_LEFT, currentY + obsBoxH, MARGIN_RIGHT, currentY + obsBoxH, linePaint)
        if (report.observacoes.isNotBlank()) {
            drawMultilineText(canvas1, report.observacoes, MARGIN_LEFT + 6f, currentY + 11f, CONTENT_WIDTH - 12f, textSmall, 9f)
        }
        currentY += obsBoxH

        // 11. FOOTER: ROAD MARKINGS LEGEND + LIBERAÇÃO DA OBRA + ASSINATURA
        val footerH = PAGE_HEIGHT - 18f - currentY
        val roadLegendW = 280f

        canvas1.drawLine(MARGIN_LEFT + roadLegendW, currentY, MARGIN_LEFT + roadLegendW, currentY + footerH, linePaint)
        drawRoadMarkingLegend(canvas1, MARGIN_LEFT, currentY, roadLegendW, footerH, linePaint)

        val rightX = MARGIN_LEFT + roadLegendW
        val appRowH = 18f
        canvas1.drawLine(rightX, currentY + appRowH, MARGIN_RIGHT, currentY + appRowH, thinLinePaint)
        canvas1.drawText("LIBERAÇÃO DA OBRA:", rightX + 6f, currentY + 12f, textBold)

        val chkApp1X = rightX + 110f
        drawCheckbox(canvas1, chkApp1X, currentY + 5f, report.liberacaoStatus == "APROVADO", linePaint)
        canvas1.drawText("APROVADO", chkApp1X + 9f, currentY + 12f, textSmall)

        val chkApp2X = chkApp1X + 54f
        drawCheckbox(canvas1, chkApp2X, currentY + 5f, report.liberacaoStatus == "APROVADO COM RETRABALHO", linePaint)
        canvas1.drawText("APROV. RETRAB.", chkApp2X + 9f, currentY + 12f, textSmall)

        val chkApp3X = chkApp2X + 68f
        drawCheckbox(canvas1, chkApp3X, currentY + 5f, report.liberacaoStatus == "REPROVADO", linePaint)
        canvas1.drawText("REPROVADO", chkApp3X + 9f, currentY + 12f, textSmall)

        val sigAreaY = currentY + appRowH
        val sigAreaH = footerH - appRowH

        if (!report.assinaturaBitmapPath.isNullOrBlank()) {
            val sigFile = File(report.assinaturaBitmapPath)
            if (sigFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(sigFile.absolutePath)
                if (bitmap != null) {
                    val destRect = RectF(rightX + 20f, sigAreaY + 4f, rightX + 160f, sigAreaY + sigAreaH - 18f)
                    canvas1.drawBitmap(bitmap, null, destRect, null)
                }
            }
        }

        val sigLineY = sigAreaY + sigAreaH - 18f
        canvas1.drawLine(rightX + 16f, sigLineY, rightX + 180f, sigLineY, linePaint)
        canvas1.drawText("ASS. RESPONSÁVEL", rightX + 55f, sigLineY + 10f, textBold)
        if (report.responsavelNome.isNotBlank()) {
            canvas1.drawText(report.responsavelNome, rightX + 55f, sigLineY - 4f, textSmall)
        }

        val dateX = rightX + 195f
        canvas1.drawText("DATA: " + (report.dataObra.ifBlank { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }), dateX, sigLineY + 10f, textBold)

        document.finishPage(page1)

        // ======================== PAGE 2+: ANNEX IF MORE THAN 7 ITEMS ========================
        if (allProdItems.size > 7) {
            val remainingItems = allProdItems.drop(7)
            val itemsPerPage = 35
            val totalAnnexPages = (remainingItems.size + itemsPerPage - 1) / itemsPerPage

            for (pageIdx in 0 until totalAnnexPages) {
                val pageNumber = pageIdx + 2
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val annexPage = document.startPage(pageInfo)
                val canvasA = annexPage.canvas
                var ay = 18f

                // Border
                canvasA.drawRect(MARGIN_LEFT, ay, MARGIN_RIGHT, PAGE_HEIGHT - 18f, linePaint)

                // Header
                val aHeaderH = 26f
                canvasA.drawRect(MARGIN_LEFT, ay, MARGIN_RIGHT, ay + aHeaderH, headerFillPaint)
                canvasA.drawLine(MARGIN_LEFT, ay + aHeaderH, MARGIN_RIGHT, ay + aHeaderH, linePaint)

                canvasA.drawText(companyName, MARGIN_LEFT + 8f, ay + 16f, logoPaint)
                val annexTitle = "ANEXO DE CONTINUAÇÃO DE PRODUÇÃO (PÁGINA $pageNumber)"
                canvasA.drawText(annexTitle, (MARGIN_LEFT + MARGIN_RIGHT) / 2f + 40f, ay + 16f, titlePaint)
                ay += aHeaderH

                // Summary Row
                val aSubH = 14f
                canvasA.drawLine(MARGIN_LEFT, ay + aSubH, MARGIN_RIGHT, ay + aSubH, linePaint)
                canvasA.drawText("OBRA: ${report.obra}", MARGIN_LEFT + 6f, ay + 10f, textBold)
                canvasA.drawText("CLIENTE: ${report.cliente}", MARGIN_LEFT + 220f, ay + 10f, textBold)
                canvasA.drawText("DATA: ${report.dataObra}", MARGIN_LEFT + 420f, ay + 10f, textBold)
                ay += aSubH

                // Table Header
                canvasA.drawRect(MARGIN_LEFT, ay, MARGIN_RIGHT, ay + prodHeaderH, subheaderFillPaint)
                canvasA.drawLine(MARGIN_LEFT, ay + prodHeaderH, MARGIN_RIGHT, ay + prodHeaderH, linePaint)

                var ax = MARGIN_LEFT
                for (i in colW.indices) {
                    val lines = colTitles[i].split("\n")
                    if (lines.size == 1) {
                        canvasA.drawText(lines[0], ax + colW[i] / 2f, ay + 11f, colCenterSmall)
                    } else {
                        canvasA.drawText(lines[0], ax + colW[i] / 2f, ay + 7f, colCenterSmall)
                        canvasA.drawText(lines[1], ax + colW[i] / 2f, ay + 14f, colCenterSmall)
                    }
                    ax += colW[i]
                    if (i < colW.size - 1) {
                        canvasA.drawLine(ax, ay, ax, ay + prodHeaderH, linePaint)
                    }
                }
                ay += prodHeaderH

                // Draw chunk items
                val startIdx = pageIdx * itemsPerPage
                val endIdx = minOf(startIdx + itemsPerPage, remainingItems.size)
                val pageChunk = remainingItems.subList(startIdx, endIdx)

                for (idx in pageChunk.indices) {
                    val item = pageChunk[idx]
                    canvasA.drawLine(MARGIN_LEFT, ay + prodRowH, MARGIN_RIGHT, ay + prodRowH, thinLinePaint)

                    var rowX = MARGIN_LEFT
                    for (i in colW.indices) {
                        val text = when (i) {
                            0 -> item.servicoExecutado
                            1 -> item.rodovia
                            2 -> item.kmInicial
                            3 -> item.kmFinal
                            4 -> item.quantFaixas.toString()
                            5 -> if (item.comprimentoM > 0) String.format(Locale.US, "%.1f", item.comprimentoM) else ""
                            6 -> if (item.larguraM > 0) String.format(Locale.US, "%.2f", item.larguraM) else ""
                            7 -> if (item.totalM2 > 0) String.format(Locale.US, "%.2f", item.totalM2) else ""
                            8 -> item.cor
                            9 -> item.espessuraMm
                            else -> ""
                        }
                        if (i == 0 || i == 1) {
                            canvasA.drawText(text, rowX + 2f, ay + 9f, textSmall)
                        } else {
                            canvasA.drawText(text, rowX + colW[i] / 2f, ay + 9f, colCenterSmall)
                        }
                        rowX += colW[i]
                        if (i < colW.size - 1) {
                            canvasA.drawLine(rowX, ay, rowX, ay + prodRowH, thinLinePaint)
                        }
                    }
                    ay += prodRowH
                }

                // If last annex page, draw consolidated total
                if (pageIdx == totalAnnexPages - 1) {
                    canvasA.drawRect(MARGIN_LEFT, ay, MARGIN_RIGHT, ay + totalRowH, subheaderFillPaint)
                    canvasA.drawLine(MARGIN_LEFT, ay + totalRowH, MARGIN_RIGHT, ay + totalRowH, linePaint)
                    val labelConsolidado = "TOTAL GERAL CONSOLIDADO DE TODOS OS TRECHOS: "
                    canvasA.drawText(totalStr, MARGIN_RIGHT - 10f, ay + 10f, textTotalPaint)
                    val totalStrWidth = textTotalPaint.measureText(totalStr)
                    canvasA.drawText(labelConsolidado, MARGIN_RIGHT - 10f - totalStrWidth - 15f, ay + 10f, textTotalPaint)
                }

                document.finishPage(annexPage)
            }
        }

        // Output directory
        val outputDir = File(context.getExternalFilesDir(null), "rdo_relatorios")
        if (!outputDir.exists()) outputDir.mkdirs()

        val cleanObra = report.obra.replace(Regex("[^a-zA-Z0-9]"), "_").ifBlank { "obra" }
        val dateStr = report.dataObra.replace("/", "-").ifBlank { SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) }
        val file = File(outputDir, "RDO_${cleanObra}_${dateStr}_${report.id}.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawCheckbox(canvas: Canvas, x: Float, y: Float, checked: Boolean, paint: Paint) {
        val size = 7f
        val rect = RectF(x, y, x + size, y + size)
        val boxPaint = Paint(paint).apply { strokeWidth = 0.6f }
        canvas.drawRect(rect, boxPaint)
        if (checked) {
            val fillPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            canvas.drawRect(x + 1.5f, y + 1.5f, x + size - 1.5f, y + size - 1.5f, fillPaint)
        }
    }

    private fun drawRoadMarkingLegend(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        borderPaint: Paint
    ) {
        val asphaltPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, asphaltPaint)
        canvas.drawRect(x, y, x + width, y + height, borderPaint)

        val labels = arrayOf("LBO", "LMS-1", "LMS-2", "LFO-1", "LFO-2", "LFO-3", "LFO-4")
        val labelCount = labels.size
        val colWidth = width / labelCount

        val textPaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            textSize = 6f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val whiteLinePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        val yellowLinePaint = Paint().apply {
            color = Color.rgb(245, 158, 11)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        val dashedEffect = DashPathEffect(floatArrayOf(6f, 5f), 0f)
        val whiteDashedPaint = Paint(whiteLinePaint).apply { pathEffect = dashedEffect }
        val yellowDashedPaint = Paint(yellowLinePaint).apply { pathEffect = dashedEffect }

        val lineTopY = y + 15f
        val lineBottomY = y + height - 8f

        for (i in labels.indices) {
            val colCenterX = x + (i * colWidth) + (colWidth / 2f)
            canvas.drawText(labels[i], colCenterX, y + 10f, textPaint)

            when (i) {
                0 -> canvas.drawLine(colCenterX, lineTopY, colCenterX, lineBottomY, whiteLinePaint)
                1 -> canvas.drawLine(colCenterX, lineTopY, colCenterX, lineBottomY, whiteLinePaint)
                2 -> canvas.drawLine(colCenterX, lineTopY, colCenterX, lineBottomY, whiteDashedPaint)
                3 -> canvas.drawLine(colCenterX, lineTopY, colCenterX, lineBottomY, yellowLinePaint)
                4 -> canvas.drawLine(colCenterX, lineTopY, colCenterX, lineBottomY, yellowDashedPaint)
                5 -> {
                    val offset = 2f
                    canvas.drawLine(colCenterX - offset, lineTopY, colCenterX - offset, lineBottomY, yellowLinePaint)
                    canvas.drawLine(colCenterX + offset, lineTopY, colCenterX + offset, lineBottomY, yellowLinePaint)
                }
                6 -> {
                    val offset = 2f
                    canvas.drawLine(colCenterX - offset, lineTopY, colCenterX - offset, lineBottomY, yellowLinePaint)
                    canvas.drawLine(colCenterX + offset, lineTopY, colCenterX + offset, lineBottomY, yellowDashedPaint)
                }
            }
        }
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float
    ) {
        val words = text.split(" ")
        var line = StringBuilder()
        var y = startY

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                y += lineSpacing
                line = StringBuilder(word)
            } else {
                line = StringBuilder(testLine)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), x, y, paint)
        }
    }
}
