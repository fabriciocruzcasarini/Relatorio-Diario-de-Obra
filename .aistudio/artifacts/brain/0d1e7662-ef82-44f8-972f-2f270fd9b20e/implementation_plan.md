# Plano de Substituição: Carregamento de Foto da Assinatura

Plano para substituir o componente de desenho na tela por um seletor moderno de foto da assinatura da galeria (utilizando o Android Photo Picker de zero permissões), permitindo salvar a assinatura como padrão reutilizável para todos os novos RDOs emitidos.

---

## Decisões Confirmadas pelo Usuário

> [!IMPORTANT]
> Definições de produto confirmadas:

1. **Substituição Completa do Desenho**:
   - O pad de desenho tátil será substituído por um componente limpo e direto de **carregamento de imagem/foto da assinatura** da galeria do aparelho.
2. **Assinatura Padrão Reutilizável**:
   - A imagem carregada poderá ser salva nas preferências do app/empresa para ser carregada automaticamente em todos os novos RDOs criados, economizando tempo do encarregado em campo.
3. **Android Photo Picker Sem Permissões Intrusivas**:
   - Uso de `ActivityResultContracts.PickVisualMedia()`, padrão oficial do Google Play que não exige permissões de leitura de armazenamento.
4. **Renderização Proporcional no PDF**:
   - No motor `RdoPdfGenerator`, a foto da assinatura será ajustada mantendo a proporção original (aspect ratio), evitando distorções no rodapé do documento.

---

## 1. Experiência do Usuário (Fluxo de Assinatura)

1. **Na Aba 5 (Liberação & Assinatura)**:
   - Se nenhuma assinatura estiver cadastrada:
     - Um card com botão destacado **"Carregar Foto da Assinatura"** (com ícone de imagem/galeria).
     - Orientações de boas práticas (ex: "Fotografe sua assinatura em um papel branco com boa iluminação").
   - Quando a imagem é selecionada:
     - Exibição de pré-visualização imediata da assinatura na tela.
     - Botões rápidos para **"Trocar Foto"** ou **"Remover"**.
     - Checkbox ativado por padrão: *"Salvar como assinatura padrão para os próximos RDOs"*.
2. **Ao Criar Novo RDO**:
   - O formulário já inicializa automaticamente com a assinatura padrão e o nome do responsável pré-carregados.
3. **No PDF Emitido**:
   - A foto da assinatura é desenhada com precisão vetorial sobre a linha do responsável, acompanhada do nome do fiscal e da data da obra.

---

## 2. Arquitetura Técnica

```
Usuário seleciona Foto
      │ (Android Photo Picker - PickVisualMedia)
      ▼
Salva cópia local segura em filesDir/signatures/
      │
      ├── Atualiza report.assinaturaBitmapPath
      └── Se marcado "Assinatura Padrão" -> Atualiza CompanySettings.defaultAssinaturaPath
            │
            ▼
RdoPdfGenerator.kt
  ├── Decodifica bitmap com BitmapFactory.decodeFile
  ├── Calcula escala mantendo proporção original (Aspect Fit)
  └── Desenha no rodapé de cada folha do PDF
```

---

## 3. Próximos Passos de Execução

1. Atualizar a entidade `CompanySettings` e `RdoDao` para persistir o caminho da assinatura padrão e nome do responsável padrão.
2. Criar o componente Compose `SignaturePhotoPicker.kt` com suporte a `PickVisualMedia` e preview do bitmap.
3. Atualizar `RdoFormScreen.kt` substituindo o `SignaturePad` pelo novo seletor de foto.
4. Aprimorar o `RdoPdfGenerator.kt` para renderizar o bitmap da assinatura preservando o aspect ratio (sem esticar).
5. Atualizar o `RdoViewModel.kt` para injetar a assinatura padrão ao criar novos relatórios.
6. Executar os testes e compilar com `compile_applet`.
