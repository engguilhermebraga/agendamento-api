# Manual de Capturas de Tela para o TCC
**Sistema de Agendamento — Spring Boot 4.0.5**
**Base URL:** `http://localhost:8081`

---

## Instruções Gerais

Antes de iniciar as capturas, certifique-se de que:
- O sistema está em execução (`mvn spring-boot:run` ou via IDE)
- O banco H2 contém dados de demonstração (pelo menos 3 clientes, 3 profissionais, 3 serviços e 6 agendamentos nos estados AGENDADO, CONFIRMADO, CONCLUIDO e CANCELADO)
- O navegador está em inglês ou português para evitar artefatos de idioma nas capturas
- As ferramentas de desenvolvedor do navegador estão fechadas
- A barra de favoritos do navegador está oculta (para maximizar a área útil)

---

## Screenshot 1 — Dashboard Principal com Métricas e Gráfico

- **URL:** `http://localhost:8081/dashboard`
- **Pré-requisito:** Estar autenticado como ADMIN. Ter pelo menos 6 agendamentos distribuídos entre os quatro status para que o gráfico doughnut do Chart.js exiba todas as fatias. A tabela de "Próximos Agendamentos" deve ter ao menos 3 registros com datas futuras.
- **O que mostrar:** Os quatro cards de métricas no topo (Total de Agendamentos, Agendamentos Hoje, Confirmados, Cancelados); o gráfico doughnut centralizado com a legenda de cores; a tabela de próximos agendamentos com as colunas de data, cliente e status badge; o menu lateral (sidebar) totalmente visível com todos os itens de navegação.
- **Capturar:** Resolução 1440 × 900 px, zoom do navegador em 100%. Captura de tela completa (full-page screenshot) para incluir sidebar + conteúdo principal.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Apresentação do Sistema (subseção Painel Administrativo). Usar como primeira figura da seção de resultados para contextualizar o layout geral da interface administrativa.
- **Nota:** Adicionar anotações com setas numeradas apontando para: (1) cards de métricas, (2) gráfico Chart.js, (3) tabela de próximos agendamentos, (4) menu de navegação lateral. Usar uma ferramenta de anotação de imagem (ver seção "Ferramentas recomendadas") para inserir as setas em vermelho sobre fundo branco.

---

## Screenshot 2 — Listagem de Agendamentos com Badges de Status Coloridos

- **URL:** `http://localhost:8081/web/agendamentos/listar`
- **Pré-requisito:** Ter agendamentos em todos os quatro status na base de dados (AGENDADO, CONFIRMADO, CONCLUIDO, CANCELADO). Não aplicar nenhum filtro — exibir a lista completa sem paginação forçada.
- **O que mostrar:** A tabela completa com colunas (ID, Cliente, Profissional, Serviço, Data/Hora, Status, Ações); os quatro tipos de badge coloridos visíveis simultaneamente na coluna Status; os botões de ação (Detalhes, Editar, Mudar Status, Excluir) na coluna Ações.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Caso a tabela tenha mais de 8 registros, use a rolagem para exibir os primeiros registros que cobrem todos os quatro status. Captura de tela completa da área de conteúdo.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Gestão de Agendamentos. Citar na discussão sobre o sistema de codificação visual por cores para representar estados do workflow.
- **Nota:** Adicionar um retângulo destacando apenas a coluna Status para chamar a atenção para a variação de cores dos badges. Considerar inserir uma legenda abaixo da imagem explicando cada cor no próprio texto do TCC.

---

## Screenshot 3 — Listagem de Agendamentos com Filtro Aplicado

- **URL:** `http://localhost:8081/web/agendamentos/listar?status=CONFIRMADO` (ou equivalente conforme parâmetro da aplicação)
- **Pré-requisito:** Ter ao menos 3 agendamentos no status CONFIRMADO. Aplicar o filtro pelo campo de seleção de status disponível no topo da listagem ou via parâmetro de URL.
- **O que mostrar:** O campo de filtro preenchido com o valor selecionado (ex.: "CONFIRMADO"); a tabela exibindo somente registros com o badge verde CONFIRMADO; a ausência dos demais status na listagem (evidenciando que o filtro está ativo).
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Captura apenas da área de conteúdo (sem necessidade de full-page).
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Gestão de Agendamentos. Demonstrar o recurso de filtragem dinâmica da listagem.
- **Nota:** Usar uma seta ou círculo para destacar o campo de filtro preenchido e o resultado filtrado na tabela. Mencionar no texto do TCC que o filtro é processado server-side pelo Spring MVC.

