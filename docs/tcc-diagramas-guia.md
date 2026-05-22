# Guia Completo de Diagramas UML — TCC Agendamento API
## Guilherme Braga — UNDB 2026

> **Como renderizar**: Cole cada bloco PlantUML em https://plantuml.com/plantuml
> ou use o plugin IntelliJ IDEA "PlantUML Integration" (File → Settings → Plugins → buscar "PlantUML").
> Para exportar PNG em alta resolução: no site online, clique em "PNG" após renderizar.

---

## DIAGRAMA 1 — Casos de Uso (Use Case)

```plantuml
@startuml TCC_CasosDeUso
!theme plain
skinparam actorStyle awesome

left to right direction

actor "Administrador" as ADM
actor "Cliente" as CLI
actor "Sistema" as SYS <<system>>

rectangle "Sistema de Agendamento" {
    usecase "Login administrativo\n(HTTP Basic / JWT)" as UC01
    usecase "Gerenciar Clientes\n(CRUD)" as UC02
    usecase "Gerenciar Profissionais\n(CRUD)" as UC03
    usecase "Gerenciar Serviços\n(CRUD)" as UC04
    usecase "Gerenciar Agendamentos\n(CRUD + status)" as UC05
    usecase "Visualizar Dashboard\n(métricas e gráficos)" as UC06
    usecase "Alterar status\ndo agendamento" as UC07
    usecase "Consultar API REST\n(Swagger UI)" as UC08

    usecase "Identificar-se\npor e-mail" as UC09
    usecase "Cadastrar-se\nno portal" as UC10
    usecase "Visualizar serviços\ndisponíveis" as UC11
    usecase "Realizar agendamento\n(wizard 4 etapas)" as UC12
    usecase "Consultar meus\nagendamentos" as UC13
    usecase "Cancelar\nagendamento" as UC14

    usecase "Verificar conflito\nde horário" as UC15
    usecase "Calcular dataHoraFim\n(+ duração serviço)" as UC16
    usecase "Enviar notificação" as UC17 #LightGray
}

ADM --> UC01
ADM --> UC02
ADM --> UC03
ADM --> UC04
ADM --> UC05
ADM --> UC06
ADM --> UC07
ADM --> UC08

CLI --> UC09
CLI --> UC10
CLI --> UC11
CLI --> UC12
CLI --> UC13
CLI --> UC14

UC12 ..> UC15 : <<include>>
UC05 ..> UC15 : <<include>>
UC12 ..> UC16 : <<include>>
UC05 ..> UC16 : <<include>>
UC12 ..> UC17 : <<extend>>

note right of UC17
  Funcionalidade futura
  (Sprint Mail + SMTP)
end note
@enduml
```

---

## DIAGRAMA 2 — Diagrama de Classes (Domínio)

