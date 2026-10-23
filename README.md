# Relatório Diário de Obra (RDO) - Sinalização Horizontal

Aplicativo Android nativo em **Kotlin** e **Jetpack Compose** desenvolvido para equipes de engenharia viária, fiscais de obras e operadores de sinalização horizontal em rodovias.

O aplicativo substitui o preenchimento manual em pranchetas de papel, permitindo registrar todos os parâmetros de campo, calcular a produção automaticamente, carregar e aplicar a foto da assinatura digital do responsável e emitir o **Relatório Diário de Obra (RDO)** oficial em formato PDF de alta fidelidade visual, com envio imediato via WhatsApp.

---

## Principais Funcionalidades

- **Formulário Completo e Fiel ao Modelo Oficial**:
  - **Identificação da Obra**: Cliente/Concessionária, Nome da Obra, Rodovia, Veículo KM, Fiscal Responsável, Turno (Dia/Noite), Horários de Saída e Chegada.
  - **Controle de Fechamento de Faixa**: Local de execução, contato/CCO, horário de liberação e horário de encerramento do fechamento.
  - **Condições do Pavimento**: Tipo de pavimento (CBUQ, Concreto, etc.), umidade da pista (Seco, Úmido, Molhado), repintura, contaminações e tempo sem chuva.
  - **Condições Climáticas e Aplicação**: Tempo (Aberto, Nublado, Chuva), temperatura ambiente, umidade relativa do ar (com limite operacional até 65%), ponto de orvalho, temperatura do pavimento e tempo para liberação ao tráfego.
  - **Relatório de Produção Viária**: Checkboxes normativos de métodos e tintas (Base Água, Acrílica Solvente, Plástica a Frio, Manual, Mecânico, Termoplástico Extrudado, Hot Spray, Termo Relevo, Laminado, Remoção a Fogo/Fresca).
  - **Cálculo Automático de Área ($M^2$) com Entrada Fluida**: Cálculo instantâneo da área demarcada ($\text{Faixas} \times \text{Comprimento} \times \text{Largura}$) com suporte livre a vírgula ou ponto (`0,15`, `1500,5`) mantendo o texto digitado sem truncamentos.
  - **Recursos Humanos e Frota**: Apontamento da equipe de campo (encarregados, operadores, pintores), veículos, placas e instrumentos de medição (termohigrômetro, espessímetro).
  - **Materiais e Insumos Químicos**: Rastreabilidade com quantidade, unidade, marca, descrição, data de fabricação, validade e número de lote.
  - **Parecer e Liberação da Obra**: Seleção do parecer da fiscalização (*Aprovado*, *Aprovado com Retrabalho*, *Reprovado*).

- **Assinatura Digital do Encarregado por Foto**:
  - **Upload de Foto da Assinatura**: Carregamento da assinatura física do encarregado/fiscal direto da galeria usando o Android Photo Picker seguro (`ActivityResultContracts.PickVisualMedia`, sem necessidade de permissões perigosas de armazenamento).
  - **Assinatura Padrão Salva**: Opção para salvar a foto da assinatura como padrão nas configurações da empresa, sendo preenchida automaticamente em todos os novos RDOs emitidos.
  - **Ajuste Proporcional no PDF**: O motor de geração calcula a escala preservando a proporção de aspecto (sem distorção ou achatamento) e posiciona a assinatura perfeitamente sobre a linha de responsabilidade técnica.

- **Geração de PDF de Alta Fidelidade (Padrão A4 Oficial)**:
  - Diagramação vetorial nativa via `android.graphics.pdf.PdfDocument`.
  - Cabeçalho personalizado com logotipo e razão social da empresa executora.
  - **Capacidade Expandida para 12 Itens por Folha**: A tabela de produção comporta até 12 itens por folha A4 com 12 linhas padronizadas, reduzindo o número de páginas e mantendo a grade geométrica limpa mesmo com preenchimento parcial.
  - **Paginação Multi-Folhas Estruturada**: Quando a produção ultrapassa 12 itens, páginas subsequentes são geradas mantendo todo o conteúdo institucional e ambiental da Folha 1 (cabeçalho, fechamento, pavimento, condições de aplicação, recursos, materiais e rodapé), alternando exclusivamente os itens daquela folha.
  - **Numeração e Totais por Folha**: Indicador `(FOLHA X DE Y)` no cabeçalho superior e na barra de produção, acompanhado de `SUBTOTAL PÁG: X.XX M²` e `TOTAL GERAL: X.XX M²` consolidado.
  - **Painel Normativo de Faixas Viárias**: Ilustração gráfica no rodapé reproduzindo os padrões LBO, LMS-1, LMS-2, LFO-1, LFO-2, LFO-3 e LFO-4 sobre asfalto com cores regulamentares (branco e amarelo).

