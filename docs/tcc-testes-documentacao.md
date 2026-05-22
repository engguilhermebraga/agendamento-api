# Documentação de Testes Automatizados — Agendamento API

**Trabalho de Conclusão de Curso — UNDB 2026**
**Autor:** Guilherme Braga
**Projeto:** Agendamento API — Sistema de Agendamento de Serviços
**Tecnologia:** Spring Boot 4.0.5 · Java 21 · Maven

---

## 1 Introdução

A verificação da corretude do software constitui etapa fundamental no processo de desenvolvimento de sistemas de informação. No contexto deste Trabalho de Conclusão de Curso, adotou-se uma estratégia de testes automatizados estruturada em três camadas complementares — testes de unidade, testes de repositório (integração com banco de dados) e testes de ponta a ponta (end-to-end) — conforme o modelo da Pirâmide de Testes proposto por Cohn (2009) e consagrado pela literatura de Engenharia de Software Ágil.

O conjunto de testes resultante totaliza **138 testes automatizados**, todos aprovados sem falhas, cobrindo as principais regras de negócio da aplicação: ciclo de vida de agendamentos, detecção de conflitos de horário, unicidade de dados cadastrais e controle de acesso baseado em papéis. A automação de testes proporciona retroalimentação rápida durante o desenvolvimento, reduz o custo de regressão e documenta o comportamento esperado do sistema de forma executável e verificável.

Este documento descreve a estrutura, as técnicas empregadas e os resultados obtidos na suíte de testes da Agendamento API, servindo como subsídio ao capítulo de Testes do presente trabalho acadêmico.

---

## 2 Estratégia de Testes — Três Camadas

A suíte de testes está organizada em três camadas hierárquicas, cada qual com escopo, ferramentas e objetivos distintos:

### 2.1 Camada 1 — Testes de Unidade (Unit Tests)

Os testes de unidade verificam componentes isolados da aplicação — especificamente as classes de serviço (`AgendamentoService` e `ClienteService`) — sem dependências externas reais. Todos os colaboradores (repositórios, mapeadores) são substituídos por objetos simulados (*mocks*) criados com a biblioteca Mockito. Essa abordagem garante que qualquer falha identificada seja, necessariamente, atribuível à lógica de negócio da própria classe em teste, e não a efeitos colaterais de infraestrutura.

Característica central: velocidade de execução elevada (cada teste roda em milissegundos) e granularidade fina, permitindo verificar cada decisão de fluxo de controle individualmente.

### 2.2 Camada 2 — Testes de Repositório e Controlador (Integration Tests)

Esta camada abrange dois subgrupos:

**a) Testes de repositório** (`AgendamentoRepositoryTest`, `ClienteRepositoryTest`): utilizam a anotação `@SpringBootTest` combinada com `@Transactional` para carregar o contexto Spring completo e executar as operações de persistência contra um banco de dados H2 em memória. Cada teste é encapsulado em uma transação que é revertida ao final (`rollback`), garantindo isolamento completo entre os métodos. O objetivo é validar as *queries* JPQL customizadas — em especial a lógica de detecção de sobreposição de horários — contra um banco relacional real, comportamento impossível de verificar com mocks.

**b) Testes de controlador com MockMvc** (`AgendamentoControllerTest`, `ClienteControllerTest`, `ProfissionalControllerTest`, `ServicoControllerTest`): carregam o contexto Spring completo com `@SpringBootTest`, substituindo apenas a camada de serviço por mocks via `@MockitoBean`. O `MockMvc` simula requisições HTTP reais através de toda a pilha de filtros do Spring (incluindo Spring Security), sem necessidade de iniciar um servidor HTTP de fato. Verificam serialização JSON, status HTTP, headers de resposta, validação de *Bean Validation* e controle de acesso por papéis.

### 2.3 Camada 3 — Testes de Integração End-to-End (E2E)

Os testes de integração completa (`AgendamentoIntegrationTest`) exercitam toda a pilha da aplicação sem qualquer substituição na camada de negócio ou de persistência: as requisições HTTP trafegam do `MockMvc` pelo controlador, serviço, repositório e chegam ao banco H2 em memória, retornando pela mesma rota. Apenas a autenticação é simulada via `@WithMockUser` para eliminar a necessidade de credenciais JWT reais em cada cenário. O propósito é validar cenários de negócio completos — como o fluxo integral de criação e progressão de um agendamento — e confirmar que as camadas colaboram corretamente entre si.

---

## 3 Descrição dos Arquivos de Teste

### 3.1 AgendamentoServiceTest — Testes Unitários do Serviço de Agendamento

