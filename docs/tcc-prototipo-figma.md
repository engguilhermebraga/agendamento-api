# Documento de Prototipagem — Figma
**Sistema de Agendamento — TCC**
**Versão:** 1.0
**Data:** Maio de 2026

---

## Paleta de Cores Global

Baseada no Bootstrap 5 com variáveis CSS customizadas do projeto.

| Token | Valor Hex | Uso |
|---|---|---|
| `--bs-primary` | `#0d6efd` | Botões primários, links, elementos de ação principal |
| `--bs-success` | `#198754` | Confirmações, badges CONFIRMADO, botões de sucesso |
| `--bs-warning` | `#ffc107` | Alertas, badges AGENDADO, avisos |
| `--bs-danger` | `#dc3545` | Erros, badges CANCELADO, botões destrutivos |
| `--bs-muted` | `#6c757d` | Textos secundários, badges CONCLUIDO, placeholders |
| `--bs-light` | `#f8f9fa` | Fundo das páginas, cards de fundo claro |
| `--bs-dark` | `#212529` | Texto principal, elementos escuros |
| `--bs-white` | `#ffffff` | Fundo de cards, modais, formulários |
| `--bs-border` | `#dee2e6` | Bordas de tabelas, inputs, divisores |
| Sidebar bg | `#212529` | Fundo do menu lateral administrativo |
| Sidebar text | `#adb5bd` | Texto dos itens do menu lateral |
| Sidebar active | `#ffffff` | Texto do item ativo do menu lateral |
| Sidebar hover bg | `#343a40` | Fundo do item ao passar o mouse |

### Cores dos Badges de Status

| Status | Cor de Fundo | Cor do Texto | Classe Bootstrap |
|---|---|---|---|
| AGENDADO | `#fff3cd` | `#664d03` | `badge bg-warning text-dark` |
| CONFIRMADO | `#d1e7dd` | `#0a3622` | `badge bg-success` (ou custom) |
| CONCLUIDO | `#e9ecef` | `#495057` | `badge bg-secondary` (ou custom) |
| CANCELADO | `#f8d7da` | `#58151c` | `badge bg-danger` (ou custom) |

---

## Tipografia Global

| Elemento | Fonte | Peso | Tamanho | Cor |
|---|---|---|---|---|
| Fonte base | `system-ui, -apple-system, sans-serif` | 400 | 16px (1rem) | `#212529` |
| Heading H1 | system-ui | 700 | 2rem (32px) | `#212529` |
| Heading H2 | system-ui | 700 | 1.5rem (24px) | `#212529` |
| Heading H3 | system-ui | 600 | 1.25rem (20px) | `#212529` |
| Heading H4 | system-ui | 600 | 1rem (16px) | `#212529` |
| Texto muted | system-ui | 400 | 0.875rem (14px) | `#6c757d` |
| Label de formulário | system-ui | 500 | 0.875rem (14px) | `#212529` |
| Texto de tabela | system-ui | 400 | 0.875rem (14px) | `#212529` |
| Badge | system-ui | 600 | 0.75rem (12px) | variável |

---

## Espaçamento e Grid

- **Grid Bootstrap 5:** 12 colunas, gutter padrão de 24px (1.5rem)
- **Container máximo:** 1320px (`.container-xxl`)
- **Padding de página:** 24px (1.5rem) horizontal em desktop
- **Espaçamento entre cards:** 24px (`gap-3` ou `mb-3`)
- **Padding interno de card:** 20px (`p-3`) a 24px (`p-4`)
- **Altura do navbar:** 56px fixo
- **Largura do sidebar:** 260px (desktop), recolhido em mobile
- **Border radius padrão:** 0.375rem (6px)
- **Border radius de cards:** 0.5rem (8px)
- **Border radius de botões:** 0.375rem (6px)
- **Sombra de card padrão:** `box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,.075)`
- **Sombra de card elevado:** `box-shadow: 0 0.5rem 1rem rgba(0,0,0,.15)`

---

## Tela 1 — Layout Base Administrativo (Sidebar + Navbar + Área de Conteúdo)

**URL de referência:** Todas as páginas do painel admin
**Finalidade no Figma:** Frame mestre (Master Frame) — todas as telas administrativas herdam este layout.

### Estrutura de Layout

```
┌────────────────────────────────────────────────────────────┐
│  NAVBAR (altura: 56px, fundo: #ffffff, borda-bottom: 1px)  │
├──────────────┬─────────────────────────────────────────────┤
│   SIDEBAR    │                                             │
│  (260px)     │         ÁREA DE CONTEÚDO PRINCIPAL          │
│  fundo:      │         (flex: 1, overflow-y: auto)         │
│  #212529     │         padding: 24px                       │
│              │                                             │
│  Largura     │                                             │
│  fixa em     │                                             │
│  desktop     │                                             │
└──────────────┴─────────────────────────────────────────────┘
```

