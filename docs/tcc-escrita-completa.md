# TCC UNDB 2026 — Sistema de Agendamento
## Guilherme Braga — Roteiro Completo de Escrita

> **Como usar este arquivo**: Cole cada seção no Claude (claude.ai) com a instrução:
> "Escreva o texto acadêmico para o seguinte tópico do TCC, em português acadêmico formal,
> seguindo normas ABNT, com ~X palavras. Use os dados técnicos abaixo como base."
> Adapte ~X ao tamanho desejado.

---

# ESTRUTURA DO TCC

```
Capa / Folha de Rosto / Resumo / Abstract
1. Introdução
2. Fundamentação Teórica
3. Metodologia
4. Desenvolvimento (Implementação)
5. Resultados e Discussão
6. Conclusão
Referências
Apêndices
```

---

# CAPÍTULO 1 — INTRODUÇÃO

## 1.1 Contextualização

**Dados para o Claude expandir:**
- Contexto: pequenas e médias empresas de serviços (salões de beleza, clínicas,
  consultórios, academias) perdem tempo e receita com agendamentos manuais
  (cadernos, WhatsApp, planilhas)
- Problema: duplo agendamento (double booking), esquecimentos, dificuldade de
  acompanhamento de status, falta de visibilidade do histórico do cliente
- Solução proposta: sistema web de agendamento com painel administrativo e portal
  de autoagendamento para o cliente
- Relevância: mercado de software para gestão de micro/pequenas empresas em
  crescimento no Brasil pós-pandemia (digitalização acelerada)
- Justificativa acadêmica: integra conceitos de engenharia de software (MVC,
  REST, Spring, JPA, segurança), aplicados em contexto real

## 1.2 Objetivos

**Objetivo Geral:**
Desenvolver um sistema web de agendamento de serviços que permita ao administrador
gerenciar toda a operação e ao cliente realizar autoagendamento de forma autônoma.

**Objetivos Específicos:**
1. Implementar API RESTful com Spring Boot 4.0.5 e documentação OpenAPI 3.0
2. Criar painel administrativo web com Thymeleaf para CRUD de clientes, profissionais, serviços e agendamentos
3. Desenvolver portal de autoagendamento para clientes com wizard multi-etapa
4. Aplicar Spring Security com autenticação JWT para proteção dos endpoints REST
5. Implementar regra de negócio de detecção e prevenção de conflito de horário
6. Garantir persistência com Spring Data JPA sobre H2 (dev) e PostgreSQL (prod)

## 1.3 Justificativa

**Dados para o Claude expandir:**
- Estatísticas sobre digitalização de pequenas empresas (buscar dado atual SEBRAE/FGV)
- Problema concreto: um salão com 3 profissionais e 8h/dia perde ~15% do tempo
  útil em gestão manual de agenda (ligações, confirmações, reagendamentos)
- Solução de software reduz essa perda e permite histórico consultável
- Escolha tecnológica justificada: Java/Spring é a plataforma mais utilizada em
  sistemas corporativos no Brasil (pesquisa JetBrains Developer Ecosystem 2024)

## 1.4 Estrutura do Trabalho

Parágrafo padrão descrevendo a organização dos capítulos.

---

# CAPÍTULO 2 — FUNDAMENTAÇÃO TEÓRICA

## 2.1 Arquitetura de Software

### 2.1.1 Padrão MVC (Model-View-Controller)
**Dados técnicos:**
- Definição do padrão MVC e suas três camadas
- Como o Spring MVC implementa: DispatcherServlet como Front Controller,
  @Controller processa requests, Model carrega dados para a View
- View renderizada pelo Thymeleaf no servidor (SSR) vs SPA (React/Angular)
- Por que SSR para este projeto: simplicidade, sem build frontend, SEO, menos
  round-trips para operações CRUD simples