- **Envio Direto para WhatsApp & Armazenamento Seguro**:
  - Compartilhamento via `FileProvider` seguro (`ACTION_SEND`).
  - Disparo prioritário para o WhatsApp ou WhatsApp Business, com fallback para o seletor geral do Android.
  - Persistência 100% offline em banco de dados **Room (SQLite)** local para consulta posterior, edição, duplicação e reenvio de relatórios.

---

## Arquitetura e Tecnologias

O projeto segue as práticas recomendadas de arquitetura Android (MVVM / Clean Architecture):

- **Linguagem**: Kotlin 100%
- **Interface**: Jetpack Compose com Material Design 3 (M3)
- **Persistência de Dados**: Room Database (SQLite) com KSP e Coroutines Flow
- **Seleção de Mídia**: Android Photo Picker (`PickVisualMedia`) com cópia em sandbox interno (`files/signatures/`)
- **Gerador de Documentos**: Android Graphics Canvas & `PdfDocument` nativo (sem dependências pesadas de terceiros)
- **Compartilhamento**: AndroidX `FileProvider`
- **Injeção e Ciclo de Vida**: ViewModel e `StateFlow`
- **Testes Automatizados**: JUnit 4 e Robolectric

---

## Estrutura do Código-Fonte

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Ponto de entrada, navegação de telas e diálogos
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt               # Banco de dados Room
│   │   └── RdoDao.kt                    # Data Access Object com queries e transações
│   ├── model/
│   │   └── RdoEntities.kt               # Entidades Room (Relatório, Produção, Recursos, Materiais, Configurações)
│   └── repository/
│       └── RdoRepository.kt             # Camada de repositório
├── pdf/
│   ├── RdoPdfGenerator.kt               # Motor de desenho vetorial do PDF A4 (12 itens/folha, multi-folhas e assinatura)
│   └── RdoShareHelper.kt                # Integração com FileProvider e Intent para WhatsApp
├── ui/
│   ├── components/
│   │   ├── CompanySettingsDialog.kt     # Diálogo de configuração da empresa e assinatura padrão
│   │   ├── PdfExportDialog.kt           # Diálogo com ações de envio WhatsApp e visualização
│   │   └── SignaturePhotoPicker.kt      # Seletor e gerenciador de foto de assinatura via Photo Picker
│   ├── screens/
│   │   ├── RdoFormScreen.kt             # Formulário em 5 abas com validação e DatePickerDialog
│   │   └── RdoListScreen.kt             # Painel de histórico com métricas, busca e cartões
│   ├── theme/
│   │   ├── Color.kt                     # Paleta de cores temática de engenharia e trânsito
│   │   ├── Theme.kt                     # Configuração de temas Claro e Escuro
│   │   └── Type.kt                      # Tipografia
│   └── viewmodel/
│       └── RdoViewModel.kt              # Gerenciamento de estado, cálculos de m² e geração assíncrona
└── util/
    └── FormatUtils.kt                   # Normalizador numérico decimal brasileiro e datas
```

---

## Como Executar e Testar

### Compilação do Aplicativo
O projeto utiliza o Gradle Kotlin DSL:
```bash
gradle :app:assembleDebug
```

### Execução da Suíte de Testes
Para rodar os testes unitários e de ambiente com Robolectric (cálculo de m², paginação de 12 itens e aspect ratio de assinatura):
```bash
gradle :app:testDebugUnitTest
```

---

## Licença e Uso
Desenvolvido para gestão técnica de sinalização viária e obras rodoviárias.
