# TRABALHO DE CONCLUSÃO DE CURSO — UNDB 2026
## Autor: Guilherme Braga
## Título: Sistema de Agendamento Online com Spring Boot — Desenvolvimento de uma API RESTful com Portal do Cliente e Painel Administrativo

---

# CAPÍTULO 3 — METODOLOGIA

## 3.1 Caracterização da Pesquisa

Este trabalho classifica-se como pesquisa aplicada, de natureza exploratória e descritiva, com abordagem qualitativa e quantitativa. Do ponto de vista dos procedimentos técnicos, adota a metodologia de desenvolvimento de software, combinando revisão bibliográfica sobre as tecnologias utilizadas com a construção e validação de um sistema funcional.

A estratégia de desenvolvimento adotada foi iterativa e incremental, inspirada nos princípios ágeis: o sistema foi construído em ciclos curtos (commits atômicos), com validação contínua por meio de testes automatizados (JUnit 5 + Mockito), revisão de código e refatorações planejadas documentadas neste trabalho.

## 3.2 Tecnologias e Ferramentas

### 3.2.1 Back-end

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 21 (LTS) | Linguagem principal |
| Spring Boot | 4.0.5 | Framework de aplicação |
| Spring Framework | 7.x | Módulos Web MVC, Security, Data |
| Spring Security | 7.x | Autenticação e autorização |
| Spring Data JPA | 4.x | Mapeamento objeto-relacional |
| Hibernate | 7.x | Implementação JPA |
| JJWT (jwtToken) | 0.12.x | Geração e validação de JWT |
| Lombok | 1.18.x | Redução de código boilerplate |
| Jakarta EE | 11 | Especificações Servlet, Validation |

### 3.2.2 Front-end (Server-Side Rendering)

| Tecnologia | Versão | Função |
|---|---|---|
| Thymeleaf | 3.1.x | Motor de templates HTML |
| Bootstrap | 5.3.3 | Framework CSS responsivo |
| Bootstrap Icons | 1.11.3 | Biblioteca de ícones SVG |
| JavaScript (ES6) | — | Validação client-side e UX |

### 3.2.3 Banco de Dados e Persistência

| Tecnologia | Versão | Função |
|---|---|---|
| H2 Database | 2.x | Banco em memória (ambiente dev) |
| Hibernate DDL Auto | create-drop | Recriação automática do schema |
| Spring `@Profile("dev")` | — | Isolamento do DataInitializer |

### 3.2.4 Documentação e Qualidade

| Ferramenta | Versão | Função |
|---|---|---|
| SpringDoc OpenAPI | 2.x | Documentação Swagger UI automática |
| JUnit 5 | 5.x | Testes unitários |
| Mockito | 5.x | Mocks para isolamento de testes |
| Maven | 3.9.x | Gerenciador de dependências e build |
| Git / GitHub | — | Controle de versão e colaboração |

### 3.2.5 Ambiente de Desenvolvimento

- **IDE:** IntelliJ IDEA / Visual Studio Code
- **Sistema Operacional:** Linux (Ubuntu/Debian)
- **Build:** `mvn compile -Dmaven.compiler.release=21`
- **Testes:** `mvn test` — 52 testes, 0 falhas

## 3.3 Arquitetura do Sistema

O sistema foi desenvolvido segundo o padrão de arquitetura em camadas (*Layered Architecture*), seguindo os princípios SOLID e as convenções do ecossistema Spring. As camadas definidas são:

```
┌────────────────────────────────────────────┐
│           Camada de Apresentação            │
│   Controller (Web MVC + REST + Portal)      │
├────────────────────────────────────────────┤
│           Camada de Serviço (Service)       │
│   Regras de negócio, validações, BCrypt     │
├────────────────────────────────────────────┤
│          Camada de Acesso a Dados           │
│   Repository (Spring Data JPA)              │
├────────────────────────────────────────────┤
│             Camada de Dados                 │
│   Entity (JPA/Hibernate) + H2 Database      │
└────────────────────────────────────────────┘
```

**Decisão arquitetural:** a separação entre os controllers REST (`/api/v1/**`), os controllers Web administrativos (`/web/**`) e os controllers do Portal do Cliente (`/portal/**`) permite que cada interface evolua de forma independente, sem acoplamento de lógica de apresentação com regras de negócio.

### 3.3.1 Diagrama de Componentes

```
[ESPAÇO RESERVADO PARA DIAGRAMA DE COMPONENTES]

Sugestão: diagrama UML de componentes mostrando:
- Módulo Portal do Cliente (/portal/**)
  └── PortalHomeController
  └── PortalClienteController (wizard steps 1-4)
  └── PortalAgendamentoController
  └── PortalMeusAgendamentosController
- Módulo Painel Administrativo (/web/**)
  └── DashboardWebController
  └── ClienteWebController
  └── ProfissionalWebController
  └── ServicoWebController
  └── AgendamentoWebController
- Módulo API REST (/api/v1/**)
  └── AgendamentoController
  └── ClienteController
  └── ProfissionalController
  └── ServicoController
  └── AuthController
- Camada Security (Spring Security + JWT)
  └── SecurityConfig
  └── JwtAuthenticationFilter
  └── JwtTokenProvider
- Camada de Serviço
  └── AgendamentoService
  └── ClienteService
  └── ProfissionalService
  └── ServicoService
- Camada de Repositório (Spring Data JPA)
  └── AgendamentoRepository
  └── ClienteRepository
  └── ProfissionalRepository
  └── ServicoRepository
- Banco de dados H2
```