### 2.1.2 Arquitetura em Camadas
**Dados técnicos:**
- Camadas do projeto: Controller → Service → Repository → Entity
- Cada camada tem responsabilidade única (Single Responsibility Principle)
- DTO (Data Transfer Object) entre Controller e Service isola o modelo de domínio
- MapStruct para geração de código de mapeamento em tempo de compilação

### 2.1.3 API REST
**Dados técnicos:**
- Roy Fielding, 2000, tese "Architectural Styles and the Design of Network-based
  Software Architectures"
- 6 constraints REST: stateless, client-server, cacheable, uniform interface,
  layered system, code on demand (opcional)
- Verbos HTTP: GET (leitura idempotente), POST (criação), PUT (atualização
  completa), PATCH (atualização parcial), DELETE (remoção)
- Status codes: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized,
  403 Forbidden, 404 Not Found, 409 Conflict
- Nível de maturidade Richardson: o projeto está no nível 2 (Resources + Verbs)

## 2.2 Tecnologias Utilizadas

### 2.2.1 Spring Boot 4.0.5
**Dados técnicos:**
- Lançado em 2024, baseado em Spring Framework 7.x e Jakarta EE 11
- Autoconfiguração: detecta dependências no classpath e configura beans automaticamente
- Servidor embutido Tomcat: aplicação executável com java -jar
- Spring Initializr: geração do projeto com dependências Maven
- Vantagem sobre Spring MVC puro: elimina XML de configuração (Convention over Configuration)
- Produção: compatível com JDK 21 (LTS), deploy via Docker/Railway/AWS

### 2.2.2 Spring Data JPA
**Dados técnicos:**
- JPA (Jakarta Persistence API): especificação ORM para mapeamento objeto-relacional
- Hibernate como implementação JPA: gera DDL, executa queries, gerencia o ciclo
  de vida das entidades (@Entity, @Id, @Column, @ManyToOne, @OneToMany)
- Spring Data JPA: camada de abstração sobre JPA com JpaRepository
- Queries derivadas por convenção de nome: findByEmail, existsByProfissionalIdAndDataHoraFimGreaterThan
- @Transactional: atomicidade, consistência e isolamento nas operações de banco
- H2 em desenvolvimento (criar-e-destruir no startup), PostgreSQL em produção

### 2.2.3 Spring Security
**Dados técnicos:**
- SecurityFilterChain: cadeia de filtros que intercepta cada requisição HTTP
- Dois mecanismos de autenticação configurados em paralelo:
  1. HTTP Basic: para Swagger UI e testes diretos
  2. JWT Bearer token: para clientes REST/mobile
- RBAC (Role-Based Access Control): ROLE_USER (leitura) e ROLE_ADMIN (escrita)
- Autorização por anotação: hasRole("ADMIN") no SecurityFilterChain
- BCrypt para hash de senhas (fator de custo adaptável contra brute force)
- CSRF desabilitado para API stateless (tokens JWT já protegem contra CSRF)

### 2.2.4 JWT (JSON Web Token)
**Dados técnicos:**
- RFC 7519, 2015
- Estrutura: header.payload.signature (Base64URL)
- Header: {"alg":"HS512","typ":"JWT"}
- Payload: {"sub":"admin","iat":..., "exp":...}
- Assinatura: HMAC-SHA512 do header+payload com secret key
- Vantagem: stateless — servidor não precisa armazenar sessão
- Validade de 24h, renovação via novo login
- jjwt-api 0.12.x com Jwts.builder() / Jwts.parser()

### 2.2.5 Thymeleaf
**Dados técnicos:**
- Template engine Java para Server-Side Rendering
- Dialeto Spring: th:object, th:field, th:errors, th:if, th:each, th:href
- Natural templates: HTML válido mesmo sem servidor (útil para design/mockup)
- Fragmentos parametrizados: th:replace="~{layout :: html(~{:: conteudo})}"
- Integração com Spring Security: sec:authorize para mostrar/esconder elementos
- Integração com Bean Validation: th:errors="*{campo}" exibe mensagens de erro