### Componente: Navbar

- **Altura:** 56px
- **Fundo:** `#ffffff`
- **Borda inferior:** `1px solid #dee2e6`
- **Sombra:** `box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,.075)`
- **Conteúdo à esquerda:** Botão hambúrguer (ícone Bootstrap Icons `bi-list`, 24px) + logo/nome do sistema em texto `H4` bold `#212529`
- **Conteúdo à direita:** Nome do usuário logado (texto muted 14px) + ícone de usuário (`bi-person-circle`, 24px, `#6c757d`) + dropdown com "Perfil" e "Sair"
- **Comportamento responsivo:** Em mobile (< 992px), o sidebar é ocultado e o hambúrguer ativa um offcanvas Bootstrap

### Componente: Sidebar

- **Largura:** 260px (fixo em desktop)
- **Altura:** 100vh (tela inteira menos o navbar — usar `calc(100vh - 56px)`)
- **Fundo:** `#212529`
- **Overflow-y:** `auto` (scroll quando conteúdo excede)
- **Padding:** 16px 0 (vertical) — sem padding horizontal (os itens têm padding próprio)

**Seção de logo/brand dentro do sidebar:**
- Altura: 64px
- Fundo: `#1a1d20` (ligeiramente mais escuro)
- Texto: Nome do sistema, `H5`, `#ffffff`, centralizado

**Itens de navegação:**
- Padding: 12px 20px
- Texto: 14px, `#adb5bd`, font-weight 400
- Ícone: Bootstrap Icons, 18px, margem-direita 10px, cor `#adb5bd`
- Hover: fundo `#343a40`, texto e ícone `#ffffff` (transição 150ms)
- Estado ativo: fundo `#0d6efd`, texto e ícone `#ffffff`, font-weight 600
- Sem borda arredondada nos itens (flush com as bordas laterais)

**Grupos de navegação:**
- Título do grupo: texto `0.6875rem` (11px), `#6c757d`, uppercase, letter-spacing 1px, padding 16px 20px 6px
- Separador: `1px solid #343a40` com margem 8px 0

**Itens mínimos no sidebar:**
1. Dashboard (`bi-speedometer2`)
2. Agendamentos (`bi-calendar-check`)
3. Clientes (`bi-people`)
4. Profissionais (`bi-person-badge`)
5. Serviços (`bi-clipboard2-pulse`)
6. Separador
7. Configurações (`bi-gear`) — se aplicável
8. Sair (`bi-box-arrow-right`)

### Componente: Área de Conteúdo Principal

- **Fundo:** `#f8f9fa`
- **Padding:** 24px (desktop), 16px (mobile)
- **Máximo de largura do conteúdo interno:** sem limite rígido (ocupa toda a área disponível)
- **Cabeçalho de seção padrão (Page Header):**
  - Linha com: título `H2` à esquerda + botão de ação primária à direita
  - Margem inferior: 24px
  - Borda inferior: opcional `1px solid #dee2e6` com padding-bottom 16px
  - Breadcrumb (opcional): texto 13px, `#6c757d`, logo abaixo do título

---

## Tela 2 — Dashboard

**URL de referência:** `http://localhost:8081/dashboard`

### Estrutura de Conteúdo

Usa o Layout Base (Tela 1). O conteúdo principal é dividido em três blocos verticais.

### Bloco 1: Cards de Métricas (topo)

- **Layout:** 4 colunas iguais (`col-xl-3 col-md-6`), gutter 24px
- **Espaçamento abaixo:** 24px (margin-bottom)

**Card de Métrica individual:**
- Dimensão: largura variável, altura ~100px
- Fundo: `#ffffff`
- Border radius: 0.5rem
- Sombra: `0 0.125rem 0.25rem rgba(0,0,0,.075)`
- Padding: 20px 24px
- **Conteúdo:**
  - Linha superior: ícone Bootstrap Icons (24px, cor variável por tipo) à direita + label de texto 13px `#6c757d` à esquerda
  - Número principal: `H2` ou `H3`, bold, `#212529`, 28px
  - Variação/subtexto opcional: texto 13px `#6c757d` (ex.: "este mês")
- **Variações por tipo de card:**
  - Total de Agendamentos: ícone `bi-calendar3`, borda-esquerda 4px `#0d6efd`
  - Agendamentos Hoje: ícone `bi-calendar-day`, borda-esquerda 4px `#ffc107`
  - Confirmados: ícone `bi-check-circle`, borda-esquerda 4px `#198754`
  - Cancelados: ícone `bi-x-circle`, borda-esquerda 4px `#dc3545`

### Bloco 2: Gráfico + Resumo (meio)

- **Layout:** 2 colunas — gráfico (`col-xl-5`) + tabela de resumo (`col-xl-7`), ou inverso

