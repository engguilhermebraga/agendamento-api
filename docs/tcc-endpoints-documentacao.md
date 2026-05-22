# Documentação Completa dos Endpoints — Agendamento API
## TCC UNDB 2026 — Guilherme Braga

**Base URL (desenvolvimento):** `http://localhost:8081`  
**Formato:** JSON (`Content-Type: application/json`)  
**Autenticação:** JWT Bearer Token ou HTTP Basic Auth  
**Documentação interativa:** http://localhost:8081/swagger-ui.html  
**OpenAPI 3.0 spec:** http://localhost:8081/api-docs

---

## Autenticação

O sistema suporta dois mecanismos de autenticação em paralelo:

| Mecanismo | Como usar | Quando usar |
|---|---|---|
| **JWT Bearer** | `Authorization: Bearer <token>` | Clientes REST / aplicativos |
| **HTTP Basic** | `Authorization: Basic base64(user:pass)` | Swagger UI / testes |

**Roles disponíveis:**
- `ROLE_USER` — somente leitura (GET)
- `ROLE_ADMIN` — leitura e escrita (GET, POST, PUT, PATCH, DELETE)

**Credenciais padrão (perfil dev):**
```
admin / admin123  →  ROLE_ADMIN
user  / user123   →  ROLE_USER
```

---

## 1. Autenticação (Auth)

### POST /api/v1/auth/login
Autentica o usuário e retorna um JWT Bearer token válido por 24 horas.

**Auth:** Pública (sem autenticação)

**Request Body:**
```json
{
  "email": "admin",
  "senha": "admin123"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| email | String | Sim | Username do usuário (admin ou user) |
| senha | String | Sim | Senha do usuário |

**Responses:**

`200 OK` — Token gerado com sucesso
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIs...",
  "tipo": "Bearer",
  "expiracao": "2026-05-23T03:30:00"
}
```

`401 Unauthorized` — Credenciais inválidas
```json
{
  "timestamp": "2026-05-22T03:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials"
}
```

**Exemplo curl:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","senha":"admin123"}'
```

---

## 2. Clientes

### GET /api/v1/clientes
Lista todos os clientes cadastrados.

**Auth:** ROLE_USER ou ROLE_ADMIN

**Response 200:**
```json
[
  {
    "id": 1,
    "nome": "Maria Fernanda Oliveira",
    "email": "maria.fernanda@email.com",
    "telefone": "98988881111",
    "cpf": "11122233344",
    "criadoEm": "2026-05-22T03:31:39",
    "atualizadoEm": "2026-05-22T03:31:39"
  }
]
```

**Exemplo curl:**
```bash
# JWT Bearer
curl http://localhost:8081/api/v1/clientes \
  -H "Authorization: Bearer <TOKEN>"

