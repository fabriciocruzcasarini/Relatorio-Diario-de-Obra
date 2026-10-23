package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "rdo_reports")
data class RdoReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataObra: String = "", // DD/MM/AAAA
    val cliente: String = "",
    val obra: String = "",
    val veiculoKm: String = "",
    val fiscal: String = "",
    val turnoDia: Boolean = true,
    val turnoNoite: Boolean = false,
    val horaSaida: String = "",
    val horaChegada: String = "",
    // Controle de Fechamento
    val fechamentoLocal: String = "",
    val fechamentoContato: String = "",
    val fechamentoHorario: String = "",
    val fechamentoLiberadoPor: String = "",
    val fechamentoHorarioFim: String = "",
    // Pavimento
    val tipoPavimento: String = "", // CBUQ, Concreto, etc.
    val pavimentoSeco: Boolean = true,
    val pavimentoUmido: Boolean = false,
    val pavimentoMolhado: Boolean = false,
    val repinturaSobre: String = "",
    val aderenciaPinturaAntiga: String = "",
    val diasSemChuva: String = "",
    val contaminacoes: String = "BOM", // BOM, REGULAR, RUIM
    val pavimentoNovo: Boolean = false,
    // Condições de Aplicação / Clima
    val tempoAberto: Boolean = true,
    val tempoNublado: Boolean = false,
    val tempoChuva: Boolean = false,
    val tempAmbiente: String = "",
    val umidadeRelativa: String = "", // até 65%
    val pontoOrvalho: String = "",
    val tempPavimento: String = "",
    val liberacaoTrafegoMin: String = "",
    val choveuAposPintura: String = "NÃO",
    // Métodos / Tintas
    val metodoBaseAgua: Boolean = false,
    val metodoAcrilicaSolvente: Boolean = true,
    val metodoPlasticaAFrio: Boolean = false,
    val metodoManual: Boolean = false,
    val metodoMecanico: Boolean = true,
    val metodoTermExtrudado: Boolean = false,
    val metodoHotSpray: Boolean = false,
    val metodoTermRelevo: Boolean = false,
    val metodoLaminado: Boolean = false,
    val remocaoFresca: Boolean = false,
    val remocaoFogo: Boolean = false,
    val metodoOutros: String = "",
    // Observações e Liberação
    val observacoes: String = "",
    val liberacaoStatus: String = "APROVADO", // APROVADO, APROVADO COM RETRABALHO, REPROVADO
    val responsavelNome: String = "",
    val assinaturaBitmapPath: String? = null,
    val pdfGeradoPath: String? = null
)

@Entity(tableName = "rdo_production_items")
data class RdoProductionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long = 0,
    val servicoExecutado: String = "", // ex: LBO, LMS-1, LFO-3, etc.
    val rodovia: String = "",
    val kmInicial: String = "",
    val kmFinal: String = "",
    val quantFaixas: Int = 1,
    val comprimentoM: Double = 0.0,
    val larguraM: Double = 0.15, // 0.10, 0.15, 0.20 padrão
    val totalM2: Double = 0.0,
    val cor: String = "BRANCO", // BRANCO, AMARELO, PRETO, AZUL, VERMELHO
    val espessuraMm: String = "0.6"
)

@Entity(tableName = "rdo_resource_items")
data class RdoResourceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long = 0,
    val equipe: String = "",
    val veiculos: String = "",
    val placa: String = "",
    val equipamentosMedicao: String = ""
)

@Entity(tableName = "rdo_material_items")
data class RdoMaterialItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long = 0,
    val quantidade: String = "",
    val unidade: String = "GL", // GL, KG, L, SC
    val marca: String = "",
    val descricao: String = "",
    val fabricacao: String = "",
    val validade: String = "",
    val lote: String = ""
)

@Entity(tableName = "company_settings")
data class CompanySettings(
    @PrimaryKey
    val id: Int = 1,
    val nomeEmpresa: String = "Impacto Sinalização",
    val subtitulo: String = "Sinalização Viária Horizontal e Vertical",
    val cnpj: String = "",
    val telefone: String = "",
    val email: String = "",
    val defaultAssinaturaPath: String? = null,
    val defaultResponsavelNome: String = ""
)

data class RdoWithDetails(
    @Embedded val report: RdoReport,
    @Relation(
        parentColumn = "id",
        entityColumn = "reportId"
    )
    val productionItems: List<RdoProductionItem>,
    @Relation(
        parentColumn = "id",
        entityColumn = "reportId"
    )
    val resourceItems: List<RdoResourceItem>,
    @Relation(
        parentColumn = "id",
        entityColumn = "reportId"
    )
    val materialItems: List<RdoMaterialItem>
)