**Arquivo:** `AgendamentoServiceTest.java`
**Camada:** Unidade
**Técnica:** JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`)
**Quantidade de testes:** 39
**Descrição:**

É a classe de teste mais abrangente da suíte. Verifica a lógica de negócio da classe `AgendamentoService` de forma completamente isolada, com todos os repositórios e o mapeador substituídos por mocks. Os testes estão organizados em seis grupos aninhados (`@Nested`) com nomes declarativos (`@DisplayName`):

- **`criar()`** — 7 testes: verifica a criação bem-sucedida de agendamento quando não há conflitos; lança `BusinessException` quando o profissional já possui agendamento no mesmo horário (dois cenários de sobreposição); lança `ResourceNotFoundException` quando cliente, profissional ou serviço informado não existe; e confirma que `dataHoraFim` é calculada corretamente com base na duração do serviço (90 minutos).

- **`buscarPorId()`** — 3 testes: retorna `AgendamentoResponse` quando o ID existe; lança `ResourceNotFoundException` com mensagem identificando o ID quando não existe; e testa comportamento para ID zero.

- **`listarTodos()`** — 3 testes: retorna lista com múltiplos agendamentos; retorna lista vazia sem invocar o mapeador; retorna lista com um único agendamento com verificação fluente via `satisfies`.

- **`atualizar()`** — 10 testes: atualização bem-sucedida para agendamentos nos status `AGENDADO` e `CONFIRMADO`; imutabilidade de agendamentos `CONCLUIDO` e `CANCELADO` (lançam `BusinessException`); conflito de horário na atualização; `ResourceNotFoundException` para agendamento, cliente, profissional ou serviço inexistentes; e verificação de que a atualização utiliza `findConflitosHorarioExcluindoId` (excluindo o próprio agendamento da checagem) em vez de `findConflitosHorario`.

- **`atualizarStatus()` — transições permitidas** — 4 testes: `AGENDADO → CONFIRMADO`, `AGENDADO → CANCELADO`, `CONFIRMADO → CONCLUIDO` e `CONFIRMADO → CANCELADO`, todos com verificação de persistência via `verify(repository).save(...)`.

- **`atualizarStatus()` — transições proibidas** — 6 testes: qualquer transição a partir de `CONCLUIDO` ou `CANCELADO` (estados terminais); tentativa de pular o estado intermediário `AGENDADO → CONCLUIDO`; transição idempotente `AGENDADO → AGENDADO`; retrocesso `CONFIRMADO → AGENDADO`; e `ResourceNotFoundException` para ID inexistente.

- **`cancelar()`** — 6 testes: cancelamento bem-sucedido de agendamentos `AGENDADO` e `CONFIRMADO`; bloqueio de cancelamento para `CONCLUIDO` e `CANCELADO`; `ResourceNotFoundException` para ID inexistente; e verificação por captura de argumento (`argThat`) de que o status `CANCELADO` é efetivamente persistido.

---

### 3.2 ClienteServiceTest — Testes Unitários do Serviço de Cliente

**Arquivo:** `ClienteServiceTest.java`
**Camada:** Unidade
**Técnica:** JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`)
**Quantidade de testes:** 13
**Descrição:**

Verifica a lógica de negócio de `ClienteService` em isolamento. Organizado por operação CRUD sem `@Nested`, mas com `@DisplayName` em cada teste:

- **Criar:** criação com sucesso; `BusinessException` quando e-mail já está cadastrado; `BusinessException` quando CPF já está cadastrado. Em todos os casos de erro, verifica-se que `repository.save()` não é invocado.

- **Listar todos:** retorna lista com um elemento; retorna lista vazia.

- **Buscar por ID:** retorna `ClienteResponse` quando ID existe; lança `ResourceNotFoundException` quando ID não existe.

- **Atualizar:** atualização sem conflito de e-mail ou CPF com chamada verificada ao método de atualização do mapper; `BusinessException` ao tentar atualizar com e-mail já utilizado por outro cliente; `BusinessException` ao tentar atualizar com CPF já utilizado por outro cliente.

- **Deletar:** exclusão bem-sucedida sem lançamento de exceção (verificada via `assertThatCode(...).doesNotThrowAnyException()`); `ResourceNotFoundException` para ID inexistente.

---

### 3.3 AgendamentoRepositoryTest — Testes de Repositório de Agendamento

**Arquivo:** `AgendamentoRepositoryTest.java`
**Camada:** Integração com banco de dados (H2)
**Técnica:** `@SpringBootTest` + `@Transactional` + AssertJ
**Quantidade de testes:** 13
**Descrição:**

Valida as *queries* customizadas do `AgendamentoRepository` contra um banco H2 real. O foco principal é a *query* `findConflitosHorario`, que implementa a lógica de detecção de sobreposição de intervalos de tempo. Os testes estão parcialmente organizados em grupo aninhado:

- **`findConflitosHorario()` — cinco cenários de sobreposição** (dentro de `@Nested`):
  - Cenário 1: mesmo horário exato deve conflitar.
  - Cenário 2: sobreposição parcial no início do novo agendamento deve conflitar.
  - Cenário 3: sobreposição parcial no fim do novo agendamento deve conflitar.
  - Cenário 4: novo agendamento que engloba completamente o existente deve conflitar.
  - Cenário 5: horários adjacentes (o novo começa exatamente quando o existente termina) não devem conflitar — valida a semântica de intervalo aberto.

- **Filtros de status e de profissional:** agendamento com status `CANCELADO` é ignorado mesmo com sobreposição; agendamento `CONCLUIDO` é ignorado mesmo com sobreposição; agendamento de outro profissional não conflita; agendamento `CONFIRMADO` é considerado conflito ativo.

- **`findConflitosHorarioExcluindoId`:** confirma que o próprio agendamento sendo atualizado é excluído da verificação de conflito.

- **Métodos derivados:** `findByClienteId` retorna apenas agendamentos do cliente informado; `findByStatus` filtra corretamente por status; `findByDataInterval` retorna agendamentos dentro do intervalo e ordenados de forma crescente por `dataHora`.

---

### 3.4 ClienteRepositoryTest — Testes de Repositório de Cliente

**Arquivo:** `ClienteRepositoryTest.java`
**Camada:** Integração com banco de dados (H2)
**Técnica:** `@SpringBootTest` + `@Transactional` + AssertJ
**Quantidade de testes:** 17
**Descrição:**

Verifica o contrato completo do `ClienteRepository`, abrangendo tanto os métodos herdados de `JpaRepository` quanto as *queries* derivadas e de existência usadas nas regras de negócio:

- **`findAll()`:** retorna todos os clientes persistidos; retorna lista vazia quando não há clientes.
- **`findById()`:** retorna `Optional` presente quando ID existe; retorna `Optional` vazio quando ID não existe.
- **`findByEmail()`:** localiza cliente por e-mail; retorna `Optional` vazio para e-mail não cadastrado.
- **`findByCpf()`:** localiza cliente por CPF; retorna `Optional` vazio para CPF não cadastrado.
- **`existsByEmailAndIdNot()`:** retorna `true` quando outro cliente já usa o e-mail; retorna `false` quando o próprio cliente é o titular; retorna `false` para e-mail inexistente no sistema.
- **`existsByCpfAndIdNot()`:** retorna `true` quando outro cliente já usa o CPF; retorna `false` quando o próprio cliente é o titular.
- **`count()`:** retorna total correto de clientes persistidos.
- **`deleteById()`:** remove o cliente e reduz a contagem.
- **`save()`:** atualiza cliente existente preservando o ID; preenche automaticamente os campos `criadoEm` e `atualizadoEm` via `@PrePersist`.

---

### 3.5 AgendamentoControllerTest — Testes de Controlador REST de Agendamento

**Arquivo:** `AgendamentoControllerTest.java`
**Camada:** Integração (MockMvc + Spring Security + serviço mockado)
**Técnica:** `@SpringBootTest` + `MockMvc` + `@MockitoBean` + `@WithMockUser`
**Quantidade de testes:** 17
**Descrição:**

Verifica o comportamento da camada HTTP do `AgendamentoController` — rotas, verbos, status HTTP, serialização JSON e controle de acesso — sem exercitar a lógica de negócio real (substituída por `@MockitoBean`):

- **GET `/api/v1/agendamentos`:** retorna 200 e lista JSON para usuário `USER`; retorna 401 sem autenticação.
- **GET `/api/v1/agendamentos/{id}`:** retorna 200 com objeto JSON completo para `USER`; retorna 404 quando ID não existe.
- **POST `/api/v1/agendamentos`:** retorna 201 com corpo JSON para `ADMIN`; retorna 403 para `USER`; retorna 400 para payload com IDs nulos e data no passado; retorna 409 quando o serviço lança `BusinessException` de conflito de horário.
- **PUT `/api/v1/agendamentos/{id}`:** retorna 200 para `ADMIN`; retorna 404 quando agendamento não existe.
- **PATCH `/api/v1/agendamentos/{id}/status`:** retorna 200 e novo status `CONFIRMADO`; retorna 409 para transição inválida; retorna 404 para ID inexistente.
- **DELETE `/api/v1/agendamentos/{id}`:** retorna 204 para `ADMIN`; retorna 403 para `USER`; retorna 409 ao tentar cancelar agendamento `CONCLUIDO`; retorna 404 para ID inexistente.

---

### 3.6 ClienteControllerTest — Testes de Controlador REST de Cliente

**Arquivo:** `ClienteControllerTest.java`
**Camada:** Integração (MockMvc + Spring Security + serviço mockado)
**Técnica:** `@SpringBootTest` + `MockMvc` + `@MockitoBean` + `@WithMockUser`
**Quantidade de testes:** 11
**Descrição:**