### 3.3.2 Diagrama de Classes (Entidades)

```
[ESPAÇO RESERVADO PARA DIAGRAMA DE CLASSES UML]

Sugestão: diagrama UML de classes mostrando:

Cliente
  - id: Long
  - nome: String
  - email: String (único)
  - cpf: String (único)
  - telefone: String
  - senha: String (BCrypt hash)
  - criadoEm: LocalDateTime
  - [OneToMany] → Agendamento

Profissional
  - id: Long
  - nome: String
  - especialidade: String
  - email: String
  - criadoEm: LocalDateTime
  - [OneToMany] → Agendamento

Servico
  - id: Long
  - nome: String
  - descricao: String
  - preco: BigDecimal
  - duracaoMinutos: Integer
  - [OneToMany] → Agendamento

Agendamento
  - id: Long
  - dataHora: LocalDateTime
  - dataHoraFim: LocalDateTime
  - status: StatusAgendamento (enum)
  - criadoEm: LocalDateTime
  - atualizadoEm: LocalDateTime
  - [ManyToOne] → Cliente
  - [ManyToOne] → Profissional
  - [ManyToOne] → Servico

StatusAgendamento (enum)
  - AGENDADO
  - CONFIRMADO
  - CANCELADO
  - CONCLUIDO
```

## 3.4 Modelo de Segurança

O sistema implementa três mecanismos de autenticação coexistentes em um único `SecurityFilterChain`:

### 3.4.1 JWT Bearer Token (API REST)

Clientes de API (aplicações mobile, terceiros, Swagger UI) enviam requisições com o cabeçalho `Authorization: Bearer <token>`. O `JwtAuthenticationFilter` (extensão de `OncePerRequestFilter`) intercepta a requisição, valida a assinatura do token via HMAC-SHA512 e popula o `SecurityContextHolder` antes do processamento pelo Spring Security.

```
[ESPAÇO RESERVADO PARA DIAGRAMA DE SEQUÊNCIA — AUTENTICAÇÃO JWT]

Sugestão: diagrama de sequência UML mostrando:
1. Cliente → POST /api/v1/auth/login {username, password}
2. AuthController → AuthenticationManager.authenticate()
3. AuthenticationManager → UserDetailsService.loadUserByUsername()
4. AuthController ← sucesso: Authentication
5. AuthController → JwtTokenProvider.generateToken()
6. Cliente ← 200 OK {token, tipo, expiracao}
7. Cliente → GET /api/v1/clientes [Authorization: Bearer <token>]
8. JwtAuthenticationFilter → JwtTokenProvider.validateToken()
9. JwtAuthenticationFilter → SecurityContextHolder.setAuthentication()
10. ClienteController ← requisição autenticada
11. Cliente ← 200 OK [lista de clientes]
```

### 3.4.2 Form Login (Painel Administrativo)

Administradores acessam `/web/login`, submetem credenciais via formulário HTML, e o Spring Security autentica contra um `InMemoryUserDetailsManager` com senhas BCrypt. Após o login, a sessão HTTP mantém a autenticação e o acesso é controlado por roles (`ROLE_USER`, `ROLE_ADMIN`).

### 3.4.3 Sessão HTTP Customizada (Portal do Cliente)

O Portal do Cliente utiliza autenticação baseada em sessão HTTP gerenciada pela aplicação (não pelo Spring Security). Após validação das credenciais via `ClienteService.autenticarPorEmailESenha()` (BCrypt), o objeto `Cliente` é armazenado na `HttpSession`. Cada endpoint do portal verifica a presença do atributo `clienteLogado` na sessão e redireciona para identificação se ausente.

**Justificativa:** esta abordagem foi escolhida para isolar completamente o fluxo do portal do mecanismo de autenticação Spring Security, permitindo que regras de acesso do portal sejam aplicadas de forma simples e transparente, sem depender de configurações complexas de múltiplas cadeias de segurança.

## 3.5 Fluxo do Wizard de Agendamento

O processo de agendamento no Portal do Cliente foi implementado como um wizard de 4 etapas, utilizando `@SessionAttributes("wizard")` para persistir o estado entre as requisições HTTP.