---

## Screenshot 4 — Modal de Mudança de Status (Aberto)

- **URL:** `http://localhost:8081/web/agendamentos/listar` — clicar no botão "Mudar Status" de um agendamento com status AGENDADO.
- **Pré-requisito:** Ter um agendamento no status AGENDADO visível na listagem. O modal deve exibir as transições válidas disponíveis (ex.: AGENDADO → CONFIRMADO, AGENDADO → CANCELADO).
- **O que mostrar:** O modal Bootstrap aberto sobre a listagem (fundo com overlay escurecido); o nome do cliente e o status atual exibidos no modal; as opções de status disponíveis para transição (select ou radio buttons dentro do modal); os botões de confirmação e cancelamento.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Captura da tela inteira para mostrar o overlay do modal sobre a listagem ao fundo.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Workflow de Status. Demonstrar a máquina de estados implementada e a interface de transição de status.
- **Nota:** Adicionar uma caixa de texto na imagem explicando "Transições válidas conforme regra de negócio". Se a aplicação não exibir estados inválidos (os oculta ou desabilita), destacar isso como feature de UX na legenda.

---

## Screenshot 5 — Modal de Confirmação de Exclusão

- **URL:** `http://localhost:8081/web/agendamentos/listar` — clicar no botão "Excluir" de qualquer agendamento.
- **Pré-requisito:** Qualquer agendamento visível na listagem. O modal deve aparecer com dados do agendamento e a mensagem de aviso sobre a irreversibilidade da ação.
- **O que mostrar:** O modal Bootstrap de confirmação com: nome do cliente, data e hora do agendamento, texto de aviso (ex.: "Esta ação não pode ser desfeita"); o botão "Cancelar" (secondary) e o botão "Confirmar Exclusão" (danger/vermelho).
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Tela inteira com overlay visível.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Medidas de Segurança na Interface. Ilustrar o padrão de double-confirmation para operações destrutivas.
- **Nota:** Destacar o botão vermelho de confirmação com uma seta e adicionar a nota "Ação irreversível — dupla confirmação obrigatória".

---

## Screenshot 6 — Formulário Novo Agendamento (Estado Limpo)

- **URL:** `http://localhost:8081/web/agendamentos/novo`
- **Pré-requisito:** Estar autenticado como ADMIN. O formulário deve estar completamente vazio, sem nenhum dado preenchido.
- **O que mostrar:** Todos os campos do formulário visíveis (select de Cliente, select de Profissional, select de Serviço, campos de Data e Hora); os labels dos campos; o botão de submit "Salvar" e o botão "Cancelar"; o título da página no topo.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Full-page se necessário para mostrar o formulário completo.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Cadastro de Agendamentos. Mostrar o formulário de criação como parte do fluxo administrativo.
- **Nota:** Nenhuma anotação especial necessária. A imagem serve como referência clean do layout do formulário.

---

## Screenshot 7 — Formulário Novo Agendamento com Erro de Conflito

- **URL:** `http://localhost:8081/web/agendamentos/novo` — após submeter um formulário com data/hora já ocupada pelo mesmo profissional.
- **Pré-requisito:** Criar previamente um agendamento para o Profissional X às 14h00 de determinada data. Em seguida, tentar criar um novo agendamento para o mesmo Profissional X na mesma data e horário.
- **O que mostrar:** O formulário reexibido com os campos preenchidos; a mensagem de erro de conflito em destaque (ex.: alerta Bootstrap danger ou campo com borda vermelha); o campo de data/hora evidenciando o problema; os dados previamente digitados mantidos no formulário (comportamento de retenção de dados após erro).
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Garantir que a mensagem de erro esteja completamente visível na captura.
- **Onde usar no TCC:** Capítulo 4, Seção 4.3 — Validação de Conflitos de Horário. Esta é uma das capturas mais importantes do TCC, pois demonstra a regra de negócio central do sistema.
- **Nota:** Usar seta vermelha apontando para a mensagem de erro. Adicionar caixa de texto "Regra de Negócio: Conflito detectado pelo AgendamentoService.criar()". Esta imagem deve ser referenciada junto ao trecho de código da lógica de conflito (Screenshot 22).

