# Checklist de Testes de Interface — Agendamento API
## TCC UNDB — Guilherme Braga

---

## 1. Login Administrativo (`/web/login`)

- [ ] Tela carrega com fundo escuro gradiente e card centralizado
- [ ] Campo "Usuário" exibe ícone de pessoa à esquerda
- [ ] Campo "Senha" exibe ícone de cadeado à esquerda
- [ ] Botão olho (toggle) alterna senha entre oculto (`••••`) e visível
- [ ] Submit com credenciais corretas redireciona para `/dashboard`
- [ ] Submit com credenciais erradas exibe alerta vermelho "Usuário ou senha incorretos"
- [ ] Logout via `/web/logout` exibe alerta verde "Sessão encerrada"
- [ ] Link "Ir para o Portal do Cliente" navega para `/portal`
- [ ] Layout responsivo em 375 px (mobile) — card sem overflow horizontal

---

## 2. Navbar Administrativa (todas as páginas admin)

- [ ] Logo com ícone de calendário aparece no topo esquerdo
- [ ] 5 itens de menu: Dashboard, Clientes, Profissionais, Serviços, Agendamentos
- [ ] Cada item de menu possui ícone Bootstrap Icons à esquerda
- [ ] Botões "Portal" e "Sair" aparecem à direita
- [ ] Em tela ≤ 991 px, menu colapsa em botão hamburguer
- [ ] Menu hamburguer abre/fecha corretamente
- [ ] Item ativo é destacado via JavaScript (classe `active`) na página atual
- [ ] Alertas de sucesso e erro aparecem com ícone e botão de fechar (✕)

---

## 3. Dashboard (`/dashboard`)