```
[ESPAÇO RESERVADO PARA DIAGRAMA DE SEQUÊNCIA — WIZARD DE AGENDAMENTO]

Sugestão: diagrama de sequência UML mostrando:

Cliente (Browser)       PortalClienteController      Services       H2 Database
     |                           |                      |                |
     |── GET /portal/step1 ────►|                      |                |
     |                           |── listarServicos() ─►|               |
     |                           |                      |── SELECT ────►|
     |◄── 200 step1.html ───────|                      |◄── servicos ──|
     |── POST /portal/step2 ───►|                      |                |
     |    {servicoId}            |── salvar wizard ────►|               |
     |◄── redirect step2 ───────|                      |                |
     |── GET /portal/step2 ─────►|                     |                |
     |                           |── listarProfissionais() ─►|         |
     |◄── 200 step2.html ───────|                      |                |
     |── POST /portal/step3 ────►|                     |                |
     |    {profissionalId, dataHora}                    |                |
     |◄── redirect step3 ────────|                     |                |
     |── GET /portal/step3 ─────►|                     |                |
     |◄── 200 step3.html ────────|   (resumo wizard)   |                |
     |── POST /portal/step4 ────►|                     |                |
     |                           |── agendamento.criar()─►|            |
     |                           |                      |── INSERT ────►|
     |                           |                      |◄── id ────────|
     |◄── redirect step4 ────────|                     |                |
     |── GET /portal/step4 ─────►|                     |                |
     |◄── 200 step4.html ────────|   (confirmação)     |                |
```

## 3.6 Estrutura de Pastas do Projeto

```
agendamento-api/
├── src/main/java/com/guilhermebraga/agendamento_api/
│   ├── config/
│   │   ├── DataInitializer.java       (seed de dados — @Profile("dev"))
│   │   ├── OpenApiConfig.java         (Swagger UI)
│   │   ├── SecurityConfig.java        (Spring Security)
│   │   └── WebConfig.java
│   ├── controller/
│   │   ├── AgendamentoController.java (REST)
│   │   ├── AuthController.java        (REST — gera JWT)
│   │   ├── ClienteController.java     (REST)
│   │   ├── ProfissionalController.java(REST)
│   │   ├── ServicoController.java     (REST)
│   │   ├── portal/
│   │   │   ├── PortalHomeController.java
│   │   │   ├── PortalClienteController.java (wizard steps)
│   │   │   ├── PortalAgendamentoController.java
│   │   │   └── PortalMeusAgendamentosController.java
│   │   └── web/
│   │       ├── LoginWebController.java
│   │       └── controller/
│   │           ├── DashboardWebController.java
│   │           ├── ClienteWebController.java
│   │           ├── ProfissionalWebController.java
│   │           ├── ServicoWebController.java
│   │           └── AgendamentoWebController.java
│   ├── dto/
│   │   ├── form/       (WizardForm, NovoAgendamentoForm)
│   │   ├── request/    (AgendamentoRequest, ClienteRequest, ...)
│   │   └── response/   (AgendamentoResponse, ClienteResponse, ...)
│   ├── entity/
│   │   ├── Agendamento.java
│   │   ├── Cliente.java
│   │   ├── Profissional.java
│   │   ├── Servico.java
│   │   └── StatusAgendamento.java (enum)
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── GlobalExceptionHandler.java (@RestControllerAdvice)
│   │   └── ResourceNotFoundException.java
│   ├── mapper/
│   │   └── (AgendamentoMapper, ClienteMapper, ...)
│   ├── repository/
│   │   └── (AgendamentoRepository, ClienteRepository, ...)
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtTokenProvider.java
│   └── service/
│       ├── AgendamentoService.java
│       ├── ClienteService.java
│       ├── ProfissionalService.java
│       └── ServicoService.java
├── src/main/resources/
│   ├── templates/
│   │   ├── layout/         (base.html, fragments.html — painel admin)
│   │   ├── portal/         (layout.html, home, cadastro, identificacao, ...)
│   │   │   └── wizard/     (step1.html → step4.html)
│   │   ├── web/            (login.html)
│   │   ├── agendamentos/   (listar.html, formulario.html)
│   │   ├── clientes/       (listar.html, formulario.html)
│   │   ├── profissionais/  (listar.html, formulario.html)
│   │   ├── servicos/       (listar.html, formulario.html)
│   │   └── dashboard.html
│   └── application.properties
└── src/test/java/
    ├── AgendamentoApiApplicationTests.java
    ├── AgendamentoServiceTest.java
    └── ClienteServiceTest.java
```

## 3.7 Estratégia de Testes

Foram implementados **52 testes automatizados** distribuídos em três classes:

- **`AgendamentoApiApplicationTests`:** teste de carregamento do contexto Spring (Smoke Test). Verifica se todos os beans são instanciados corretamente e se não há erros de configuração.
- **`AgendamentoServiceTest`:** testes unitários da camada de serviço de agendamentos. Cobrem os cenários: criação com sucesso, conflito de horário, cliente/profissional/serviço inexistente, agendamento em horário passado, cancelamento por cliente, atualização de status.
- **`ClienteServiceTest`:** testes unitários da `ClienteService`. Cobrem: criação com sucesso, e-mail duplicado, CPF duplicado, autenticação com BCrypt (senha correta e incorreta), cadastro pelo portal com hash de senha.