---

## Screenshot 8 — Editar Agendamento — Estado Bloqueado (CONCLUIDO/CANCELADO)

- **URL:** `http://localhost:8081/web/agendamentos/{id}/editar` — usando o ID de um agendamento com status CONCLUIDO ou CANCELADO.
- **Pré-requisito:** Ter ao menos um agendamento nos status CONCLUIDO ou CANCELADO. Navegar para a página de edição desse agendamento.
- **O que mostrar:** O formulário de edição com os campos desabilitados (atributo `disabled` ou `readonly` visível no estilo visual); o badge de status (CONCLUIDO em cinza ou CANCELADO em vermelho) indicando o estado final; a mensagem ou indicação visual de que o agendamento não pode ser editado; os botões de ação disponíveis (apenas "Voltar", sem "Salvar").
- **Capturar:** Resolução 1440 × 900 px, zoom 100%.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Controles de Integridade. Demonstrar que o sistema impede a edição de registros em estados finais do workflow.
- **Nota:** Adicionar seta apontando para os campos desabilitados e outra para o badge de status final. Explicar na legenda que isso é uma restrição implementada no controller/service.

---

## Screenshot 9 — Listagem de Clientes

- **URL:** `http://localhost:8081/web/clientes`
- **Pré-requisito:** Ter ao menos 5 clientes cadastrados com nome, e-mail e telefone preenchidos.
- **O que mostrar:** A tabela de clientes com colunas (ID, Nome, E-mail, Telefone, Ações); os botões de ação por linha (Ver Detalhes, Editar, Excluir); o botão "Novo Cliente" no topo da página; o título da seção.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Módulo de Clientes. Demonstrar o CRUD de clientes da interface administrativa.
- **Nota:** Nenhuma anotação especial. Garantir que pelo menos dois números de telefone estejam com formatação brasileira visível para contextualizar o público-alvo do sistema.

---

## Screenshot 10 — Detalhes do Cliente com Lista de Agendamentos

- **URL:** `http://localhost:8081/web/clientes/{id}` — usando o ID de um cliente que possui agendamentos.
- **Pré-requisito:** O cliente selecionado deve ter ao menos 3 agendamentos em status variados. O layout deve exibir os dados do cliente à esquerda e a lista de agendamentos associados à direita (ou abaixo, conforme o layout).
- **O que mostrar:** Dados completos do cliente (nome, e-mail, telefone, data de cadastro); a lista de agendamentos associados com status badges coloridos; os botões de edição e retorno.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Full-page para capturar cliente + agendamentos.
- **Onde usar no TCC:** Capítulo 4, Seção 4.2 — Relacionamento Cliente-Agendamento. Demonstrar a visão 360° do cliente dentro do sistema.
- **Nota:** Adicionar seta ligando a seção de dados do cliente à seção de agendamentos, ilustrando o relacionamento de entidades no banco de dados.

---

## Screenshot 11 — Formulário Novo Cliente com Erros de Bean Validation

- **URL:** `http://localhost:8081/web/clientes/novo` — após submeter o formulário vazio ou com dados inválidos.
- **Pré-requisito:** Acessar o formulário de novo cliente e clicar em "Salvar" sem preencher os campos obrigatórios (ou com e-mail inválido).
- **O que mostrar:** Os campos obrigatórios com borda vermelha (classe `is-invalid` do Bootstrap 5); as mensagens de erro de validação abaixo de cada campo (ex.: "Nome é obrigatório", "E-mail inválido"); o campo de e-mail com formato incorreto (ex.: "teste@" sem domínio) mostrando a validação de formato.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Garantir que todas as mensagens de erro estejam visíveis.
- **Onde usar no TCC:** Capítulo 4, Seção 4.3 — Validação de Dados com Bean Validation. Demonstrar a integração entre Spring Validation e Thymeleaf para exibição de erros no frontend.
- **Nota:** Usar retângulos vermelhos para destacar cada campo com erro e sua respectiva mensagem. Esta captura deve ser referenciada no texto ao discutir a anotação `@Valid` e as anotações `@NotBlank`, `@Email`, etc.

