# Análise de QA e Plano de Melhorias - Relatório Diário de Obra

Plano de refinamento e correções identificado na análise de qualidade (QA) do aplicativo de Relatório Diário de Obra (RDO - Sinalização Horizontal), visando robustez de dados em campo, flexibilidade de digitação numérica, validação preventiva e expansão da geração do PDF para múltiplos trechos.

---

## Diagnóstico de QA e Decisões Confirmadas

> [!IMPORTANT]
> A análise de QA levantou os seguintes pontos críticos, confirmados pelo usuário:

1. **Parser Numérico Brasileiro (Vírgula e Ponto)**:
   - *Problema*: Usuários em dispositivos em português digitam medidas com vírgula (ex: `1,5` ou `0,15`). O método `toDoubleOrNull()` nativo falha silenciosamente, zerando a área calculada.
   - *Solução*: Implementação de normalizador robusto que converte vírgula para ponto e formata de acordo com a localidade brasileira.
2. **Validação de Campos Obrigatórios com Alertas Visuais**:
   - *Problema*: Emissão de relatório com campos em branco (como Obra, Cliente, Fiscal ou Data), gerando PDFs incompletos.
   - *Solução*: Verificação de integridade antes da emissão, destacando em vermelho os campos pendentes e exibindo mensagem instrutiva (Snackbar/Dialog).
3. **Seletor Visual de Data (DatePicker)**:
   - *Problema*: Digitação manual livre suscetível a erros de digitação (ex: barras invertidas, formatos invertidos).
   - *Solução*: Integração com o componente nativo `DatePickerDialog` do Material 3, garantindo padrão `DD/MM/AAAA`.
4. **Paginação Automática no PDF (Anexos de Produção)**:
   - *Problema*: Em obras de rodovias extensas com mais de 7 trechos de sinalização, itens adicionais eram omitidos na página principal.
   - *Solução*: Se houver mais de 7 trechos, a página 1 exibe os 7 primeiros e a página 2 (ou subsequentes) gera o anexo "CONTINUAÇÃO DO RELATÓRIO DE PRODUÇÃO" com cabeçalho resumido e totalizadores consolidados.
5. **Sincronização de Metadados e Nome do Aplicativo**:
   - Atualização do `app_name` em `res/values/strings.xml` para `"Relatório Diário de Obra"`, alinhado com `metadata.json`.

---

## 1. Visão Geral das Melhorias

- **Confiabilidade Matemática**: Garantia de que qualquer valor decimal digitado em campo (ex: `0,15`, `0.15`, `1250,5`) resulte no cálculo preciso da área em metros quadrados ($M^2$) e totalizador geral.
- **Prevenção de Erros Humanos**: Fiscalização guiada onde o engenheiro ou apontador não perde dados e recebe alertas amigáveis caso esqueça de identificar a rodovia ou o fiscal responsável.
- **Relatórios Extensos**: Capacidade de registrar 10, 20 ou mais trechos de pintura no mesmo RDO sem perda de informação no PDF oficial.

---

## 2. Experiência do Usuário e Ajustes na Interface

### Fluxo de Validação e Feedback
- Ao tocar em **"Gerar PDF & WhatsApp"**:
  - Se os campos essenciais (**Nome da Obra**, **Cliente**, **Data da Obra** e **Fiscal**) estiverem preenchidos: o PDF é gerado e a janela de compartilhamento abre imediatamente.
  - Se faltar algum campo: uma barra de alerta suave informa qual informação está faltando e a tela foca na aba correspondente.

### Seletor de Data Amigável
- O campo de data passa a ter um ícone de calendário clicável que abre o calendário visual, além de permitir digitação rápida com máscara.

---

## 3. Arquitetura da Solução Técnica

```
┌─────────────────────────────────────────────────────────────────┐
│                     Camada de Apresentação                      │
│                                                                 │
│   RdoFormScreen                                                 │
│    ├── NumberFormatHelper (normaliza '1,5' -> 1.5)              │
│    ├── Material 3 DatePickerDialog                              │
│    └── FormValidationState (erros em Obra, Cliente, Fiscal)     │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────────┐
│                     Motor de Geração de PDF                     │
│                                                                 │
│   RdoPdfGenerator                                               │
│    ├── Página 1: Folha Principal (RDO Padrão até 7 itens)       │
│    └── Página 2+: Anexo de Produção (Se trechos > 7)            │
│         └── Totalizador Geral Consolidado                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. Próximos Passos de Execução

1. Atualizar `res/values/strings.xml` para refletir `"Relatório Diário de Obra"`.
2. Criar `FormatUtils.kt` com parser seguro de números decimais tolerante a vírgulas e pontos.
3. Atualizar `RdoPdfGenerator.kt` para suportar paginação automática de anexos de produção quando houver mais de 7 itens.
4. Adicionar seletor `DatePickerDialog` e validação visual de formulário no `RdoFormScreen.kt`.
5. Executar os testes automatizados e compilar o applet via `compile_applet`.