Todos os testes utilizam **Mockito** para isolar as dependências (repositórios e mappers são mockados), garantindo que cada unidade seja testada de forma independente.

---

# CAPÍTULO 4 — RESULTADOS E DISCUSSÃO

## 4.1 Visão Geral do Sistema Desenvolvido

O sistema de agendamento foi entregue como um monólito Spring Boot com três interfaces distintas e complementares:

1. **Portal do Cliente** (`/portal/**`) — interface pública, autocontida, para que clientes finais realizem login, cadastro e agendamentos em um fluxo guiado de 4 etapas.
2. **Painel Administrativo** (`/web/**`) — interface protegida por Spring Security Form Login, para operadores e administradores gerirem clientes, profissionais, serviços e agendamentos via CRUD completo.
3. **API REST** (`/api/v1/**`) — interface programática protegida por JWT, documentada via Swagger UI, para integração com aplicações de terceiros (aplicativos móveis, sistemas externos).

## 4.2 Interface do Portal do Cliente

### 4.2.1 Tela de Identificação / Login

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/identificacao.html]
Descrição: Tela de login do portal com gradiente índigo/violeta (linear-gradient
135deg #4f46e5 → #7c3aed), card branco centralizado, campos de e-mail e senha
com ícones Bootstrap Icons, botão de submit com gradiente matching e link para
a tela de cadastro. Largura máxima 420px, border-radius 20px.
```

A tela de identificação foi desenhada para causar impacto positivo na primeira interação do cliente. O fundo utiliza gradiente índigo-violeta (#4f46e5 → #7c3aed), identidade visual adotada em todo o portal. O card central com sombra pronunciada (`box-shadow: 0 24px 60px rgba(0,0,0,.25)`) cria profundidade visual e isola o formulário do fundo.

**Decisão de UX:** o ícone da aplicação — `scissors` (tesoura) — foi escolhido para remeter ao segmento de barbearia/salão de beleza, domínio típico de uso do sistema. Campos com ícones flutuantes (`position: absolute`) transmitem modernidade sem poluição visual.

### 4.2.2 Tela de Cadastro

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/cadastro.html]
Descrição: Tela de cadastro com os campos nome completo, e-mail, telefone, CPF,
senha e confirmar senha, todos com ícones Bootstrap Icons à esquerda. Validação
client-side em JavaScript para verificação de senhas iguais com exibição de
mensagem de erro inline. Identidade visual idêntica à tela de identificação.
```

O formulário de cadastro inclui validação em duas camadas:

- **Client-side (JavaScript):** verificação imediata da igualdade das senhas antes do envio do formulário, sem round-trip ao servidor.
- **Server-side (Spring):** validação de e-mail e CPF únicos via `ClienteService.criarPeloPortal()`, com senha hasheada via BCrypt antes da persistência.

Esta dupla validação garante tanto responsividade para o usuário quanto integridade dos dados no banco.

### 4.2.3 Home do Portal

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/home.html]
Descrição: Dashboard do portal com card de boas-vindas em gradiente índigo/violeta,
nome do cliente logado, e três cards de métricas (Serviços Disponíveis, Profissionais
Disponíveis, Meus Agendamentos) com borda colorida lateral (border-left) e botão
"Novo Agendamento" em destaque.
```

### 4.2.4 Wizard de Agendamento — Etapa 1 (Seleção de Serviço)

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/wizard/step1.html]
Descrição: Grid responsivo de cards de serviço (2 colunas em md, 3 em lg), cada
card com nome, descrição, preço formatado (R$ X,XX) e duração em minutos com
ícone de relógio. Card selecionado com borda índigo e fundo levemente azulado.
Indicador de etapas no topo mostrando passo 1 como ativo (círculo preenchido índigo).
```

A etapa 1 implementa seleção de serviço via radio buttons estilizados como cards clicáveis. A lógica JavaScript mantém referência O(1) ao card ativo (sem iterar todos os elementos a cada clique), otimização relevante quando o catálogo de serviços é extenso.

**Validação progressiva:** ao tentar avançar sem selecionar um serviço, o formulário impede o envio (client-side) e exibe alerta Bootstrap `alert-danger` com ícone explicativo. Esta validação eliminou o erro "Whitelabel Error Page" que ocorria quando o controller recebia `servicoId` nulo.

### 4.2.5 Wizard de Agendamento — Etapa 2 (Data, Hora e Profissional)

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/wizard/step2.html]
Descrição: Formulário com seleção de profissional (dropdown com nome e
especialidade), campo de data (date picker nativo do browser) e campo de
hora (time picker nativo). Indicador de etapas com passo 2 ativo. Botão
Voltar (outline secundário) e Avançar (índigo preenchido).
```

### 4.2.6 Wizard de Agendamento — Etapa 3 (Confirmação)

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/wizard/step3.html]
Descrição: Card de resumo do agendamento com linhas: Serviço (nome + preço),
Duração, Profissional (nome + especialidade), Data e Hora formatados. Alerta
informativo sobre política de cancelamento. Botões Voltar e "Confirmar
Agendamento" (índigo preenchido).
```