---

## Screenshot 12 — Portal do Cliente — Página de Identificação/Login

- **URL:** `http://localhost:8081/portal`
- **Pré-requisito:** Não estar autenticado no portal. Acessar a URL em uma aba anônima/privada do navegador.
- **O que mostrar:** O layout centralizado e mobile-friendly do portal; o campo de e-mail ou CPF para identificação do cliente; o botão de acesso; o logotipo ou nome do sistema no topo; o design simplificado em contraste com o painel administrativo.
- **Capturar:** Resolução 375 × 812 px (iPhone 14) simulada via DevTools do Chrome (F12 → Toggle Device Toolbar → iPhone 14), zoom 100%. Esta captura deve ser mobile para demonstrar o design responsivo.
- **Onde usar no TCC:** Capítulo 4, Seção 4.4 — Portal do Cliente. Introduzir o portal como interface voltada ao usuário final, contrastando com o painel administrativo.
- **Nota:** Adicionar uma nota lateral "Visualização Mobile — 375px" para contextualizar a resolução usada. Mencionar no texto que o portal foi projetado mobile-first.

---

## Screenshot 13 — Portal — Passo 1: Seleção de Serviço (Radio Cards)

- **URL:** `http://localhost:8081/portal/agendar/passo1` (ou rota equivalente após identificação)
- **Pré-requisito:** Estar identificado/logado no portal como cliente. Ter ao menos 4 serviços cadastrados com nome, descrição e preço.
- **O que mostrar:** A grade de cards de serviço com radio buttons; cada card mostrando nome do serviço, descrição breve e preço; o indicador de progresso do wizard (Passo 1 de 4) no topo; o estado visual de seleção de um card (borda destacada ou fundo colorido ao selecionar); o botão "Próximo" desabilitado ou habilitado conforme seleção.
- **Capturar:** Duas sub-capturas: (a) estado sem seleção — todos os cards neutros; (b) estado com um card selecionado — mostrando o feedback visual de seleção. Resolução 375 × 812 px (mobile) para ambas.
- **Onde usar no TCC:** Capítulo 4, Seção 4.4 — Wizard de Agendamento, Passo 1. Demonstrar o padrão de UI de radio button cards para seleção de serviços.
- **Nota:** Na sub-captura (b), adicionar seta apontando para o card selecionado e para o indicador de progresso. Mencionar no texto que o padrão de radio card é uma escolha de UX para tornar a seleção mais intuitiva em dispositivos touch.

---

## Screenshot 14 — Portal — Passo 2: Seleção de Data e Grade de Horários

- **URL:** `http://localhost:8081/portal/agendar/passo2` (ou rota equivalente)
- **Pré-requisito:** Ter completado o Passo 1 (serviço selecionado). Ter horários disponíveis para ao menos 3 datas futuras. Ter ao menos 1 horário marcado como indisponível para demonstrar o contraste visual.
- **O que mostrar:** O componente de seleção de data (date picker ou calendário); a grade de botões de horário (slots de tempo) mostrando: horários disponíveis (botão azul/primary), horário selecionado (botão preenchido/active), horários indisponíveis (botão desabilitado/acinzentado); o indicador de progresso no topo (Passo 2 de 4); os botões "Anterior" e "Próximo".
- **Capturar:** Resolução 375 × 812 px (mobile), zoom 100%. Selecionar uma data e um horário antes de capturar para mostrar o estado ativo.
- **Onde usar no TCC:** Capítulo 4, Seção 4.4 — Wizard de Agendamento, Passo 2. Demonstrar a lógica de disponibilidade de horários e o feedback visual de slots ocupados.
- **Nota:** Usar setas para apontar para (1) um horário disponível, (2) o horário selecionado, (3) um horário indisponível. Esta é uma das capturas mais ricas em termos de lógica de negócio visível na interface.

---

## Screenshot 15 — Portal — Passo 3: Resumo da Reserva para Revisão