# HTTP Basic
curl http://localhost:8081/api/v1/clientes -u user:user123
```

---

### GET /api/v1/clientes/{id}
Busca um cliente pelo ID.

**Auth:** ROLE_USER ou ROLE_ADMIN

**Path Params:** `id` (Long)

**Response 200:** (mesmo schema acima, objeto único)

**Response 404:**
```json
{
  "timestamp": "2026-05-22T03:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente não encontrado com id: 99"
}
```

---

### POST /api/v1/clientes
Cria um novo cliente.

**Auth:** ROLE_ADMIN

**Request Body:**
```json
{
  "nome": "João Pedro Santos",
  "email": "joao.pedro@email.com",
  "telefone": "98988882222",
  "cpf": "22233344455"
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| nome | String | Sim | 2–100 caracteres |
| email | String | Sim | Formato e-mail válido; único |
| telefone | String | Sim | 8–20 caracteres |
| cpf | String | Sim | Exatamente 11 dígitos; único |

**Response 201 Created:**
```json
{
  "id": 4,
  "nome": "João Pedro Santos",
  "email": "joao.pedro@email.com",
  "telefone": "98988882222",
  "cpf": "22233344455",
  "criadoEm": "2026-05-22T10:00:00",
  "atualizadoEm": "2026-05-22T10:00:00"
}
```

**Response 400 — Validação falhou:**
```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 400,
  "errors": {
    "email": "deve ser um endereço de e-mail bem formado",
    "cpf": "CPF deve conter exatamente 11 dígitos"
  }
}
```

**Response 409 — E-mail ou CPF já cadastrado:**
```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "E-mail já está em uso: joao.pedro@email.com"
}
```

**Exemplo curl:**
```bash
curl -X POST http://localhost:8081/api/v1/clientes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "nome": "João Pedro Santos",
    "email": "joao.pedro@email.com",
    "telefone": "98988882222",
    "cpf": "22233344455"
  }'
```

---

### PUT /api/v1/clientes/{id}
Atualiza todos os campos de um cliente.

**Auth:** ROLE_ADMIN

**Request Body:** Mesmo schema do POST.

**Response 200:** Cliente atualizado (mesmo schema do GET/{id}).

**Response 404:** Cliente não encontrado.

---

### DELETE /api/v1/clientes/{id}
Remove permanentemente um cliente e seus agendamentos.

**Auth:** ROLE_ADMIN

**Response 204 No Content:** Removido com sucesso (sem corpo).

**Response 404:** Cliente não encontrado.

**Exemplo curl:**
```bash
curl -X DELETE http://localhost:8081/api/v1/clientes/1 \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 3. Profissionais

### GET /api/v1/profissionais
**Auth:** ROLE_USER ou ROLE_ADMIN

**Response 200:**
```json
[
  {
    "id": 1,
    "nome": "Dra. Carla Mendes",
    "especialidade": "Fisioterapeuta",
    "email": "carla.mendes@clinica.com",
    "telefone": "98977771111",
    "criadoEm": "2026-05-22T03:31:39",
    "atualizadoEm": "2026-05-22T03:31:39"
  }
]
```

### GET /api/v1/profissionais/{id}
**Auth:** ROLE_USER ou ROLE_ADMIN — Response 200 (objeto único) ou 404.

### POST /api/v1/profissionais
**Auth:** ROLE_ADMIN

**Request Body:**
```json
{
  "nome": "Dr. Lucas Pereira",
  "especialidade": "Nutricionista",
  "email": "lucas.pereira@nutri.com",
  "telefone": "98977773333"
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| nome | String | Sim | 2–100 caracteres |
| especialidade | String | Sim | 2–100 caracteres |
| email | String | Não | Formato e-mail válido |
| telefone | String | Não | 8–20 caracteres |

**Response 201:** Profissional criado.

### PUT /api/v1/profissionais/{id}
**Auth:** ROLE_ADMIN — Atualiza todos os campos.

### DELETE /api/v1/profissionais/{id}
**Auth:** ROLE_ADMIN — Remove permanentemente. **Response 204**.

---

## 4. Serviços

### GET /api/v1/servicos
**Auth:** ROLE_USER ou ROLE_ADMIN

**Response 200:**
```json
[
  {
    "id": 1,
    "nome": "Sessão de Fisioterapia",
    "descricao": "Atendimento individual de fisioterapia com avaliação postural.",
    "duracaoMinutos": 60,
    "preco": 150.00,
    "criadoEm": "2026-05-22T03:31:39",
    "atualizadoEm": "2026-05-22T03:31:39"
  }
]
```

### GET /api/v1/servicos/{id}
**Auth:** ROLE_USER ou ROLE_ADMIN — Response 200 ou 404.

### POST /api/v1/servicos
**Auth:** ROLE_ADMIN

**Request Body:**
```json
{
  "nome": "Massagem Relaxante",
  "descricao": "Massagem relaxante corporal com óleos essenciais.",
  "duracaoMinutos": 60,
  "preco": 120.00
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| nome | String | Sim | 2–100 caracteres |
| descricao | String | Não | Até 500 caracteres |
| duracaoMinutos | Integer | Sim | Mínimo 1 |
| preco | BigDecimal | Sim | Mínimo 0.00 |

**Response 201:** Serviço criado.

### PUT /api/v1/servicos/{id}
**Auth:** ROLE_ADMIN — Atualiza todos os campos.

### DELETE /api/v1/servicos/{id}
**Auth:** ROLE_ADMIN — Remove permanentemente. **Response 204**.

---

## 5. Agendamentos

### GET /api/v1/agendamentos
Lista todos os agendamentos.

**Auth:** ROLE_USER ou ROLE_ADMIN

**Response 200:**
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
    "dataHora": "2026-05-23T10:00:00",
    "status": "AGENDADO",
    "criadoEm": "2026-05-22T03:31:39",
    "atualizadoEm": "2026-05-22T03:31:39"
  }
]
```

**Exemplo curl:**
```bash
curl http://localhost:8081/api/v1/agendamentos \
  -H "Authorization: Bearer <TOKEN>"
```

---

### GET /api/v1/agendamentos/{id}
Busca agendamento pelo ID.

**Auth:** ROLE_USER ou ROLE_ADMIN — Response 200 (objeto único) ou 404.

---

### GET /api/v1/agendamentos/cliente/{clienteId}
Lista todos os agendamentos de um cliente específico.

**Auth:** ROLE_USER ou ROLE_ADMIN

**Path Params:** `clienteId` (Long)

**Response 200:** Array de agendamentos filtrados por clienteId.

---

### POST /api/v1/agendamentos
Cria um novo agendamento. Verifica conflito de horário automaticamente.

**Auth:** ROLE_ADMIN

**Request Body:**
```json
{
  "clienteId": 1,
  "profissionalId": 1,
  "servicoId": 1,
  "dataHora": "2026-05-25T09:00:00"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| clienteId | Long | Sim | ID do cliente cadastrado |
| profissionalId | Long | Sim | ID do profissional |
| servicoId | Long | Sim | ID do serviço |
| dataHora | LocalDateTime | Sim | ISO 8601: "2026-05-25T09:00:00" |
| status | String | Não | Padrão: AGENDADO |

**Response 201 — Criado com sucesso:**
```json
{
  "id": 5,
  "clienteId": 1,
  "clienteNome": "Maria Fernanda Oliveira",
  "profissionalId": 1,
  "profissionalNome": "Dra. Carla Mendes",
  "profissionalEspecialidade": "Fisioterapeuta",
  "servicoId": 1,
  "servicoNome": "Sessão de Fisioterapia",
  "servicoDuracaoMinutos": 60,
  "dataHora": "2026-05-25T09:00:00",
  "status": "AGENDADO",
  "criadoEm": "2026-05-22T10:00:00",
  "atualizadoEm": "2026-05-22T10:00:00"
}
```

**Response 409 — Conflito de horário:**
```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Conflito de horário: Dra. Carla Mendes já possui agendamento entre 09:00 e 10:00."
}
```

**Response 404 — Entidade não encontrada:**
```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Profissional não encontrado com id: 99"
}
```

**Exemplo curl:**
```bash
curl -X POST http://localhost:8081/api/v1/agendamentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "clienteId": 1,
    "profissionalId": 1,
    "servicoId": 1,
    "dataHora": "2026-05-25T09:00:00"
  }'