### 4.2.7 Wizard de Agendamento — Etapa 4 (Conclusão)

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/wizard/step4.html]
Descrição: Tela de sucesso com ícone de checkmark verde em círculo (bi-check-circle-fill),
número do agendamento gerado, resumo compacto do que foi agendado, e dois botões:
"Meus Agendamentos" e "Novo Agendamento".
```

A etapa 4 marca o encerramento bem-sucedido do fluxo. O `@SessionAttributes("wizard")` é limpo automaticamente após a criação do agendamento, evitando que dados residuais causem inconsistências em um novo agendamento iniciado na mesma sessão.

### 4.2.8 Meus Agendamentos

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — portal/meus-agendamentos.html]
Descrição: Listagem dos agendamentos do cliente logado em tabela responsiva
(desktop) com colunas: Data/Hora, Serviço, Profissional, Status (badge colorido).
Em mobile, transforma em cards empilhados. Badges de status: verde (AGENDADO),
azul (CONFIRMADO), vermelho (CANCELADO), cinza (CONCLUIDO).
```

## 4.3 Interface do Painel Administrativo

### 4.3.1 Tela de Login Admin

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — web/login.html]
Descrição: Tela de login admin com gradiente navy escuro (linear-gradient
135deg #1e293b → #0f172a), card branco, ícone de escudo (shield-lock-fill),
campos username e senha, mensagem de erro para credenciais inválidas
(param.error) e mensagem de logout. Rodapé com hint das credenciais de demo.
```

O painel administrativo utiliza identidade visual distinta (navy escuro) para diferenciar claramente os dois contextos de uso — cliente final versus administrador. Esta separação visual reduz o risco de confusão e reforça o controle de acesso.

### 4.3.2 Dashboard Administrativo

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — dashboard.html]
Descrição: Dashboard com 4 cards de métricas: Total de Clientes, Total de
Profissionais, Total de Serviços e Total de Agendamentos. Navbar lateral
ou superior em navy escuro com links para as entidades. Rodapé com link
para o Portal do Cliente.
```

O dashboard carrega os totais via quatro chamadas aos serviços (`clienteService.listarTodos().size()` etc.). Esta abordagem é adequada para o volume de dados do TCC; em produção com alto volume, seria substituída por queries COUNT no repositório.

### 4.3.3 CRUD Completo — Padrão Visual

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — exemplo: clientes/listar.html]
Descrição: Tabela de clientes com colunas ID, Nome, E-mail, CPF, Telefone,
e coluna Ações com botões Editar (azul) e Excluir (vermelho). Header da
página com título e botão "Novo Cliente". Flash messages de sucesso/erro
no topo com auto-dismiss.
```

Todos os CRUDs do painel seguem o mesmo padrão visual (tabela + formulário), garantindo consistência e previsibilidade para o operador. As operações destrutivas (excluir) são acessadas via GET com confirmação implícita — em uma evolução futura, seria adicionado modal de confirmação JavaScript.

### 4.3.4 Navbar Administrativo (fragments.html)

A navbar foi redesenhada com gradiente navy escuro e active state dinâmico via expressão Thymeleaf:

```thymeleaf
th:classappend="${#httpServletRequest.requestURI.contains('/clientes')} ? ' active' : ''"
```

O botão de logout submete um formulário POST para `/web/logout` (não um link GET), atendendo à especificação CSRF do Spring Security que exige POST para operações destrutivas de sessão.

## 4.4 API REST — Documentação Swagger UI

```
[ESPAÇO RESERVADO PARA CAPTURA DE TELA — Swagger UI /swagger-ui.html]
Descrição: Página do Swagger UI com os grupos de endpoints:
- /api/v1/auth/login → POST (público — retorna JWT)
- /api/v1/agendamentos → GET, POST, PUT, DELETE (requer JWT)
- /api/v1/clientes → GET, POST, PUT, DELETE (requer JWT)
- /api/v1/profissionais → GET, POST, PUT, DELETE (requer JWT)
- /api/v1/servicos → GET, POST, PUT, DELETE (requer JWT)
Botão "Authorize" para inserir Bearer token.
```

## 4.5 Regras de Negócio Implementadas

### 4.5.1 Prevenção de Conflito de Horário

O sistema impede que dois agendamentos sejam criados para o mesmo profissional em horários sobrepostos. A verificação é feita via query JPQL no `AgendamentoRepository`:

```java
@Query("""
    SELECT COUNT(a) > 0 FROM Agendamento a
    WHERE a.profissional.id = :profissionalId
    AND a.status IN :statusAtivos
    AND a.dataHora < :fim
    AND a.dataHoraFim > :inicio
""")
boolean existeConflito(Long profissionalId, LocalDateTime inicio,
                        LocalDateTime fim, List<StatusAgendamento> statusAtivos);