```plantuml
@startuml TCC_DiagramaClasses
!theme plain
skinparam classAttributeIconSize 0

class Cliente {
    - id: Long
    - nome: String
    - email: String
    - telefone: String
    - cpf: String
    - criadoEm: LocalDateTime
    - atualizadoEm: LocalDateTime
    + getNome(): String
    + getEmail(): String
}

class Profissional {
    - id: Long
    - nome: String
    - especialidade: String
    - email: String
    - telefone: String
    - criadoEm: LocalDateTime
    - atualizadoEm: LocalDateTime
}

class Servico {
    - id: Long
    - nome: String
    - descricao: String
    - duracaoMinutos: Integer
    - preco: BigDecimal
    - criadoEm: LocalDateTime
    - atualizadoEm: LocalDateTime
}

enum StatusAgendamento {
    AGENDADO
    CONFIRMADO
    CONCLUIDO
    CANCELADO
}

class Agendamento {
    - id: Long
    - dataHora: LocalDateTime
    - dataHoraFim: LocalDateTime
    - status: StatusAgendamento
    - criadoEm: LocalDateTime
    - atualizadoEm: LocalDateTime
}

class AgendamentoService {
    + criar(request): AgendamentoResponse
    + atualizar(id, request): AgendamentoResponse
    + atualizarStatus(id, status): AgendamentoResponse
    + cancelar(id): void
    + deletar(id): void
    + criarPeloPortal(...): AgendamentoResponse
    - verificarConflito(...): void
    - calcularDataHoraFim(...): LocalDateTime
}

class ClienteService {
    + criar(request): ClienteResponse
    + atualizar(id, request): ClienteResponse
    + deletar(id): void
    + autenticarPorEmail(email): Cliente
    + criarPeloPortal(...): Cliente
}

class JwtTokenProvider {
    + generateToken(usuario): String
    + validateToken(token): boolean
    + getUsuarioFromToken(token): String
}

class JwtAuthenticationFilter {
    + doFilterInternal(...): void
}

Agendamento "*" --> "1" Cliente : cliente
Agendamento "*" --> "1" Profissional : profissional
Agendamento "*" --> "1" Servico : servico
Agendamento --> StatusAgendamento : status

AgendamentoService ..> Agendamento : gerencia
ClienteService ..> Cliente : gerencia
JwtAuthenticationFilter --> JwtTokenProvider : usa
@enduml
```

---

## DIAGRAMA 3 — Sequência: Wizard de Agendamento (Portal Cliente)

```plantuml
@startuml TCC_SequenciaWizard
!theme plain
autonumber

actor "Cliente" as CLI
participant "Browser\n(Thymeleaf)" as BR
participant "PortalHomeController" as PHC
participant "PortalClienteController" as PCC
participant "AgendamentoService" as SVC
participant "AgendamentoRepository" as REPO
database "H2/PostgreSQL" as DB

== Identificação ==
CLI -> BR: Acessa /portal
BR -> PHC: GET /portal
PHC -> BR: Renderiza portal/identificacao.html
CLI -> BR: Digita e-mail + Submit
BR -> PHC: POST /portal/login {email}
PHC -> DB: SELECT FROM clientes WHERE email=?
DB --> PHC: Cliente encontrado
PHC -> PHC: session.setAttribute("clienteLogado", cliente)
PHC --> BR: Redirect /portal
BR --> CLI: Exibe portal/home.html

== Step 1: Selecionar Serviço ==
CLI -> BR: Clica "Novo Agendamento"
BR -> PCC: GET /portal/step1
PCC -> DB: SELECT FROM servicos
DB --> PCC: List<Servico>
PCC --> BR: Renderiza wizard/step1.html (radio cards)
CLI -> BR: Seleciona serviço + clica "Próximo"
BR -> PCC: POST /portal/step2 {servicoId}
PCC -> PCC: wizard.setServicoId/Nome/Duracao/Preco
note right: @SessionAttributes("wizard")\npersiste WizardForm entre requests

== Step 2: Selecionar Data e Hora ==
PCC --> BR: Renderiza wizard/step2.html
BR -> BR: JS: gerarHorarios() — cria time slots 08:00–17:30
CLI -> BR: Seleciona data + horário
BR -> PCC: POST /portal/step3 {profissionalId, data, hora}
PCC -> PCC: wizard.setProfissional/Data/Hora

== Step 3: Confirmar ==
PCC --> BR: Renderiza wizard/step3.html (resumo read-only)
CLI -> BR: Clica "Confirmar Agendamento"
BR -> PCC: POST /portal/step4

== Step 4: Criação e Sucesso ==
PCC -> SVC: criarPeloPortal(clienteId, profissionalId, servicoId, dataHora)
SVC -> SVC: calcularDataHoraFim(dataHora, duracaoMinutos)
SVC -> REPO: existeConflito(profissionalId, dataHora, dataHoraFim)
REPO -> DB: SELECT COUNT > 0 (overlap check)
DB --> REPO: false (sem conflito)
REPO --> SVC: false
SVC -> REPO: save(agendamento)
REPO -> DB: INSERT INTO agendamentos
DB --> REPO: id gerado
REPO --> SVC: Agendamento salvo
SVC --> PCC: AgendamentoResponse(id=42, ...)
PCC -> PCC: sessionStatus.setComplete() — limpa wizard da sessão
PCC --> BR: Renderiza wizard/step4.html (protocolo #42)
BR --> CLI: "Agendamento Confirmado! Protocolo: #42"
@enduml
```