**Card do Gráfico Chart.js:**
- Fundo: `#ffffff`, border-radius 0.5rem, sombra padrão, padding 20px
- Título do card: `H5` 16px bold, `#212529` + texto muted 13px
- Gráfico Doughnut: centralizado, diâmetro ~240px
- Legenda: abaixo do gráfico, horizontal, com quadrado colorido + label + valor
- Cores das fatias: AGENDADO `#ffc107`, CONFIRMADO `#198754`, CONCLUIDO `#6c757d`, CANCELADO `#dc3545`

### Bloco 3: Tabela de Próximos Agendamentos (baixo)

**Card de Tabela:**
- Fundo: `#ffffff`, border-radius 0.5rem, sombra padrão
- Header do card: padding 16px 20px, título `H5` + badge com número de registros
- Tabela Bootstrap `table table-hover table-striped`:
  - Header: fundo `#f8f9fa`, texto bold 13px `#6c757d` uppercase
  - Linhas: altura 48px, texto 14px `#212529`
  - Colunas: Data/Hora | Cliente | Profissional | Serviço | Status
  - Coluna Status: badge colorido conforme status
  - Hover de linha: fundo `rgba(0,0,0,.025)`
- Rodapé do card: link "Ver todos os agendamentos" alinhado à direita, texto `#0d6efd` 14px

---

## Tela 3 — Listagem de Agendamentos

**URL de referência:** `http://localhost:8081/web/agendamentos/listar`

### Estrutura de Conteúdo

Usa o Layout Base (Tela 1). Conteúdo organizado em dois blocos verticais.

### Bloco 1: Cabeçalho + Filtros

- **Page Header:** título "Agendamentos" `H2` à esquerda + botão "Novo Agendamento" (`btn btn-primary`, ícone `bi-plus-lg` + texto) à direita

**Barra de Filtros:**
- Fundo: `#ffffff`, border-radius 0.5rem, sombra leve, padding 16px 20px, margin-bottom 20px
- Layout: linha horizontal com `row g-3`
- Campos de filtro (cada um em `col-md-3`):
  - Status: `<select>` com opções TODOS/AGENDADO/CONFIRMADO/CONCLUIDO/CANCELADO
  - Data inicial: `<input type="date">`
  - Data final: `<input type="date">`
  - Profissional: `<select>` com lista de profissionais
- Botões (alinhados à direita, `col-md auto`):
  - "Filtrar" (`btn btn-primary`, ícone `bi-funnel`)
  - "Limpar" (`btn btn-outline-secondary`, ícone `bi-x`)
- Altura total da barra de filtros: ~72px

### Bloco 2: Tabela de Resultados

**Card de Tabela:**
- Fundo: `#ffffff`, border-radius 0.5rem, sombra padrão
- Tabela `table table-hover`:
  - Header: fundo `#f8f9fa`, texto 12px uppercase bold `#6c757d`, padding 12px 16px
  - Colunas: # | Cliente | Profissional | Serviço | Data/Hora | Status | Ações
  - Largura da coluna Ações: 200px (fixa)
  - Largura da coluna Status: 130px
  - Demais colunas: distribuição automática

**Coluna Ações (por linha):**
- Grupo de botões compactos (`btn-group btn-sm`):
  - "Detalhes" (`btn btn-outline-info`, ícone `bi-eye`)
  - "Editar" (`btn btn-outline-warning`, ícone `bi-pencil`)
  - "Status" (`btn btn-outline-primary`, ícone `bi-arrow-repeat`) — abre Modal de Mudança de Status
  - "Excluir" (`btn btn-outline-danger`, ícone `bi-trash`) — abre Modal de Confirmação

### Componente: Modal de Mudança de Status

- **Tipo:** Bootstrap Modal, tamanho padrão (500px)
- **Fundo overlay:** `rgba(0,0,0,.5)`
- **Estrutura do modal:**
  - **Header:** título "Alterar Status do Agendamento" + botão de fechar (×)
  - **Body:**
    - Card de dados do agendamento: nome do cliente, data/hora, status atual (badge)
    - Divider
    - Label "Novo status:" + `<select>` com apenas os status válidos para transição
    - Texto auxiliar 13px `#6c757d` explicando as transições permitidas
  - **Footer:**
    - Botão "Cancelar" (`btn btn-secondary`)
    - Botão "Confirmar Alteração" (`btn btn-primary`)
- **Animação:** fade-in Bootstrap padrão (300ms)

### Componente: Modal de Confirmação de Exclusão

- **Tipo:** Bootstrap Modal, tamanho padrão (500px)
- **Header:** fundo `#dc3545`, texto `#ffffff`, título "Confirmar Exclusão" + ícone `bi-exclamation-triangle`
- **Body:**
  - Card com dados do agendamento a ser excluído (cliente, serviço, data)
  - Alerta Bootstrap danger com o texto de aviso (ex.: "Esta ação é irreversível e não poderá ser desfeita.")
- **Footer:**
  - Botão "Cancelar" (`btn btn-secondary`)
  - Botão "Excluir Definitivamente" (`btn btn-danger`, ícone `bi-trash`)