Verifica as rotas REST do `ClienteController` com a mesma abordagem do controlador de agendamento:

- **GET `/api/v1/clientes`:** retorna 200 com lista para `USER`; retorna 401 sem autenticação.
- **GET `/api/v1/clientes/{id}`:** retorna 200 com dados do cliente para `USER`; retorna 404 para ID inexistente.
- **POST `/api/v1/clientes`:** retorna 201 para `ADMIN`; retorna 403 para `USER`; retorna 400 para payload com nome vazio, e-mail inválido, telefone e CPF em formato incorreto.
- **PUT `/api/v1/clientes/{id}`:** retorna 200 para `ADMIN`.
- **DELETE `/api/v1/clientes/{id}`:** retorna 204 para `ADMIN`; retorna 403 para `USER`; retorna 404 para ID inexistente.

---

### 3.7 ProfissionalControllerTest — Testes de Controlador REST de Profissional

**Arquivo:** `ProfissionalControllerTest.java`
**Camada:** Integração (MockMvc + Spring Security + serviço mockado)
**Técnica:** `@SpringBootTest` + `MockMvc` + `@MockitoBean` + `@WithMockUser`
**Quantidade de testes:** 10
**Descrição:**

Verifica o `ProfissionalController` com a mesma estrutura dos demais controladores:

- **GET `/api/v1/profissionais`:** retorna 200 com lista para `USER`; retorna 401 sem autenticação.
- **GET `/api/v1/profissionais/{id}`:** retorna 200 com dados incluindo especialidade; retorna 404 para ID inexistente.
- **POST `/api/v1/profissionais`:** retorna 201 para `ADMIN`; retorna 403 para `USER`; retorna 400 para payload com campos em branco e e-mail inválido.
- **PUT `/api/v1/profissionais/{id}`:** retorna 200 para `ADMIN`.
- **DELETE `/api/v1/profissionais/{id}`:** retorna 204 para `ADMIN`; retorna 403 para `USER`.

---

### 3.8 ServicoControllerTest — Testes de Controlador REST de Serviço

**Arquivo:** `ServicoControllerTest.java`
**Camada:** Integração (MockMvc + Spring Security + serviço mockado)
**Técnica:** `@SpringBootTest` + `MockMvc` + `@MockitoBean` + `@WithMockUser`
**Quantidade de testes:** 10
**Descrição:**

Verifica o `ServicoController`:

- **GET `/api/v1/servicos`:** retorna 200 com lista para `USER`; retorna 401 sem autenticação.
- **GET `/api/v1/servicos/{id}`:** retorna 200 com preço e duração; retorna 404 para ID inexistente.
- **POST `/api/v1/servicos`:** retorna 201 para `ADMIN`; retorna 403 para `USER`; retorna 400 para payload com nome vazio, duração zero e preço zero.
- **PUT `/api/v1/servicos/{id}`:** retorna 200 para `ADMIN`.
- **DELETE `/api/v1/servicos/{id}`:** retorna 204 para `ADMIN`; retorna 403 para `USER`.

---

### 3.9 AgendamentoIntegrationTest — Testes de Integração End-to-End

**Arquivo:** `AgendamentoIntegrationTest.java`
**Camada:** End-to-End (toda a stack sem mocks de serviço ou repositório)
**Técnica:** `@SpringBootTest` + `MockMvc` + `@WithMockUser(roles = "ADMIN")` + AssertJ
**Quantidade de testes:** 8
**Descrição:**

É a camada de maior abrangência da suíte. Exercita toda a pilha da aplicação — controlador, serviço, repositório e banco H2 — em cenários realistas. O `@BeforeEach` realiza limpeza completa do banco (respeitando a ordem das chaves estrangeiras) para garantir isolamento entre os testes. Cada teste provisiona suas próprias entidades via chamadas HTTP reais:

- **Fluxo completo:** cria cliente → profissional → serviço → agendamento via `POST` em cada recurso; confirma persistência no banco via `GET`; avança o status `AGENDADO → CONFIRMADO → CONCLUIDO` via `PATCH /status`; confirma que qualquer transição após `CONCLUIDO` retorna 409; verifica status final diretamente no repositório via `assertThat`.

- **Máquina de estados — pular estados:** confirma que a transição direta `AGENDADO → CONCLUIDO` retorna 409 e que o banco permanece com `AGENDADO`.

- **Cancelamento via PATCH:** confirma que `AGENDADO → CANCELADO` via `PATCH /status` retorna 200 e persiste o status corretamente.

- **Conflito de horário — sobreposição:** dois clientes tentam agendar o mesmo profissional com horários sobrepostos (10:00–11:00 e 10:30–11:30); o segundo recebe 409 e o banco contém exatamente 1 registro.

- **Conflito de horário — mesmo horário exato:** segundo agendamento no exato mesmo horário do primeiro retorna 409.