---

## DIAGRAMA 4 — Sequência: Autenticação JWT + API REST

```plantuml
@startuml TCC_SequenciaJWT
!theme plain
autonumber

actor "Cliente REST\n(Postman/App)" as CLI
participant "AuthController\n/api/v1/auth/login" as AUTH
participant "AuthenticationManager" as AM
participant "InMemoryUserDetails\nManager" as UDM
participant "JwtTokenProvider" as JWT
participant "JwtAuthentication\nFilter" as JWTF
participant "AgendamentoController\n/api/v1/**" as AC
participant "SecurityContextHolder" as SCH

== Obtenção do Token ==
CLI -> AUTH: POST /api/v1/auth/login\n{email:"admin", senha:"admin123"}
AUTH -> AM: authenticate(\n  UsernamePasswordAuthToken\n  ("admin","admin123"))
AM -> UDM: loadUserByUsername("admin")
UDM --> AM: UserDetails(ROLE_ADMIN)
AM -> AM: BCrypt.matches(senha, hash)
AM --> AUTH: Authentication autenticada
AUTH -> JWT: generateToken("admin")
JWT -> JWT: Jwts.builder()\n.subject("admin")\n.signWith(HS512, secretKey)\n.compact()
JWT --> AUTH: "eyJhbGciOiJIUzUxMiJ9..."
AUTH --> CLI: 200 OK\n{token, tipo:"Bearer", expiracao}

== Uso do Token em Request Subsequente ==
CLI -> JWTF: GET /api/v1/agendamentos\nAuthorization: Bearer eyJ...
JWTF -> JWTF: Extrai token do header
JWTF -> JWT: validateToken(token)
JWT -> JWT: Jwts.parser().verifyWith(key)\n.parseSignedClaims(token)
JWT --> JWTF: true (válido, não expirado)
JWTF -> JWT: getUsuarioFromToken(token)
JWT --> JWTF: "admin"
JWTF -> UDM: loadUserByUsername("admin")
UDM --> JWTF: UserDetails(ROLE_ADMIN)
JWTF -> SCH: setAuthentication(auth)
JWTF -> AC: filterChain.doFilter() — continua
AC -> AC: verifica hasAnyRole("USER","ADMIN") — OK
AC -> AC: executa lógica de negócio
AC --> CLI: 200 OK [{agendamentos...}]
@enduml
```

---

## DIAGRAMA 5 — Componentes e Camadas