---

## Tela 4 — Formulário Genérico (Layout de Formulário com Estados de Validação)

**URL de referência:** `/web/*/novo` e `/web/*/editar`

Esta tela documenta o padrão reutilizável de formulário aplicado em Agendamentos, Clientes, Profissionais e Serviços.

### Estrutura de Conteúdo

- **Page Header:** título "Novo [Entidade]" `H2` + breadcrumb (ex.: "Dashboard > Agendamentos > Novo")
- **Card de formulário:**
  - Fundo: `#ffffff`, border-radius 0.5rem, sombra padrão
  - Padding: 32px
  - Largura máxima: 800px (centralizado ou alinhado à esquerda conforme layout)

### Layout Interno do Formulário

Usa grid de 12 colunas Bootstrap para organizar campos:
- Campos de texto curto (nome, telefone): `col-md-6`
- Campos de texto longo (endereço, observações): `col-md-12`
- Campos de data e hora: `col-md-4`
- Selects (cliente, profissional, serviço): `col-md-6`

### Estados dos Campos de Input

**Estado padrão:**
- Border: `1px solid #ced4da`
- Border-radius: 0.375rem
- Height: 38px (inputs), auto (selects e textareas)
- Padding: 6px 12px
- Fundo: `#ffffff`
- Texto placeholder: `#6c757d`
- Focus: borda `#86b7fe`, sombra `0 0 0 0.25rem rgba(13,110,253,.25)`

**Estado inválido (Bean Validation error):**
- Border: `1px solid #dc3545`
- Fundo: `#ffffff` (não muda o fundo)
- Sombra no focus: `0 0 0 0.25rem rgba(220,53,69,.25)`
- Ícone de erro: `bi-exclamation-circle` à direita do campo (Bootstrap `is-invalid`)
- Mensagem de erro: texto 13px `#dc3545`, logo abaixo do campo (`invalid-feedback`)
- Exemplo de mensagens: "Campo obrigatório", "E-mail inválido", "Data deve ser futura"

**Estado válido (após correção):**
- Border: `1px solid #198754`
- Ícone de sucesso: `bi-check-circle` à direita (`is-valid`)

**Estado desabilitado (agendamento em status final):**
- Fundo: `#e9ecef`
- Border: `1px solid #ced4da`
- Cursor: `not-allowed`
- Opacidade: 0.65 para botões de submit desabilitados
- Banner/alert de aviso no topo do formulário: `alert alert-warning` com ícone e texto "Este agendamento não pode ser editado pois está [CONCLUIDO/CANCELADO]."

### Rodapé do Formulário

- Alinhamento: `d-flex justify-content-between` ou `justify-content-end`
- Botão "Cancelar": `btn btn-outline-secondary`, ícone `bi-arrow-left`
- Botão "Salvar": `btn btn-primary`, ícone `bi-check-lg`
- Ordem visual: Cancelar à esquerda, Salvar à direita
- Espaçamento acima do rodapé: border-top `1px solid #dee2e6`, padding-top 20px, margin-top 24px

---

## Tela 5 — Layout Base do Portal (Mobile-first, Conteúdo Centralizado)

**URL de referência:** `http://localhost:8081/portal`
**Finalidade no Figma:** Frame mestre do portal — todas as telas do portal herdam este layout.

### Estrutura de Layout

```
┌──────────────────────────────────┐
│   HEADER DO PORTAL (56px)        │
│   Logo + Nome do Sistema         │
├──────────────────────────────────┤
│                                  │
│   ÁREA DE CONTEÚDO               │
│   max-width: 480px               │
│   margin: 0 auto                 │
│   padding: 24px 16px             │
│   fundo: #f8f9fa                 │
│                                  │
└──────────────────────────────────┘
│   FOOTER (opcional, 48px)        │
│   texto muted centralizado       │
└──────────────────────────────────┘
```

### Componente: Header do Portal

- **Altura:** 56px
- **Fundo:** `#0d6efd` (primário — contraste com o admin branco)
- **Sombra:** `box-shadow: 0 2px 4px rgba(0,0,0,.2)`
- **Conteúdo:** Logo/ícone (`bi-calendar2-heart`, 24px, `#ffffff`) + nome do sistema `H5` `#ffffff`, centralizados verticalmente
- **Alinhamento:** `d-flex align-items-center justify-content-between`
- **Elemento à direita:** link "Meus Agendamentos" (texto `#ffffff` 14px, sublinhado no hover) — se autenticado; ou nada se não autenticado
- **Responsividade:** Mantém a mesma altura em mobile e desktop

### Área de Conteúdo do Portal

- **Largura máxima:** 480px (centralizada com `mx-auto`)
- **Padding:** 24px 16px (desktop), 16px 12px (mobile < 480px)
- **Fundo da página:** `#f8f9fa`
- **Fundo do card de conteúdo:** `#ffffff`