```

---

### PUT /api/v1/agendamentos/{id}
Atualiza um agendamento existente. Revalida conflito de horário excluindo o próprio agendamento.

**Auth:** ROLE_ADMIN

**Request Body:** Mesmo schema do POST (todos os campos obrigatórios).

**Regra:** Agendamentos com status `CONCLUIDO` ou `CANCELADO` não podem ser atualizados — retorna 409.

**Response 200:** Agendamento atualizado.

---

### PATCH /api/v1/agendamentos/{id}/status
Altera apenas o status do agendamento, seguindo a máquina de estados.

**Auth:** ROLE_ADMIN

**Query Param:** `?status=CONFIRMADO`

**Transições válidas:**
```
AGENDADO   → CONFIRMADO, CANCELADO
CONFIRMADO → CONCLUIDO, CANCELADO
CONCLUIDO  → (estado final, sem transição)
CANCELADO  → (estado final, sem transição)
```

**Response 200:** Agendamento com novo status.

**Response 409 — Transição inválida:**
```json
{
  "message": "Transição inválida: CONCLUIDO → AGENDADO"
}
```

**Exemplo curl:**
```bash
curl -X PATCH \
  "http://localhost:8081/api/v1/agendamentos/1/status?status=CONFIRMADO" \
  -H "Authorization: Bearer <TOKEN>"
```

---

### DELETE /api/v1/agendamentos/{id}
Remove permanentemente um agendamento.

**Auth:** ROLE_ADMIN

**Response 204 No Content:** Removido com sucesso.

---

## 6. Respostas de Erro Padrão

| Status | Quando ocorre |
|---|---|
| 400 Bad Request | Body inválido, campos obrigatórios ausentes, validação Bean Validation |
| 401 Unauthorized | Token ausente, expirado ou inválido; credenciais erradas |
| 403 Forbidden | Autenticado mas sem permissão (USER tentando escrita) |
| 404 Not Found | Recurso não encontrado por ID |
| 409 Conflict | Duplicidade (e-mail/CPF), conflito de horário, transição de status inválida |
| 500 Internal Server Error | Erro inesperado no servidor |

**Formato padrão de erro:**
```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descrição do erro",
  "path": "/api/v1/clientes"
}
```

---

## 7. Testando com Swagger UI

1. Acesse http://localhost:8081/swagger-ui.html
2. Clique em **POST /api/v1/auth/login** → **Try it out**
3. Preencha `{"email": "admin", "senha": "admin123"}` → **Execute**
4. Copie o valor do campo `token` da resposta
5. Clique em **Authorize** (botão com cadeado, topo da página)
6. No campo **bearerAuth (http, Bearer)**, cole o token → **Authorize**
7. Agora todos os endpoints protegidos aceitarão seu token automaticamente

---

## 8. Coleção Postman (comandos curl)

### Fluxo completo de teste manual:

```bash
# 1. Obter token
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","senha":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 2. Listar clientes (leitura com ROLE_USER)
curl http://localhost:8081/api/v1/clientes -H "Authorization: Bearer $TOKEN"