- [ ] 4 cards de métricas com gradiente e ícones aparecem na linha superior
- [ ] Cada card exibe valor numérico e rótulo corretos (dados do banco)
- [ ] Hover nos cards eleva visualmente (transform translateY)
- [ ] Gráfico doughnut renderiza ao carregar a página
- [ ] Gráfico exibe 4 fatias: AGENDADO (azul #667eea), CONFIRMADO (verde #28a745), CONCLUIDO (cinza), CANCELADO (vermelho)
- [ ] Badges abaixo do gráfico exibem contagem de cada status
- [ ] Tabela de próximos agendamentos exibe no máximo 3 registros
- [ ] Tabela de próximos agendamentos mostra badges de status coloridos
- [ ] Estado vazio (sem agendamentos futuros) exibe ícone e mensagem
- [ ] 3 cards de acesso rápido (Clientes, Profissionais, Serviços) com contadores
- [ ] Botões "Gerenciar" e "Novo" dos cards de acesso rápido navegam corretamente

---

## 4. Listagem de Clientes (`/web/clientes`)

- [ ] Cabeçalho exibe contagem de clientes e botão "Novo Cliente"
- [ ] Barra de busca filtra a tabela em tempo real (nome, e-mail, telefone)
- [ ] Tabela com colunas: #, Nome, E-mail, Telefone, Ações
- [ ] Cada linha exibe botões ícone: Ver (azul), Editar (amarelo), Deletar (vermelho)
- [ ] Botão Deletar abre modal de confirmação centralizado
- [ ] Modal exibe nome do cliente e alerta de cascata de dados
- [ ] Confirmar exclusão no modal remove o cliente e retorna com mensagem de sucesso
- [ ] Estado vazio exibe ícone e link para cadastrar primeiro cliente
- [ ] Mensagem "Nenhum resultado" aparece ao buscar texto sem correspondência

---

## 5. Formulário de Cliente (`/web/clientes/novo` e `/{id}/editar`)

- [ ] Título do cabeçalho é "Novo Cliente" ou "Editar Cliente" conforme o caso
- [ ] Botão voltar (←) retorna à listagem sem salvar
- [ ] Campos: Nome, E-mail, Telefone, CPF — todos obrigatórios
- [ ] Validação HTML5 impede submit com campos inválidos
- [ ] Erro de validação do servidor exibe mensagem abaixo do campo
- [ ] Botão de submit usa texto "Cadastrar" (novo) ou "Salvar Alterações" (edição)
- [ ] Após salvar, redireciona para listagem com mensagem de sucesso

---

## 6. Listagem de Profissionais (`/web/profissionais`)

- [ ] Cabeçalho exibe contagem e botão "Novo Profissional"
- [ ] Barra de busca filtra em tempo real
- [ ] Tabela com colunas: #, Nome, Especialidade (badge roxo), E-mail, Telefone, Ações
- [ ] Badge de especialidade exibe cor roxa (#e0e7ff / #3730a3)
- [ ] Botão Editar (ícone lápis) abre formulário de edição
- [ ] Botão Remover (ícone lixo) abre modal de confirmação
- [ ] Modal exibe nome do profissional
- [ ] Confirmar remove e retorna com mensagem de sucesso
- [ ] Estado vazio exibe ícone e CTA de cadastro

---

## 7. Listagem de Serviços (`/web/servicos`)

- [ ] Cabeçalho exibe contagem e botão "Novo Serviço"
- [ ] Barra de busca filtra em tempo real
- [ ] Tabela com colunas: #, Nome, Descrição, Duração (badge azul), Preço (alinhado à direita), Ações
- [ ] Badge de duração exibe ícone de relógio e valor em minutos
- [ ] Preço exibido com formato `R$ X,XX`
- [ ] Botão Editar e Remover funcionam (modal de confirmação)
- [ ] Estado vazio com CTA

---

## 8. Listagem de Agendamentos (`/web/agendamentos/listar`)

- [ ] Filtro por Status (select), De (data), Até (data) — botões Filtrar e Limpar
- [ ] Tabela com colunas: #, Cliente, Profissional (+ especialidade), Serviço, Data/Hora, Status, Ações
- [ ] Badge de status com cor semântica por estado
- [ ] Botão Editar (ícone lápis) aparece apenas para AGENDADO e CONFIRMADO
- [ ] Botão Alterar Status (ícone setas) aparece para AGENDADO e CONFIRMADO
- [ ] Modal de status exibe status atual (badge), select com transições válidas
- [ ] Select CANCELADO exibe aviso de ação irreversível
- [ ] Botão Deletar aparece para todos; modal exibe dados do agendamento
- [ ] Estado vazio com ícone de calendário e mensagem

---

## 9. Formulário de Agendamento (`/web/agendamentos/novo`)

- [ ] Selects: Cliente, Profissional, Serviço preenchidos com dados do banco
- [ ] Ao selecionar serviço, aparecem badges com duração e preço
- [ ] Campo Data e Hora (datetime-local) obrigatório
- [ ] Botões: Cancelar (outline) e Criar Agendamento (verde)
- [ ] Após criar, redireciona para listagem com mensagem de sucesso

---

## 10. Portal — Home (`/portal`)

- [ ] Hero card com gradiente roxo e botões "Ver Serviços" / "Agendar Agora"
- [ ] Ícone decorativo visível em telas md+
- [ ] 3 stat cards com ícones coloridos, valores numéricos e rótulos
- [ ] Navbar com gradiente, logo, links e (se autenticado) nome do cliente + Sair

---

## 11. Portal — Wizard de Agendamento (Etapas 1–4)

### Etapa 1 (`/portal/step1`)
- [ ] Indicador de progresso: Etapa 1 ATIVA (azul), 2-4 inativas
- [ ] Grid de cards de serviço responsivo
- [ ] Clicar no card seleciona (borda azul, fundo lilás) e desmarca o anterior
- [ ] Tentar avançar sem seleção exibe erro "Selecione um serviço"
- [ ] Avançar com serviço selecionado vai para Etapa 2

### Etapa 2 (`/portal/step2`)
- [ ] Etapa 1 marcada com ✓ verde; Etapa 2 ATIVA
- [ ] Badge com nome/duração/preço do serviço selecionado
- [ ] Select de profissional populado
- [ ] Campo de data com mínimo = hoje
- [ ] Botões de horário gerados (08:00 a 17:30, intervalo 30min)
- [ ] Clicar no horário seleciona (azul) e desmarca o anterior
- [ ] Tentar avançar sem horário exibe erro
- [ ] Avançar leva à Etapa 3

### Etapa 3 (`/portal/step3`)
- [ ] Etapas 1 e 2 com ✓; Etapa 3 ATIVA
- [ ] Resumo exibe: Serviço, Duração, Valor, Profissional, Data, Horário
- [ ] Alerta informativo sobre status inicial AGENDADO
- [ ] Botão "Recomeçar" volta para Etapa 1 (sem perder dados do wizard)
- [ ] Confirmar cria o agendamento e vai para Etapa 4

### Etapa 4 (`/portal/step4`)
- [ ] Todas as etapas com ✓
- [ ] Ícone verde de check-mark exibido
- [ ] Título "Agendamento Confirmado!" em verde
- [ ] Número de protocolo (#ID) exibido
- [ ] Resumo final completo
- [ ] Botões: "Meus Agendamentos" e "Novo Agendamento"

---

## 12. Responsividade Geral

- [ ] Todas as páginas admin navegáveis em viewport 375 px (mobile)
- [ ] Todas as páginas do portal navegáveis em viewport 375 px
- [ ] Nenhum overflow horizontal em mobile
- [ ] Botões de ação em tabelas não transbordam a célula em mobile

---

## 13. Testes de Regressão

- [ ] Build Maven compila sem erros (`mvn compile`)
- [ ] `mvn test` passa sem falhas
- [ ] Fluxo completo admin: login → dashboard → criar cliente → criar profissional → criar serviço → criar agendamento → alterar status → excluir
- [ ] Fluxo completo portal: home → wizard step1 → step2 → step3 → confirmar → step4 → meus agendamentos

---

## Lista de Capturas de Tela Recomendadas para o TCC

| # | Arquivo sugerido | Tela |
|---|---|---|
| 1 | `tela-login.png` | Login com card e botão toggle de senha |
| 2 | `admin-navbar.png` | Navbar admin com ícones (desktop) |
| 3 | `admin-navbar-mobile.png` | Navbar admin colapsada (mobile) |
| 4 | `dashboard.png` | Dashboard completo (métricas + gráfico + tabela + acesso rápido) |
| 5 | `dashboard-chart.png` | Gráfico doughnut isolado |
| 6 | `clientes-lista.png` | Listagem de clientes com busca |
| 7 | `clientes-modal-delete.png` | Modal de confirmação de exclusão |
| 8 | `form-cliente.png` | Formulário de cadastro de cliente |
| 9 | `profissionais-lista.png` | Listagem de profissionais com badges |
| 10 | `servicos-lista.png` | Listagem de serviços com preço formatado |
| 11 | `agendamentos-lista.png` | Listagem de agendamentos com status coloridos |
| 12 | `agendamentos-modal-status.png` | Modal de alteração de status |
| 13 | `form-agendamento.png` | Formulário de agendamento com info dinâmica |
| 14 | `portal-home.png` | Home do portal com hero gradiente |
| 15 | `portal-navbar.png` | Navbar do portal com gradiente |
| 16 | `wizard-step1.png` | Etapa 1: seleção de serviço com card selecionado |
| 17 | `wizard-step2.png` | Etapa 2: seleção de data, profissional e horário |
| 18 | `wizard-step3.png` | Etapa 3: confirmação com resumo |
| 19 | `wizard-step4.png` | Etapa 4: sucesso com protocolo |
| 20 | `portal-meus-agendamentos.png` | Listagem de agendamentos do cliente |