### Componente: Card de Conteúdo do Portal

- **Fundo:** `#ffffff`
- **Border-radius:** 0.75rem (12px — levemente maior que o admin para feel mobile)
- **Sombra:** `0 0.25rem 0.75rem rgba(0,0,0,.1)`
- **Padding:** 28px 24px (desktop), 20px 16px (mobile)
- **Margin-top:** 24px

---

## Tela 6 — Portal Home (Boas-vindas + Ações Rápidas)

**URL de referência:** `http://localhost:8081/portal` (após identificação)

### Estrutura de Conteúdo

Usa o Layout Base do Portal (Tela 5).

### Seção de Boas-vindas

- Ícone grande: `bi-calendar2-check`, 48px, `#0d6efd`, centralizado
- Título: `H3` "Olá, [Nome do Cliente]!" — bold, `#212529`, centralizado, margin-top 16px
- Subtítulo: texto 15px `#6c757d`, centralizado, "Gerencie seus agendamentos ou agende um novo serviço."
- Espaçamento abaixo: 24px

### Cards de Ações Rápidas

- **Layout:** 2 cards empilhados verticalmente (em mobile) ou side-by-side (em telas > 480px)
- **Margem entre cards:** 12px

**Card "Novo Agendamento":**
- Fundo: `#0d6efd`
- Texto e ícone: `#ffffff`
- Ícone: `bi-plus-circle`, 28px
- Título: "Agendar Serviço", 16px bold
- Subtítulo: "Escolha serviço, data e horário", 13px, `rgba(255,255,255,.8)`
- Border-radius: 0.75rem
- Padding: 20px
- Hover: fundo `#0b5ed7` (darker), cursor pointer, transição 150ms
- Toda a área é clicável

**Card "Meus Agendamentos":**
- Fundo: `#ffffff`
- Borda: `1px solid #dee2e6`
- Ícone: `bi-list-check`, 28px, `#0d6efd`
- Título: "Meus Agendamentos", 16px bold, `#212529`
- Subtítulo: "Veja e acompanhe seus agendamentos", 13px, `#6c757d`
- Border-radius: 0.75rem
- Padding: 20px
- Hover: sombra aumentada, cursor pointer

---

## Tela 7 — Portal Wizard Passo 1 — Seleção de Serviço (Radio Button Cards)

**URL de referência:** `/portal/agendar/passo1` (ou equivalente)

### Barra de Progresso do Wizard

- **Posição:** topo do card de conteúdo, antes do título do passo
- **Altura:** 6px
- **Fundo da barra completa:** `#e9ecef`
- **Barra de progresso (Passo 1 = 25%):** largura 25%, fundo `#0d6efd`
- **Indicador textual:** "Passo 1 de 4" — texto 12px `#6c757d`, alinhado à direita
- **Border-radius da barra:** 3px

### Cabeçalho do Passo

- Ícone: `bi-clipboard2-pulse`, 28px, `#0d6efd`
- Título: "Selecione o Serviço" `H4` bold `#212529`
- Subtítulo: texto 14px `#6c757d`, "Escolha o serviço que deseja agendar."
- Espaçamento abaixo: 20px

### Grade de Radio Button Cards

- **Layout:** Grid de 2 colunas em mobile (< 480px: 1 coluna), 2 colunas em telas maiores
- **Gap:** 12px
- **Cada card de serviço:**

**Estado não selecionado:**
- Fundo: `#ffffff`
- Borda: `1.5px solid #dee2e6`
- Border-radius: 0.5rem
- Padding: 16px
- Cursor: pointer
- Hover: borda `#0d6efd`, sombra `0 0 0 3px rgba(13,110,253,.15)`, transição 150ms

**Estado selecionado:**
- Fundo: `#e8f0fe` (azul muito claro)
- Borda: `2px solid #0d6efd`
- Sombra: `0 0 0 3px rgba(13,110,253,.15)`
- Ícone de check: `bi-check-circle-fill` `#0d6efd` no canto superior direito

**Conteúdo interno do card:**
- Ícone do serviço: `bi-scissors` (ou similar), 24px, `#0d6efd`, no topo
- Nome do serviço: 15px bold `#212529`, margin-top 10px
- Descrição breve: 13px `#6c757d`, 2 linhas máximo, overflow ellipsis
- Preço: 16px bold `#198754`, margin-top auto (empurrado para o rodapé do card)
- Duração: 12px `#6c757d`, ícone `bi-clock`, ao lado do preço

**Input radio:**
- Oculto visualmente (`display: none`) — o clique no card ativa o radio
- Acessibilidade: usar `<label>` englobando o card inteiro

### Rodapé do Wizard (navegação)