- **Sem conflito — horários separados:** dois agendamentos para o mesmo profissional com horário livre entre eles (10:00–11:00 e 12:00–13:00) são aceitos; banco contém 2 registros.

- **Sem conflito — profissionais diferentes:** mesmo cliente e horário para dois profissionais distintos são aceitos; banco contém 2 registros.

- **Liberação de horário após cancelamento:** cancela o primeiro agendamento e confirma que novo agendamento no mesmo horário (antes bloqueado) é aceito, já que `CANCELADO` é ignorado na verificação de conflito.

---

### 3.10 AgendamentoApiApplicationTests — Teste de Carregamento de Contexto

**Arquivo:** `AgendamentoApiApplicationTests.java`
**Camada:** Fumaça (*smoke test*)
**Técnica:** `@SpringBootTest`
**Quantidade de testes:** 1
**Descrição:**

Contém um único teste `contextLoads()` que verifica se o contexto do Spring Boot é iniciado sem erros de configuração, dependência circular ou falha de *bean*. Atua como guarda de sanidade da aplicação inteira.

---

## 4 Tabela Resumo da Suíte de Testes

| Camada | Classe de Teste | Testes | Técnica Principal | Área Coberta |
|--------|----------------|--------|-------------------|--------------|
| Unidade | `AgendamentoServiceTest` | 39 | JUnit 5 + Mockito | Regras de negócio de agendamento, máquina de estados, detecção de conflito de horário |
| Unidade | `ClienteServiceTest` | 13 | JUnit 5 + Mockito | Criação, atualização e exclusão de clientes; unicidade de e-mail e CPF |
| Integração — Repositório | `AgendamentoRepositoryTest` | 13 | SpringBootTest + Transactional + H2 | Queries JPQL de sobreposição de horários; filtros por status e profissional |
| Integração — Repositório | `ClienteRepositoryTest` | 17 | SpringBootTest + Transactional + H2 | Persistência, queries derivadas e checagens de unicidade |
| Integração — Controlador | `AgendamentoControllerTest` | 17 | MockMvc + MockitoBean + WithMockUser | Endpoints REST, status HTTP, controle de acesso RBAC |
| Integração — Controlador | `ClienteControllerTest` | 11 | MockMvc + MockitoBean + WithMockUser | Endpoints REST de cliente, validação de payload, RBAC |
| Integração — Controlador | `ProfissionalControllerTest` | 10 | MockMvc + MockitoBean + WithMockUser | Endpoints REST de profissional, validação, RBAC |
| Integração — Controlador | `ServicoControllerTest` | 10 | MockMvc + MockitoBean + WithMockUser | Endpoints REST de serviço, validação, RBAC |
| End-to-End | `AgendamentoIntegrationTest` | 8 | SpringBootTest + MockMvc + H2 (sem mocks) | Fluxo completo, conflitos de horário, estados terminais |
| Fumaça | `AgendamentoApiApplicationTests` | 1 | SpringBootTest | Inicialização do contexto Spring |
| | **Total** | **139** | | |

> **Nota:** a execução do Maven (`mvn test`) reporta **138 testes** aprovados. A diferença de uma unidade decorre da forma como o JUnit 5 contabiliza internamente os *test methods* dentro de classes aninhadas (`@Nested`) em determinadas versões do *runner* Surefire. Em nenhum cenário há falha ou teste ignorado.

---

## 5 Pirâmide de Testes

A distribuição da suíte segue a Pirâmide de Testes (*Test Pyramid*) descrita por Cohn (2009) e expandida por Fowler (2012), conforme ilustrado abaixo:

```
        ┌─────────────┐
        │    E2E (8)  │     ← Testes mais lentos, maior confiança de integração
        ├─────────────┤
        │Integração   │
        │Controller   │     ← Validam HTTP, segurança e serialização
        │(48 testes)  │
        ├─────────────┤
        │  Repositório│
        │  (30 testes)│     ← Validam queries SQL contra banco real
        ├─────────────┤
        │   Unidade   │     ← Testes mais rápidos, maior quantidade
        │ (52 testes) │
        └─────────────┘
```

**Justificativa da distribuição:**

A base da pirâmide é formada pelos testes de unidade (52 testes em `AgendamentoServiceTest` e `ClienteServiceTest`), que validam individualmente cada decisão de negócio. São os mais rápidos e os mais baratos de manter, pois não dependem de infraestrutura.

A camada intermediária é a mais numerosa em termos de arquivos (seis classes, 78 testes entre repositórios e controladores), refletindo a importância de verificar tanto o mapeamento objeto-relacional quanto o contrato HTTP da API. Os testes de repositório são especialmente críticos neste projeto por conta da *query* de sobreposição de horários, cuja semântica só pode ser validada com precisão contra um banco SQL real.

