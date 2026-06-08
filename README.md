# Agendamento API

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT%20%7C%20BCrypt-6DB33F?logo=springsecurity&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Tests](https://img.shields.io/badge/Tests-138%20passing-brightgreen)
![Build](https://img.shields.io/badge/Build-passing-brightgreen?logo=githubactions)

> Trabalho de Conclusão de Curso — Engenharia de Software — UNDB 2026  
> **Autor:** Guilherme Braga  
> **Repositório:** [github.com/engguilhermebraga/agendamento-api](https://github.com/engguilhermebraga/agendamento-api)

API REST para gestão de agendamentos de serviços, desenvolvida com Spring Boot e Java 21.
O sistema integra API REST, painel administrativo Thymeleaf e portal de autoatendimento
para o cliente final, com autenticação JWT, documentação Swagger UI e 138 testes automatizados.

---

## Capturas de tela


### Login Administrativo
<!-- Captura: http://localhost:8081/web/login -->
![Login Administrativo](docs/screenshots/login-administrativo.png)

### Dashboard Administrativo
<!-- Captura: http://localhost:8081/dashboard -->
![Dashboard](docs/screenshots/dashboard-administrativo.png)

### Listagem de Agendamentos
<!-- Captura: http://localhost:8081/web/agendamentos/listar -->
![Agendamentos](docs/screenshots/listagem-agendamentos.png)

### Portal do Cliente — Seleção de Serviço
<!-- Captura: http://localhost:8081/portal/step1 (logado) -->
![Portal Step 1](docs/screenshots/portal-cliente-step1.png)

### Portal do Cliente — Confirmação
<!-- Captura: http://localhost:8081/portal/step4 (após wizard) -->
![Portal Confirmação](docs/screenshots/portal-cliente-confirmacao.png)

### Swagger UI
<!-- Captura: http://localhost:8081/swagger-ui.html -->
![Swagger UI](docs/screenshots/swagger-ui-endpoints.png)

### Execução dos Testes
<!-- Captura: terminal após ./mvnw test -->
![Testes](docs/screenshots/execucao-testes.png)

---

## Funcionalidades

- **API REST** — 22 endpoints com autenticação JWT e controle de acesso por papel (ROLE\_USER / ROLE\_ADMIN)
- **Painel Administrativo** — CRUD completo de clientes, profissionais, serviços e agendamentos com Thymeleaf
- **Portal do Cliente** — fluxo guiado de autoatendimento em 4 etapas com verificação de disponibilidade
- **Dashboard** — métricas em tempo real e distribuição de agendamentos por status
- **Detecção de conflito** — algoritmo de sobreposição de intervalos para bloquear horários em uso
- **Máquina de estados** — controle de transições AGENDADO → CONFIRMADO → CONCLUIDO / CANCELADO
- **Segurança** — Spring Security com JWT (HS512, 24h) e senhas com hash BCrypt
- **Documentação** — Swagger UI gerado automaticamente via SpringDoc OpenAPI 2.8.6
- **Testes** — 138 testes automatizados (JUnit 5 + Mockito + MockMvc + integração end-to-end)
- **CI/CD** — GitHub Actions com build, testes e análise estática Qodana

---

## Stack tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | **21 (LTS)** | Linguagem principal |
| Spring Boot | 4.0.5 | Framework da aplicação |
| Spring Web / Spring MVC | — | Controllers REST e MVC |
| Spring Data JPA | — | Repositórios e consultas JPQL |
| Hibernate | — | Implementação JPA / ORM |
| Spring Security | — | Autenticação e autorização |
| JJWT | 0.12.3 | Geração e validação de tokens JWT (HS512) |
| Thymeleaf | — | Templates HTML server-side |
| Bootstrap | 5.3.3 | Estilização das interfaces web |
| SpringDoc OpenAPI | 2.8.6 | Swagger UI automático |
| H2 Database | — | Banco em memória (perfil dev/test) |
| MySQL Connector | — | Driver para banco em produção |
| Lombok | — | Redução de código boilerplate |
| JUnit 5 + Mockito | — | Testes unitários e de integração |
| Maven | 3.9.x | Build e dependências |
| Git / GitHub | — | Controle de versão |
| GitHub Actions | — | Integração contínua |
| Qodana | — | Análise estática de código |

> **Nota:** o `pom.xml` declara `java.version=21`. O pipeline de CI compila com Java 25
> para antecipação de compatibilidade, mas o código-fonte é compatível com Java 21.

---

## Pré-requisitos

- **Java 21+** — [Adoptium](https://adoptium.net/)
- **Git** — para clonar o repositório
- **Maven Wrapper** incluído (`./mvnw`) — não requer Maven instalado

---

## Início rápido

### 1. Clonar o repositório

```bash
git clone https://github.com/engguilhermebraga/agendamento-api.git
cd agendamento-api/agendamento-api
```

### 2. Executar (perfil dev — banco H2 em memória)

```bash
./mvnw spring-boot:run
```

### 3. Acessar os recursos

| Recurso | URL |
|---|---|
| Painel administrativo | http://localhost:8081/dashboard |
| Portal do cliente | http://localhost:8081/portal |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Console H2 | http://localhost:8081/h2-console |

> **Console H2:** JDBC URL: `jdbc:h2:mem:agendamentodb` · Usuário: `sa` · Senha: _(vazia)_

---

## Credenciais padrão (perfil dev)

| Usuário | Senha | Papel | Uso |
|---|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` | Painel admin — acesso total |
| `user` | `user123` | `ROLE_USER` | API — somente leitura (GET) |
| _(e-mail do cliente)_ | `cliente123` | — | Portal de autoatendimento |

> Senhas armazenadas com **BCrypt**. Usuários `admin` e `user` são in-memory (apenas para dev).

---

## Dados de demonstração (perfil dev)

Ao iniciar no perfil `dev`, o `DataInitializer` popula automaticamente o H2 com:

| Entidade | Qtd | Exemplos |
|---|---|---|
| Clientes | 3 | Maria Fernanda, João Pedro, Ana Carolina |
| Profissionais | 3 | Dra. Carla Mendes (Fisioterapeuta), Renata Costa (Cabeleireira), Dr. Lucas Pereira (Nutricionista) |
| Serviços | 4 | Fisioterapia 60min / R$150 · Corte 90min / R$80 · Nutrição 45min / R$200 · Massagem 60min / R$120 |
| Agendamentos | 4 | 1 CONFIRMADO + 3 AGENDADOS, todos com datas futuras |

---

## Endpoints da API REST

Base URL: `http://localhost:8081/api/v1`

### Autenticação

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth/login` | Gera token JWT | Público |

### Clientes, Profissionais, Serviços

Cada recurso expõe: `GET /` · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}`

- Leitura: `ROLE_USER` ou `ROLE_ADMIN`
- Escrita: `ROLE_ADMIN`

### Agendamentos

| Método | Rota | Descrição | Papel |
|---|---|---|---|
| GET | `/agendamentos` | Lista todos | USER/ADMIN |
| GET | `/agendamentos/{id}` | Busca por ID | USER/ADMIN |
| POST | `/agendamentos` | Cria com detecção de conflito | ADMIN |
| PUT | `/agendamentos/{id}` | Atualiza | ADMIN |
| PATCH | `/agendamentos/{id}/status` | Muda status | ADMIN |
| DELETE | `/agendamentos/{id}` | Cancela | ADMIN |

> Documentação completa e interativa em `http://localhost:8081/swagger-ui.html`

---

## Autenticação JWT

```bash
# 1. Obter token
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","senha":"admin123"}'

# 2. Usar token nas requisições
curl -X GET http://localhost:8081/api/v1/agendamentos \
  -H "Authorization: Bearer <token>"
```

**Configurações do JWT:** algoritmo HS512 · validade 24h · chave configurável via `jwt.secret`

---

## Executar os testes

```bash
# Todos os 138 testes
./mvnw test

# Classe específica
./mvnw test -Dtest=AgendamentoServiceTest

# Compilar sem testes
./mvnw package -DskipTests
```

Resultado esperado: `Tests run: 138, Failures: 0, Errors: 0, Skipped: 0`

---

## Regras de negócio

| Código | Regra |
|---|---|
| RN01 | Profissional não pode ter dois agendamentos com horários sobrepostos e status ativo |
| RN02 | Transições: `AGENDADO` → `CONFIRMADO` ou `CANCELADO`; `CONFIRMADO` → `CONCLUIDO` ou `CANCELADO` |
| RN03 | Agendamentos `CONCLUIDO` ou `CANCELADO` são imutáveis |
| RN04 | `dataHoraFim` calculado automaticamente: `dataHora + duracaoMinutos` do serviço |
| RN05 | Cancelamento pelo cliente exige antecedência mínima configurável (padrão: 2 horas) |
| RN06 | CPF e e-mail do cliente são únicos; e-mail do profissional é único; nome do serviço é único |

---

## Estrutura do projeto

```
agendamento-api/
├── src/main/java/com/guilhermebraga/agendamento_api/
│   ├── config/          # SecurityConfig, OpenApiConfig, DataInitializer
│   ├── controller/      # REST controllers (/api/v1/**)
│   ├── controller/web/  # MVC controllers do painel admin
│   ├── controller/portal/ # MVC controllers do portal do cliente
│   ├── dto/             # Request, Response e Form DTOs
│   ├── entity/          # Entidades JPA (Cliente, Profissional, Servico, Agendamento)
│   ├── exception/       # GlobalExceptionHandler, BusinessException
│   ├── mapper/          # Conversão entidade ↔ DTO
│   ├── repository/      # Spring Data JPA com queries JPQL
│   └── security/        # JwtAuthenticationFilter, JwtTokenProvider
├── src/main/resources/
│   ├── templates/       # Templates Thymeleaf (admin + portal)
│   ├── static/css/      # style.css (Bootstrap complementar)
│   └── application.properties
├── src/test/            # 138 testes (unit + repository + controller + integration)
└── .github/workflows/   # CI: build + testes + Qodana
```

---

## Perfis da aplicação

| Perfil | Banco | DDL | DataInitializer |
|---|---|---|---|
| `dev` (padrão) | H2 em memória | `create-drop` | Executa — popula dados demo |
| `test` (testes) | H2 em memória | `create-drop` | **Não executa** — banco limpo |
| `prod` | MySQL externo | `validate` | Não executa |

### Executar em produção com MySQL

```bash
java -jar agendamento-api-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://host:3306/agendamentodb \
  --spring.datasource.username=usuario \
  --spring.datasource.password=SENHA_SEGURA \
  --jwt.secret=CHAVE_COM_MINIMO_64_CARACTERES_PARA_HS512
```

---

## Contexto acadêmico

Este projeto é o Trabalho de Conclusão de Curso de **Guilherme Braga** para o
Curso de **Engenharia de Software** da **UNDB** (turma 2026).

O sistema demonstra a aplicação prática de:
- Arquitetura em camadas (Controller → Service → Repository)
- Padrões de projeto: Repository, Service Layer, DTO, Mapper, MVC
- API REST com versionamento `/api/v1/` e documentação OpenAPI 3.0
- Segurança com JWT e controle de acesso baseado em papéis (RBAC)
- Testes automatizados em 4 camadas: unitários, repositório, controller e integração
- Server-Side Rendering com Thymeleaf + Bootstrap 5
- Integração contínua com GitHub Actions e análise estática com Qodana

---

## Licença

MIT License — Copyright (c) 2026 Guilherme Braga