- **Posição:** abaixo da grade de cards, padding-top 20px, border-top `1px solid #dee2e6`
- **Layout:** `d-flex justify-content-between`
- **Botão "Anterior":** `btn btn-outline-secondary`, ícone `bi-arrow-left` — desabilitado no Passo 1 (opacity 0.5, cursor not-allowed)
- **Botão "Próximo":** `btn btn-primary`, ícone `bi-arrow-right` (à direita do texto) — desabilitado se nenhum card estiver selecionado

---

## Tela 8 — Portal Wizard Passo 2 — Seleção de Data e Grade de Horários

**URL de referência:** `/portal/agendar/passo2` (ou equivalente)

### Barra de Progresso

- Largura da barra preenchida: 50% (Passo 2 de 4), fundo `#0d6efd`

### Seção de Seleção de Data

- **Título da seção:** "Escolha a Data" `H5` bold `#212529`
- **Componente:** `<input type="date">` estilizado, largura 100%, height 44px
- **Restrições visuais:** datas passadas devem aparecer desabilitadas (via atributo `min` do input)
- **Fundo do input:** `#ffffff`, borda `#ced4da`, border-radius 0.375rem, padding 8px 12px
- **Ícone de calendário:** `bi-calendar3` à esquerda do input (ou nativo do browser)
- **Espaçamento abaixo:** 20px

### Seção de Grade de Horários

- **Título da seção:** "Horários Disponíveis" `H5` bold `#212529` + subtítulo 13px `#6c757d` "Selecione um horário para [ServiçoSelecionado]"
- **Layout da grade:** flexbox com wrap, gap 8px

**Botão de horário — Estado disponível:**
- Dimensão: 76px × 40px
- Fundo: `#ffffff`
- Borda: `1px solid #dee2e6`
- Texto: 14px `#212529`, centralizado
- Border-radius: 0.375rem
- Hover: borda `#0d6efd`, fundo `#e8f0fe`

**Botão de horário — Estado selecionado:**
- Fundo: `#0d6efd`
- Texto: `#ffffff`
- Borda: `#0d6efd`
- Sem hover (já está no estado máximo)

**Botão de horário — Estado indisponível:**
- Fundo: `#e9ecef`
- Texto: `#adb5bd`
- Borda: `#dee2e6`
- Cursor: `not-allowed`
- Risco visual sobre o texto (opcional): `text-decoration: line-through`

**Mensagem quando nenhum horário disponível:**
- Alert Bootstrap `alert-info` com ícone `bi-info-circle` e texto "Nenhum horário disponível para esta data. Tente outro dia."

### Rodapé do Wizard

- Botão "Anterior": habilitado (volta ao Passo 1)
- Botão "Próximo": habilitado apenas quando data E horário estiverem selecionados

---

## Tela 9 — Portal Wizard Passo 3 — Revisão do Resumo

**URL de referência:** `/portal/agendar/passo3` (ou equivalente)

### Barra de Progresso

- Largura da barra preenchida: 75% (Passo 3 de 4)

### Card de Resumo

**Estrutura do card:**
- Fundo: `#ffffff`
- Borda: `1.5px solid #dee2e6`
- Border-radius: 0.75rem
- Padding: 24px

**Header do card de resumo:**
- Ícone: `bi-receipt`, 28px, `#0d6efd`
- Título: "Confirme seu Agendamento" `H4` bold `#212529`
- Separador: `1px solid #dee2e6`, margin 16px 0

**Linhas de informação (cada item):**
- Layout: `d-flex justify-content-between align-items-center`
- Altura: 44px
- Borda inferior: `1px solid #f1f3f5`
- Label: 14px `#6c757d` font-weight 500
- Valor: 14px `#212529` font-weight 600 (alinhado à direita)

Campos exibidos:
1. Serviço: nome do serviço selecionado
2. Profissional: nome do profissional (se selecionável)
3. Data: data formatada (ex.: "Segunda, 26 de Maio de 2026")
4. Horário: horário formatado (ex.: "14:00")
5. Duração: duração do serviço (ex.: "60 minutos")
6. Valor: preço formatado em R$ (ex.: "R$ 80,00") — bold, `#198754`

**Separador antes do total:**
- Linha `1px solid #dee2e6`

**Linha de total:**
- Label: "Total" 16px bold `#212529`
- Valor: 20px bold `#198754`

**Alerta de confirmação:**
- Logo abaixo do card de resumo
- `alert alert-info` com ícone `bi-info-circle`
- Texto: "Ao confirmar, você concorda com os termos de agendamento."

### Rodapé do Wizard

- Botão "Anterior": habilitado
- Botão "Confirmar Agendamento": `btn btn-success btn-lg`, ícone `bi-check-lg`, width 100% em mobile

---

## Tela 10 — Portal Wizard Passo 4 — Estado de Sucesso

**URL de referência:** `/portal/agendar/sucesso` (ou `/passo4`)

### Barra de Progresso

- Largura da barra preenchida: 100%, fundo `#198754` (verde — indica conclusão)

### Card de Sucesso