- **URL:** `http://localhost:8081/portal/agendar/passo3` (ou rota equivalente)
- **Pré-requisito:** Ter completado os Passos 1 e 2 (serviço + data/hora selecionados).
- **O que mostrar:** O card de resumo contendo todos os dados selecionados: nome do serviço, profissional (se selecionável), data e hora, nome do cliente logado; o preço total do serviço; os botões "Voltar" e "Confirmar Agendamento"; o indicador de progresso (Passo 3 de 4).
- **Capturar:** Resolução 375 × 812 px (mobile), zoom 100%.
- **Onde usar no TCC:** Capítulo 4, Seção 4.4 — Wizard de Agendamento, Passo 3. Demonstrar a etapa de revisão antes da confirmação definitiva.
- **Nota:** Adicionar setas numeradas para cada dado exibido no card de resumo. Mencionar no texto que esta etapa reduz erros de agendamento ao dar ao cliente uma última oportunidade de revisar os dados.

---

## Screenshot 16 — Portal — Passo 4: Confirmação de Sucesso com Protocolo

- **URL:** `http://localhost:8081/portal/agendar/passo4` ou `/portal/agendar/sucesso` (rota equivalente pós-confirmação)
- **Pré-requisito:** Ter completado todos os passos anteriores e clicado em "Confirmar Agendamento". O sistema deve gerar e exibir o número de protocolo do agendamento.
- **O que mostrar:** Ícone de sucesso (check verde ou ícone Bootstrap); mensagem de confirmação (ex.: "Agendamento realizado com sucesso!"); o número de protocolo gerado (ID ou código único do agendamento); um resumo final dos dados confirmados; opções de ação pós-confirmação ("Novo Agendamento", "Meus Agendamentos"); o indicador de progresso completo (Passo 4 de 4).
- **Capturar:** Resolução 375 × 812 px (mobile), zoom 100%.
- **Onde usar no TCC:** Capítulo 4, Seção 4.4 — Wizard de Agendamento, Passo 4. Demonstrar o estado final do fluxo do portal e o feedback positivo ao usuário.
- **Nota:** Destacar com retângulo o número de protocolo. Esta captura é fundamental para demonstrar que o fluxo completo do wizard funciona end-to-end.

---

## Screenshot 17 — Swagger UI — Página Principal com Endpoints

- **URL:** `http://localhost:8081/swagger-ui.html` ou `http://localhost:8081/swagger-ui/index.html`
- **Pré-requisito:** A aplicação deve estar rodando. O Springdoc OpenAPI deve estar configurado e ativo (dependência no `pom.xml`).
- **O que mostrar:** A página principal do Swagger UI com o título da API; todos os grupos de endpoints listados e recolhidos (ex.: `/api/v1/auth`, `/api/v1/agendamentos`, `/api/v1/clientes`); o botão "Authorize" no topo direito; a versão da API exibida.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Full-page para mostrar todos os grupos de endpoints.
- **Onde usar no TCC:** Capítulo 3, Seção 3.4 — Documentação da API REST. Demonstrar que a API está documentada seguindo o padrão OpenAPI/Swagger.
- **Nota:** Nenhuma anotação especial. A imagem fala por si ao mostrar todos os recursos da API organizados.

---

## Screenshot 18 — Swagger UI — POST /api/v1/auth/login Expandido

- **URL:** `http://localhost:8081/swagger-ui.html` — expandir o endpoint `POST /api/v1/auth/login`
- **Pré-requisito:** Na página do Swagger UI, clicar no endpoint de login para expandi-lo.
- **O que mostrar:** O endpoint expandido mostrando: o schema do corpo da requisição (campos `username`/`email` e `password`); os códigos de resposta documentados (200, 401); o schema da resposta de sucesso (com o campo `token` JWT); o botão "Try it out".
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Captura da seção expandida apenas (pode ser scroll-crop).
- **Onde usar no TCC:** Capítulo 3, Seção 3.4 — Autenticação JWT na API. Demonstrar a documentação do endpoint de autenticação.
- **Nota:** Adicionar seta apontando para o campo `token` no schema de resposta, com nota "Token JWT retornado para uso nas requisições subsequentes".

---

## Screenshot 19 — Swagger UI — Executando GET com Autenticação Bearer

