# Agendamento API

Aplicação web para gestão de agendamentos de serviços, desenvolvida como Trabalho de Conclusão de Curso em Engenharia de Software — UNDB 2026.

O sistema é composto por uma **API REST**, um **painel administrativo** e um **portal de autoatendimento do cliente**, todos compartilhando a mesma base de regras de negócio e persistência.

---

## Stack

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 21 (LTS) | Linguagem principal |
| Spring Boot | 4.0.5 | Framework base + servidor embutido |
| Spring Web MVC | — | Camada REST e MVC |
| Spring Data JPA + Hibernate | — | Persistência e ORM |
| Spring Security | — | Autenticação e autorização |
| JJWT | 0.12.3 | Tokens JWT para a API REST |
| Thymeleaf | — | Templates HTML server-side |
| Bootstrap | 5.3.3 | Estilização das interfaces |
| SpringDoc OpenAPI | 2.8.6 | Documentação automática (Swagger UI) |
| H2 Database | — | Banco em memória (dev/testes) |
| MySQL Connector | — | Driver para banco persistente (prod) |
| Lombok | — | Redução de boilerplate |
| JUnit 5 + Mockito | — | Testes unitários e de integração |
| Maven | 3.9.x | Build e dependências |
| GitHub Actions + Qodana | — | Análise estática de qualidade |

---

## Pré-requisitos

- Java 21 ou superior — [Adoptium](https://adoptium.net/)
- Git
- Maven Wrapper incluído (não requer instalação separada)

---

## Executar localmente

```bash
# 1. Clonar o repositório
git clone https://github.com/engguilhermebraga/agendamento-api.git
cd agendamento-api/agendamento-api

# 2. Executar no perfil de desenvolvimento (H2 em memória)
./mvnw spring-boot:run

# 3. Executar os testes
./mvnw test
# Esperado: Tests run: 138, Failures: 0, Errors: 0, Skipped: 0
```

---

## URLs disponíveis após inicialização

| Recurso | URL |
|---|---|
| Painel administrativo | http://localhost:8081/dashboard |
| Portal do cliente | http://localhost:8081/portal |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/api-docs |
| Console H2 | http://localhost:8081/h2-console |

---

## Credenciais de desenvolvimento

**Painel administrativo (Form Login):**

| Usuário | Senha | Papel |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `user` | `user123` | ROLE_USER |

**Portal do cliente:** qualquer e-mail dos clientes criados pelo `DataInitializer` com senha `cliente123`.

**API REST — obter token JWT:**

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Usar o token retornado no header `Authorization: Bearer <token>`.

---

## Estrutura de pacotes

```
com.guilhermebraga.agendamento_api/
├── controller/          # Controllers REST (@RestController) — JSON
├── controller/web/      # Controllers MVC do painel administrativo
├── controller/portal/   # Controllers MVC do portal do cliente
├── service/             # Regras de negócio
├── repository/          # Interfaces Spring Data JPA + queries JPQL
├── entity/              # Entidades JPA
├── dto/request/         # Objetos de entrada (Bean Validation)
├── dto/response/        # Objetos de saída
├── dto/form/            # Formulários multi-etapa (@SessionAttributes)
├── mapper/              # Conversão entidade ↔ DTO
├── security/            # Filtro JWT e provedor de tokens
├── exception/           # Exceções de domínio e GlobalExceptionHandler
└── config/              # SecurityConfig, OpenApiConfig, MVC config
```

---

## API REST — Endpoints

Base: `/api/v1/`

| Grupo | Métodos | Acesso |
|---|---|---|
| `POST /auth/login` | Autenticar, obter JWT | Público |
| `/clientes` | GET, POST, PUT, DELETE | USER/ADMIN |
| `/profissionais` | GET, POST, PUT, DELETE | USER/ADMIN |
| `/servicos` | GET, POST, PUT, DELETE | USER/ADMIN |
| `/agendamentos` | GET, POST, PUT, PATCH /status, DELETE | USER/ADMIN |

Operações de escrita (POST, PUT, PATCH, DELETE) exigem `ROLE_ADMIN`.

---

## Domínio

### Entidades

| Entidade | Descrição |
|---|---|
| `Cliente` | Pessoa que realiza agendamentos |
| `Profissional` | Prestador do serviço |
| `Servico` | Tipo de atendimento com duração e preço |
| `Agendamento` | Relaciona cliente + profissional + serviço + data/hora |

### Ciclo de vida do Agendamento

```
          criar
            ↓
        AGENDADO ──────────────────→ CANCELADO
            │
     admin confirma
            ↓
       CONFIRMADO ─────────────────→ CANCELADO
            │
      admin conclui
            ↓
        CONCLUIDO  (estado final)
```

### Regra de conflito de horário

O campo `dataHoraFim` é calculado como `dataHora + duracaoMinutos` e persistido na tabela `agendamento`. A verificação de sobreposição usa consulta JPQL com o algoritmo padrão de overlap de intervalos, portável entre H2 e MySQL. Apenas agendamentos com status `AGENDADO` ou `CONFIRMADO` bloqueiam horários.

---

## Testes

```bash
./mvnw test
```

138 testes organizados em 4 camadas:

| Camada | Ferramenta | Escopo |
|---|---|---|
| Unitária | JUnit 5 + Mockito | Serviços em isolamento (sem infraestrutura) |
| Repositório | JUnit 5 + H2 real | Consultas JPQL com rollback automático |
| Controlador | MockMvc + @WithMockUser | Status HTTP, serialização, controle de acesso |
| Integração | MockMvc + H2 real | Fluxo completo criação → transições de status |

---

## Qualidade de código

O projeto usa [Qodana](https://www.jetbrains.com/qodana/) via GitHub Actions para análise estática a cada push. O workflow está em `.github/workflows/qodana_code_quality.yml`.

---

## Configuração de banco para produção

Criar um arquivo `application-prod.properties` (não versionar) com:

```properties
spring.datasource.url=jdbc:mysql://host:3306/agendamento_db
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
jwt.secret=${JWT_SECRET}
```

---

## Autor

**Guilherme Braga**
Engenharia de Software — UNDB 2026
Orientador: Prof. Rodrigo Justino

---

## Licença

MIT License
## Licença

MIT License — Copyright (c) 2026 Guilherme Braga