**Container central:**
- Largura máxima: 420px, centralizado
- Padding: 40px 24px (generoso para dar respiro visual)
- Fundo: `#ffffff`
- Border-radius: 0.75rem
- Sombra: `0 0.5rem 1.5rem rgba(0,0,0,.1)`

**Ícone de sucesso:**
- Ícone: `bi-check-circle-fill`, 64px, `#198754`
- Animação: `scale-in` (0 → 1) 300ms ease-out — definir no Figma como Smart Animate
- Centralizado horizontalmente

**Textos:**
- Título: "Agendamento Confirmado!" `H3` bold `#212529`, margin-top 20px, centralizado
- Subtítulo: "Seu agendamento foi realizado com sucesso.", 15px `#6c757d`, centralizado

**Número de Protocolo:**
- Fundo: `#e8f0fe` (azul claro)
- Border-radius: 0.5rem
- Padding: 12px 20px
- Texto de label: "Protocolo" 13px `#6c757d` uppercase letter-spacing 1px
- Número: 22px bold `#0d6efd` (ex.: "#00042")
- Margin: 20px 0
- Border-left: `4px solid #0d6efd`

**Resumo compacto abaixo do protocolo:**
- Linhas simples: ícone 16px `#6c757d` + texto 14px `#212529`
- Serviço, data, horário — 3 linhas

**Botões de ação pós-confirmação:**
- Botão primário "Novo Agendamento": `btn btn-primary`, largura 100%
- Botão secundário "Meus Agendamentos": `btn btn-outline-secondary`, largura 100%, margin-top 8px

---

## Tela 11 — Página de Erro (Template Genérico)

**URL de referência:** Qualquer URL inválida (404), acesso negado (403), erro interno (500)

### Estrutura de Layout

- **Fundo:** `#f8f9fa` (tela inteira)
- **Conteúdo:** centralizado vertical e horizontalmente (`d-flex flex-column align-items-center justify-content-center`, min-height 100vh)
- **Largura máxima do conteúdo:** 480px

### Conteúdo da Página de Erro

**Código de erro:**
- Tamanho: 120px (6rem) — impactante e visível
- Fonte: system-ui, weight 900 (ou 800)
- Cor por tipo:
  - 404: `#6c757d` (cinza — "não encontrado, mas sem urgência")
  - 403: `#ffc107` (amarelo — "atenção, acesso negado")
  - 500: `#dc3545` (vermelho — "erro crítico")

**Ícone:**
- Bootstrap Icons, 56px
- 404: `bi-search` ou `bi-map`
- 403: `bi-shield-exclamation`
- 500: `bi-exclamation-octagon`
- Cor: mesma cor do código de erro
- Margin-bottom: 16px
- Exibido acima do código de erro

**Título:**
- `H2` bold `#212529`, 24px
- 404: "Página não encontrada"
- 403: "Acesso negado"
- 500: "Erro interno do servidor"

**Descrição:**
- Texto 15px `#6c757d`, centralizado, max-width 360px
- 404: "A página que você está procurando não existe ou foi movida."
- 403: "Você não tem permissão para acessar este recurso."
- 500: "Ocorreu um erro inesperado. Nossa equipe foi notificada."

**Divider:**
- Linha horizontal `1px solid #dee2e6`, largura 80px, margin 24px auto

**Botão CTA (Call-to-Action):**
- 404: "Voltar ao Dashboard" — `btn btn-primary`
- 403: "Ir para o Login" — `btn btn-warning text-dark`
- 500: "Tentar Novamente" — `btn btn-danger`
- Ícone à esquerda: `bi-house` (404), `bi-box-arrow-in-right` (403), `bi-arrow-clockwise` (500)
- Padding: 10px 28px

**Link secundário (opcional):**
- Texto 13px `#6c757d`, abaixo do botão, margin-top 12px
- "Precisa de ajuda? Entre em contato com o suporte."

### Variações de Tema por Código de Erro

| Elemento | Erro 404 | Erro 403 | Erro 500 |
|---|---|---|---|
| Código de erro (cor) | `#6c757d` | `#ffc107` | `#dc3545` |
| Ícone | `bi-search` | `bi-shield-exclamation` | `bi-exclamation-octagon` |
| Botão CTA | `btn-primary` | `btn-warning text-dark` | `btn-danger` |
| Tom geral | Neutro/informativo | Alerta | Crítico |

---

## Guia de Componentização no Figma

Para facilitar a montagem do protótipo, recomenda-se criar os seguintes componentes reutilizáveis:

### Componentes Primitivos

1. **Button/Primary** — `btn btn-primary` com estados: Default, Hover, Disabled
2. **Button/Secondary** — `btn btn-secondary` com estados: Default, Hover, Disabled
3. **Button/Outline-Danger** — `btn btn-outline-danger` com estados
4. **Badge/Status** — 4 variantes: AGENDADO, CONFIRMADO, CONCLUIDO, CANCELADO
5. **Input/Default** — campo de texto com estados: Default, Focus, Invalid, Valid, Disabled
6. **Select/Default** — dropdown com estados: Default, Focus, Disabled