### 2.2.6 Padrão DTO e MapStruct
**Dados técnicos:**
- DTO (Data Transfer Object): objeto sem comportamento para transporte de dados
- Problema sem DTO: entidades JPA com relações bidirecionais causam
  StackOverflowError na serialização JSON (ciclo infinito)
- MapStruct: processador de anotações que gera código de mapeamento em tempo de
  compilação (não usa reflexão em runtime → mais performático)
- @Mapper(componentModel = "spring"): bean Spring gerado pelo MapStruct
- Request DTOs: entrada (validação Bean Validation); Response DTOs: saída (exposição controlada)

### 2.2.7 Bootstrap 5 e Chart.js
**Dados técnicos:**
- Bootstrap 5.3.3: framework CSS grid system 12 colunas, componentes (cards,
  modals, badges, alerts), responsividade mobile-first via CDN
- Bootstrap Icons 1.11.3: ícones SVG inline
- Chart.js 4.4.3: biblioteca JavaScript para gráficos; gráfico doughnut no
  dashboard para distribuição de agendamentos por status
- Dados passados para Chart.js via atributos data-* em elemento hidden (evita
  interpolação de string em JS, mais seguro contra XSS)

## 2.3 Segurança de Aplicações Web

### 2.3.1 OWASP Top 10
**Dados técnicos:**
- A01:2021 Broken Access Control → mitigado por RBAC com Spring Security
- A02:2021 Cryptographic Failures → BCrypt para senhas, JWT assinado com HS512
- A03:2021 Injection → Spring Data JPA com queries parametrizadas (sem SQL concatenado)
- A04:2021 Insecure Design → validação em múltiplas camadas (HTML5 + Bean Validation)
- A05:2021 Security Misconfiguration → CSRF desabilitado apenas para API stateless;
  operações destrutivas usam POST (não GET) para evitar ações acidentais

### 2.3.2 Autenticação vs Autorização
**Dados técnicos:**
- Autenticação: "quem você é?" — verificada por senha (BCrypt) ou token JWT
- Autorização: "o que você pode fazer?" — verificada por roles (ADMIN/USER)
- Spring Security: AuthenticationManager (autenticação) + SecurityFilterChain (autorização)
- Portal do cliente: identificação por e-mail sem senha (escopo TCC — magic link
  seria o ideal em produção)

## 2.4 Boas Práticas de Desenvolvimento

### 2.4.1 Clean Code e SOLID
- Single Responsibility: cada Controller gerencia um recurso
- Open/Closed: novas validações via Service sem alterar Controller
- Dependency Inversion: Controller depende da interface Service, não da implementação concreta

### 2.4.2 Lombok
- @Data, @Builder, @RequiredArgsConstructor, @Slf4j
- Geração em tempo de compilação via APT (Annotation Processing Tool)
- Elimina getters/setters/construtores/equals/hashCode boilerplate

### 2.4.3 Logging com SLF4J + Logback
- @Slf4j (Lombok): injeta logger SLF4J
- log.info() para eventos de negócio (agendamento criado/cancelado)
- log.warn() para situações esperadas mas anômalas (conflito de horário)
- log.error() para exceções inesperadas com stack trace

---

# CAPÍTULO 3 — METODOLOGIA

## 3.1 Tipo de Pesquisa
**Dados para o Claude expandir:**
- Pesquisa aplicada: foco na solução de um problema prático (agendamento)
- Abordagem qualitativa (análise dos requisitos) e quantitativa (métricas do sistema)
- Método: desenvolvimento iterativo e incremental (não waterfall, mas também
  não Scrum formal — desenvolvimento solo)

## 3.2 Levantamento de Requisitos
**Dados técnicos:**

**Requisitos Funcionais:**
- RF01: CRUD de Clientes (nome, email, telefone, CPF)
- RF02: CRUD de Profissionais (nome, especialidade, email, telefone)
- RF03: CRUD de Serviços (nome, descrição, duração em minutos, preço)
- RF04: CRUD de Agendamentos com detecção de conflito de horário
- RF05: Fluxo de status: AGENDADO → CONFIRMADO → CONCLUIDO / CANCELADO
- RF06: Portal de autoagendamento para clientes (wizard 4 etapas)
- RF07: Dashboard administrativo com métricas e gráficos
- RF08: API REST documentada com Swagger UI
- RF09: Autenticação JWT para a API REST