```plantuml
@startuml TCC_Componentes
!theme plain
skinparam componentStyle rectangle

package "Presentation Layer" {
    component "Admin Web\n(Thymeleaf)\n/web/** /dashboard" as ADMIN_WEB
    component "Portal Cliente\n(Thymeleaf)\n/portal/**" as PORTAL_WEB
    component "REST API\n(JSON)\n/api/v1/**" as REST_API
    component "Swagger UI\n/swagger-ui.html" as SWAGGER
}

package "Security Layer" {
    component "SecurityFilterChain\n(Spring Security)" as SEC_CHAIN
    component "JwtAuthentication\nFilter" as JWT_FILTER
    component "JwtTokenProvider\n(HS512)" as JWT_PROV
    component "BCryptPassword\nEncoder" as BCRYPT
}

package "Business Layer" {
    component "AgendamentoService\n(conflito, status)" as AGS
    component "ClienteService" as CS
    component "ProfissionalService" as PS
    component "ServicoService" as SS
}

package "Data Layer" {
    component "AgendamentoRepository\n(JPA + custom query)" as AGR
    component "ClienteRepository" as CR
    component "ProfissionalRepository" as PR
    component "ServicoRepository" as SR
    component "MapStruct Mappers\n(Entity↔DTO)" as MAPPERS
}

package "Infrastructure" {
    database "H2 In-Memory\n(dev profile)" as H2
    database "PostgreSQL\n(prod)" as PG
    component "DataInitializer\n(CommandLineRunner)" as DI
}

ADMIN_WEB --> SEC_CHAIN
PORTAL_WEB --> SEC_CHAIN
REST_API --> SEC_CHAIN
SWAGGER --> SEC_CHAIN
SEC_CHAIN --> JWT_FILTER
JWT_FILTER --> JWT_PROV
SEC_CHAIN --> BCRYPT

ADMIN_WEB --> AGS
ADMIN_WEB --> CS
PORTAL_WEB --> AGS
PORTAL_WEB --> CS
REST_API --> AGS
REST_API --> CS
REST_API --> PS
REST_API --> SS

AGS --> AGR
AGS --> MAPPERS
CS --> CR
CS --> MAPPERS
PS --> PR
SS --> SR

AGR --> H2
CR --> H2
PR --> H2
SR --> H2
H2 -[dashed]-> PG : troca via\napplication.properties

DI --> AGS
DI --> CS
DI --> PS
DI --> SS
@enduml
```

---

## DIAGRAMA 6 — Máquina de Estados do Agendamento

```plantuml
@startuml TCC_MaquinaEstados
!theme plain
skinparam state {
    BackgroundColor<<agendado>> #FFF3CD
    BorderColor<<agendado>> #664D03
    BackgroundColor<<confirmado>> #D1E7DD
    BorderColor<<confirmado>> #0A3622
    BackgroundColor<<concluido>> #E9ECEF
    BorderColor<<concluido>> #495057
    BackgroundColor<<cancelado>> #F8D7DA
    BorderColor<<cancelado>> #58151C
}

[*] --> AGENDADO : criar agendamento\n(admin ou portal cliente)

state AGENDADO <<agendado>> : Status inicial\nCliente aguarda confirmação
state CONFIRMADO <<confirmado>> : Profissional confirmou\nPresença garantida
state CONCLUIDO <<concluido>> : Serviço realizado\n[Estado final — imutável]
state CANCELADO <<cancelado>> : Cancelado por qualquer parte\n[Estado final — imutável]

AGENDADO --> CONFIRMADO : Admin confirma\n[atualizarStatus(CONFIRMADO)]
AGENDADO --> CANCELADO : Admin ou cliente cancela\n[cancelar() / atualizarStatus(CANCELADO)]
CONFIRMADO --> CONCLUIDO : Serviço concluído\n[atualizarStatus(CONCLUIDO)]
CONFIRMADO --> CANCELADO : Cancelamento tardio\n[atualizarStatus(CANCELADO)]

CONCLUIDO --> [*]
CANCELADO --> [*]

note right of AGENDADO
  dataHoraFim calculada automaticamente
  = dataHora + servico.duracaoMinutos
  
  Conflito de horário verificado no criar()
  Intervalos [A,B) e [C,D) se sobrepõem
  quando A<D AND C<B
end note

note right of CONCLUIDO
  Campos bloqueados para edição
  (th:disabled no formulário Thymeleaf)
  Hidden inputs preservam valores no POST
end note
@enduml
```

---

## DIAGRAMA 7 — Modelo Entidade-Relacionamento (Banco de Dados)