# 3. Criar cliente (escrita, precisa ROLE_ADMIN)
curl -X POST http://localhost:8081/api/v1/clientes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nome":"Teste JWT","email":"jwt@test.com","telefone":"11999998888","cpf":"99988877700"}'

# 4. Criar profissional
curl -X POST http://localhost:8081/api/v1/profissionais \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nome":"Dr. Teste","especialidade":"Clínico Geral","email":"dr@teste.com","telefone":"11888887777"}'

# 5. Criar serviço
curl -X POST http://localhost:8081/api/v1/servicos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nome":"Consulta","descricao":"Consulta médica","duracaoMinutos":30,"preco":200.00}'

# 6. Criar agendamento (sem conflito)
curl -X POST http://localhost:8081/api/v1/agendamentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"clienteId":1,"profissionalId":1,"servicoId":1,"dataHora":"2026-06-01T09:00:00"}'

# 7. Confirmar agendamento
curl -X PATCH "http://localhost:8081/api/v1/agendamentos/1/status?status=CONFIRMADO" \
  -H "Authorization: Bearer $TOKEN"

# 8. Tentar criar conflito (mesmo profissional, mesmo horário)
curl -X POST http://localhost:8081/api/v1/agendamentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"clienteId":2,"profissionalId":1,"servicoId":1,"dataHora":"2026-06-01T09:30:00"}'
# → 409 Conflict

# 9. Verificar RBAC: ROLE_USER não pode criar
curl -X POST http://localhost:8081/api/v1/clientes \
  -H "Content-Type: application/json" \
  -u user:user123 \
  -d '{"nome":"Teste","email":"t@t.com","telefone":"11","cpf":"12345678900"}'
# → 403 Forbidden
```

---

## 9. Tabela Resumo de Endpoints

| Método | Endpoint | Auth | Role | Descrição |
|---|---|---|---|---|
| POST | /api/v1/auth/login | Pública | — | Gera JWT |
| GET | /api/v1/clientes | Sim | USER/ADMIN | Lista clientes |
| GET | /api/v1/clientes/{id} | Sim | USER/ADMIN | Busca por ID |
| POST | /api/v1/clientes | Sim | ADMIN | Cria cliente |
| PUT | /api/v1/clientes/{id} | Sim | ADMIN | Atualiza cliente |
| DELETE | /api/v1/clientes/{id} | Sim | ADMIN | Remove cliente |
| GET | /api/v1/profissionais | Sim | USER/ADMIN | Lista profissionais |
| GET | /api/v1/profissionais/{id} | Sim | USER/ADMIN | Busca por ID |
| POST | /api/v1/profissionais | Sim | ADMIN | Cria profissional |
| PUT | /api/v1/profissionais/{id} | Sim | ADMIN | Atualiza profissional |
| DELETE | /api/v1/profissionais/{id} | Sim | ADMIN | Remove profissional |
| GET | /api/v1/servicos | Sim | USER/ADMIN | Lista serviços |
| GET | /api/v1/servicos/{id} | Sim | USER/ADMIN | Busca por ID |
| POST | /api/v1/servicos | Sim | ADMIN | Cria serviço |
| PUT | /api/v1/servicos/{id} | Sim | ADMIN | Atualiza serviço |
| DELETE | /api/v1/servicos/{id} | Sim | ADMIN | Remove serviço |
| GET | /api/v1/agendamentos | Sim | USER/ADMIN | Lista todos |
| GET | /api/v1/agendamentos/{id} | Sim | USER/ADMIN | Busca por ID |
| GET | /api/v1/agendamentos/cliente/{id} | Sim | USER/ADMIN | Por cliente |
| POST | /api/v1/agendamentos | Sim | ADMIN | Cria agendamento |
| PUT | /api/v1/agendamentos/{id} | Sim | ADMIN | Atualiza agendamento |
| PATCH | /api/v1/agendamentos/{id}/status | Sim | ADMIN | Muda status |
| DELETE | /api/v1/agendamentos/{id} | Sim | ADMIN | Remove agendamento |