**Requisitos Não-Funcionais:**
- RNF01: Tempo de resposta < 2s para operações de listagem
- RNF02: Interface responsiva (funciona em celular e desktop)
- RNF03: Senhas armazenadas com BCrypt (sem texto plano)
- RNF04: API documentada (OpenAPI 3.0)
- RNF05: Dados de demonstração pré-carregados no startup (dev profile)

**Regras de Negócio:**
- RN01: Conflito de horário — profissional não pode ter dois agendamentos simultâneos
- RN02: Transições de status válidas: AGENDADO→{CONFIRMADO,CANCELADO}, CONFIRMADO→{CONCLUIDO,CANCELADO}
- RN03: Agendamentos CONCLUIDO ou CANCELADO não podem ser editados
- RN04: O campo dataHoraFim = dataHora + duracaoMinutos do serviço
- RN05: Identificação de clientes no portal apenas por e-mail (sem senha)

## 3.3 Ferramentas e Ambiente de Desenvolvimento
| Ferramenta | Versão | Uso |
|---|---|---|
| Java (OpenJDK) | 21 LTS | Linguagem e runtime |
| Spring Boot | 4.0.5 | Framework principal |
| Maven | 3.9.x | Gerenciamento de dependências |
| H2 Database | 2.x | Banco em memória (desenvolvimento) |
| PostgreSQL | 16 | Banco relacional (produção) |
| IntelliJ IDEA | 2024.x | IDE |
| Git/GitHub | — | Versionamento de código |
| Postman | — | Testes manuais da API |

## 3.4 Modelagem do Banco de Dados
**Dados técnicos:**

**Entidades e atributos:**
```
clientes: id, nome, email (UNIQUE), telefone, cpf, criado_em, atualizado_em
profissionais: id, nome, especialidade, email, telefone, criado_em, atualizado_em
servicos: id, nome, descricao, duracao_minutos, preco (DECIMAL 10,2), criado_em, atualizado_em
agendamentos: id, cliente_id (FK), profissional_id (FK), servico_id (FK),
              data_hora, data_hora_fim, status (ENUM), criado_em, atualizado_em
```

**Relacionamentos:**
- agendamentos N:1 clientes (um cliente pode ter N agendamentos)
- agendamentos N:1 profissionais (um profissional pode ter N agendamentos)
- agendamentos N:1 servicos (um serviço pode aparecer em N agendamentos)

**Índices sugeridos para produção:**
- INDEX(profissional_id, data_hora) na tabela agendamentos (query de conflito)
- UNIQUE(email) em clientes e profissionais

---

# CAPÍTULO 4 — DESENVOLVIMENTO (IMPLEMENTAÇÃO)

> **NOTA**: Este capítulo deve conter prints de tela do sistema em execução.
> Capture screenshots das seguintes telas:
> - Dashboard com dados preenchidos
> - Listagem de agendamentos com cores de status
> - Wizard step1, step2, step3, step4
> - Swagger UI com endpoints
> - Formulário com erro de validação (Bean Validation)
> - Modal de confirmação de deleção

## 4.1 Estrutura do Projeto

**Pacotes principais:**
```
com.guilhermebraga.agendamento_api
├── config/          → SecurityConfig, DataInitializer
├── controller/
│   ├── web/controller/  → AdminWebControllers (Thymeleaf)
│   └── portal/      → PortalControllers (Thymeleaf cliente)
│   └── api/         → REST Controllers
├── dto/
│   ├── request/     → DTOs de entrada (Bean Validation)
│   └── response/    → DTOs de saída
├── entity/          → Entidades JPA
├── exception/       → BusinessException, ResourceNotFoundException
├── mapper/          → Interfaces MapStruct
├── repository/      → Interfaces Spring Data JPA
├── security/        → JwtTokenProvider, JwtAuthenticationFilter
└── service/         → Lógica de negócio
```