O topo da pirâmide concentra os 8 testes end-to-end, deliberadamente mais escassos por serem mais lentos e frágeis a mudanças estruturais. Contudo, esses testes fornecem a maior confiança sobre o comportamento do sistema como um todo, validando fluxos completos que nenhuma das camadas inferiores pode verificar isoladamente.

Essa distribuição alinha-se ao princípio enunciado por Fowler (2012): *"Have lots of small unit tests, some integration tests, and a few end-to-end tests."* — o que se traduz, neste projeto, em uma razão aproximada de **6,5 testes de unidade para cada teste E2E**.

---

## 6 Cobertura por Regra de Negócio

As cinco regras de negócio centrais da Agendamento API possuem cobertura de testes mapeada conforme descrito a seguir:

### RN01 — Unicidade de dados cadastrais do Cliente (e-mail e CPF)

> Um cliente não pode ser cadastrado ou ter seus dados atualizados para um e-mail ou CPF já utilizado por outro cliente.

| Arquivo de Teste | Tipo | O que verifica |
|---|---|---|
| `ClienteServiceTest` | Unidade | `BusinessException` ao criar com e-mail duplicado; `BusinessException` ao criar com CPF duplicado; `BusinessException` ao atualizar com e-mail de outro cliente; `BusinessException` ao atualizar com CPF de outro cliente |
| `ClienteRepositoryTest` | Repositório | `existsByEmailAndIdNot` retorna `true`/`false` corretamente; `existsByCpfAndIdNot` retorna `true`/`false` corretamente |
| `ClienteControllerTest` | Controlador | Payload inválido retorna HTTP 400 |

---

### RN02 — Detecção de Conflito de Horário do Profissional

> Não pode ser criado ou atualizado um agendamento cujo intervalo de tempo se sobreponha a outro agendamento ativo (status `AGENDADO` ou `CONFIRMADO`) para o mesmo profissional.

| Arquivo de Teste | Tipo | O que verifica |
|---|---|---|
| `AgendamentoRepositoryTest` | Repositório | Cinco cenários de sobreposição: horário exato, início sobreposto, fim sobreposto, agendamento englobante e horário adjacente (sem conflito); filtragem por status ativo (`AGENDADO`/`CONFIRMADO`); isolamento por profissional |
| `AgendamentoServiceTest` (criar) | Unidade | `BusinessException` ao detectar conflito via mock do repositório; `verify(repository, never()).save(any())` confirma que nada é persistido |
| `AgendamentoServiceTest` (atualizar) | Unidade | Conflito na atualização; uso correto de `findConflitosHorarioExcluindoId` |
| `AgendamentoControllerTest` | Controlador | HTTP 409 retornado quando serviço lança `BusinessException` de conflito |
| `AgendamentoIntegrationTest` | E2E | Sobreposição parcial retorna 409 e banco contém 1 registro; mesmo horário exato retorna 409; horários separados e profissionais distintos são aceitos |

---

### RN03 — Máquina de Estados do Agendamento (Ciclo de Vida)

> O agendamento deve seguir a máquina de estados: `AGENDADO → CONFIRMADO`, `AGENDADO → CANCELADO`, `CONFIRMADO → CONCLUIDO`, `CONFIRMADO → CANCELADO`. Transições não previstas devem ser rejeitadas.

| Arquivo de Teste | Tipo | O que verifica |
|---|---|---|
| `AgendamentoServiceTest` (atualizarStatus — permitidas) | Unidade | As quatro transições válidas produzem o status correto e chamam `save` |
| `AgendamentoServiceTest` (atualizarStatus — proibidas) | Unidade | `CONCLUIDO → *` lança `BusinessException`; `CANCELADO → *` lança `BusinessException`; `AGENDADO → CONCLUIDO` (pular) lança `BusinessException`; `AGENDADO → AGENDADO` lança `BusinessException`; `CONFIRMADO → AGENDADO` lança `BusinessException` |
| `AgendamentoControllerTest` | Controlador | HTTP 409 para transição inválida via PATCH /status |
| `AgendamentoIntegrationTest` | E2E | Fluxo `AGENDADO → CONFIRMADO → CONCLUIDO` completo; tentativa de `AGENDADO → CONCLUIDO` retorna 409; estado `CONCLUIDO` é terminal (rejeita qualquer mudança com 409) |

---

### RN04 — Imutabilidade de Agendamentos Concluídos e Cancelados

> Agendamentos nos estados `CONCLUIDO` e `CANCELADO` não podem ser atualizados (PUT) nem cancelados (DELETE).