```

A verificação considera apenas agendamentos com status AGENDADO ou CONFIRMADO — agendamentos CANCELADOS ou CONCLUÍDOS não bloqueiam o horário.

### 4.5.2 Fluxo de Status

O sistema controla transições de status para evitar operações inválidas (ex.: reativar um agendamento CONCLUÍDO):

```
[ESPAÇO RESERVADO PARA DIAGRAMA DE ESTADOS — StatusAgendamento]

AGENDADO ──►CONFIRMADO──►CONCLUIDO
    │              │
    ▼              ▼
CANCELADO    CANCELADO

Regra: CONCLUIDO é estado terminal — não permite transição.
Regra: CANCELADO só pode ser definido com antecedência mínima
       configurável (padrão: 2 horas antes do agendamento).
```

### 4.5.3 Janela de Atendimento

Agendamentos fora do horário comercial são rejeitados. O horário é configurável via `application.properties`:

```properties
agendamento.horario.inicio=08:00
agendamento.horario.fim=18:00
agendamento.cancelamento.antecedencia-horas=2
```

Esta externalização evita recompilação para ajustes operacionais rotineiros.

## 4.6 Segurança — Resultados de Implementação

| Aspecto | Implementação | Resultado |
|---|---|---|
| Senha do cliente | BCrypt (10 rounds) | Hash irreversível, resistente a rainbow tables |
| Senha do admin | BCrypt via Spring Security | InMemoryUserDetailsManager |
| Token JWT | HMAC-SHA512, 24h | Stateless, não requer sessão no servidor |
| CSRF | Desabilitado para API REST | APIs REST stateless não precisam de CSRF |
| Frames (H2 Console) | `sameOrigin` | Permite H2 Console no desenvolvimento |
| CORS | Configurado para localhost:3000 | Pronto para integração com SPA futura |

## 4.7 Problemas Encontrados e Soluções

### 4.7.1 Whitelabel Error Page no Wizard

**Problema:** ao submeter a etapa 1 do wizard sem selecionar um serviço, o controller recebia `servicoId = null`, causava `NullPointerException` no service e o Spring retornava Whitelabel Error Page.

**Solução:** validação dupla implementada. No client-side, JavaScript impede o `submit` se nenhum radio está selecionado. No server-side, o controller verifica o campo e adiciona `mensagemErro` como Flash Attribute, redirecionando de volta para a mesma etapa com mensagem amigável.

### 4.7.2 Templates Ausentes (step3, step4, web/login)

**Problema:** `step3.html`, `step4.html` e `web/login.html` não existiam no repositório, causando `TemplateInputException` (Thymeleaf) e Whitelabel Error Page respectivamente.

**Solução:** todos os templates foram criados do zero, seguindo o padrão de fragment parametrizado estabelecido para o portal:
```thymeleaf
th:replace="~{portal/layout :: html(~{:: section})}"
```

### 4.7.3 Controllers Vazios

**Problema:** `DashboardWebController` e `ClienteWebController` existiam como arquivos no repositório mas continham apenas a declaração de classe, sem métodos.

**Solução:** implementação completa dos controllers com toda a lógica CRUD e integração com as camadas de serviço.

### 4.7.4 Arquivo `clientes/formulario.html` Corrompido

**Problema:** o arquivo continha apenas as tags de fechamento HTML, sem nenhum formulário funcional.

**Solução:** reconstrução completa com formulário de 4 campos (nome, email, telefone, CPF), suporte a modo criação e edição via variável `clienteId` no model.

### 4.7.5 JwtAuthenticationFilter Ausente

**Problema:** `SecurityConfig.java` recebia `JwtAuthenticationFilter` como parâmetro do método `securityFilterChain()`, mas a classe não existia no projeto, causando falha de compilação.

**Solução:** criação da classe `JwtAuthenticationFilter extends OncePerRequestFilter`, com lógica de extração e validação do Bearer token via `JwtTokenProvider`.

## 4.8 Métricas do Projeto

| Métrica | Valor |
|---|---|
| Arquivos Java | 51 arquivos |
| Templates Thymeleaf | 27 arquivos HTML |
| Testes automatizados | 52 testes |
| Taxa de aprovação | 100% (0 falhas) |
| Commits no branch de desenvolvimento | 10+ commits |
| Endpoints REST | 17 endpoints (GET, POST, PUT, DELETE) |
| Entidades JPA | 4 (Cliente, Profissional, Servico, Agendamento) |
| Linhas adicionadas (refactor principal) | 1.627 linhas |

## 4.9 Diagrama de Implantação (Deployment)

```
[ESPAÇO RESERVADO PARA DIAGRAMA DE IMPLANTAÇÃO UML]

Sugestão: diagrama de deployment mostrando:

┌────────────────────────────────────────────────────────┐
│                   Servidor de Aplicação                │
│                   (JVM — Java 21)                      │
│                                                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │          Spring Boot Application                 │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │  │
│  │  │  Portal   │  │ Admin    │  │   API REST    │  │  │
│  │  │ /portal   │  │  /web    │  │  /api/v1     │  │  │
│  │  └──────────┘  └──────────┘  └──────────────┘  │  │
│  │         ↓             ↓              ↓           │  │
│  │  ┌─────────────────────────────────────────┐    │  │
│  │  │          Service Layer                   │    │  │
│  │  └─────────────────────────────────────────┘    │  │
│  │         ↓                                        │  │
│  │  ┌─────────────────────────────────────────┐    │  │
│  │  │     Spring Data JPA / Hibernate          │    │  │
│  │  └─────────────────────────────────────────┘    │  │
│  │         ↓                                        │  │
│  │  ┌─────────────────────────────────────────┐    │  │
│  │  │          H2 In-Memory Database           │    │  │
│  │  └─────────────────────────────────────────┘    │  │
│  └─────────────────────────────────────────────────┘  │
│                                                        │
│  Porta: 8080                                           │
└────────────────────────────────────────────────────────┘
         ↑                  ↑                 ↑
  Browser (Cliente)   Browser (Admin)    API Client
  /portal/**          /web/**            (Swagger/App)
                                         Bearer JWT
```

## 4.10 Limitações e Trabalhos Futuros

As seguintes limitações foram identificadas no escopo atual e constituem oportunidades de evolução:

1. **Banco de dados em memória (H2):** dados são perdidos a cada reinicialização. Para produção, migrar para PostgreSQL com flyway/liquibase para migrations.
2. **InMemoryUserDetailsManager:** credenciais de admin hardcoded. Evolução: `UserDetailsService` com persistência em banco.
3. **Notificações:** não há envio de e-mail ou SMS ao confirmar/cancelar agendamento. Integração futura com Spring Mail ou API de SMS.
4. **Disponibilidade de profissionais:** o sistema não gerencia folgas ou horários personalizados por profissional — apenas previne conflitos de horário nos agendamentos já cadastrados.
5. **Painel mobile:** a interface administrativa não é totalmente responsiva. O portal do cliente foi projetado mobile-first.
6. **Múltiplos tenants:** o sistema serve um único estabelecimento. Evolução para SaaS com múltiplos salões/barbearias exigiria separação de dados por tenant.

---

# DECISÕES TÉCNICAS RELEVANTES
*(Seção auxiliar para a defesa do TCC — Guilherme Braga / UNDB 2026)*

---

## DTR-01: Fragment Parametrizado no Thymeleaf

**Contexto:** o layout do portal precisava ser compartilhado entre a home, os cards de serviços, a lista de agendamentos e os 4 steps do wizard. Os templates usavam padrões incompatíveis (alguns com `th:replace` de fragmento simples, outros com passagem de conteúdo por variável).

**Decisão:** unificar todos os templates do portal no padrão de *fragment parametrizado* do Thymeleaf:

```html
<!-- portal/layout.html -->
<html th:fragment="html(content)" xmlns:th="http://www.thymeleaf.org">
  <head>...</head>
  <body>
    <th:block th:replace="${content}"/>
  </body>
</html>
```

```html
<!-- qualquer página do portal -->
<html th:replace="~{portal/layout :: html(~{:: section})}">
<body>
  <section>
    <!-- conteúdo da página aqui -->
  </section>
</body>
</html>
```

**Benefícios:** DRY — header, footer, Bootstrap, variáveis CSS e flash messages definidos em um único arquivo. Mudanças de layout afetam todas as páginas automaticamente.

**Referência para defesa:** esta é a mesma abordagem de *template composition* usada em Razor (ASP.NET), Blade (Laravel) e Jinja2 (Flask) — o Thymeleaf resolve em tempo de renderização server-side, sem requisições adicionais.

---

## DTR-02: Três Mecanismos de Autenticação em um SecurityFilterChain

**Contexto:** o sistema precisa atender três tipos de clientes com necessidades distintas de autenticação — API consumers (JWT), administradores humanos via browser (Form Login) e clientes do portal (sessão customizada).

**Decisão:** configurar um único `SecurityFilterChain` com a cadeia de filtros:

```
Requisição HTTP
      ↓
JwtAuthenticationFilter  ← lida com Bearer token
      ↓
UsernamePasswordAuthenticationFilter  ← lida com form login
      ↓
Regras de autorização (requestMatchers)
```

O Portal do Cliente (`/portal/**`) é inteiramente `permitAll()` no Spring Security, e a autenticação do cliente é gerenciada pela aplicação via `HttpSession` — desacoplamento intencional.

**Trade-off documentado:** esta abordagem é mais simples que múltiplas `SecurityFilterChain` (possível no Spring Security 6+), mas significa que `/portal/**` é formalmente não-autenticado do ponto de vista do Spring Security. Em uma evolução, poder-se-ia adicionar uma segunda filter chain específica para o portal com autenticação Spring nativa.

---

## DTR-03: BCrypt para Senhas de Clientes (não apenas admins)

**Contexto:** na versão inicial, o campo `senha` não existia na entidade `Cliente` — o portal usava autenticação apenas por e-mail (sem senha). A adição de senha exigia decisão sobre o algoritmo de hash.

**Decisão:** usar `BCryptPasswordEncoder` (custo 10, padrão Spring Security) para todos os hashes de senha — tanto clientes quanto admins.

**Justificativa técnica:** BCrypt é um algoritmo *adaptive* — o custo pode ser aumentado conforme o hardware evolui, mantendo a proteção sem invalidar senhas existentes (basta re-hash no próximo login). É resistente a ataques de GPU por seu design *intentionally slow*.

**Implementação:**

```java
// Cadastro — hash antes de persistir
cliente.setSenha(passwordEncoder.encode(senhaTextoClaro));

// Login — verificação sem reversão do hash
boolean valido = passwordEncoder.matches(senhaTextoClaro, hashArmazenado);
```

---

## DTR-04: @SessionAttributes para o Wizard Multi-Step

**Contexto:** o wizard de agendamento requer que dados coletados em etapas anteriores estejam disponíveis nas etapas seguintes (ex.: o `servicoId` escolhido na etapa 1 deve estar acessível na etapa 4 para criar o agendamento).

**Decisão:** usar `@SessionAttributes("wizard")` no controller do wizard com um DTO `WizardForm` acumulando os dados de cada etapa.

**Benefício:** o estado do wizard vive na `HttpSession` do usuário — thread-safe, sem uso de variáveis estáticas, e automaticamente isolado por sessão. `SessionStatus.setComplete()` limpa o atributo ao final do fluxo.

**Alternativas consideradas:** armazenamento em banco (mais complexo, requer limpeza de wizard abandonados) e hidden fields (risco de manipulação client-side).

---

## DTR-05: Validação Dupla (Client-Side + Server-Side)

**Contexto:** ao tentar avançar no wizard sem selecionar um serviço, o sistema gerava Whitelabel Error Page porque o controller não tratava `null`.

**Decisão:** toda validação existe em duas camadas:
- **JavaScript:** feedback imediato sem round-trip (UX)
- **Spring Controller:** verificação antes de chamar o service (segurança — não confiar no browser)

**Princípio de defesa:** a validação client-side pode ser bypassada (DevTools, cURL direto ao endpoint). A validação server-side é a que garante integridade dos dados.

---

## DTR-06: CSS Custom Properties (Design System)

**Contexto:** o sistema tinha dois portais com identidades visuais distintas e inconsistentes, sem sistema de design definido.

**Decisão:** definir variáveis CSS customizadas (`:root`) em cada layout, criando mini design-systems independentes:

```css
/* Portal do Cliente — Índigo/Violeta */
:root {
  --clr-primary: #4f46e5;    /* índigo */
  --clr-accent:  #7c3aed;    /* violeta */
  --clr-gradient: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
}