**Templates Thymeleaf:**
```
templates/
├── layout/base.html         → Layout admin (navbar, sidebar)
├── portal/layout.html       → Layout portal cliente
├── dashboard/index.html     → Dashboard com Chart.js
├── agendamentos/            → listar, formulario, editar
├── clientes/                → listar, formulario, detalhe
├── profissionais/           → listar, formulario
├── servicos/                → listar, formulario
├── portal/wizard/           → step1, step2, step3, step4
└── error/                   → 403, 404, 500
```

## 4.2 Fluxo de Autenticação JWT

**Sequência técnica:**
1. Cliente envia POST /api/v1/auth/login com {email, senha}
2. AuthController usa AuthenticationManager.authenticate() com UsernamePasswordAuthenticationToken
3. Spring Security valida contra InMemoryUserDetailsManager (BCrypt.matches)
4. Sucesso: JwtTokenProvider.generateToken(username) → Jwts.builder().subject().signWith(HS512)
5. Resposta: {token, tipo:"Bearer", expiracao:+24h}
6. Próximas requisições: header "Authorization: Bearer <token>"
7. JwtAuthenticationFilter (OncePerRequestFilter) extrai o token
8. JwtTokenProvider.validateToken() → Jwts.parser().verifyWith(secretKey).parseSignedClaims()
9. Se válido: SecurityContextHolder.getContext().setAuthentication(...)
10. Spring Security verifica se o usuário autenticado tem a role necessária

## 4.3 Detecção de Conflito de Horário

**Código/lógica técnica:**
```java
// AgendamentoRepository.java
@Query("SELECT COUNT(a) > 0 FROM Agendamento a " +
       "WHERE a.profissional.id = :profissionalId " +
       "AND a.status NOT IN (CANCELADO) " +
       "AND a.dataHora < :dataHoraFim " +
       "AND a.dataHoraFim > :dataHora " +
       "AND (:ignorarId IS NULL OR a.id != :ignorarId)")
boolean existeConflito(...);
```
Dois intervalos [A,B) e [C,D) se sobrepõem quando A < D AND C < B.
dataHoraFim = dataHora + servico.duracaoMinutos

## 4.4 Wizard Multi-etapa (@SessionAttributes)

**Fluxo técnico:**
- WizardForm implements Serializable: objeto mantido em sessão HTTP
- @SessionAttributes("wizard") no PortalClienteController
- Step1 POST: wizard.setServicoId/Nome/Duracao/Preco
- Step2 POST: wizard.setProfissionalId/Nome/Especialidade + data + hora
- Step3 POST: exibe resumo (read-only), aguarda confirmação
- Step4 POST: agendamentoService.criarPeloPortal(clienteId, ...) → grava no banco
- SessionStatus.setComplete(): limpa o WizardForm da sessão

## 4.5 API REST — Endpoints Principais

| Método | URL | Roles | Descrição |
|---|---|---|---|
| POST | /api/v1/auth/login | PUBLIC | Gera JWT |
| GET | /api/v1/clientes | USER, ADMIN | Lista clientes |
| POST | /api/v1/clientes | ADMIN | Cria cliente |
| GET | /api/v1/clientes/{id} | USER, ADMIN | Busca cliente |
| PUT | /api/v1/clientes/{id} | ADMIN | Atualiza cliente |
| DELETE | /api/v1/clientes/{id} | ADMIN | Remove cliente |
| PATCH | /api/v1/agendamentos/{id}/status | ADMIN | Muda status |
| (idem para profissionais, servicos, agendamentos) | | | |

---

# CAPÍTULO 5 — RESULTADOS E DISCUSSÃO

## 5.1 Funcionalidades Implementadas (checklist)

**Dados para o Claude expandir em prosa:**

