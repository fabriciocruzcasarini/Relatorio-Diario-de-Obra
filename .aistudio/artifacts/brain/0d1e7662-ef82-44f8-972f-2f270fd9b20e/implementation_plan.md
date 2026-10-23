# Expansão da Grade de Produção no PDF para 12 Itens por Folha

Ajustar a capacidade da tabela de itens de produção no relatório PDF gerado (RDO), aumentando a quantidade exibida por folha de 7 para 12 itens, mantendo espaçamento uniforme, proporções ideais e suporte consistente a relatórios de página única ou múltiplas folhas.

## User Review & Critical Decisions

> [!IMPORTANT]
> As decisões de formato foram alinhadas com as respostas fornecidas:
> - **Quantidade por folha**: Atualizada de 7 para 12 itens.
> - **Preenchimento da grade**: Manter sempre as 12 linhas desenhadas, exibindo linhas em branco quando houver menos de 12 itens na folha para manter o layout visualmente idêntico e consistente.
> - **Altura das linhas**: Mantida na altura padrão de 13pt, aproveitando a folga generosa da página A4 e mantendo excelente legibilidade das colunas.

---

### 1. Overview & Core Concept

- **O que faz**:
  No gerador de PDF (`RdoPdfGenerator`), o particionamento de itens de produção passa de lotes de 7 itens para 12 itens por página.
  - Se um relatório tiver de 1 a 12 itens, ele é gerado em uma única folha A4.
  - Se tiver de 13 a 24 itens, é gerado em duas folhas com os subtotais e totais consolidados.
  - As linhas não preenchidas na página mantêm as grades vazias padronizadas, preservando a identidade visual do formulário técnico de engenharia.
- **Público-alvo**:
  Fiscais, encarregados e engenheiros de sinalização viária que necessitam de mais itens lançados por folha, reduzindo a quantidade de páginas impressas e otimizando o arquivamento.

---

### 2. User Experience & Visual Design

- **Distribuição no A4**:
  - Altura total da página A4: 842pt.
  - Cabeçalho, controle de fechamento, pavimento e condições climáticas: ~246pt.
  - Cabeçalho da produção (18pt) + 12 linhas de produção (12 × 13pt = 156pt) + Linha de Total (14pt): 188pt.
  - Recursos e Materiais: ~120pt.
  - Observações: 46pt.
  - Rodapé com legenda viária e assinatura digital do encarregado: ~242pt restantes, oferecendo amplo espaço para a legenda e para a foto da assinatura com seu dimensionamento proporcional.
- **Consistência Visual**:
  - Tipografia de 7pt e 5.5pt mantida para clareza em todas as 10 colunas (Serviço, Rodovia, KM Inicial, KM Final, Quant. Faixas, Comprimento, Largura, Total M², Cor e Espessura).
  - Indicadores de folha no cabeçalho ("FOLHA X DE Y") e subtotais continuam perfeitamente sincronizados com o novo particionador de 12 itens.

---

### 3. Key Product Decisions & Trade-Offs

- **Decisão 1: `ITEMS_PER_PAGE = 12` em vez de 7**
  - *Abordagem*: Elevação direta da constante que define a paginação dos itens de produção.
  - *Benefício*: Otimização de espaço no A4, permitindo que a grande maioria dos relatórios diários de obras caibam em uma única página, gerando economia de papel e visualização rápida dos serviços executados.
- **Decisão 2: Manutenção de 12 linhas estruturadas na grade**
  - *Abordagem*: O loop da tabela itera até 12 (`for (r in 0 until ITEMS_PER_PAGE)`), desenhando as divisões de linhas e colunas mesmo quando `item == null`.
  - *Benefício*: O formulário impresso mantém rigidez geométrica, permitindo anotações manuais suplementares se necessário e evitando que seções inferiores fiquem "dançando" de posição dependendo da quantidade de itens.
- **Decisão 3: Atualização dos Testes Unitários de Paginação**
  - *Abordagem*: Atualizar o teste de particionamento multi-folhas em `ExampleRobolectricTest.kt` para validar a lógica com 12 itens (ex: 15 itens gerando 2 folhas: Folha 1 com 0..12, Folha 2 com 12..15).
  - *Benefício*: Prevenção de regressão e validação automatizada imediata via JVM.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   RdoWithDetails                       │
│    (RdoReport + List<RdoProductionItem> + Recursos)    │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                 RdoPdfGenerator.kt                     │
│  - ITEMS_PER_PAGE = 12                                 │
│  - totalPages = ceil(items.size / 12)                  │
│  - Loop: subList(pageIdx * 12, minOf((pageIdx+1)*12))   │
│  - Grid: 12 rows @ 13pt (padded empty rows as needed)  │
│  - Footer & Signature: auto-positioned in remaining space│
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Documento PDF A4                     │
│  Folha 1: Itens 1 a 12                                 │
│  Folha 2 (se > 12 itens): Itens 13 a 24...             │
└────────────────────────────────────────────────────────┘
```

#### Arquivos impactados:
1. `/app/src/main/java/com/example/pdf/RdoPdfGenerator.kt`: Atualização de `ITEMS_PER_PAGE = 12`.
2. `/app/src/test/java/com/example/ExampleRobolectricTest.kt`: Atualização das asserções de particionamento de páginas para a base de 12 itens.