### Componentes Compostos

7. **Card/Metrica** — card de métrica do dashboard com ícone, número e label
8. **Card/Servico** — radio card de seleção de serviço com estados: Default, Hover, Selected
9. **Slot/Horario** — botão de horário com estados: Disponivel, Selecionado, Indisponivel
10. **Modal/ConfirmacaoExclusao** — modal de exclusão completo
11. **Modal/MudancaStatus** — modal de troca de status completo
12. **Sidebar/Item** — item de menu com ícone e estados: Default, Hover, Active
13. **WizardProgress** — barra de progresso com variante por passo (25/50/75/100%)
14. **TableRow/Agendamento** — linha de tabela com todos os dados e botões de ação

### Tokens de Design (Figma Styles)

Criar as seguintes variáveis de cor no Figma como **Color Styles**:

- `Color/Primary` → `#0d6efd`
- `Color/Success` → `#198754`
- `Color/Warning` → `#ffc107`
- `Color/Danger` → `#dc3545`
- `Color/Muted` → `#6c757d`
- `Color/Light` → `#f8f9fa`
- `Color/Dark` → `#212529`
- `Color/White` → `#ffffff`
- `Color/Border` → `#dee2e6`
- `Color/Sidebar-Bg` → `#212529`
- `Color/Badge-Agendado-Bg` → `#fff3cd`
- `Color/Badge-Agendado-Text` → `#664d03`
- `Color/Badge-Confirmado-Bg` → `#d1e7dd`
- `Color/Badge-Confirmado-Text` → `#0a3622`
- `Color/Badge-Concluido-Bg` → `#e9ecef`
- `Color/Badge-Concluido-Text` → `#495057`
- `Color/Badge-Cancelado-Bg` → `#f8d7da`
- `Color/Badge-Cancelado-Text` → `#58151c`

Criar os seguintes **Text Styles**:

- `Text/Heading-H1` → system-ui, Bold 700, 32px
- `Text/Heading-H2` → system-ui, Bold 700, 24px
- `Text/Heading-H3` → system-ui, SemiBold 600, 20px
- `Text/Heading-H4` → system-ui, SemiBold 600, 16px
- `Text/Body-Base` → system-ui, Regular 400, 16px
- `Text/Body-Small` → system-ui, Regular 400, 14px
- `Text/Caption` → system-ui, Regular 400, 12px
- `Text/Label` → system-ui, Medium 500, 14px
- `Text/Badge` → system-ui, SemiBold 600, 12px

Criar os seguintes **Effect Styles**:

- `Shadow/Card-Default` → `box-shadow: 0 2px 4px rgba(0,0,0,.075)`
- `Shadow/Card-Elevated` → `box-shadow: 0 8px 16px rgba(0,0,0,.15)`
- `Shadow/Modal` → `box-shadow: 0 16px 48px rgba(0,0,0,.25)`

---

## Fluxo de Navegação (Figma Prototype Connections)

### Fluxo Administrativo

```
Login ──► Dashboard
Dashboard ──► Listagem de Agendamentos
Dashboard ──► Listagem de Clientes
Dashboard ──► Listagem de Profissionais
Dashboard ──► Listagem de Serviços
Listagem Agendamentos ──► Formulário Novo Agendamento
Listagem Agendamentos ──► Formulário Editar Agendamento
Listagem Agendamentos ──[clique "Status"]──► Modal Mudança de Status (overlay)
Listagem Agendamentos ──[clique "Excluir"]──► Modal Confirmação Exclusão (overlay)
Formulário ──[Salvar com erro]──► Formulário (mesmo, com erros visíveis)
Formulário ──[Salvar com sucesso]──► Listagem + toast de sucesso
```

### Fluxo do Portal

```
Portal Home (não autenticado) ──► Página de Identificação
Página de Identificação ──[sucesso]──► Portal Home (autenticado)
Portal Home ──[Agendar]──► Wizard Passo 1
Wizard Passo 1 ──[Próximo]──► Wizard Passo 2
Wizard Passo 2 ──[Próximo]──► Wizard Passo 3
Wizard Passo 3 ──[Confirmar]──► Wizard Passo 4 (Sucesso)
Wizard Passo 4 ──[Novo Agendamento]──► Wizard Passo 1
Wizard Passo 4 ──[Meus Agendamentos]──► Lista de agendamentos do portal
Qualquer passo ──[Anterior]──► Passo anterior
```

### Conexões de Erro

```
Qualquer URL inválida ──► Página de Erro 404
Acesso sem permissão ──► Página de Erro 403
Erro do servidor ──► Página de Erro 500
Página de Erro 404 ──[Botão]──► Dashboard (admin) ou Portal Home (portal)
```