/* Painel Admin — Navy Escuro */
.navbar {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}
```

**Benefício:** todas as referências à cor primária usam `var(--clr-primary)` — uma mudança na variável atualiza toda a interface. Manutenibilidade e consistência visual garantidas por contrato.

---

## DTR-07: @Profile("dev") no DataInitializer

**Contexto:** dados de seed (clientes, profissionais, serviços e agendamentos de exemplo) são necessários em desenvolvimento mas não em produção.

**Decisão:** anotar `DataInitializer` com `@Profile("dev")`. O bean só é instanciado quando o Spring profile ativo inclui "dev" (`spring.profiles.active=dev` em `application.properties`).

**Benefício:** ao deploiar com `spring.profiles.active=prod`, o DataInitializer não é carregado — o banco de produção não é populado com dados de teste.

---

## DTR-08: FetchType.LAZY com @ToString.Exclude / @EqualsAndHashCode.Exclude

**Contexto:** as entidades JPA usam `@Data` do Lombok, que gera `toString()`, `equals()` e `hashCode()` usando todos os campos. Com `FetchType.LAZY`, acessar um campo lazy fora de uma transação ativa lança `LazyInitializationException`.

**Decisão:** anotar todos os relacionamentos `@ManyToOne` com `@ToString.Exclude` e `@EqualsAndHashCode.Exclude`, instruindo o Lombok a excluir esses campos das implementações geradas.

**Documentação no código:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "cliente_id")
@ToString.Exclude        // evita LazyInitializationException no toString()
@EqualsAndHashCode.Exclude  // evita queries no equals/hashCode
private Cliente cliente;
```

**Referência para defesa:** este é um padrão documentado pela comunidade Spring como "armadilha clássica" de JPA + Lombok. A solução correta é excluir os campos lazy das implementações geradas, não desativar LAZY loading (que degrada performance com N+1 queries).

---

*Fim do documento. Versão gerada em 2026-05-28.*
*Branch: claude/setup-spring-security-3RaAV*
*Repositório: engguilhermebraga/agendamento-api*