- **URL:** `http://localhost:8081/swagger-ui.html`
- **Pré-requisito:** (1) Fazer login via `POST /api/v1/auth/login` no "Try it out" do Swagger para obter o token JWT. (2) Clicar em "Authorize" e inserir o token no campo `Bearer <token>`. (3) Executar um endpoint GET (ex.: `GET /api/v1/agendamentos`) via "Try it out" e "Execute".
- **O que mostrar:** O resultado da execução do endpoint GET com: o curl command gerado pelo Swagger com o cabeçalho `Authorization: Bearer ...`; o código de resposta 200; o corpo da resposta JSON com a lista de agendamentos; o campo de autorização preenchido no topo.
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Pode ser necessário capturar em duas partes: (a) o modal de autorização preenchido; (b) o resultado da execução com o response body.
- **Onde usar no TCC:** Capítulo 3, Seção 3.4 — Testando Endpoints Protegidos. Demonstrar o fluxo completo de autenticação e autorização via JWT na API.
- **Nota:** Ofuscar/editar parte do token JWT na imagem por segurança (substituir os últimos 20 caracteres por asteriscos usando editor de imagem).

---

## Screenshot 20 — Página de Erro 404 Personalizada

- **URL:** `http://localhost:8081/pagina-que-nao-existe` (qualquer URL inexistente)
- **Pré-requisito:** Nenhum. Basta navegar para uma URL que não existe no sistema.
- **O que mostrar:** O layout da página de erro 404 com: o código de erro grande (404) em destaque; a mensagem descritiva (ex.: "Página não encontrada"); o ícone ou ilustração de erro; o botão "Voltar ao Início" ou link para o dashboard; o cabeçalho/navbar do sistema ainda visível (se aplicável).
- **Capturar:** Resolução 1440 × 900 px, zoom 100%. Captura da página inteira.
- **Onde usar no TCC:** Capítulo 4, Seção 4.5 — Tratamento de Erros e Experiência do Usuário. Demonstrar que o sistema possui páginas de erro personalizadas em vez da página de erro padrão do Tomcat.
- **Nota:** Mencionar no texto a diferença entre a página de erro do Tomcat (genérica, técnica) e a página personalizada (amigável ao usuário). Se possível, incluir a captura da página padrão do Tomcat para contraste.

---

## Screenshot 21 — Console H2 Conectado ao Banco de Dados

- **URL:** `http://localhost:8081/h2-console`
- **Pré-requisito:** O H2 Console deve estar habilitado nas configurações (`spring.h2.console.enabled=true`). Fazer login no console com as credenciais configuradas no `application.properties`.
- **O que mostrar:** (a) A tela de login do H2 Console preenchida com JDBC URL, usuário e senha; (b) após login: o painel do H2 com a árvore de tabelas à esquerda (TB_AGENDAMENTO, TB_CLIENTE, TB_PROFISSIONAL, TB_SERVICO, etc.) e uma query SQL executada à direita (ex.: `SELECT * FROM TB_AGENDAMENTO LIMIT 10`) com o resultado em tabela.
- **Capturar:** Duas sub-capturas: (a) tela de login — resolução 1440 × 900 px; (b) console com query e resultado — resolução 1440 × 900 px, full-page.
- **Onde usar no TCC:** Capítulo 3, Seção 3.2 — Banco de Dados. Demonstrar a estrutura das tabelas e o banco em memória H2 utilizado em desenvolvimento.
- **Nota:** Na sub-captura (b), adicionar seta para a árvore de tabelas indicando "Estrutura de tabelas mapeadas pelo Hibernate/JPA". Ofuscar senhas na tela de login se estiverem visíveis.

---

## Screenshot 22 — Código: AgendamentoService.criar() — Lógica de Detecção de Conflito