```plantuml
@startuml TCC_ModeloER
!theme plain
skinparam linetype ortho

entity "clientes" as CLI {
    * **id** : BIGINT <<PK>>
    --
    * nome : VARCHAR(100)
    * email : VARCHAR(150) <<UK>>
    * telefone : VARCHAR(20)
    * cpf : VARCHAR(11) <<UK>>
    criado_em : TIMESTAMP
    atualizado_em : TIMESTAMP
}

entity "profissionais" as PRO {
    * **id** : BIGINT <<PK>>
    --
    * nome : VARCHAR(100)
    * especialidade : VARCHAR(100)
    email : VARCHAR(150)
    telefone : VARCHAR(20)
    criado_em : TIMESTAMP
    atualizado_em : TIMESTAMP
}

entity "servicos" as SER {
    * **id** : BIGINT <<PK>>
    --
    * nome : VARCHAR(100)
    descricao : TEXT
    * duracao_minutos : INTEGER
    * preco : DECIMAL(10,2)
    criado_em : TIMESTAMP
    atualizado_em : TIMESTAMP
}

entity "agendamentos" as AGN {
    * **id** : BIGINT <<PK>>
    --
    * cliente_id : BIGINT <<FK>>
    * profissional_id : BIGINT <<FK>>
    * servico_id : BIGINT <<FK>>
    * data_hora : TIMESTAMP
    * data_hora_fim : TIMESTAMP
    * status : VARCHAR(20)
    criado_em : TIMESTAMP
    atualizado_em : TIMESTAMP
}

CLI ||--o{ AGN : "realiza"
PRO ||--o{ AGN : "atende"
SER ||--o{ AGN : "compõe"
@enduml
```

---

## DIAGRAMA 8 — Diagrama de Pacotes (Package Diagram)

```plantuml
@startuml TCC_Pacotes
!theme plain

package "com.guilhermebraga.agendamento_api" {
    package "config" {
        class SecurityConfig
        class DataInitializer
        class H2ConsoleConfig
    }
    package "controller" {
        package "web.controller" {
            class AgendamentoWebController
            class ClienteWebController
            class DashboardWebController
            class ProfissionalWebController
            class ServicoWebController
        }
        package "portal" {
            class PortalClienteController
            class PortalHomeController
            class PortalAgendamentoController
            class PortalMeusAgendamentosController
        }
        package "api" {
            class AgendamentoController
            class ClienteController
            class ProfissionalController
            class ServicoController
            class AuthController
        }
    }
    package "service" {
        class AgendamentoService
        class ClienteService
        class ProfissionalService
        class ServicoService
    }
    package "repository" {
        interface AgendamentoRepository
        interface ClienteRepository
        interface ProfissionalRepository
        interface ServicoRepository
    }
    package "entity" {
        class Agendamento
        class Cliente
        class Profissional
        class Servico
        enum StatusAgendamento
    }
    package "dto" {
        package "request" {
            class AgendamentoRequest
            class ClienteRequest
            class WizardForm
        }
        package "response" {
            class AgendamentoResponse
            class ClienteResponse
        }
    }
    package "mapper" {
        interface AgendamentoMapper
        interface ClienteMapper
    }
    package "security" {
        class JwtTokenProvider
        class JwtAuthenticationFilter
    }
    package "exception" {
        class BusinessException
        class ResourceNotFoundException
    }
}
@enduml
```

---

## DIAGRAMA 9 — Arquitetura de Deployment

```plantuml
@startuml TCC_Deployment
!theme plain
skinparam nodeBackgroundColor #F8F9FA

node "Servidor de Aplicação\n(Railway / AWS EC2)" {
    artifact "agendamento-api.jar\n(Spring Boot Fat JAR)" as JAR {
        component "Tomcat\n(Embedded)" as TOMCAT
        component "Spring Boot 4.0.5" as SB
        component "Thymeleaf Engine" as TH
        component "Spring Security\n(JWT + BCrypt)" as SS
    }
}

node "Banco de Dados\n(Railway PostgreSQL / AWS RDS)" {
    database "PostgreSQL 16" as PG
}

node "Desenvolvedor / CI" {
    artifact "Maven Build\nmvn package" as MVN
}

cloud "CDN" {
    artifact "Bootstrap 5.3.3" as BS
    artifact "Bootstrap Icons 1.11.3" as BI
    artifact "Chart.js 4.4.3" as CJS
}

actor "Administrador" as ADM
actor "Cliente" as CLI
actor "Dev / Postman" as DEV

ADM --> TOMCAT : HTTPS\n/dashboard\n/web/**
CLI --> TOMCAT : HTTPS\n/portal/**
DEV --> TOMCAT : HTTPS + Bearer JWT\n/api/v1/**

TOMCAT --> PG : JDBC + HikariCP\n(connection pool)
SB --> TH
SB --> SS

JAR ..> BS : CDN
JAR ..> BI : CDN
JAR ..> CJS : CDN

MVN ..> JAR : gera

note bottom of JAR
  Perfil dev: H2 em memória
  Perfil prod: PostgreSQL externo
  Troca via application.properties
end note
@enduml
```

