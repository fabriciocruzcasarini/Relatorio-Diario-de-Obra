# Plano de Geração de PDF com Folhas Completas Padronizadas

Reestruturação do motor de geração de PDF do Relatório Diário de Obra (RDO) para que, quando a quantidade de trechos ultrapassar a capacidade da grade de produção (7 itens), sejam geradas folhas completas idênticas à folha 1, contendo todos os dados cadastrais, fechamento, clima, recursos, materiais, rodapé normativo e assinatura, alterando apenas a partição dos itens de produção e exibindo subtotal e total geral.

---

## Decisões Confirmadas pelo Usuário

> [!IMPORTANT]
> Definições alinhadas para a emissão multi-folhas:

1. **Estrutura Idêntica em Todas as Páginas**:
   - Cada folha gerada conterá o modelo completo do RDO (Cabeçalho da Empresa, Dados da Obra/Cliente, Fechamento de Faixa, Pavimento, Clima/Condições, Recursos, Materiais, Observações, Rodapé com diagramas normativos de faixas viárias e Assinatura Digital do fiscal).
2. **Particionamento de Produção em Lotes de 7 Itens**:
   - Folha 1: Trechos 1 a 7
   - Folha 2: Trechos 8 a 14
   - Folha 3 (se houver): Trechos 15 a 21, e assim por diante.
3. **Indicador de Folha no Cabeçalho**:
   - Exibição dinâmica no título central:
     - Página única: `DIÁRIO DE OBRA - SINALIZAÇÃO HORIZONTAL`
     - Múltiplas páginas: `DIÁRIO DE OBRA - SINALIZAÇÃO HORIZONTAL (FOLHA X DE Y)`
4. **Subtotal da Página e Total Geral Consolidado**:
   - Na linha de totalização de produção:
     - Em folhas múltiplas: exibe o `SUBTOTAL PÁGINA: X,XX M²` e o `TOTAL GERAL: Y,YY M²`.
     - Em folha única: exibe `TOTAL EXECUTADO: Y,YY M²`.

---

## 1. Arquitetura da Geração de Páginas

```
Lista de Itens de Produção (N itens)
  │
  ├── Partição em chunks de 7 itens: [1..7], [8..14], [15..21]...
  │
  └── Para cada chunk (Folha i de totalPages):
       ├── document.startPage(pageInfo)
       ├── Desenha Cabeçalho (com "FOLHA i DE totalPages")
       ├── Desenha Dados Gerais, Fechamento, Pavimento e Clima
       ├── Desenha Métodos de Aplicação (Laminado, Remoção, etc.)
       ├── Desenha Tabela de Produção para o chunk atual (7 linhas)
       ├── Desenha Linha de Total: Subtotal da Página + Total Geral Consolidado
       ├── Desenha Recursos e Materiais
       ├── Desenha Observações
       ├── Desenha Rodapé com Gráfico de Faixas (LBO, LMS, LFO)
       ├── Desenha Liberação da Obra, Assinatura Digital e Data
       └── document.finishPage(page)
```

---

## 2. Benefícios Técnicos e Operacionais em Campo

- **Validade Jurídica e Fiscal**: Cada folha emitida é um documento autônomo e completo com a assinatura do fiscal e os dados da rodovia, sem fragmentação em anexos desprovidos de assinatura.
- **Conferência Facilitada**: O fiscal e a concessionária podem conferir o subtotal de cada folha separadamente e o total geral consolidado acumulado de toda a obra.
- **Fidelidade Visual 100%**: Todas as páginas mantêm exatamente o design original do modelo físico anexado.

---

## 3. Próximos Passos de Execução

1. Refatorar a função de desenho de folha em `RdoPdfGenerator.kt` para uma função modular reutilizável `drawFullRdoSheet(canvas, report, prodChunk, pageIndex, totalPages, grandTotalM2)`.
2. Implementar a exibição da paginação no título e a divisão entre subtotal da folha e total geral consolidado.
3. Validar a geração com testes unitários cobrindo cenários de 1 folha e múltiplas folhas (ex: 15 itens gerando 3 folhas completas).
4. Compilar e verificar o projeto com `compile_applet`.
