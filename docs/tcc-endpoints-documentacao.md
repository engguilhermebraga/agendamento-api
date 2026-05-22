# Documentação de Endpoints — Agendamento API

**TCC UNDB 2026 — Guilherme Braga**
**Versão da API:** 1.0.0
**Base URL:** `http://localhost:8081/api/v1`
**Especificação OpenAPI interativa:** http://localhost:8081/swagger-ui.html

---

## Sumário

1. [Visão Geral da Autenticação](#1-visão-geral-da-autenticação)
2. [Formato Padrão das Respostas de Erro](#2-formato-padrão-das-respostas-de-erro)
3. [Autenticação — /api/v1/auth](#3-autenticação)
4. [Clientes — /api/v1/clientes](#4-clientes)
5. [Profissionais — /api/v1/profissionais](#5-profissionais)
6. [Serviços — /api/v1/servicos](#6-serviços)
7. [Agendamentos — /api/v1/agendamentos](#7-agendamentos)
8. [Como testar com curl](#8-como-testar-com-curl)
9. [Como testar com o Swagger UI](#9-como-testar-com-o-swagger-ui)
10. [Tabela consolidada de endpoints](#10-tabela-consolidada-de-endpoints)

---

## 1. Visão Geral da Autenticação

A API suporta dois mecanismos de autenticação simultâneos:

### JWT Bearer Token (recomendado)

Obtenha um token via `POST /api/v1/auth/login` e inclua-o no header `Authorization` de cada requisição:

```
Authorization: Bearer <token>
```

**Características do JWT:**
- Algoritmo de assinatura: **HS512** (HMAC com SHA-512)
- Expiração: **24 horas** a partir da emissão
- Payload: campo `sub` contém o nome do usuário (`admin` ou `user`)
- Chave secreta: configurável via propriedade `jwt.secret` no `application.properties`

### HTTP Basic Auth (alternativa)

Envie as credenciais codificadas em Base64 no header:

```
Authorization: Basic <base64(usuario:senha)>
```

| Usuário | Senha | Papel | Permissões |
|---|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` | GET, POST, PUT, PATCH, DELETE |
| `user` | `user123` | `ROLE_USER` | Somente GET |

### Regras de autorização (RBAC)

| Método HTTP | Papel exigido |
|---|---|
| GET | `ROLE_USER` ou `ROLE_ADMIN` |
| POST | `ROLE_ADMIN` |
| PUT | `ROLE_ADMIN` |
| PATCH | `ROLE_ADMIN` |
| DELETE | `ROLE_ADMIN` |

### Caminhos públicos (sem autenticação)

- `POST /api/v1/auth/login`
- `/swagger-ui.html`, `/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**`
- `/h2-console/**` (somente perfil dev)
- `/dashboard/**`, `/portal/**`, `/web/**` e recursos estáticos (`/css/**`, `/js/**`)

---

## 2. Formato Padrão das Respostas de Erro

Todos os erros nos endpoints REST retornam um JSON com a seguinte estrutura:

```json
{
  "timestamp": "2026-05-22T14:30:00.123",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Descrição legível do erro",
  "detalhes": {
    "campo1": "Mensagem de validação do campo1",
    "campo2": "Mensagem de validação do campo2"
  }
}
```

> O campo `detalhes` aparece apenas em erros de validação (`400`). Para `404`, `409` e `500`, apenas `timestamp`, `status`, `erro` e `mensagem` são retornados.

| Código HTTP | Significado | Quando ocorre |
|---|---|---|
| `400 Bad Request` | Dados de entrada inválidos | Falha na validação `@Valid` — campo obrigatório ausente, formato incorreto, valor fora do intervalo |
| `401 Unauthorized` | Não autenticado | Token JWT ausente, expirado ou inválido; credenciais Basic incorretas |
| `403 Forbidden` | Sem permissão | Usuário autenticado mas sem o papel necessário para a operação (ex: `ROLE_USER` tentando POST) |
| `404 Not Found` | Recurso inexistente | ID fornecido não corresponde a nenhum registro no banco de dados |
| `409 Conflict` | Conflito de regra de negócio | Duplicidade de CPF/e-mail/nome; conflito de horário; transição de status inválida; agendamento imutável |
| `500 Internal Server Error` | Erro inesperado | Exceção não tratada na camada de serviço ou infraestrutura |

---

## 3. Autenticação

### POST `/api/v1/auth/login`

Autentica um usuário e retorna um token JWT válido por 24 horas.

| Atributo | Valor |
|---|---|
| **Método** | POST |
| **URL completa** | `http://localhost:8081/api/v1/auth/login` |
| **Autenticação** | Nenhuma (endpoint público) |
| **Content-Type** | `application/json` |

**Corpo da requisição:**

```json
{
  "email": "admin",
  "senha": "admin123"
}
```

> Atenção: o campo se chama `email` no DTO por convenção de nomenclatura, mas o `InMemoryUserDetailsManager` usa o nome de usuário (`admin` ou `user`) como identificador. Informe o nome do usuário neste campo.

| Campo | Tipo | Obrigatório | Validações |
|---|---|---|---|
| `email` | string | Sim | Não pode ser vazio (`@NotBlank`) |
| `senha` | string | Sim | Não pode ser vazia (`@NotBlank`) |

**Resposta de sucesso — `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxNjM5MjAwMCwiZXhwIjoxNzE2NDc4NDAwfQ.ASSINATURA_HS512",
  "tipo": "Bearer",
  "expiracao": "2026-05-23T14:00:00.000"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `token` | string | Token JWT a ser usado no header `Authorization: Bearer <token>` |
| `tipo` | string | Sempre `"Bearer"` |
| `expiracao` | string (ISO 8601) | Data e hora de expiração do token (24 h após a emissão) |

**Resposta de erro — `400 Bad Request` (campos ausentes ou vazios):**

```json
{
  "timestamp": "2026-05-22T14:00:00.000",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação",
  "detalhes": {
    "email": "O e-mail é obrigatório.",
    "senha": "A senha é obrigatória."
  }
}
```

**Resposta de erro — `401 Unauthorized` (credenciais incorretas):**

O Spring Security intercepta antes do controller; o corpo varia conforme a configuração interna do framework.

---

## 4. Clientes

Base URL do recurso: `/api/v1/clientes`

---

### POST `/api/v1/clientes`

Cadastra um novo cliente no sistema. CPF e e-mail devem ser únicos entre todos os clientes.

| Atributo | Valor |
|---|---|
| **Método** | POST |
| **URL completa** | `http://localhost:8081/api/v1/clientes` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

**Corpo da requisição:**

```json
{
  "nome": "Maria Fernanda Oliveira",
  "email": "maria.fernanda@email.com",
  "telefone": "98988881111",
  "cpf": "11122233344"
}
```

| Campo | Tipo | Obrigatório | Validações |
|---|---|---|---|
| `nome` | string | Sim | 3 a 100 caracteres |
| `email` | string | Sim | Formato de e-mail válido (`@Email`); máx. 150 caracteres; deve ser único |
| `telefone` | string | Sim | 10 a 20 caracteres |
| `cpf` | string | Sim | Exatamente 11 dígitos numéricos sem pontuação (ex.: `11122233344`); deve ser único |

**Resposta de sucesso — `201 Created`:**

```json
{
  "id": 1,
  "nome": "Maria Fernanda Oliveira",
  "email": "maria.fernanda@email.com",
  "telefone": "98988881111",
  "cpf": "11122233344",
  "criadoEm": "2026-05-22T10:00:00.000",
  "atualizadoEm": "2026-05-22T10:00:00.000"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único gerado automaticamente |
| `nome` | string | Nome completo do cliente |
| `email` | string | E-mail do cliente |
| `telefone` | string | Telefone do cliente |
| `cpf` | string | CPF do cliente (11 dígitos) |
| `criadoEm` | string (ISO 8601) | Data e hora de criação do registro |
| `atualizadoEm` | string (ISO 8601) | Data e hora da última atualização |

**Resposta de erro — `400 Bad Request` (dados inválidos):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação",
  "detalhes": {
    "cpf": "O CPF deve conter exatamente 11 dígitos numéricos.",
    "email": "Informe um e-mail válido."
  }
}
```

**Resposta de erro — `409 Conflict` (CPF ou e-mail duplicado):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Já existe um cliente com o CPF informado."
}
```

---

### GET `/api/v1/clientes`

Retorna a lista completa de todos os clientes cadastrados.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/clientes` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

**Resposta de sucesso — `200 OK`:**

```json
[
  {
    "id": 1,
    "nome": "Maria Fernanda Oliveira",
    "email": "maria.fernanda@email.com",
    "telefone": "98988881111",
    "cpf": "11122233344",
    "criadoEm": "2026-05-22T10:00:00.000",
    "atualizadoEm": "2026-05-22T10:00:00.000"
  },
  {
    "id": 2,
    "nome": "João Pedro Santos",
    "email": "joao.pedro@email.com",
    "telefone": "98988882222",
    "cpf": "22233344455",
    "criadoEm": "2026-05-22T10:00:01.000",
    "atualizadoEm": "2026-05-22T10:00:01.000"
  }
]
```

> Retorna um array vazio `[]` quando não há clientes cadastrados.

---

### GET `/api/v1/clientes/{id}`

Retorna os dados de um cliente específico pelo seu identificador.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/clientes/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único do cliente |

**Resposta de sucesso — `200 OK`:**

```json
{
  "id": 1,
  "nome": "Maria Fernanda Oliveira",
  "email": "maria.fernanda@email.com",
  "telefone": "98988881111",
  "cpf": "11122233344",
  "criadoEm": "2026-05-22T10:00:00.000",
  "atualizadoEm": "2026-05-22T10:00:00.000"
}
```

**Resposta de erro — `404 Not Found`:**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Cliente com ID 99 não encontrado."
}
```

---

### PUT `/api/v1/clientes/{id}`

Atualiza todos os dados de um cliente existente. O CPF e o e-mail devem continuar únicos em relação aos demais clientes.

| Atributo | Valor |
|---|---|
| **Método** | PUT |
| **URL completa** | `http://localhost:8081/api/v1/clientes/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do cliente a ser atualizado |

**Corpo da requisição:** mesmo esquema do `POST /api/v1/clientes` (todos os campos obrigatórios).

**Resposta de sucesso — `200 OK`:** mesmo esquema do `GET /api/v1/clientes/{id}`.

**Respostas de erro possíveis:**

| Código | Situação |
|---|---|
| `400` | Dados de entrada inválidos |
| `404` | Cliente com o ID informado não existe |
| `409` | CPF ou e-mail já pertence a outro cliente |

---

### DELETE `/api/v1/clientes/{id}`

Remove permanentemente um cliente do sistema.

| Atributo | Valor |
|---|---|
| **Método** | DELETE |
| **URL completa** | `http://localhost:8081/api/v1/clientes/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do cliente a ser removido |

**Resposta de sucesso — `204 No Content`:** corpo vazio.

**Resposta de erro — `404 Not Found`:** mesmo formato dos exemplos anteriores.

---

## 5. Profissionais

Base URL do recurso: `/api/v1/profissionais`

---

### POST `/api/v1/profissionais`

Cadastra um novo profissional. O e-mail deve ser único entre todos os profissionais.

| Atributo | Valor |
|---|---|
| **Método** | POST |
| **URL completa** | `http://localhost:8081/api/v1/profissionais` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

**Corpo da requisição:**

```json
{
  "nome": "Dra. Carla Mendes",
  "especialidade": "Fisioterapeuta",
  "email": "carla.mendes@clinica.com",
  "telefone": "98977771111"
}
```

| Campo | Tipo | Obrigatório | Validações |
|---|---|---|---|
| `nome` | string | Sim | 3 a 100 caracteres |
| `especialidade` | string | Sim | 3 a 100 caracteres |
| `email` | string | Sim | Formato de e-mail válido; máx. 150 caracteres; deve ser único |
| `telefone` | string | Sim | 10 a 20 caracteres |

**Resposta de sucesso — `201 Created`:**

```json
{
  "id": 1,
  "nome": "Dra. Carla Mendes",
  "especialidade": "Fisioterapeuta",
  "email": "carla.mendes@clinica.com",
  "telefone": "98977771111",
  "criadoEm": "2026-05-22T10:00:00.000",
  "atualizadoEm": "2026-05-22T10:00:00.000"
}
```

**Resposta de erro — `400 Bad Request` (dados inválidos):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação",
  "detalhes": {
    "nome": "O nome deve ter entre 3 e 100 caracteres.",
    "especialidade": "A especialidade é obrigatória."
  }
}
```

**Resposta de erro — `409 Conflict` (e-mail duplicado):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Já existe um profissional com o e-mail informado."
}
```

---

### GET `/api/v1/profissionais`

Retorna a lista completa de profissionais cadastrados.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/profissionais` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

**Resposta de sucesso — `200 OK`:**

```json
[
  {
    "id": 1,
    "nome": "Dra. Carla Mendes",
    "especialidade": "Fisioterapeuta",
    "email": "carla.mendes@clinica.com",
    "telefone": "98977771111",
    "criadoEm": "2026-05-22T10:00:00.000",
    "atualizadoEm": "2026-05-22T10:00:00.000"
  },
  {
    "id": 2,
    "nome": "Renata Costa",
    "especialidade": "Cabeleireira",
    "email": "renata.costa@salao.com",
    "telefone": "98977772222",
    "criadoEm": "2026-05-22T10:00:01.000",
    "atualizadoEm": "2026-05-22T10:00:01.000"
  },
  {
    "id": 3,
    "nome": "Dr. Lucas Pereira",
    "especialidade": "Nutricionista",
    "email": "lucas.pereira@nutri.com",
    "telefone": "98977773333",
    "criadoEm": "2026-05-22T10:00:02.000",
    "atualizadoEm": "2026-05-22T10:00:02.000"
  }
]
```

---

### GET `/api/v1/profissionais/{id}`

Retorna os dados de um profissional específico.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/profissionais/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único do profissional |

**Resposta de sucesso — `200 OK`:** mesmo esquema do item único no array de `GET /api/v1/profissionais`.

**Resposta de erro — `404 Not Found`:**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Profissional com ID 99 não encontrado."
}
```

---

### PUT `/api/v1/profissionais/{id}`

Atualiza todos os dados de um profissional existente.

| Atributo | Valor |
|---|---|
| **Método** | PUT |
| **URL completa** | `http://localhost:8081/api/v1/profissionais/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do profissional a ser atualizado |

**Corpo da requisição:** mesmo esquema do `POST /api/v1/profissionais`.

**Resposta de sucesso — `200 OK`:** mesmo esquema do `GET /api/v1/profissionais/{id}`.

**Respostas de erro possíveis:** `400`, `404`, `409`

---

### DELETE `/api/v1/profissionais/{id}`

Remove permanentemente um profissional do sistema.

| Atributo | Valor |
|---|---|
| **Método** | DELETE |
| **URL completa** | `http://localhost:8081/api/v1/profissionais/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do profissional a ser removido |

**Resposta de sucesso — `204 No Content`:** corpo vazio.

---

## 6. Serviços

Base URL do recurso: `/api/v1/servicos`

---

### POST `/api/v1/servicos`

Cadastra um novo serviço oferecido. O nome deve ser único.

| Atributo | Valor |
|---|---|
| **Método** | POST |
| **URL completa** | `http://localhost:8081/api/v1/servicos` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

**Corpo da requisição:**

```json
{
  "nome": "Sessão de Fisioterapia",
  "descricao": "Atendimento individual de fisioterapia com avaliação postural.",
  "duracaoMinutos": 60,
  "preco": 150.00
}
```

| Campo | Tipo | Obrigatório | Validações |
|---|---|---|---|
| `nome` | string | Sim | 3 a 100 caracteres; deve ser único |
| `descricao` | string | Não | Máx. 255 caracteres |
| `duracaoMinutos` | integer | Sim | 1 a 480 minutos (`@Min(1)` e `@Max(480)`) |
| `preco` | decimal | Sim | Mínimo `0.01`; máx. 8 dígitos inteiros e 2 casas decimais |

**Resposta de sucesso — `201 Created`:**

```json
{
  "id": 1,
  "nome": "Sessão de Fisioterapia",
  "descricao": "Atendimento individual de fisioterapia com avaliação postural.",
  "duracaoMinutos": 60,
  "preco": 150.00,
  "criadoEm": "2026-05-22T10:00:00.000",
  "atualizadoEm": "2026-05-22T10:00:00.000"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único gerado automaticamente |
| `nome` | string | Nome do serviço |
| `descricao` | string | Descrição do serviço (pode ser `null`) |
| `duracaoMinutos` | integer | Duração do serviço em minutos; usado para calcular `dataHoraFim` nos agendamentos (RN04) |
| `preco` | decimal | Preço cobrado pelo serviço |
| `criadoEm` | string (ISO 8601) | Data e hora de criação |
| `atualizadoEm` | string (ISO 8601) | Data e hora da última atualização |

**Resposta de erro — `400 Bad Request`:**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação",
  "detalhes": {
    "duracaoMinutos": "A duração máxima é de 480 minutos (8 horas).",
    "preco": "O preço mínimo é R$ 0,01."
  }
}
```

**Resposta de erro — `409 Conflict` (nome de serviço duplicado):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Já existe um serviço com o nome informado."
}
```

---

### GET `/api/v1/servicos`

Retorna a lista completa de serviços cadastrados.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/servicos` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

**Resposta de sucesso — `200 OK`:**

```json
[
  {
    "id": 1,
    "nome": "Sessão de Fisioterapia",
    "descricao": "Atendimento individual de fisioterapia com avaliação postural.",
    "duracaoMinutos": 60,
    "preco": 150.00,
    "criadoEm": "2026-05-22T10:00:00.000",
    "atualizadoEm": "2026-05-22T10:00:00.000"
  },
  {
    "id": 2,
    "nome": "Corte de Cabelo Feminino",
    "descricao": "Corte, lavagem e finalização.",
    "duracaoMinutos": 90,
    "preco": 80.00,
    "criadoEm": "2026-05-22T10:00:01.000",
    "atualizadoEm": "2026-05-22T10:00:01.000"
  },
  {
    "id": 3,
    "nome": "Consulta Nutricional",
    "descricao": "Consulta de avaliação nutricional com elaboração de plano alimentar.",
    "duracaoMinutos": 45,
    "preco": 200.00,
    "criadoEm": "2026-05-22T10:00:02.000",
    "atualizadoEm": "2026-05-22T10:00:02.000"
  },
  {
    "id": 4,
    "nome": "Massagem Relaxante",
    "descricao": "Massagem relaxante corporal com óleos essenciais.",
    "duracaoMinutos": 60,
    "preco": 120.00,
    "criadoEm": "2026-05-22T10:00:03.000",
    "atualizadoEm": "2026-05-22T10:00:03.000"
  }
]
```

---

### GET `/api/v1/servicos/{id}`

Retorna os dados de um serviço específico.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/servicos/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único do serviço |

**Resposta de sucesso — `200 OK`:** mesmo esquema do item único no array de `GET /api/v1/servicos`.

**Resposta de erro — `404 Not Found`:**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Serviço com ID 99 não encontrado."
}
```

---

### PUT `/api/v1/servicos/{id}`

Atualiza todos os dados de um serviço existente.

| Atributo | Valor |
|---|---|
| **Método** | PUT |
| **URL completa** | `http://localhost:8081/api/v1/servicos/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do serviço a ser atualizado |

**Corpo da requisição:** mesmo esquema do `POST /api/v1/servicos`.

**Resposta de sucesso — `200 OK`:** mesmo esquema do `GET /api/v1/servicos/{id}`.

**Respostas de erro possíveis:** `400`, `404`, `409`

---

### DELETE `/api/v1/servicos/{id}`

Remove permanentemente um serviço do sistema.

| Atributo | Valor |
|---|---|
| **Método** | DELETE |
| **URL completa** | `http://localhost:8081/api/v1/servicos/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do serviço a ser removido |

**Resposta de sucesso — `204 No Content`:** corpo vazio.

---

## 7. Agendamentos

Base URL do recurso: `/api/v1/agendamentos`

### Ciclo de vida do status (RN02)

```
AGENDADO ──┬──► CONFIRMADO ──┬──► CONCLUIDO  (estado terminal)
           │                 │
           └──► CANCELADO    └──► CANCELADO  (estado terminal)
```

Transições permitidas:
- `AGENDADO` → `CONFIRMADO` ou `CANCELADO`
- `CONFIRMADO` → `CONCLUIDO` ou `CANCELADO`
- `CONCLUIDO` e `CANCELADO` — nenhuma transição permitida; qualquer tentativa retorna `409`

---

### POST `/api/v1/agendamentos`

Cria um novo agendamento. Valida disponibilidade do profissional no horário solicitado (RN01). A `dataHoraFim` é calculada automaticamente como `dataHora + duracaoMinutos` do serviço (RN04).

| Atributo | Valor |
|---|---|
| **Método** | POST |
| **URL completa** | `http://localhost:8081/api/v1/agendamentos` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

**Corpo da requisição:**

```json
{
  "clienteId": 1,
  "profissionalId": 1,
  "servicoId": 1,
  "dataHora": "2026-06-10T10:00:00"
}
```

| Campo | Tipo | Obrigatório | Validações |
|---|---|---|---|
| `clienteId` | long | Sim | ID de um cliente existente |
| `profissionalId` | long | Sim | ID de um profissional existente |
| `servicoId` | long | Sim | ID de um serviço existente |
| `dataHora` | string (ISO 8601) | Sim | Data e hora futuras (`@Future`) — formato `"2026-06-10T10:00:00"` |
| `status` | string | Não | Se omitido, assume `AGENDADO` automaticamente via `@PrePersist` |

**Resposta de sucesso — `201 Created`:**

```json
{
  "id": 1,
  "clienteId": 1,
  "clienteNome": "Maria Fernanda Oliveira",
  "profissionalId": 1,
  "profissionalNome": "Dra. Carla Mendes",
  "profissionalEspecialidade": "Fisioterapeuta",
  "servicoId": 1,
  "servicoNome": "Sessão de Fisioterapia",
  "servicoDuracaoMinutos": 60,
  "dataHora": "2026-06-10T10:00:00",
  "status": "AGENDADO",
  "criadoEm": "2026-05-22T10:00:00.000",
  "atualizadoEm": "2026-05-22T10:00:00.000"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único do agendamento |
| `clienteId` | long | ID do cliente |
| `clienteNome` | string | Nome do cliente (desnormalizado para facilitar o consumo) |
| `profissionalId` | long | ID do profissional |
| `profissionalNome` | string | Nome do profissional |
| `profissionalEspecialidade` | string | Especialidade do profissional |
| `servicoId` | long | ID do serviço |
| `servicoNome` | string | Nome do serviço |
| `servicoDuracaoMinutos` | integer | Duração do serviço em minutos |
| `dataHora` | string (ISO 8601) | Início do agendamento |
| `status` | string | Status atual: `AGENDADO`, `CONFIRMADO`, `CONCLUIDO` ou `CANCELADO` |
| `criadoEm` | string (ISO 8601) | Data e hora de criação |
| `atualizadoEm` | string (ISO 8601) | Data e hora da última atualização |

**Resposta de erro — `400 Bad Request` (dados inválidos):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação",
  "detalhes": {
    "dataHora": "O agendamento deve ser para uma data futura.",
    "clienteId": "O ID do cliente é obrigatório."
  }
}
```

**Resposta de erro — `404 Not Found` (entidade relacionada não existe):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Profissional com ID 99 não encontrado."
}
```

**Resposta de erro — `409 Conflict` (conflito de horário — RN01):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "O profissional já possui um agendamento neste horário. Por favor, escolha outro horário ou profissional."
}
```

---

### GET `/api/v1/agendamentos`

Retorna a lista completa de todos os agendamentos cadastrados.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/agendamentos` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

**Resposta de sucesso — `200 OK`:**

```json
[
  {
    "id": 1,
    "clienteId": 1,
    "clienteNome": "Maria Fernanda Oliveira",
    "profissionalId": 1,
    "profissionalNome": "Dra. Carla Mendes",
    "profissionalEspecialidade": "Fisioterapeuta",
    "servicoId": 1,
    "servicoNome": "Sessão de Fisioterapia",
    "servicoDuracaoMinutos": 60,
    "dataHora": "2026-06-10T10:00:00",
    "status": "AGENDADO",
    "criadoEm": "2026-05-22T10:00:00.000",
    "atualizadoEm": "2026-05-22T10:00:00.000"
  },
  {
    "id": 2,
    "clienteId": 2,
    "clienteNome": "João Pedro Santos",
    "profissionalId": 2,
    "profissionalNome": "Renata Costa",
    "profissionalEspecialidade": "Cabeleireira",
    "servicoId": 2,
    "servicoNome": "Corte de Cabelo Feminino",
    "servicoDuracaoMinutos": 90,
    "dataHora": "2026-06-10T14:00:00",
    "status": "CONFIRMADO",
    "criadoEm": "2026-05-22T10:00:01.000",
    "atualizadoEm": "2026-05-22T10:05:00.000"
  }
]
```

---

### GET `/api/v1/agendamentos/{id}`

Retorna os dados de um agendamento específico.

| Atributo | Valor |
|---|---|
| **Método** | GET |
| **URL completa** | `http://localhost:8081/api/v1/agendamentos/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_USER` ou `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | Identificador único do agendamento |

**Resposta de sucesso — `200 OK`:** mesmo esquema do item único no array de `GET /api/v1/agendamentos`.

**Resposta de erro — `404 Not Found`:**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Agendamento com ID 99 não encontrado."
}
```

---

### PUT `/api/v1/agendamentos/{id}`

Atualiza os dados de um agendamento existente. Agendamentos com status `CONCLUIDO` ou `CANCELADO` não podem ser alterados (RN03). A verificação de conflito de horário é reexecutada, ignorando o próprio agendamento na consulta.

| Atributo | Valor |
|---|---|
| **Método** | PUT |
| **URL completa** | `http://localhost:8081/api/v1/agendamentos/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |
| **Content-Type** | `application/json` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do agendamento a ser atualizado |

**Corpo da requisição:** mesmo esquema do `POST /api/v1/agendamentos` (todos os campos obrigatórios).

**Resposta de sucesso — `200 OK`:** mesmo esquema do `GET /api/v1/agendamentos/{id}`.

**Resposta de erro — `409 Conflict` (agendamento imutável — RN03):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Agendamentos concluídos não podem ser alterados."
}
```

**Resposta de erro — `409 Conflict` (agendamento cancelado — RN03):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Agendamentos cancelados não podem ser alterados."
}
```

**Respostas de erro possíveis:** `400`, `404`, `409`

---

### PATCH `/api/v1/agendamentos/{id}/status`

Atualiza apenas o status de um agendamento. Valida o fluxo de transições permitido (RN02).

| Atributo | Valor |
|---|---|
| **Método** | PATCH |
| **URL completa** | `http://localhost:8081/api/v1/agendamentos/{id}/status` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do agendamento |

| Parâmetro de query | Tipo | Obrigatório | Valores aceitos |
|---|---|---|---|
| `status` | string | Sim | `AGENDADO`, `CONFIRMADO`, `CONCLUIDO`, `CANCELADO` |

**Exemplo de URL completa:**

```
PATCH http://localhost:8081/api/v1/agendamentos/2/status?status=CONFIRMADO
```

**Resposta de sucesso — `200 OK`:**

```json
{
  "id": 2,
  "clienteId": 2,
  "clienteNome": "João Pedro Santos",
  "profissionalId": 2,
  "profissionalNome": "Renata Costa",
  "profissionalEspecialidade": "Cabeleireira",
  "servicoId": 2,
  "servicoNome": "Corte de Cabelo Feminino",
  "servicoDuracaoMinutos": 90,
  "dataHora": "2026-06-10T14:00:00",
  "status": "CONFIRMADO",
  "criadoEm": "2026-05-22T10:00:01.000",
  "atualizadoEm": "2026-05-22T10:05:00.000"
}
```

**Resposta de erro — `409 Conflict` (transição de status inválida — RN02):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Transição de status inválida: CONCLUIDO → AGENDADO. Verifique o fluxo permitido de status."
}
```

**Respostas de erro possíveis:** `404`, `409`

---

### DELETE `/api/v1/agendamentos/{id}`

Cancela um agendamento ativo. Agendamentos já com status `CONCLUIDO` ou `CANCELADO` geram erro `409`.

| Atributo | Valor |
|---|---|
| **Método** | DELETE |
| **URL completa** | `http://localhost:8081/api/v1/agendamentos/{id}` |
| **Autenticação** | JWT Bearer ou HTTP Basic — `ROLE_ADMIN` |

| Parâmetro de path | Tipo | Descrição |
|---|---|---|
| `id` | long | ID do agendamento a ser cancelado |

**Resposta de sucesso — `204 No Content`:** corpo vazio.

**Resposta de erro — `409 Conflict` (agendamento já concluído):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Agendamentos concluídos não podem ser cancelados."
}
```

**Resposta de erro — `409 Conflict` (agendamento já cancelado):**

```json
{
  "timestamp": "2026-05-22T10:00:00.000",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Este agendamento já está cancelado."
}
```

---

## 8. Como testar com curl

### Pré-requisitos

- `curl` instalado
- Aplicação rodando em `http://localhost:8081` (perfil dev)
- `python3` instalado (opcional, apenas para formatar o JSON da saída)

### Fluxo completo de demonstração

#### Passo 1 — Obter token JWT e salvar em variável

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","senha":"admin123"}' \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")

echo "Token obtido: ${TOKEN:0:50}..."
```

#### Passo 2 — Listar clientes (ROLE_USER — somente leitura)

```bash
# Via JWT Bearer
curl -s http://localhost:8081/api/v1/clientes \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Via HTTP Basic com ROLE_USER
curl -s http://localhost:8081/api/v1/clientes \
  --user user:user123 | python3 -m json.tool
```

#### Passo 3 — Cadastrar novo cliente (ROLE_ADMIN)

```bash
curl -s -X POST http://localhost:8081/api/v1/clientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Carlos Eduardo Silva",
    "email": "carlos.silva@email.com",
    "telefone": "98912345678",
    "cpf": "99988877766"
  }' | python3 -m json.tool
```

#### Passo 4 — Buscar cliente por ID

```bash
curl -s http://localhost:8081/api/v1/clientes/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### Passo 5 — Atualizar cliente

```bash
curl -s -X PUT http://localhost:8081/api/v1/clientes/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Fernanda Oliveira Costa",
    "email": "maria.fernanda@email.com",
    "telefone": "98988881111",
    "cpf": "11122233344"
  }' | python3 -m json.tool
```

#### Passo 6 — Cadastrar profissional

```bash
curl -s -X POST http://localhost:8081/api/v1/profissionais \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Dr. Pedro Alves",
    "especialidade": "Psicólogo",
    "email": "pedro.alves@clinica.com",
    "telefone": "98944445555"
  }' | python3 -m json.tool
```

#### Passo 7 — Cadastrar serviço

```bash
curl -s -X POST http://localhost:8081/api/v1/servicos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Consulta Psicológica",
    "descricao": "Atendimento psicológico individual.",
    "duracaoMinutos": 50,
    "preco": 180.00
  }' | python3 -m json.tool
```

#### Passo 8 — Criar agendamento

```bash
curl -s -X POST http://localhost:8081/api/v1/agendamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "profissionalId": 1,
    "servicoId": 1,
    "dataHora": "2026-07-01T09:00:00"
  }' | python3 -m json.tool
```

#### Passo 9 — Confirmar agendamento (AGENDADO → CONFIRMADO)

```bash
curl -s -X PATCH "http://localhost:8081/api/v1/agendamentos/1/status?status=CONFIRMADO" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### Passo 10 — Concluir agendamento (CONFIRMADO → CONCLUIDO)

```bash
curl -s -X PATCH "http://localhost:8081/api/v1/agendamentos/1/status?status=CONCLUIDO" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### Passo 11 — Tentar criar conflito de horário (esperado: 409)

```bash
# Supondo que o profissional 1 já tem agendamento de 10:00 a 11:00 (serviço de 60 min)
# Tentar marcar às 10:30 vai gerar conflito
curl -s -X POST http://localhost:8081/api/v1/agendamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 2,
    "profissionalId": 1,
    "servicoId": 1,
    "dataHora": "2026-07-01T10:30:00"
  }' | python3 -m json.tool
# Resultado esperado: 409 Conflict
```

#### Passo 12 — Verificar RBAC: ROLE_USER não pode criar (esperado: 403)

```bash
curl -s -X POST http://localhost:8081/api/v1/clientes \
  --user user:user123 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Tentativa",
    "email": "tentativa@email.com",
    "telefone": "11900000000",
    "cpf": "12345678900"
  }'
# Resultado esperado: 403 Forbidden
```

#### Passo 13 — Cancelar agendamento (via DELETE)

```bash
curl -s -X DELETE http://localhost:8081/api/v1/agendamentos/2 \
  -H "Authorization: Bearer $TOKEN" -o /dev/null -w "HTTP Status: %{http_code}\n"
# Resultado esperado: HTTP Status: 204
```

#### Passo 14 — Remover cliente

```bash
curl -s -X DELETE http://localhost:8081/api/v1/clientes/3 \
  -H "Authorization: Bearer $TOKEN" -o /dev/null -w "HTTP Status: %{http_code}\n"
# Resultado esperado: HTTP Status: 204
```

---

## 9. Como testar com o Swagger UI

O Swagger UI está disponível em: **http://localhost:8081/swagger-ui.html**

Somente os endpoints REST (`/api/v1/**`) são exibidos na documentação. Os controllers MVC (`/web/**` e `/portal/**`) são excluídos conforme a propriedade `springdoc.paths-to-match=/api/v1/**` em `application.properties`.

### Configuração inicial

#### Opção A — Autenticar com HTTP Basic

1. Acesse http://localhost:8081/swagger-ui.html
2. Clique no botão **Authorize** (ícone de cadeado) no canto superior direito
3. Na seção **basicAuth (http, Basic)**, preencha:
   - **Username:** `admin`
   - **Password:** `admin123`
4. Clique em **Authorize** e depois em **Close**

Todas as requisições feitas pelo Swagger UI incluirão o header Basic Auth automaticamente.

#### Opção B — Autenticar com JWT Bearer

1. Expanda o endpoint `POST /api/v1/auth/login`
2. Clique em **Try it out**
3. Preencha o body com:
   ```json
   {"email": "admin", "senha": "admin123"}
   ```
4. Clique em **Execute**
5. Na resposta (`200 OK`), localize e copie o valor do campo `token`
6. Clique no botão **Authorize** (cadeado) no topo da página
7. Na seção **bearerAuth (http, Bearer)**, cole o token no campo **Value** (sem o prefixo `"Bearer "`)
8. Clique em **Authorize** e depois em **Close**

### Executando requisições

1. Escolha o recurso desejado na lista de endpoints (ex.: **Clientes**, **Agendamentos**)
2. Clique no endpoint que deseja testar (ex.: `POST /api/v1/agendamentos`)
3. Clique em **Try it out** (botão que aparece no canto direito do endpoint)
4. Preencha o corpo da requisição com um JSON válido (o Swagger exibe um exemplo pré-preenchido baseado no schema do DTO)
5. Preencha os parâmetros de path (ex.: `{id}`) ou de query (ex.: `?status=CONFIRMADO`) quando solicitado
6. Clique em **Execute**
7. Analise a resposta nos campos exibidos abaixo:
   - **Response body** — JSON retornado pelo servidor
   - **Response code** — código HTTP (200, 201, 204, 400, 404, 409, etc.)
   - **Response headers** — cabeçalhos da resposta

### Visualizando os schemas dos DTOs

Role até o final da página para ver a seção **Schemas**, que exibe a definição completa de todos os DTOs de entrada e saída:
- `AgendamentoRequest`, `AgendamentoResponse`
- `ClienteRequest`, `ClienteResponse`
- `ProfissionalRequest`, `ProfissionalResponse`
- `ServicoRequest`, `ServicoResponse`
- `LoginRequest`, `LoginResponse`

### Dicas importantes

- Campos marcados com `*` no formulário do Swagger são obrigatórios
- Erros `401` no Swagger indicam que a autenticação não foi configurada corretamente — repita os passos da Opção A ou B
- Erros `403` indicam que o usuário autenticado (`user`) não tem permissão de escrita — use `admin:admin123`
- Para o endpoint `PATCH /api/v1/agendamentos/{id}/status`, o parâmetro `status` aparece como campo de query string no formulário — preencha com um dos valores do enum: `AGENDADO`, `CONFIRMADO`, `CONCLUIDO`, `CANCELADO`
- O Swagger exibe o campo `curl` equivalente logo abaixo do formulário de execução, útil para reproduzir a chamada no terminal

---

## 10. Tabela Consolidada de Endpoints

| Método | URL | Autenticação | Papel mínimo | Sucesso | Erros possíveis |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/login` | Nenhuma | — | 200 | 400, 401 |
| GET | `/api/v1/clientes` | JWT / Basic | ROLE_USER | 200 | 401, 403 |
| GET | `/api/v1/clientes/{id}` | JWT / Basic | ROLE_USER | 200 | 401, 403, 404 |
| POST | `/api/v1/clientes` | JWT / Basic | ROLE_ADMIN | 201 | 400, 401, 403, 409 |
| PUT | `/api/v1/clientes/{id}` | JWT / Basic | ROLE_ADMIN | 200 | 400, 401, 403, 404, 409 |
| DELETE | `/api/v1/clientes/{id}` | JWT / Basic | ROLE_ADMIN | 204 | 401, 403, 404 |
| GET | `/api/v1/profissionais` | JWT / Basic | ROLE_USER | 200 | 401, 403 |
| GET | `/api/v1/profissionais/{id}` | JWT / Basic | ROLE_USER | 200 | 401, 403, 404 |
| POST | `/api/v1/profissionais` | JWT / Basic | ROLE_ADMIN | 201 | 400, 401, 403, 409 |
| PUT | `/api/v1/profissionais/{id}` | JWT / Basic | ROLE_ADMIN | 200 | 400, 401, 403, 404, 409 |
| DELETE | `/api/v1/profissionais/{id}` | JWT / Basic | ROLE_ADMIN | 204 | 401, 403, 404 |
| GET | `/api/v1/servicos` | JWT / Basic | ROLE_USER | 200 | 401, 403 |
| GET | `/api/v1/servicos/{id}` | JWT / Basic | ROLE_USER | 200 | 401, 403, 404 |
| POST | `/api/v1/servicos` | JWT / Basic | ROLE_ADMIN | 201 | 400, 401, 403, 409 |
| PUT | `/api/v1/servicos/{id}` | JWT / Basic | ROLE_ADMIN | 200 | 400, 401, 403, 404, 409 |
| DELETE | `/api/v1/servicos/{id}` | JWT / Basic | ROLE_ADMIN | 204 | 401, 403, 404 |
| GET | `/api/v1/agendamentos` | JWT / Basic | ROLE_USER | 200 | 401, 403 |
| GET | `/api/v1/agendamentos/{id}` | JWT / Basic | ROLE_USER | 200 | 401, 403, 404 |
| POST | `/api/v1/agendamentos` | JWT / Basic | ROLE_ADMIN | 201 | 400, 401, 403, 404, 409 |
| PUT | `/api/v1/agendamentos/{id}` | JWT / Basic | ROLE_ADMIN | 200 | 400, 401, 403, 404, 409 |
| PATCH | `/api/v1/agendamentos/{id}/status` | JWT / Basic | ROLE_ADMIN | 200 | 401, 403, 404, 409 |
| DELETE | `/api/v1/agendamentos/{id}` | JWT / Basic | ROLE_ADMIN | 204 | 401, 403, 404, 409 |

---

*Documentação elaborada para o TCC — UNDB 2026 — Guilherme Braga*