✅ BACKEND:
- CRUD completo: Clientes, Profissionais, Serviços, Agendamentos
- Regra de conflito de horário com detecção e rejeição
- Fluxo de status com transições válidas
- API REST com autenticação JWT (HS512) + HTTP Basic
- RBAC: ROLE_USER (leitura) / ROLE_ADMIN (escrita)
- DataInitializer com dados de demonstração (perfil dev)
- Logging estruturado com SLF4J + @Slf4j

✅ FRONTEND:
- Dashboard com 4 cards de métricas + gráfico doughnut Chart.js + tabela próximos
- CRUD admin completo com Thymeleaf (Bootstrap 5)
- Validação Bean Validation + th:errors no frontend
- Portal de autoagendamento wizard 4 etapas
- Modal de confirmação para ações destrutivas
- Páginas de erro customizadas (403, 404, 500)
- Layout responsivo mobile-first Bootstrap 5

✅ DOCUMENTAÇÃO:
- Swagger UI em /swagger-ui.html
- OpenAPI 3.0 spec em /api-docs
- Endpoints filtrados (apenas /api/v1/**)

## 5.2 Limitações Identificadas

**Para o Claude expandir com justificativa técnica:**
1. Autenticação do portal por e-mail (sem senha) — adequado para TCC, produção exigiria magic link ou OAuth2
2. InMemoryUserDetailsManager — adequado para TCC, produção exigiria UserDetailsService com banco de dados
3. Sem paginação nos endpoints de listagem — sem carga real, não impacta o TCC
4. Sem envio de email de confirmação — seria implementado com Spring Mail + SMTP
5. H2 Console inacessível via browser no Spring Boot 4.x — banco consultável via API
6. Testes automatizados — estrutura criada mas cobertura mínima (foco em funcionalidade)

## 5.3 Comparação com Trabalhos Relacionados

**Sugestão de trabalhos para referenciar:**
- Sistemas de agendamento similares em TCC (buscar no BDTD)
- Comparar abordagem MVC+Thymeleaf vs SPA (React+API)
- Vantagens do approach escolhido: menos complexidade operacional, sem CORS por padrão, SSR para SEO

## 5.4 Discussão dos Resultados

**Pontos para desenvolver:**
- O sistema atende todos os requisitos funcionais levantados na seção 3.2
- A detecção de conflito foi testada e funciona corretamente
- O wizard garante uma experiência de autoagendamento sem erros de preenchimento parcial
- A autenticação JWT permite uso da API por aplicativos mobile futuros
- A arquitetura MVC facilita manutenção e extensão

---

# CAPÍTULO 6 — CONCLUSÃO

## 6.1 Conclusão

**Para o Claude expandir:**
- O trabalho atingiu todos os objetivos específicos propostos
- A tecnologia Spring Boot 4.0.5 se mostrou adequada para o desenvolvimento rápido
  de sistemas web com múltiplas interfaces (admin, portal, API)
- O padrão MVC com camadas bem definidas facilita a manutenção
- O wizard multi-etapa com @SessionAttributes demonstrou uma solução elegante
  para formulários complexos sem expor todos os dados em hidden fields
- A segurança foi implementada em múltiplas camadas (validação, autenticação, autorização)

## 6.2 Trabalhos Futuros

1. Autenticação robusta no portal (magic link por email, OAuth2/Google)
2. Notificações por email (Spring Mail) e SMS (Twilio API)
3. Paginação com Spring Data Pageable nos endpoints de listagem
4. Relatórios: exportação PDF/Excel do histórico de agendamentos
5. Aplicativo mobile (React Native) consumindo a API REST existente
6. Cache (Spring Cache + Redis) para listas de serviços/profissionais
7. Deploy automatizado com GitHub Actions + Docker + Railway/AWS
8. Testes automatizados com cobertura >80% (JUnit 5 + Mockito + @SpringBootTest)
9. Multi-tenant (suporte a múltiplas empresas em uma única instância)

---

# REFERÊNCIAS ABNT (Template)

> **Instrução**: Cole esta lista no Claude com o pedido:
> "Formate as referências abaixo no padrão ABNT NBR 6023:2018"

- Spring Boot Documentation 4.0.x — https://docs.spring.io/spring-boot/
- Spring Security Reference — https://docs.spring.io/spring-security/
- Thymeleaf Documentation — https://www.thymeleaf.org/documentation.html
- JJWT (Java JWT) — https://github.com/jwtk/jjwt
- RFC 7519: JSON Web Token — https://tools.ietf.org/html/rfc7519
- OWASP Top Ten 2021 — https://owasp.org/www-project-top-ten/
- FOWLER, Martin. Patterns of Enterprise Application Architecture. Addison-Wesley, 2002.
- MARTIN, Robert C. Clean Code: A Handbook of Agile Software Craftsmanship. Prentice Hall, 2008.
- WALLS, Craig. Spring in Action. 6ª ed. Manning, 2022.
- FIELDING, Roy T. Architectural Styles and the Design of Network-based Software Architectures. 
  Tese (Doutorado) — University of California, Irvine, 2000.
- Bootstrap Documentation 5.3 — https://getbootstrap.com/docs/5.3/
- Chart.js Documentation 4.x — https://www.chartjs.org/docs/
- MapStruct Documentation — https://mapstruct.org/documentation/
- PRESSMAN, Roger S. Engenharia de Software: Uma Abordagem Profissional. 8ª ed. McGraw-Hill, 2016.
- SOMMERVILLE, Ian. Engenharia de Software. 10ª ed. Pearson, 2019.

---

# APÊNDICE A — MANUAL DE EXECUÇÃO DO SISTEMA

## Pré-requisitos
- Java 21 (OpenJDK ou Oracle JDK)
- Maven 3.9+

## Como executar
```bash
# Clonar o repositório
git clone https://github.com/engguilhermebraga/agendamento-api.git
cd agendamento-api/agendamento-api

# Executar com perfil dev (dados de demonstração)
mvn spring-boot:run

# Acessos:
# Dashboard admin:  http://localhost:8081/dashboard
# Portal cliente:   http://localhost:8081/portal
# Swagger UI:       http://localhost:8081/swagger-ui.html
# API auth:         POST http://localhost:8081/api/v1/auth/login
```

## Credenciais de teste
| Usuário | Senha    | Role    | Uso |
|---------|----------|---------|-----|
| admin   | admin123 | ADMIN   | API escrita + leitura |
| user    | user123  | USER    | API somente leitura |

## Dados de demonstração (auto-carregados)
| Dado | Quantidade |
|------|-----------|
| Clientes | 3 (Maria, João, Ana) |
| Profissionais | 3 (Fisioterapeuta, Cabeleireira, Nutricionista) |
| Serviços | 4 (Fisioterapia, Corte, Consulta Nutricional, Massagem) |
| Agendamentos | 4 (1 CONFIRMADO, 3 AGENDADOS) |

---

# APÊNDICE B — DIAGRAMAS UML

> Ver arquivo: docs/tcc-capitulo-desenvolvimento.txt
> Seções 4.3 a 4.5 contêm 6 diagramas PlantUML prontos para renderizar.
> Use https://plantuml.com/plantuml ou plugin IntelliJ "PlantUML Integration".

---

# RESUMO / ABSTRACT (Template)

## Resumo (em português)

**Dados para o Claude gerar (~200 palavras):**
- Contexto: sistemas manuais de agendamento são ineficientes
- Objetivo: desenvolver sistema web de agendamento
- Metodologia: desenvolvimento com Spring Boot 4.0.5, Spring Security JWT,
  Thymeleaf, Bootstrap 5, Spring Data JPA
- Resultados: painel admin completo, portal cliente wizard 4 etapas, API REST autenticada,
  detecção de conflito de horário, dashboard com Chart.js
- Conclusão: sistema atende os requisitos, arquitetura MVC se mostrou adequada

**Palavras-chave:** Agendamento. Spring Boot. REST API. JWT. Thymeleaf. JPA.

## Abstract (em inglês)

Mesmas informações do resumo, traduzidas para inglês formal.

**Keywords:** Scheduling. Spring Boot. REST API. JWT. Thymeleaf. JPA.

---

# CHECKLIST FINAL DO TCC

## Backend
- [x] Spring Boot 4.0.5 rodando na porta 8081
- [x] CRUD: Clientes, Profissionais, Serviços, Agendamentos
- [x] Detecção de conflito de horário
- [x] Fluxo de status com transições válidas
- [x] JWT gerado (AuthController) e validado (JwtAuthenticationFilter)
- [x] RBAC: USER (leitura), ADMIN (escrita)
- [x] DataInitializer com dados de demo (perfil dev)
- [x] Logging SLF4J em todos os services e controllers
- [x] application.properties com spring.profiles.active=dev
- [ ] Testes unitários (JUnit 5 + Mockito) — pendente para nota máxima
- [ ] Testes de integração (MockMvc) — pendente para nota máxima

## Frontend
- [x] Layout base Bootstrap 5 (admin + portal)
- [x] Dashboard: cards métricas + doughnut Chart.js + tabela próximos agendamentos
- [x] Agendamentos: listar (filtros + status coloridos + modal status + modal delete)
- [x] Clientes: listar + formulario (Bean Validation) + detalhe
- [x] Profissionais: listar + formulario
- [x] Serviços: listar + formulario
- [x] Portal wizard: step1 (radio cards) + step2 (time slots JS) + step3 (resumo) + step4 (sucesso)
- [x] Páginas erro: 403, 404, 500
- [x] Formulários com validação HTML5 + th:errors Bean Validation
- [x] Operações destrutivas via POST (não GET)

## Swagger / API
- [x] Swagger UI: http://localhost:8081/swagger-ui.html
- [x] OpenAPI 3.0: http://localhost:8081/api-docs
- [x] Endpoints filtrados: apenas /api/v1/**

## TCC
- [x] Diagramas UML (6): Use Case, Classes, Sequência x2, Componentes, Máquina de estados
- [x] Regras de negócio documentadas (RN01-RN05)
- [x] Decisões técnicas documentadas (10 decisões)
- [x] Briefing de defesa Q&A (30+ perguntas, 8 blocos temáticos)
- [ ] Texto dos capítulos 1-6 (usar este arquivo como roteiro)
- [ ] Screenshots do sistema em execução (capturar com app rodando)
- [ ] Referências ABNT formatadas
- [ ] Revisão ortográfica

---

# DICAS PARA USAR COM O CLAUDE (claude.ai)

## Para cada seção, use este prompt base:

```
Escreva o texto acadêmico para a seção [NOME DA SEÇÃO] do meu TCC sobre
"Sistema de Agendamento de Serviços com Spring Boot".

Contexto do projeto:
[Cole os dados técnicos da seção]

Requisitos de escrita:
- Português acadêmico formal, normas ABNT
- Aproximadamente [X] palavras
- Evite linguagem coloquial e primeira pessoa (exceto em Conclusão)
- Use citações de autores relevantes quando pertinente
- Tom técnico mas acessível

Nível acadêmico: TCC de graduação em [Engenharia de Software / Sistemas de Informação / Ciência da Computação]
```

## Para as referências:

```
Formate as seguintes fontes no padrão ABNT NBR 6023:2018 para lista de referências:
[Cole as referências da lista acima]
```

## Para o resumo:

```
Escreva um resumo acadêmico (200-250 palavras) para um TCC com:
- Contexto: [...]
- Problema: [...]
- Objetivo: [...]
- Metodologia: Spring Boot 4.0.5, Thymeleaf, JWT, Spring Data JPA, Bootstrap 5
- Resultados: [...]
- Conclusão: [...]
Inclua 5-6 palavras-chave no final.
Depois escreva o Abstract em inglês.
```