- **Arquivo:** Abrir no IDE (IntelliJ IDEA ou VS Code) o arquivo `AgendamentoService.java`
- **Pré-requisito:** Ter o projeto aberto na IDE. Navegar até o método `criar()` ou equivalente que contém a verificação de conflito de horários.
- **O que mostrar:** O método completo de criação de agendamento com: a chamada ao repositório para verificar conflitos (ex.: `repository.existsByProfissionalAndDataHoraBetween(...)` ou similar); a lógica condicional que lança exceção em caso de conflito; a anotação `@Transactional`; os comentários (se houver); o número de linhas visível na margem esquerda da IDE.
- **Capturar:** Usar a funcionalidade de captura do próprio IDE (IntelliJ: File → Export to Image, ou usar Print Screen com o método centralizado na tela). Resolução suficiente para que o código seja legível — pelo menos 1200 × 600 px. Tema escuro (Darcula/Dark+) ou claro conforme preferência, desde que mantido consistente com outras capturas de código.
- **Onde usar no TCC:** Capítulo 3, Seção 3.3 — Implementação da Camada de Serviço. Esta é a captura de código mais importante do TCC — referenciá-la ao discutir a regra de negócio de detecção de conflito de horários (junto ao Screenshot 7).
- **Nota:** Usar a função de highlight do IDE para destacar as linhas da lógica de conflito. No editor de imagem, adicionar um retângulo colorido ao redor do bloco condicional de verificação de conflito.

---

## Screenshot 23 — Código: SecurityConfig.java — Filtro JWT e Regras RBAC

- **Arquivo:** `SecurityConfig.java` (ou nome equivalente na estrutura do projeto)
- **Pré-requisito:** Arquivo aberto na IDE com o método de configuração do `SecurityFilterChain` visível.
- **O que mostrar:** O bean `SecurityFilterChain` com: as regras de autorização por role (`hasRole("ADMIN")`, `permitAll()`, etc.); a adição do `JwtAuthenticationFilter` à cadeia de filtros; a configuração de endpoints públicos (portal, login, H2 console, Swagger) versus protegidos; a configuração CORS e CSRF (se houver).
- **Capturar:** Resolução 1200 × 700 px mínimo. Ajustar o zoom da IDE para que o código caiba na tela sem scroll horizontal. Pode ser necessária captura em duas partes se o método for muito longo.
- **Onde usar no TCC:** Capítulo 3, Seção 3.5 — Segurança: Autenticação e Autorização. Demonstrar a implementação do Spring Security com RBAC.
- **Nota:** Adicionar setas coloridas: azul para as regras de acesso por role, verde para endpoints públicos, vermelho para a linha de adição do JwtAuthenticationFilter.

---

## Screenshot 24 — Código: JwtAuthenticationFilter.java — Extração do Bearer Token

- **Arquivo:** `JwtAuthenticationFilter.java` (ou nome equivalente)
- **Pré-requisito:** Arquivo aberto na IDE no método `doFilterInternal()` ou equivalente.
- **O que mostrar:** O método de filtragem com: a extração do cabeçalho `Authorization`; a verificação do prefixo `Bearer `; a validação e parsing do token JWT; a configuração do contexto de segurança do Spring (`SecurityContextHolder.getContext().setAuthentication(...)`).
- **Capturar:** Resolução 1200 × 700 px mínimo. Focar no método principal de filtragem.
- **Onde usar no TCC:** Capítulo 3, Seção 3.5 — Implementação do Filtro JWT. Demonstrar a integração do JWT com o mecanismo de filtros do Spring Security.
- **Nota:** Adicionar seta numérica em cada etapa: (1) extração do header, (2) validação do token, (3) configuração do SecurityContext. Isso transforma a captura em um diagrama de fluxo visual.

---

## Screenshot 25 — Código: WizardForm.java + PortalClienteController (Passo 4)

- **Arquivos:** Abrir em split-view (tela dividida) na IDE: `WizardForm.java` (ou DTO equivalente) à esquerda e `PortalClienteController.java` (ou nome equivalente) com o método do passo 4 à direita.
- **Pré-requisito:** Ambos os arquivos abertos na IDE em modo split.
- **O que mostrar:** À esquerda — a classe `WizardForm` com seus campos (servicoId, dataHora, clienteId, etc.) e anotações de validação; à direita — o método do controlador para o passo 4 (confirmação) mostrando: a chamada ao service de criação, a geração/obtenção do número de protocolo, o redirect para a página de sucesso com o protocolo como atributo.
- **Capturar:** Resolução 1440 × 800 px para comportar os dois painéis. Ajustar zoom da IDE para cerca de 90% se necessário.
- **Onde usar no TCC:** Capítulo 3, Seção 3.3 — Camada de Apresentação do Portal. Demonstrar a integração entre o DTO de formulário do wizard e o controller que processa a confirmação final.
- **Nota:** Adicionar uma seta horizontal conectando os campos do `WizardForm` ao uso desses campos no método do controller. Isso ilustra o fluxo de dados do formulário até a persistência.