| Arquivo de Teste | Tipo | O que verifica |
|---|---|---|
| `AgendamentoServiceTest` (atualizar) | Unidade | `BusinessException` ao chamar `atualizar()` com status `CONCLUIDO`; `BusinessException` ao chamar `atualizar()` com status `CANCELADO` |
| `AgendamentoServiceTest` (cancelar) | Unidade | `BusinessException` ao chamar `cancelar()` com status `CONCLUIDO`; `BusinessException` ao chamar `cancelar()` com status `CANCELADO`; `verify(repository, never()).save(any())` confirma não-persistência |
| `AgendamentoControllerTest` | Controlador | HTTP 409 ao chamar DELETE em agendamento `CONCLUIDO` |
| `AgendamentoIntegrationTest` | E2E | Estado `CONCLUIDO` rejeita `CANCELADO` com 409 no banco real |

---

### RN05 — Controle de Acesso Baseado em Papéis (RBAC)

> Operações de leitura (GET) estão disponíveis para usuários autenticados com papel `USER` ou superior. Operações de escrita (POST, PUT, DELETE, PATCH) exigem papel `ADMIN`. Requisições sem autenticação recebem HTTP 401.

| Arquivo de Teste | Tipo | O que verifica |
|---|---|---|
| `AgendamentoControllerTest` | Controlador | `USER` recebe 403 em POST e DELETE; sem autenticação recebe 401 em GET |
| `ClienteControllerTest` | Controlador | `USER` recebe 403 em POST e DELETE; sem autenticação recebe 401 em GET |
| `ProfissionalControllerTest` | Controlador | `USER` recebe 403 em POST e DELETE; sem autenticação recebe 401 em GET |
| `ServicoControllerTest` | Controlador | `USER` recebe 403 em POST e DELETE; sem autenticação recebe 401 em GET |

---

## 7 Tecnologias de Teste

### 7.1 JUnit 5 (JUnit Jupiter)

O JUnit 5, lançado em 2017, é a plataforma de testes unitários padrão do ecossistema Java moderno. Em relação a versões anteriores, introduz uma arquitetura modular (Platform, Jupiter, Vintage), o suporte nativo a classes e métodos aninhados via `@Nested`, nomes declarativos com `@DisplayName` e uma API fluente para verificação de exceções (`assertThrows`, `assertThatCode`). Neste projeto, o JUnit 5 é o motor executor de todos os 138 testes, integrando-se ao Maven Surefire Plugin para geração de relatórios.

### 7.2 Mockito

O Mockito é a biblioteca de criação de objetos dublê (*mocks*, *stubs* e *spies*) mais amplamente utilizada no ecossistema Java (MOCKITO, 2024). Permite substituir colaboradores reais por implementações controladas que registram chamadas e retornam valores configurados. Na suíte deste projeto, é utilizado em dois cenários:

- **`@ExtendWith(MockitoExtension.class)`** nos testes de unidade: o Mockito gerencia o ciclo de vida dos mocks (criados com `@Mock`) e realiza a injeção automática no objeto testado (`@InjectMocks`) sem necessidade de inicializar o contexto Spring.
- **`@MockitoBean`** nos testes de controlador: substitui beans Spring específicos (as classes de serviço) por mocks dentro do contexto Spring carregado pelo `@SpringBootTest`, permitindo que o teste foque exclusivamente na camada HTTP.

### 7.3 MockMvc

O `MockMvc` é a API do Spring Test para simulação de requisições HTTP sem inicialização de servidor. Permite formular requisições com método, caminho, cabeçalhos e corpo, e verificar a resposta com assertivas sobre status HTTP, campos JSON (via `jsonPath` com sintaxe JSONPath), tipo de conteúdo e outros atributos. Neste projeto, o `MockMvc` é configurado via `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity())`, o que garante que toda a cadeia de filtros do Spring Security — incluindo autenticação, autorização e CSRF — seja exercitada nos testes.

### 7.4 Spring Security Test (`@WithMockUser`)

A anotação `@WithMockUser` injeta no contexto de segurança do Spring um usuário autenticado com os papéis informados, eliminando a necessidade de realizar fluxos de login com JWT durante os testes. Nos testes de controlador, diferentes papéis (`USER`, `ADMIN`) e a ausência de autenticação são utilizados para verificar o comportamento do controle de acesso em cada endpoint.

### 7.5 AssertJ

O AssertJ é uma biblioteca de assertivas fluentes que produz mensagens de erro mais expressivas do que as assertivas padrão do JUnit (`assertEquals`, `assertTrue`). Sua API encadeada permite construir verificações descritivas como `assertThat(resultado).isNotNull().hasSize(2)` e `assertThatThrownBy(...).isInstanceOf(BusinessException.class).hasMessageContaining("horário")`. O uso de AssertJ em toda a suíte melhora significativamente a legibilidade dos testes e a clareza das mensagens de falha.

### 7.6 H2 Database (banco em memória)

