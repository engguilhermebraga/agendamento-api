# Agendamento API

![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

API REST para gestão de agendamentos desenvolvida como Trabalho de Conclusão de Curso (TCC) na **UNDB**, turma 2026, por **Guilherme Braga**. O sistema oferece uma API RESTful completa para cadastro e controle de agendamentos, com um painel administrativo e um portal de autoatendimento para clientes, tudo em uma única aplicação Spring Boot. A documentação interativa é gerada automaticamente via Swagger UI, e 138 testes automatizados garantem a confiabilidade das regras de negócio.

---

## Funcionalidades

- 📅 **Agendamentos** — criação, consulta, atualização de status e cancelamento com validação de conflito de horários em tempo real
- 👤 **Clientes** — CRUD completo com validação de CPF e e-mail únicos
- 💼 **Profissionais** — CRUD completo com validação de e-mail único e especialidade
- 🛠️ **Serviços** — CRUD completo com duração em minutos e preço
- 🔐 **Segurança** — autenticação via JWT Bearer (HS512, 24 h) e HTTP Basic com controle de acesso por papéis (RBAC)
- 🖥️ **Painel Administrativo** — interface Thymeleaf + Bootstrap 5 para gestão completa
- 🌐 **Portal do Cliente** — fluxo de agendamento guiado em 4 passos, identificação por e-mail sem senha
- 📖 **Swagger UI** — documentação interativa gerada automaticamente via SpringDoc OpenAPI
- 🧪 **Testes** — 138 testes automatizados (JUnit 5 + Mockito + MockMvc + @SpringBootTest), todos aprovados
- 🗄️ **Múltiplos bancos** — H2 em memória para desenvolvimento, PostgreSQL para produção

---

## Stack Tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 25 (LTS) | Linguagem principal |
| Spring Boot | 4.0.5 | Framework da aplicação |
| Spring Framework | 7.x | Injeção de dependências, MVC, Security |
| Jakarta EE | 11 | Validação, persistência e servlet |
| Spring Data JPA | (managed) | Mapeamento objeto-relacional e repositórios |
| Hibernate | (managed) | Implementação JPA |
| Spring Security | (managed) | Autenticação JWT e HTTP Basic; RBAC |
| JJWT | 0.12.3 | Geração e validação de tokens JWT (HS512) |
| Thymeleaf | (managed) | Templates HTML server-side rendering |
| Bootstrap | 5 | Estilização das interfaces web e portal |
| SpringDoc OpenAPI | 2.8.6 | Swagger UI e especificação OpenAPI 3.0 |
| H2 Database | (managed) | Banco em memória (perfil dev) |
| PostgreSQL | (driver managed) | Banco relacional (perfil prod) |
| MapStruct | (managed) | Mapeamento entidade ↔ DTO em compile-time |
| Lombok | (managed) | Redução de código boilerplate |
| JUnit 5 | (managed) | Framework de testes unitários |
| Mockito | (managed) | Mocks e stubs para testes |
| MockMvc | (managed) | Testes de camada web (controllers) |

---

## Pré-requisitos

- **Java 21** ou superior — [Download OpenJDK via Adoptium](https://adoptium.net/)
- **Maven 3.9+** — ou use o wrapper `./mvnw` incluído no projeto (não requer instalação)
- **Git** — para clonar o repositório

> Para execução em produção, é necessário um servidor **PostgreSQL** acessível com banco criado previamente.

---

## Início Rápido

### 1. Clone o repositório

```bash
git clone https://github.com/engguilhermebraga/agendamento-api.git
cd agendamento-api/agendamento-api
```

### 2. Execute a aplicação (perfil dev — H2 em memória)

```bash
./mvnw spring-boot:run
```

Ou compile e execute o JAR diretamente:

```bash
./mvnw clean package -DskipTests
java -jar target/agendamento-api-1.0.0.jar
```

### 3. Acesse os recursos disponíveis

| Recurso | URL |
|---|---|
| Painel administrativo | http://localhost:8081/dashboard |
| Portal do cliente | http://localhost:8081/portal |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Especificação OpenAPI (JSON) | http://localhost:8081/api-docs |
| Console H2 (somente dev) | http://localhost:8081/h2-console/ |

> **Console H2:** na tela de login, preencha a JDBC URL com `jdbc:h2:mem:agendamentodb`, usuário `sa` e senha em branco.

---

## Credenciais Padrão (perfil dev)

| Usuário | Senha | Papel | Permissões na API REST |
|---|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` | GET, POST, PUT, PATCH, DELETE |
| `user` | `user123` | `ROLE_USER` | Somente GET |
| _(qualquer e-mail de cliente cadastrado)_ | _(sem senha)_ | Portal do cliente | Autoatendimento via portal |

> As senhas são armazenadas com hash **BCrypt**. Os usuários `admin` e `user` são criados em memória (`InMemoryUserDetailsManager`) e destinam-se exclusivamente ao ambiente de desenvolvimento.

---

## Dados de Demonstração (perfil dev)

Ao iniciar no perfil `dev`, o `DataInitializer` popula automaticamente o banco H2 com:

| Entidade | Qtd | Detalhes |
|---|---|---|
| Clientes | 3 | Maria Fernanda Oliveira, João Pedro Santos, Ana Carolina Lima |
| Profissionais | 3 | Dra. Carla Mendes (Fisioterapeuta), Renata Costa (Cabeleireira), Dr. Lucas Pereira (Nutricionista) |
| Serviços | 4 | Sessão de Fisioterapia (60 min, R$ 150,00), Corte de Cabelo (90 min, R$ 80,00), Consulta Nutricional (45 min, R$ 200,00), Massagem Relaxante (60 min, R$ 120,00) |
| Agendamentos | 4 | 1 CONFIRMADO + 3 AGENDADOS, todos para datas futuras |

---

## Resumo dos Endpoints da API

Base URL: `http://localhost:8081/api/v1`

Todos os endpoints REST, exceto o login, exigem autenticação via JWT Bearer ou HTTP Basic.

### Autenticação

| Método | URL | Descrição | Autenticação |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Obtém token JWT | Pública |

### Clientes

| Método | URL | Descrição | Papel mínimo |
|---|---|---|---|
| GET | `/api/v1/clientes` | Lista todos os clientes | ROLE_USER |
| GET | `/api/v1/clientes/{id}` | Busca cliente por ID | ROLE_USER |
| POST | `/api/v1/clientes` | Cadastra novo cliente | ROLE_ADMIN |
| PUT | `/api/v1/clientes/{id}` | Atualiza cliente | ROLE_ADMIN |
| DELETE | `/api/v1/clientes/{id}` | Remove cliente | ROLE_ADMIN |

### Profissionais

| Método | URL | Descrição | Papel mínimo |
|---|---|---|---|
| GET | `/api/v1/profissionais` | Lista todos os profissionais | ROLE_USER |
| GET | `/api/v1/profissionais/{id}` | Busca profissional por ID | ROLE_USER |
| POST | `/api/v1/profissionais` | Cadastra novo profissional | ROLE_ADMIN |
| PUT | `/api/v1/profissionais/{id}` | Atualiza profissional | ROLE_ADMIN |
| DELETE | `/api/v1/profissionais/{id}` | Remove profissional | ROLE_ADMIN |

### Serviços

| Método | URL | Descrição | Papel mínimo |
|---|---|---|---|
| GET | `/api/v1/servicos` | Lista todos os serviços | ROLE_USER |
| GET | `/api/v1/servicos/{id}` | Busca serviço por ID | ROLE_USER |
| POST | `/api/v1/servicos` | Cadastra novo serviço | ROLE_ADMIN |
| PUT | `/api/v1/servicos/{id}` | Atualiza serviço | ROLE_ADMIN |
| DELETE | `/api/v1/servicos/{id}` | Remove serviço | ROLE_ADMIN |

### Agendamentos

| Método | URL | Descrição | Papel mínimo |
|---|---|---|---|
| GET | `/api/v1/agendamentos` | Lista todos os agendamentos | ROLE_USER |
| GET | `/api/v1/agendamentos/{id}` | Busca agendamento por ID | ROLE_USER |
| POST | `/api/v1/agendamentos` | Cria novo agendamento | ROLE_ADMIN |
| PUT | `/api/v1/agendamentos/{id}` | Atualiza agendamento | ROLE_ADMIN |
| PATCH | `/api/v1/agendamentos/{id}/status` | Atualiza apenas o status | ROLE_ADMIN |
| DELETE | `/api/v1/agendamentos/{id}` | Cancela agendamento | ROLE_ADMIN |

---

## Autenticação

A API suporta dois mecanismos de autenticação que coexistem na mesma instância:

### JWT Bearer Token (recomendado para clientes REST)

1. Faça login em `POST /api/v1/auth/login` enviando `email` e `senha` no corpo JSON
2. Copie o campo `token` da resposta
3. Inclua o token no header `Authorization` de todas as requisições subsequentes:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Configurações do JWT:**
- Algoritmo de assinatura: **HS512**
- Expiração: **24 horas** a partir da emissão
- Payload: `subject` contém o nome do usuário autenticado
- Chave secreta configurável via propriedade `jwt.secret`

### HTTP Basic Auth (alternativa para Swagger UI e testes manuais)

Inclua as credenciais codificadas em Base64 no header de cada requisição:

```
Authorization: Basic <base64(usuario:senha)>
```

Exemplo com `admin:admin123`:
```
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

> No Swagger UI, clique no botão **Authorize** (cadeado) e informe usuário e senha diretamente.

---

## Estrutura do Projeto

```
agendamento-api/
├── src/
│   ├── main/
│   │   ├── java/com/guilhermebraga/agendamento_api/
│   │   │   ├── AgendamentoApiApplication.java        # Entry point (@SpringBootApplication)
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java              # Dados de demo (@Profile("dev"))
│   │   │   │   ├── H2ConsoleConfig.java              # Registro manual do servlet H2
│   │   │   │   ├── OpenApiConfig.java                # Metadados do Swagger/OpenAPI
│   │   │   │   ├── SecurityConfig.java               # JWT + HTTP Basic + RBAC + CORS
│   │   │   │   └── WebConfig.java                    # Configurações MVC adicionais
│   │   │   ├── controller/
│   │   │   │   ├── AgendamentoController.java         # REST /api/v1/agendamentos
│   │   │   │   ├── AuthController.java               # REST /api/v1/auth
│   │   │   │   ├── ClienteController.java            # REST /api/v1/clientes
│   │   │   │   ├── ProfissionalController.java       # REST /api/v1/profissionais
│   │   │   │   ├── ServicoController.java            # REST /api/v1/servicos
│   │   │   │   ├── portal/
│   │   │   │   │   ├── PortalAgendamentoController.java   # Wizard de agendamento
│   │   │   │   │   ├── PortalClienteController.java       # Cadastro de cliente no portal
│   │   │   │   │   ├── PortalHomeController.java          # Home e identificação do portal
│   │   │   │   │   └── PortalMeusAgendamentosController.java
│   │   │   │   └── web/controller/
│   │   │   │       ├── AgendamentoWebController.java  # Admin: /web/agendamentos
│   │   │   │       ├── ClienteWebController.java      # Admin: /web/clientes
│   │   │   │       ├── DashboardWebController.java    # Admin: /dashboard
│   │   │   │       ├── ProfissionalWebController.java # Admin: /web/profissionais
│   │   │   │       └── ServicoWebController.java      # Admin: /web/servicos
│   │   │   ├── dto/
│   │   │   │   ├── form/
│   │   │   │   │   ├── NovoAgendamentoForm.java
│   │   │   │   │   └── WizardForm.java
│   │   │   │   ├── request/
│   │   │   │   │   ├── AgendamentoRequest.java
│   │   │   │   │   ├── ClienteRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── ProfissionalRequest.java
│   │   │   │   │   └── ServicoRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── AgendamentoResponse.java
│   │   │   │       ├── ClienteResponse.java
│   │   │   │       ├── LoginResponse.java
│   │   │   │       ├── ProfissionalResponse.java
│   │   │   │       └── ServicoResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Agendamento.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Profissional.java
│   │   │   │   ├── Servico.java
│   │   │   │   └── StatusAgendamento.java            # Enum: AGENDADO, CONFIRMADO, CONCLUIDO, CANCELADO
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java            # Violação de regra de negócio → 409
│   │   │   │   ├── GlobalExceptionHandler.java       # @RestControllerAdvice centralizado
│   │   │   │   └── ResourceNotFoundException.java    # Recurso não encontrado → 404
│   │   │   ├── mapper/
│   │   │   │   ├── AgendamentoMapper.java            # MapStruct: Agendamento ↔ DTO
│   │   │   │   ├── ClienteMapper.java
│   │   │   │   ├── ProfissionalMapper.java
│   │   │   │   └── ServicoMapper.java
│   │   │   ├── repository/
│   │   │   │   ├── AgendamentoRepository.java
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   ├── ProfissionalRepository.java
│   │   │   │   └── ServicoRepository.java
│   │   │   └── security/
│   │   │       ├── JwtAuthenticationFilter.java      # Intercepta e valida Bearer tokens
│   │   │       └── JwtTokenProvider.java             # Geração e validação de JWTs (HS512)
│   │   └── resources/
│   │       ├── application.properties                # Configuração do perfil dev
│   │       ├── logback-spring.xml
│   │       ├── static/css/style.css
│   │       └── templates/
│   │           ├── agendamentos/                     # formulario.html, editar.html, listar.html
│   │           ├── clientes/                         # formulario.html, listar.html, detalhe.html
│   │           ├── error/                            # 403.html, 404.html, 500.html
│   │           ├── layout/                           # base.html, dashboard.html, fragments.html
│   │           ├── portal/
│   │           │   └── wizard/                       # step1.html a step4.html
│   │           ├── profissionais/
│   │           └── servicos/
│   └── test/
│       └── java/com/guilhermebraga/agendamento_api/
│           ├── AgendamentoApiApplicationTests.java
│           ├── AgendamentoControllerTest.java        # MockMvc: endpoints REST
│           ├── AgendamentoIntegrationTest.java       # @SpringBootTest: integração completa
│           ├── AgendamentoRepositoryTest.java        # Consultas JPA
│           ├── AgendamentoServiceTest.java           # Regras de negócio com Mockito
│           ├── ClienteControllerTest.java
│           ├── ClienteRepositoryTest.java
│           ├── ClienteServiceTest.java
│           ├── ProfissionalControllerTest.java
│           └── ServicoControllerTest.java
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---

## Como Executar os Testes

```bash
# Executar todos os 138 testes
./mvnw test

# Executar uma classe de teste específica
./mvnw test -Dtest=AgendamentoServiceTest

# Executar com relatório de cobertura (Surefire)
./mvnw verify

# Compilar sem executar os testes
./mvnw package -DskipTests
```

Os testes utilizam o banco H2 em memória de forma totalmente isolada, sem necessidade de conexão externa. Cada classe de teste é autossuficiente.

---

## Perfis da Aplicação

### Perfil `dev` (padrão — ativo em `application.properties`)

| Configuração | Valor |
|---|---|
| Banco de dados | H2 em memória (`jdbc:h2:mem:agendamentodb`) |
| DDL | `create-drop` (esquema recriado a cada start) |
| Console H2 | Disponível em `/h2-console/` |
| Dados de demo | Populados automaticamente pelo `DataInitializer` |
| Log SQL | Ativado (`show-sql=true`, SQL formatado) |
| Cache Thymeleaf | Desativado (templates recarregados a cada requisição) |
| Porta do servidor | 8081 |
| Horário de atendimento | 08:00 às 18:00 |
| Antecedência mínima para cancelamento | 2 horas |

### Perfil `prod`

Para executar em produção com PostgreSQL, passe as propriedades via linha de comando ou variáveis de ambiente:

```bash
java -jar agendamento-api-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:postgresql://host:5432/agendamentodb \
  --spring.datasource.username=postgres \
  --spring.datasource.password=SUA_SENHA_SEGURA \
  --spring.jpa.hibernate.ddl-auto=validate \
  --jwt.secret=SUA_CHAVE_SECRETA_COM_PELO_MENOS_64_CARACTERES_PARA_HS512
```

> Em produção: console H2 é desativado, `DataInitializer` não é executado, logs SQL devem ser desativados, e o `ddl-auto` deve ser `validate` ou `none` para evitar perda de dados.

---

## Regras de Negócio

| Código | Descrição |
|---|---|
| RN01 | Não é permitido agendar dois serviços para o mesmo profissional em horários sobrepostos. O intervalo é calculado como `dataHora` até `dataHoraFim` |
| RN02 | Transições de status permitidas: `AGENDADO` → `CONFIRMADO` ou `CANCELADO`; `CONFIRMADO` → `CONCLUIDO` ou `CANCELADO` |
| RN03 | Agendamentos com status `CONCLUIDO` ou `CANCELADO` são imutáveis — nenhuma edição é permitida |
| RN04 | `dataHoraFim` é calculada automaticamente: `dataHora + duracaoMinutos` do serviço selecionado |
| RN05 | O portal do cliente identifica o usuário por e-mail cadastrado, sem necessidade de senha |

---

## Interfaces Web

### Painel Administrativo (acesso em `/dashboard`)

| URL | Descrição |
|---|---|
| `/dashboard` | Dashboard com visão geral |
| `/web/clientes` | Listagem e gestão de clientes |
| `/web/profissionais` | Listagem e gestão de profissionais |
| `/web/servicos` | Listagem e gestão de serviços |
| `/web/agendamentos/listar` | Listagem e gestão de agendamentos |

### Portal do Cliente (acesso em `/portal`)

| URL | Descrição |
|---|---|
| `/portal` | Página inicial do portal |
| `/portal/step1` até `/portal/step4` | Wizard de agendamento em 4 passos |
| `/portal/meus-agendamentos` | Histórico e cancelamento de agendamentos |

### Páginas de Erro

| URL | Descrição |
|---|---|
| `/error/403` | Acesso negado |
| `/error/404` | Recurso não encontrado |
| `/error/500` | Erro interno do servidor |

---

## Contexto do TCC

Este projeto é o Trabalho de Conclusão de Curso de **Guilherme Braga** para a instituição **UNDB** (turma 2026). O trabalho demonstra a aplicação prática de conceitos de Engenharia de Software em um sistema real, incluindo:

- Arquitetura em camadas (Controller → Service → Repository)
- Padrões REST com versionamento de API (`/api/v1/`)
- Segurança com JWT e controle de acesso baseado em papéis (RBAC)
- Documentação automática de API com OpenAPI 3.0 / Swagger
- Testes automatizados com cobertura de unitários, repositórios e integração end-to-end
- Interface web com server-side rendering (Thymeleaf + Bootstrap 5)
- Separação de ambientes (perfis Spring: dev / prod)
- Boas práticas Java moderno com Jakarta EE 11 e Spring Boot 4.x

---

## Licença

Este projeto está licenciado sob a **Licença MIT**.

```
MIT License

Copyright (c) 2026 Guilherme Braga

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

> Desenvolvido por **Guilherme Braga** — TCC UNDB 2026