---

## COMO GERAR OS DIAGRAMAS

### Opção 1 — Online (mais simples)
1. Acesse https://www.plantuml.com/plantuml/uml/
2. Cole o código PlantUML entre `@startuml` e `@enduml`
3. O diagrama é gerado automaticamente à direita
4. Para exportar: clique no ícone de download → escolha PNG ou SVG
5. **Resolução recomendada para TCC**: PNG em 2x (use o botão "PNG" diretamente)

### Opção 2 — IntelliJ IDEA Plugin
1. Settings → Plugins → buscar "PlantUML Integration" → instalar
2. Abra qualquer arquivo `.puml` ou cole o código em um arquivo `.puml`
3. O preview aparece ao lado automaticamente
4. Clique com botão direito → Export Diagram → PNG (300 DPI para impressão)

### Opção 3 — VS Code Extension
1. Extensions → buscar "PlantUML" (jebbs.plantuml)
2. Instalar Graphviz: https://graphviz.org/download/
3. Abra arquivo `.puml` → Alt+D para preview
4. Ctrl+Shift+P → "PlantUML: Export Current File Diagrams" → PNG

### Opção 4 — Maven (automatizado)
```xml
<!-- Adicionar ao pom.xml para gerar diagramas durante o build -->
<plugin>
    <groupId>com.github.funthomas424242</groupId>
    <artifactId>plantuml-maven-plugin</artifactId>
    <version>1.6.0</version>
</plugin>
```

---

## ONDE USAR CADA DIAGRAMA NO TCC

| Diagrama | Seção Recomendada | Título na Figura |
|---|---|---|
| 1. Casos de Uso | Cap 3 — Requisitos | Figura X: Diagrama de Casos de Uso |
| 2. Classes | Cap 4 — Desenvolvimento | Figura X: Diagrama de Classes do Domínio |
| 3. Sequência Wizard | Cap 4 — Wizard | Figura X: Diagrama de Sequência — Autoagendamento |
| 4. Sequência JWT | Cap 4 — Segurança | Figura X: Diagrama de Sequência — Autenticação JWT |
| 5. Componentes | Cap 4 — Arquitetura | Figura X: Diagrama de Componentes |
| 6. Máquina de Estados | Cap 4 — Negócio | Figura X: Máquina de Estados — Agendamento |
| 7. Modelo ER | Cap 3 — Banco de Dados | Figura X: Modelo Entidade-Relacionamento |
| 8. Pacotes | Cap 4 — Estrutura | Figura X: Diagrama de Pacotes |
| 9. Deployment | Cap 4 — Infraestrutura | Figura X: Diagrama de Implantação |

### Legenda ABNT para figuras
```
Figura X — [Título do Diagrama]
Fonte: Elaborado pelo autor (2026).
```

---

## DICAS PARA APRESENTAÇÃO (SLIDES / DEFESA)

- Use os diagramas 1 (casos de uso) e 5 (componentes) nos slides principais
- Use o diagrama 6 (máquina de estados) para explicar a regra de negócio de status
- Use o diagrama 3 (sequência wizard) para demonstrar o fluxo do cliente
- Use o diagrama 4 (sequência JWT) para explicar a segurança da API
- Para slides, prefira PNG fundo branco — visível em projetor
- Tamanho recomendado nos slides: centralizado, máximo 80% da largura do slide