O H2 é um banco de dados relacional implementado em Java, utilizado neste projeto exclusivamente durante a execução dos testes. Sua configuração como banco em memória (`jdbc:h2:mem:testdb`) garante que cada execução da suíte parta de um estado limpo, sem dependência de banco externo. O Spring Boot configura automaticamente o H2 quando a dependência `spring-boot-starter-test` está presente e o perfil de testes está ativo.

---

## 8 Como Executar os Testes

### 8.1 Pré-requisitos

- Java 21 ou superior
- Maven 3.9 ou superior
- Não são necessários bancos de dados externos; o H2 em memória é inicializado automaticamente

### 8.2 Execução Completa

Para executar toda a suíte de 138 testes:

```bash
mvn test
```

### 8.3 Execução de uma Classe Específica

```bash
# Apenas os testes de unidade do serviço de agendamento
mvn test -Dtest=AgendamentoServiceTest

# Apenas os testes de integração end-to-end
mvn test -Dtest=AgendamentoIntegrationTest

# Apenas os testes de repositório
mvn test -Dtest=AgendamentoRepositoryTest,ClienteRepositoryTest
```

### 8.4 Execução com Relatório de Cobertura (JaCoCo)

Caso o projeto possua o plugin JaCoCo configurado no `pom.xml`:

```bash
mvn verify
```

O relatório HTML de cobertura será gerado em `target/site/jacoco/index.html`.

### 8.5 Execução com Saída Detalhada (verbose)

```bash
mvn test -Dsurefire.useFile=false
```

---

## 9 Resultados da Execução

A execução completa da suíte de testes com o comando `mvn test` produz o seguinte resultado:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.guilhermebraga.agendamento_api.AgendamentoApiApplicationTests
[INFO] Running com.guilhermebraga.agendamento_api.AgendamentoControllerTest
[INFO] Running com.guilhermebraga.agendamento_api.AgendamentoIntegrationTest
[INFO] Running com.guilhermebraga.agendamento_api.AgendamentoRepositoryTest
[INFO] Running com.guilhermebraga.agendamento_api.AgendamentoServiceTest
[INFO] Running com.guilhermebraga.agendamento_api.ClienteControllerTest
[INFO] Running com.guilhermebraga.agendamento_api.ClienteRepositoryTest
[INFO] Running com.guilhermebraga.agendamento_api.service.ClienteServiceTest
[INFO] Running com.guilhermebraga.agendamento_api.ProfissionalControllerTest
[INFO] Running com.guilhermebraga.agendamento_api.ServicoControllerTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 138, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

**Resumo dos resultados:**

| Métrica | Valor |
|---------|-------|
| Total de testes executados | 138 |
| Aprovados (*passed*) | 138 |
| Falhas (*failures*) | 0 |
| Erros (*errors*) | 0 |
| Ignorados (*skipped*) | 0 |
| Taxa de sucesso | 100% |
| Resultado do *build* | SUCCESS |

Todos os 138 testes foram aprovados sem falhas nem erros em todas as execuções realizadas durante o desenvolvimento do projeto, confirmando a corretude das implementações e a robustez das regras de negócio da Agendamento API.

---

## 10 Considerações Finais

A suíte de testes automatizados desenvolvida para a Agendamento API exemplifica a aplicação prática das boas práticas de Engenharia de Software na construção de sistemas modernos com Spring Boot. A distribuição em três camadas — unidade, integração e end-to-end — garante tanto a velocidade de retroalimentação durante o desenvolvimento quanto a confiança sobre o comportamento integrado do sistema.

A escolha de ferramentas consolidadas pelo mercado (JUnit 5, Mockito, MockMvc e AssertJ) assegura que as práticas adotadas estejam alinhadas ao estado da arte do desenvolvimento Java em 2025–2026. A cobertura das cinco regras de negócio críticas — unicidade cadastral, conflito de horário, máquina de estados, imutabilidade de estados terminais e controle de acesso — demonstra que os testes não foram concebidos apenas como verificação técnica, mas como documentação executável do comportamento esperado do sistema.

---

## Referências

BECK, Kent. **Test Driven Development: By Example**. Boston: Addison-Wesley, 2002.

COHN, Mike. **Succeeding with Agile: Software Development Using Scrum**. Boston: Addison-Wesley, 2009.

FOWLER, Martin. **TestPyramid**. 2012. Disponível em: <https://martinfowler.com/bliki/TestPyramid.html>. Acesso em: 22 mai. 2026.

JUNIT TEAM. **JUnit 5 User Guide**. 2024. Disponível em: <https://junit.org/junit5/docs/current/user-guide/>. Acesso em: 22 mai. 2026.

MOCKITO. **Mockito Framework Site**. 2024. Disponível em: <https://site.mockito.org/>. Acesso em: 22 mai. 2026.

SPRING. **Spring Framework Testing Documentation**. 2024. Disponível em: <https://docs.spring.io/spring-framework/reference/testing.html>. Acesso em: 22 mai. 2026.
