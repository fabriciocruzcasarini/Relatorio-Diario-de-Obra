# Plano de Correção de Inconsistências e Usabilidade - Relatório Diário de Obra

Plano para solucionar as inconsistências de digitação e fluxo identificadas na auditoria de qualidade, garantindo fluidez no preenchimento em campo e precisão na exportação dos relatórios.

---

## Inconsistências Diagnosticadas e Soluções Confirmadas

> [!IMPORTANT]
> Decisões e soluções confirmadas pelo usuário para esta rodada de ajustes:

1. **Digitação Livre de Decimais sem Apagar Vírgula/Ponto**:
   - *Problema*: Ao digitar `"15,"`, a conversão imediata para `Double` e formatação reversa apagava o caractere `","` antes que o usuário pudesse digitar a casa decimal (ex: `"5"`).
   - *Solução*: Manter o estado do texto bruto (`String`) durante a digitação no componente `ProductionItemCard`, sincronizando o valor numérico `comprimentoM` e recalculando `totalM2` sem forçar a reescrita do campo digitado pelo usuário.
2. **Salvamento Automático da Assinatura Digital**:
   - *Problema*: Se o fiscal desenhasse a assinatura no pad e tocasse diretamente em "Emitir RDO em PDF" ou "Salvar" no topo da tela, a assinatura não era salva caso ele não tivesse clicado no pequeno botão "Salvar" interno do pad.
   - *Solução*: Conectar o `SignaturePad` para expor o estado do desenho e gravar o arquivo bitmap automaticamente no momento do salvamento ou da emissão do relatório, eliminando perdas acidentais de assinatura.
3. **Harmonização do Total Consolidado no Anexo do PDF**:
   - *Problema*: Na página 1, a posição do rótulo "TOTAL EXECUTADO" foi aprimorada com `measureText`, mas no rodapé do Anexo (página 2+) a posição ainda utilizava coordenadas estáticas que podiam sobrepor o texto em números grandes.
   - *Solução*: Aplicar a mesma lógica de cálculo dinâmico de largura com `measureText` na página de anexo, mantendo o layout limpo e padronizado em todas as páginas.
4. **Alinhamento e Limpeza de Código**:
   - Manter os ajustes manuais feitos na posição de "LAMINADO" e "REMOÇÃO", limpando blocos comentados obsoletos.

---

## 1. Visão Geral das Alterações Técnicas

- **`RdoFormScreen.kt`**:
  - `ProductionItemCard`: Adicionar buffers de texto locais (`remember { mutableStateOf(...) }`) para `comprimentoStr`, `larguraStr` e `totalM2Str`, permitindo digitação fluida com vírgulas e pontos.
  - `SignaturePad`: Adicionar callback/handler de exportação imediata ou salvamento sob demanda.
- **`SignaturePad.kt`**:
  - Expor função para exportar o traço diretamente ao acionar "Salvar Relatório" ou "Emitir PDF".
- **`RdoPdfGenerator.kt`**:
  - Harmonizar o total da página de anexo com `measureText` e alinhar o layout conforme os ajustes manuais recentes.

---

## 2. Próximos Passos de Execução

1. Atualizar `SignaturePad.kt` para suportar exportação direta e automática dos traços desenhados.
2. Atualizar `RdoFormScreen.kt` com buffers de texto para digitação contínua e integração do salvamento automático da assinatura.
3. Atualizar `RdoPdfGenerator.kt` harmonizando a renderização do total no anexo de produção.
4. Rodar a suíte de testes unitários e compilar o applet via `compile_applet`.