---

## Sequência Recomendada de Captura

Para otimizar o tempo e evitar reconfigurar o ambiente repetidamente, siga esta ordem:

### Fase 1 — Preparação do Banco de Dados
Antes de qualquer captura, popular o banco com:
- 3 profissionais
- 5 clientes
- 4 serviços (com preços variados)
- 8 agendamentos distribuídos: 2 AGENDADO, 2 CONFIRMADO, 2 CONCLUIDO, 2 CANCELADO

### Fase 2 — Capturas da Interface Administrativa (Desktop 1440×900)
Sequência: Screenshot 1 → 2 → 3 → 9 → 10 → 6 → 7 → 8 → 11 → 4 → 5

Justificativa: Começa pelas listagens (mais simples, sem interação), depois formulários limpos, formulários com erros, e por último os modais (que exigem clique específico).

### Fase 3 — Capturas do Swagger UI (Desktop 1440×900)
Sequência: Screenshot 17 → 18 → 19

Justificativa: Sequência lógica de login → documentação → execução autenticada.

### Fase 4 — Capturas do Portal (Mobile 375×812)
Ativar o Device Toolbar do Chrome (F12 → ícone de dispositivo móvel → iPhone 14).
Sequência: Screenshot 12 → 13 → 14 → 15 → 16

Justificativa: Fluxo linear do wizard do início ao fim, evitando renavegação.

### Fase 5 — Captura de Erro e H2 Console
Sequência: Screenshot 20 → 21

### Fase 6 — Capturas de Código (IDE)
Sequência: Screenshot 22 → 23 → 24 → 25

Justificativa: Deixar as capturas de código para o final pois exigem troca de janela (navegador → IDE) e ajuste de zoom/layout da IDE.

---

## Ferramentas Recomendadas para Anotações

### Windows
- **Snagit** (pago, recomendado): Captura, anotações com setas, retângulos, texto e desfoque integrados. Melhor opção para qualidade profissional.
- **ShareX** (gratuito): Captura de tela com ferramentas de anotação integradas. Suporta captura de página inteira automaticamente.
- **Paint.NET** (gratuito): Editor simples para adicionar setas e texto às capturas já realizadas.
- **Microsoft PowerPoint**: Inserir imagens e adicionar setas/caixas de texto diretamente nos slides — útil para preparar as figuras do TCC junto com as legendas.

### macOS
- **Skitch** (gratuito, Evernote): Anotações rápidas com setas e texto. Ideal para uso casual.
- **CleanShot X** (pago, recomendado): Captura profissional com anotações, desfoque e exportação em alta qualidade.
- **Preview** (nativo): Ferramenta de marcação integrada do macOS para setas e formas básicas.

### Linux
- **Flameshot** (gratuito): Captura e anotação integradas. Disponível via `apt install flameshot`.
- **GIMP** (gratuito): Para edições mais complexas nas capturas.

### Web / Cross-platform
- **Figma** (gratuito para uso básico): Importar as capturas e adicionar anotações como camadas vetoriais. Permite criar versões anotadas reutilizáveis.
- **Canva** (gratuito): Alternativa simples para adicionar setas, ícones e textos sobre as capturas.

### Dicas Gerais
- Usar setas em **vermelho** para erros e alertas
- Usar setas em **azul** para elementos de navegação e fluxo
- Usar setas em **verde** para confirmações e estados positivos
- Manter fonte consistente nas anotações (recomendado: Roboto ou Arial, tamanho 12-14pt)
- Numerar as setas quando houver múltiplos elementos destacados na mesma imagem
- Salvar as imagens originais (sem anotações) separadamente para caso seja necessário reannotar
- Exportar em formato PNG para manter a qualidade (evitar JPEG para capturas de tela com texto)
- Resolução mínima para impressão em A4: 150 DPI (para texto, 300 DPI é ideal)
