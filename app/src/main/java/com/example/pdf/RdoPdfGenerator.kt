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
    private const val ITEMS_PER_PAGE = 12

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

        val totalPages = if (allProdItems.isEmpty()) 1 else (allProdItems.size + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE

        for (pageIdx in 0 until totalPages) {
            val pageNumber = pageIdx + 1
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            var currentY = 18f

            // Outer Border
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, PAGE_HEIGHT - 18f, linePaint)

            // 1. HEADER (Logo + Title)
            val headerH = 28f
            val logoWidth = 110f
            canvas.drawRect(MARGIN_LEFT + logoWidth, currentY, MARGIN_RIGHT, currentY + headerH, headerFillPaint)
            canvas.drawLine(MARGIN_LEFT + logoWidth, currentY, MARGIN_LEFT + logoWidth, currentY + headerH, linePaint)
            canvas.drawLine(MARGIN_LEFT, currentY + headerH, MARGIN_RIGHT, currentY + headerH, linePaint)

            val logoPaint = Paint().apply {
                color = Color.rgb(180, 83, 9)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                isAntiAlias = true
            }
            canvas.drawText(companyName, MARGIN_LEFT + 6f, currentY + 16f, logoPaint)
            val logoSub = Paint().apply {
                color = Color.rgb(80, 80, 80)
                textSize = 5.5f
                isAntiAlias = true
            }
            canvas.drawText("Sinalização Viária", MARGIN_LEFT + 6f, currentY + 24f, logoSub)

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = if (totalPages > 1) 9.5f else 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val titleX = (MARGIN_LEFT + logoWidth + MARGIN_RIGHT) / 2f
            val titleText = if (totalPages > 1) {
                "DIÁRIO DE OBRA - SINALIZAÇÃO HORIZONTAL (FOLHA $pageNumber DE $totalPages)"
            } else {
                "DIÁRIO DE OBRA - SINALIZAÇÃO HORIZONTAL"
            }
            canvas.drawText(titleText, titleX, currentY + 18f, titlePaint)
            currentY += headerH

            // 2. CLIENTE | OBRA | VEÍCULO KM
            val row1H = 15f
            canvas.drawLine(MARGIN_LEFT, currentY + row1H, MARGIN_RIGHT, currentY + row1H, linePaint)
            canvas.drawText("CLIENTE: ", MARGIN_LEFT + 4f, currentY + 11f, textBold)
            canvas.drawText(report.cliente, MARGIN_LEFT + 42f, currentY + 11f, textRegular)

            val colObraX = MARGIN_LEFT + 220f
            canvas.drawLine(colObraX, currentY, colObraX, currentY + row1H, linePaint)
            canvas.drawText("OBRA: ", colObraX + 4f, currentY + 11f, textBold)
            canvas.drawText(report.obra, colObraX + 32f, currentY + 11f, textRegular)

            val colKmX = MARGIN_LEFT + 410f
            canvas.drawLine(colKmX, currentY, colKmX, currentY + row1H, linePaint)
            canvas.drawText("VEÍCULO KM: ", colKmX + 4f, currentY + 11f, textBold)
            canvas.drawText(report.veiculoKm, colKmX + 54f, currentY + 11f, textRegular)
            currentY += row1H

            // 3. RESP./FISCAL | TURNO | SAÍDA | CHEGADA
            val row2H = 15f
            canvas.drawLine(MARGIN_LEFT, currentY + row2H, MARGIN_RIGHT, currentY + row2H, linePaint)
            canvas.drawText("RESP./FISCAL: ", MARGIN_LEFT + 4f, currentY + 11f, textBold)
            canvas.drawText(report.fiscal, MARGIN_LEFT + 60f, currentY + 11f, textRegular)

            val colTurnoX = MARGIN_LEFT + 230f
            canvas.drawLine(colTurnoX, currentY, colTurnoX, currentY + row2H, linePaint)
            drawCheckbox(canvas, colTurnoX + 6f, currentY + 4f, report.turnoDia, linePaint)
            canvas.drawText("DIA", colTurnoX + 16f, currentY + 11f, textBold)
            drawCheckbox(canvas, colTurnoX + 42f, currentY + 4f, report.turnoNoite, linePaint)
            canvas.drawText("NOITE", colTurnoX + 52f, currentY + 11f, textBold)

            val colSaidaX = MARGIN_LEFT + 360f
            canvas.drawLine(colSaidaX, currentY, colSaidaX, currentY + row2H, linePaint)
            canvas.drawText("SAÍDA: ", colSaidaX + 4f, currentY + 11f, textBold)
            canvas.drawText(report.horaSaida, colSaidaX + 32f, currentY + 11f, textRegular)

            val colChegadaX = MARGIN_LEFT + 445f
            canvas.drawLine(colChegadaX, currentY, colChegadaX, currentY + row2H, linePaint)
            canvas.drawText("CHEGADA: ", colChegadaX + 4f, currentY + 11f, textBold)
            canvas.drawText(report.horaChegada, colChegadaX + 46f, currentY + 11f, textRegular)
            currentY += row2H

            // 4. CONTROLE DE FECHAMENTO
            val fechamentoHeaderH = 12f
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + fechamentoHeaderH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + fechamentoHeaderH, MARGIN_RIGHT, currentY + fechamentoHeaderH, linePaint)
            canvas.drawText("CONTROLE DE FECHAMENTO", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += fechamentoHeaderH

            val fechRowH = 12f
            val colLocalW = 140f
            val colContatoW = 100f
            val colHoraW = 60f
            val colLiberadoW = 140f

            var fx = MARGIN_LEFT
            canvas.drawText("LOCAL DE EXECUÇÃO:", fx + 3f, currentY + 9f, textSmall)
            fx += colLocalW
            canvas.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
            canvas.drawText("CONTATO:", fx + 3f, currentY + 9f, textSmall)
            fx += colContatoW
            canvas.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
            canvas.drawText("HORÁRIO:", fx + 3f, currentY + 9f, textSmall)
            fx += colHoraW
            canvas.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
            canvas.drawText("LIBERADO POR:", fx + 3f, currentY + 9f, textSmall)
            fx += colLiberadoW
            canvas.drawLine(fx, currentY, fx, currentY + fechRowH * 2, linePaint)
            canvas.drawText("HORÁRIO DE FECHAMENTO:", fx + 3f, currentY + 9f, textSmall)

            canvas.drawLine(MARGIN_LEFT, currentY + fechRowH, MARGIN_RIGHT, currentY + fechRowH, thinLinePaint)
            currentY += fechRowH

            fx = MARGIN_LEFT
            canvas.drawText(report.fechamentoLocal, fx + 3f, currentY + 9f, textRegular)
            fx += colLocalW
            canvas.drawText(report.fechamentoContato, fx + 3f, currentY + 9f, textRegular)
            fx += colContatoW
            canvas.drawText(report.fechamentoHorario, fx + 3f, currentY + 9f, textRegular)
            fx += colHoraW
            canvas.drawText(report.fechamentoLiberadoPor, fx + 3f, currentY + 9f, textRegular)
            fx += colLiberadoW
            canvas.drawText(report.fechamentoHorarioFim, fx + 3f, currentY + 9f, textRegular)

            canvas.drawLine(MARGIN_LEFT, currentY + fechRowH, MARGIN_RIGHT, currentY + fechRowH, linePaint)
            currentY += fechRowH

            // 5. BANNER: PAVIMENTO
            val bannerH = 12f
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
            canvas.drawText("PAVIMENTO", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += bannerH

            val pavRow1H = 14f
            canvas.drawLine(MARGIN_LEFT, currentY + pavRow1H, MARGIN_RIGHT, currentY + pavRow1H, linePaint)
            canvas.drawText("TIPO DE PAVIMENTO:", MARGIN_LEFT + 3f, currentY + 10f, textBold)
            canvas.drawText(report.tipoPavimento, MARGIN_LEFT + 85f, currentY + 10f, textRegular)

            val colSecoX = MARGIN_LEFT + 180f
            drawCheckbox(canvas, colSecoX, currentY + 3f, report.pavimentoSeco, linePaint)
            canvas.drawText("SECO", colSecoX + 9f, currentY + 10f, textSmall)
            val colUmidoX = colSecoX + 38f
            drawCheckbox(canvas, colUmidoX, currentY + 3f, report.pavimentoUmido, linePaint)
            canvas.drawText("ÚMIDO", colUmidoX + 9f, currentY + 10f, textSmall)
            val colMolhX = colUmidoX + 42f
            drawCheckbox(canvas, colMolhX, currentY + 3f, report.pavimentoMolhado, linePaint)
            canvas.drawText("MOLHADO", colMolhX + 9f, currentY + 10f, textSmall)

            val colRepintX = MARGIN_LEFT + 315f
            canvas.drawLine(colRepintX, currentY, colRepintX, currentY + pavRow1H, linePaint)
            drawCheckbox(canvas, colRepintX + 4f, currentY + 3f, report.repinturaSobre.isNotBlank(), linePaint)
            canvas.drawText("REPINTURA SOBRE: " + report.repinturaSobre, colRepintX + 14f, currentY + 10f, textSmall)

            val colAderX = MARGIN_LEFT + 425f
            canvas.drawLine(colAderX, currentY, colAderX, currentY + pavRow1H, linePaint)
            canvas.drawText("ADERÊNCIA PINTURA ANTIGA: " + report.aderenciaPinturaAntiga, colAderX + 3f, currentY + 10f, textSmall)
            currentY += pavRow1H

            val pavRow2H = 14f
            canvas.drawLine(MARGIN_LEFT, currentY + pavRow2H, MARGIN_RIGHT, currentY + pavRow2H, linePaint)
            canvas.drawText("DIAS SEM CHUVA: " + report.diasSemChuva, MARGIN_LEFT + 3f, currentY + 10f, textSmall)

            val colContamX = MARGIN_LEFT + 100f
            canvas.drawLine(colContamX, currentY, colContamX, currentY + pavRow2H, linePaint)
            canvas.drawText("CONTAMINAÇÕES:", colContamX + 3f, currentY + 10f, textSmall)
            drawCheckbox(canvas, colContamX + 75f, currentY + 3f, report.contaminacoes == "BOM", linePaint)
            canvas.drawText("BOM", colContamX + 85f, currentY + 10f, textSmall)
            drawCheckbox(canvas, colContamX + 110f, currentY + 3f, report.contaminacoes == "REGULAR", linePaint)
            canvas.drawText("REGULAR", colContamX + 120f, currentY + 10f, textSmall)
            drawCheckbox(canvas, colContamX + 160f, currentY + 3f, report.contaminacoes == "RUIM", linePaint)
            canvas.drawText("RUIM", colContamX + 170f, currentY + 10f, textSmall)

            val colPavNovoX = MARGIN_LEFT + 330f
            canvas.drawLine(colPavNovoX, currentY, colPavNovoX, currentY + pavRow2H, linePaint)
            drawCheckbox(canvas, colPavNovoX + 6f, currentY + 3f, report.pavimentoNovo, linePaint)
            canvas.drawText("PAVIMENTO NOVO?", colPavNovoX + 16f, currentY + 10f, textSmall)
            currentY += pavRow2H

            // 6. BANNER: CONDIÇÕES DE APLICAÇÃO
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
            canvas.drawText("CONDIÇÕES DE APLICAÇÃO", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += bannerH

            val climaRow1H = 14f
            canvas.drawLine(MARGIN_LEFT, currentY + climaRow1H, MARGIN_RIGHT, currentY + climaRow1H, linePaint)
            canvas.drawText("TEMPO:", MARGIN_LEFT + 3f, currentY + 10f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 35f, currentY + 3f, report.tempoAberto, linePaint)
            canvas.drawText("ABERTO", MARGIN_LEFT + 44f, currentY + 10f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 78f, currentY + 3f, report.tempoNublado, linePaint)
            canvas.drawText("NUBLADO", MARGIN_LEFT + 87f, currentY + 10f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 126f, currentY + 3f, report.tempoChuva, linePaint)
            canvas.drawText("CHUVA", MARGIN_LEFT + 135f, currentY + 10f, textSmall)

            val colTempAmbX = MARGIN_LEFT + 172f
            canvas.drawLine(colTempAmbX, currentY, colTempAmbX, currentY + climaRow1H, linePaint)
            canvas.drawText("TEMP. AMBIENTE: " + report.tempAmbiente, colTempAmbX + 3f, currentY + 10f, textSmall)

            val colUmidX = MARGIN_LEFT + 265f
            canvas.drawLine(colUmidX, currentY, colUmidX, currentY + climaRow1H, linePaint)
            canvas.drawText("UMIDADE RELATIVA: " + report.umidadeRelativa + " (ATÉ 65%)", colUmidX + 3f, currentY + 10f, textSmall)

            val colOrvalX = MARGIN_LEFT + 425f
            canvas.drawLine(colOrvalX, currentY, colOrvalX, currentY + climaRow1H, linePaint)
            canvas.drawText("PONTO DE ORVALHO: " + report.pontoOrvalho, colOrvalX + 3f, currentY + 10f, textSmall)
            currentY += climaRow1H

            val climaRow2H = 14f
            canvas.drawLine(MARGIN_LEFT, currentY + climaRow2H, MARGIN_RIGHT, currentY + climaRow2H, linePaint)
            canvas.drawText("TEMP. PAVIMENTO: " + report.tempPavimento, MARGIN_LEFT + 3f, currentY + 10f, textSmall)

            val colLibTrafX = MARGIN_LEFT + 165f
            canvas.drawLine(colLibTrafX, currentY, colLibTrafX, currentY + climaRow2H, linePaint)
            canvas.drawText("LIB. TRÁFEGO: " + report.liberacaoTrafegoMin + " min.", colLibTrafX + 3f, currentY + 10f, textSmall)

            val colChovAposX = MARGIN_LEFT + 285f
            canvas.drawLine(colChovAposX, currentY, colChovAposX, currentY + climaRow2H, linePaint)
            canvas.drawText("CHOVEU APÓS PINTURA: " + report.choveuAposPintura, colChovAposX + 3f, currentY + 10f, textSmall)
            currentY += climaRow2H

            // 7. BANNER: RELATÓRIO DE PRODUÇÃO
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
            val prodBannerTitle = if (totalPages > 1) "RELATÓRIO DE PRODUÇÃO (FOLHA $pageNumber DE $totalPages)" else "RELATÓRIO DE PRODUÇÃO"
            canvas.drawText(prodBannerTitle, (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += bannerH

            val metRowH = 12f
            drawCheckbox(canvas, MARGIN_LEFT + 6f, currentY + 2f, report.metodoBaseAgua, linePaint)
            canvas.drawText("BASE ÁGUA", MARGIN_LEFT + 16f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 75f, currentY + 2f, report.metodoAcrilicaSolvente, linePaint)
            canvas.drawText("ACRÍLICA SOLVENTE", MARGIN_LEFT + 85f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 175f, currentY + 2f, report.metodoPlasticaAFrio, linePaint)
            canvas.drawText("PLÁSTICA A FRIO", MARGIN_LEFT + 185f, currentY + 9f, textSmall)

            canvas.drawText("TIPO:", MARGIN_LEFT + 265f, currentY + 9f, textBold)

            // Posição ajustada das opções
            drawCheckbox(canvas, MARGIN_LEFT + 315f, currentY + 2f, report.metodoLaminado, linePaint)
            canvas.drawText("LAMINADO", MARGIN_LEFT + 325f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 375f, currentY + 2f, report.remocaoFresca || report.remocaoFogo, linePaint)
            canvas.drawText("REMOÇÃO", MARGIN_LEFT + 385f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 425f, currentY + 2f, report.remocaoFresca, linePaint)
            canvas.drawText("FRESCA", MARGIN_LEFT + 434f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 463f, currentY + 2f, report.remocaoFogo, linePaint)
            canvas.drawText("FOGO", MARGIN_LEFT + 472f, currentY + 9f, textSmall)

            canvas.drawLine(MARGIN_LEFT, currentY + metRowH, MARGIN_RIGHT, currentY + metRowH, thinLinePaint)
            currentY += metRowH

            drawCheckbox(canvas, MARGIN_LEFT + 6f, currentY + 2f, report.metodoManual, linePaint)
            canvas.drawText("MANUAL", MARGIN_LEFT + 16f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 75f, currentY + 2f, report.metodoMecanico, linePaint)
            canvas.drawText("MECÂNICO", MARGIN_LEFT + 85f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 175f, currentY + 2f, report.metodoTermExtrudado, linePaint)
            canvas.drawText("TERM EXTRUD.", MARGIN_LEFT + 185f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 250f, currentY + 2f, report.metodoHotSpray, linePaint)
            canvas.drawText("HOT SPRAY", MARGIN_LEFT + 260f, currentY + 9f, textSmall)
            drawCheckbox(canvas, MARGIN_LEFT + 315f, currentY + 2f, report.metodoTermRelevo, linePaint)
            canvas.drawText("TERM RELEVO", MARGIN_LEFT + 325f, currentY + 9f, textSmall)

            canvas.drawText("OUTROS: " + report.metodoOutros, MARGIN_LEFT + 400f, currentY + 9f, textSmall)
            canvas.drawLine(MARGIN_LEFT, currentY + metRowH, MARGIN_RIGHT, currentY + metRowH, linePaint)
            currentY += metRowH

            // Production Table Header
            val prodHeaderH = 18f
            val colW = floatArrayOf(90f, 45f, 40f, 40f, 45f, 55f, 45f, 50f, 45f, 48f)
            val colTitles = arrayOf(
                "SERVIÇO EXECUTADO", "RODOVIA", "KM\nINICIAL", "KM\nFINAL",
                "QUANT. DE\nFAIXAS", "COMPRIMENTO\n(M)", "LARGURA\n(M)", "TOTAL\n(M2)",
                "COR", "ESPESSURA\n(MM)"
            )

            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + prodHeaderH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + prodHeaderH, MARGIN_RIGHT, currentY + prodHeaderH, linePaint)

            var cx = MARGIN_LEFT
            val colCenterSmall = Paint(textSmall).apply {
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 5.5f
            }

            for (i in colW.indices) {
                val lines = colTitles[i].split("\n")
                if (lines.size == 1) {
                    canvas.drawText(lines[0], cx + colW[i] / 2f, currentY + 11f, colCenterSmall)
                } else {
                    canvas.drawText(lines[0], cx + colW[i] / 2f, currentY + 7f, colCenterSmall)
                    canvas.drawText(lines[1], cx + colW[i] / 2f, currentY + 14f, colCenterSmall)
                }
                cx += colW[i]
                if (i < colW.size - 1) {
                    canvas.drawLine(cx, currentY, cx, currentY + prodHeaderH, linePaint)
                }
            }
            currentY += prodHeaderH

            // Current Page Chunk of Production Items
            val startIdx = pageIdx * ITEMS_PER_PAGE
            val endIdx = minOf(startIdx + ITEMS_PER_PAGE, allProdItems.size)
            val pageProdItems = if (startIdx < allProdItems.size) allProdItems.subList(startIdx, endIdx) else emptyList()
            val pageSubtotalM2 = pageProdItems.sumOf { it.totalM2 }

            val prodRowH = 13f
            for (r in 0 until ITEMS_PER_PAGE) {
                val item = pageProdItems.getOrNull(r)
                canvas.drawLine(MARGIN_LEFT, currentY + prodRowH, MARGIN_RIGHT, currentY + prodRowH, thinLinePaint)

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
                            canvas.drawText(text, rowX + 2f, currentY + 9f, textSmall)
                        } else {
                            canvas.drawText(text, rowX + colW[i] / 2f, currentY + 9f, colCenterSmall)
                        }
                    }
                    rowX += colW[i]
                    if (i < colW.size - 1) {
                        canvas.drawLine(rowX, currentY, rowX, currentY + prodRowH, thinLinePaint)
                    }
                }
                currentY += prodRowH
            }

            // TOTAL EXECUTADO Row
            val totalRowH = 14f
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + totalRowH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + totalRowH, MARGIN_RIGHT, currentY + totalRowH, linePaint)

            val textTotalPaint = Paint(textBold).apply { textAlign = Paint.Align.RIGHT }

            if (totalPages > 1) {
                // Subtotal da Página à esquerda
                val subtotalStr = String.format(Locale.US, "%.2f M²", pageSubtotalM2)
                canvas.drawText("SUBTOTAL PÁG: $subtotalStr", MARGIN_LEFT + 150f, currentY + 10f, textBold)

                // Total Geral Consolidado à direita
                val totalStr = String.format(Locale.US, "%.2f M²", grandTotalM2)
                val labelTotal = "TOTAL GERAL: "
                canvas.drawText(totalStr, MARGIN_RIGHT - 10f, currentY + 10f, textTotalPaint)
                val totalStrWidth = textTotalPaint.measureText(totalStr)
                canvas.drawText(labelTotal, MARGIN_RIGHT - 10f - totalStrWidth - 10f, currentY + 10f, textTotalPaint)
            } else {
                val totalStr = String.format(Locale.US, "%.2f M²", grandTotalM2)
                val labelTotal = "TOTAL EXECUTADO: "
                canvas.drawText(totalStr, MARGIN_RIGHT - 10f, currentY + 10f, textTotalPaint)
                val totalStrWidth = textTotalPaint.measureText(totalStr)
                canvas.drawText(labelTotal, MARGIN_RIGHT - 10f - totalStrWidth - 15f, currentY + 10f, textTotalPaint)
            }
            currentY += totalRowH

            // 8. BANNER: RECURSOS
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
            canvas.drawText("RECURSOS", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += bannerH

            val recColW = floatArrayOf(180f, 90f, 80f, CONTENT_WIDTH - 350f)
            val recColTitles = arrayOf("EQUIPE", "VEÍCULOS", "PLACA", "EQUIPAMENTOS/INSTRUMENTOS DE MEDIÇÃO")
            val recHeaderH = 12f
            canvas.drawLine(MARGIN_LEFT, currentY + recHeaderH, MARGIN_RIGHT, currentY + recHeaderH, linePaint)

            cx = MARGIN_LEFT
            for (i in recColW.indices) {
                canvas.drawText(recColTitles[i], cx + recColW[i] / 2f, currentY + 9f, colCenterSmall)
                cx += recColW[i]
                if (i < recColW.size - 1) {
                    canvas.drawLine(cx, currentY, cx, currentY + recHeaderH, linePaint)
                }
            }
            currentY += recHeaderH

            val recRowsCount = 3
            val recRowH = 12f
            val resourceItems = reportWithDetails.resourceItems
            for (r in 0 until recRowsCount) {
                val item = resourceItems.getOrNull(r)
                canvas.drawLine(MARGIN_LEFT, currentY + recRowH, MARGIN_RIGHT, currentY + recRowH, thinLinePaint)
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
                        canvas.drawText(text, rx + 4f, currentY + 9f, textSmall)
                    }
                    rx += recColW[i]
                    if (i < recColW.size - 1) {
                        canvas.drawLine(rx, currentY, rx, currentY + recRowH, thinLinePaint)
                    }
                }
                currentY += recRowH
            }

            // 9. BANNER: MATERIAIS
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
            canvas.drawText("MATERIAIS", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += bannerH

            val matColW = floatArrayOf(45f, 40f, 75f, 215f, 60f, 60f, 68f)
            val matColTitles = arrayOf("QUANT.", "UNID.", "MARCA", "DESCRIÇÃO", "FAB.", "VAL.", "LOTE")
            val matHeaderH = 12f
            canvas.drawLine(MARGIN_LEFT, currentY + matHeaderH, MARGIN_RIGHT, currentY + matHeaderH, linePaint)

            cx = MARGIN_LEFT
            for (i in matColW.indices) {
                canvas.drawText(matColTitles[i], cx + matColW[i] / 2f, currentY + 9f, colCenterSmall)
                cx += matColW[i]
                if (i < matColW.size - 1) {
                    canvas.drawLine(cx, currentY, cx, currentY + matHeaderH, linePaint)
                }
            }
            currentY += matHeaderH

            val matRowsCount = 3
            val matRowH = 12f
            val materialItems = reportWithDetails.materialItems
            for (r in 0 until matRowsCount) {
                val item = materialItems.getOrNull(r)
                canvas.drawLine(MARGIN_LEFT, currentY + matRowH, MARGIN_RIGHT, currentY + matRowH, thinLinePaint)
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
                            canvas.drawText(text, mx + 4f, currentY + 9f, textSmall)
                        } else {
                            canvas.drawText(text, mx + matColW[i] / 2f, currentY + 9f, colCenterSmall)
                        }
                    }
                    mx += matColW[i]
                    if (i < matColW.size - 1) {
                        canvas.drawLine(mx, currentY, mx, currentY + matRowH, thinLinePaint)
                    }
                }
                currentY += matRowH
            }

            // 10. BANNER: OBSERVAÇÕES
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + bannerH, subheaderFillPaint)
            canvas.drawLine(MARGIN_LEFT, currentY + bannerH, MARGIN_RIGHT, currentY + bannerH, linePaint)
            canvas.drawText("OBSERVAÇÕES", (MARGIN_LEFT + MARGIN_RIGHT) / 2f, currentY + 9f, textCenterBold)
            currentY += bannerH

            val obsBoxH = 34f
            canvas.drawLine(MARGIN_LEFT, currentY + obsBoxH, MARGIN_RIGHT, currentY + obsBoxH, linePaint)
            if (report.observacoes.isNotBlank()) {
                drawMultilineText(canvas, report.observacoes, MARGIN_LEFT + 6f, currentY + 11f, CONTENT_WIDTH - 12f, textSmall, 9f)
            }
            currentY += obsBoxH

            // 11. FOOTER: ROAD MARKINGS LEGEND + LIBERAÇÃO DA OBRA + ASSINATURA
            val footerH = PAGE_HEIGHT - 18f - currentY
            val roadLegendW = 280f

            canvas.drawLine(MARGIN_LEFT + roadLegendW, currentY, MARGIN_LEFT + roadLegendW, currentY + footerH, linePaint)
            drawRoadMarkingLegend(canvas, MARGIN_LEFT, currentY, roadLegendW, footerH, linePaint)

            val rightX = MARGIN_LEFT + roadLegendW
            val appRowH = 18f
            canvas.drawLine(rightX, currentY + appRowH, MARGIN_RIGHT, currentY + appRowH, thinLinePaint)
            canvas.drawText("LIBERAÇÃO DA OBRA:", rightX + 6f, currentY + 12f, textBold)

            val chkApp1X = rightX + 110f
            drawCheckbox(canvas, chkApp1X, currentY + 5f, report.liberacaoStatus == "APROVADO", linePaint)
            canvas.drawText("APROVADO", chkApp1X + 9f, currentY + 12f, textSmall)

            val chkApp2X = chkApp1X + 54f
            drawCheckbox(canvas, chkApp2X, currentY + 5f, report.liberacaoStatus == "APROVADO COM RETRABALHO", linePaint)
            canvas.drawText("APROV. RETRAB.", chkApp2X + 9f, currentY + 12f, textSmall)

            val chkApp3X = chkApp2X + 68f
            drawCheckbox(canvas, chkApp3X, currentY + 5f, report.liberacaoStatus == "REPROVADO", linePaint)
            canvas.drawText("REPROVADO", chkApp3X + 9f, currentY + 12f, textSmall)

            val sigAreaY = currentY + appRowH
            val sigAreaH = footerH - appRowH

            if (!report.assinaturaBitmapPath.isNullOrBlank()) {
                val sigFile = File(report.assinaturaBitmapPath)
                if (sigFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(sigFile.absolutePath)
                    if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                        val maxW = 150f
                        val maxH = (sigAreaH - 24f).coerceAtLeast(10f)
                        val bWidth = bitmap.width.toFloat()
                        val bHeight = bitmap.height.toFloat()
                        val scale = minOf(maxW / bWidth, maxH / bHeight)
                        val drawW = bWidth * scale
                        val drawH = bHeight * scale
                        val startX = rightX + 20f + (maxW - drawW) / 2f
                        val startY = sigAreaY + 2f + (maxH - drawH) / 2f
                        val destRect = RectF(startX, startY, startX + drawW, startY + drawH)
                        canvas.drawBitmap(bitmap, null, destRect, null)
                    }
                }
            }

            val sigLineY = sigAreaY + sigAreaH - 18f
            canvas.drawLine(rightX + 16f, sigLineY, rightX + 180f, sigLineY, linePaint)
            canvas.drawText("ASS. RESPONSÁVEL", rightX + 55f, sigLineY + 10f, textBold)
            if (report.responsavelNome.isNotBlank()) {
                canvas.drawText(report.responsavelNome, rightX + 55f, sigLineY - 4f, textSmall)
            }

            val dateX = rightX + 195f
            canvas.drawText("DATA: " + (report.dataObra.ifBlank { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }), dateX, sigLineY + 10f, textBold)

            document.finishPage(page)
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
