# Compilado TCC — Sistema de Agendamento
## UNDB — Guilherme Braga

> **Instruções de uso:** Cole este documento inteiro no Claude Projects.
> Ele contém o histórico fiel do desenvolvimento para embasar a escrita dos
> capítulos do TCC. Escreva na primeira pessoa, como desenvolvedor humano.

---

## 1. CONTEXTO DO PROJETO

**Nome:** Sistema de Agendamento  
**Stack:** Spring Boot 4.0.5 · Spring MVC · Spring Security · Spring Data JPA · PostgreSQL · Thymeleaf · Bootstrap 5.3.3 · Bootstrap Icons 1.11.3  
**Propósito:** Plataforma web de agendamentos com painel administrativo (gestão interna) e portal do cliente (autoatendimento público), desenvolvida como TCC da UNDB.  
**Repositório:** github.com/engguilhermebraga/agendamento-api  
**Branch principal:** `main` com fluxo GitHub Flow (feature branches → Pull Requests → merge)

---

## 2. ARQUITETURA DO SISTEMA

### Camadas
```
Navegador
  └─ Thymeleaf (server-side rendering)
       └─ Spring MVC Controllers
            └─ Services (regras de negócio)
                 └─ Repositories (Spring Data JPA)
                      └─ PostgreSQL
```

### Módulos principais

| Módulo | Pacote | Responsabilidade |
|---|---|---|
| Painel admin | `controller/web/controller` | CRUD de clientes, profissionais, serviços, agendamentos |
| Portal cliente | `controller/portal` | Identificação, wizard de agendamento, histórico |
| Dashboard | `DashboardWebController` | Métricas em tempo real via agregações em memória |
| API REST | `controller/api` | Endpoints JSON para integrações |
| Segurança | `SecurityConfig` | Spring Security — rotas protegidas vs. públicas |

### Entidades JPA
- `Cliente` — dados pessoais + senha BCrypt + autenticação portal
- `Profissional` — nome, especialidade, contato
- `Servico` — nome, descrição, duração (minutos), preço
- `Agendamento` — FK para as três entidades + `dataHora` + `StatusAgendamento` (enum)

### Estados de Agendamento
```
AGENDADO → CONFIRMADO → CONCLUIDO
    └──────────────────→ CANCELADO
```

---

## 3. ETAPAS DE DESENVOLVIMENTO

### Etapa 1 — Correção de compilação
**Problema:** `PasswordEncoder` utilizado em `ClienteService` sem import correspondente, causando falha de compilação (`cannot find symbol`).  
**Solução:** Adicionado `import org.springframework.security.crypto.password.PasswordEncoder;`.  
**Impacto:** Zero alteração em regra de negócio; corrigiu o build imediatamente.

### Etapa 2 — Qualidade estática com Qodana
O projeto foi integrado ao **Qodana** (ferramenta de análise estática da JetBrains) via GitHub Actions. Dois grupos de avisos foram identificados e corrigidos:

**Aviso 1 — Bulk operation (`GlobalExceptionHandler.java`)**  
Iteração manual sobre `getFieldErrors()` para popular um `Map` foi substituída por:
```java
Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
    .collect(Collectors.toMap(
        FieldError::getField,
        FieldError::getDefaultMessage));
```

**Aviso 2 — Bulk operation + Nullability (`DashboardWebController.java`)**  
- Laço `for` para inicializar `porStatus` substituído por:
```java
Map<String, Long> porStatus = Arrays.stream(StatusAgendamento.values())
    .collect(Collectors.toMap(
        StatusAgendamento::name, s -> 0L, (a, b) -> a, LinkedHashMap::new));
```
- Comparador lambda `(a, b) -> a.getDataHora().compareTo(b.getDataHora())` substituído por:
```java
.sorted(Comparator.comparing(AgendamentoResponse::getDataHora))
```
A referência de método é nula-segura em tempo de compilação; o lambda não era.

**Resultado Qodana:** 0 avisos ativos após as correções.

### Etapa 3 — Melhorias de interface (painel administrativo)

**Layout base (`layout/base.html`)**
- Navbar Bootstrap responsiva com `navbar-expand-lg`
- 5 itens de menu com ícones Bootstrap Icons semânticos (Dashboard, Clientes, Profissionais, Serviços, Agendamentos)
- Colapso em hamburger menu abaixo de 992px
- Destaque dinâmico do item ativo via `window.location.pathname`
- Alertas dismissíveis de sucesso/erro com botão de fechar

**Login (`web/login.html`)**
- Fundo escuro com gradiente, card centralizado com sombra profunda
- Botão de alternância de visibilidade de senha (password toggle) com Bootstrap Icons

**Tabelas — Profissionais e Serviços**
- Reescritas com `table-hover`, `align-middle`, cabeçalhos `table-light`
- Botões de ação ícone-apenas (outline, tamanho sm)
- Badges de especialidade e duração com cores temáticas
- Modal de confirmação Bootstrap para exclusão (substitui `window.confirm`)

**Listagem de Agendamentos (`agendamentos/listar.html`)**
- Template anterior tinha HTML malformado: `<table>` sem classes Bootstrap, `</div>` órfãos, referências a modais inexistentes
- Reescrita completa com HTML válido, mantendo toda a lógica de filtros (status, intervalo de datas), transição de status via modal e exclusão

### Etapa 4 — Melhorias do portal do cliente

**Home do portal (`portal/home.html`)**
- Hero card com gradiente violeta-roxo (`linear-gradient(135deg, #667eea, #764ba2)`)
- 3 stat cards: Serviços, Profissionais, Meus Agendamentos

**Layout do portal (`portal/layout.html`)**
- Removido `<th:block th:replace="${content}"/>` com variável errada (Thymeleaf ignorava)
- Removido bloco duplicado de mensagem de erro fora do container principal

**Wizard de agendamento — Etapa 1 (`portal/wizard/step1.html`)**
- Dois blocos `<script>` registravam os mesmos event listeners — execução duplicada
- Bloco redundante removido; mantida a implementação com referência O(1) ao card ativo

### Etapa 5 — Dashboard com métricas e visualização de status

**`DashboardWebController`** calcula em memória (sem queries extras):
- `totalAgendamentos`, `agendamentosHoje`, `proximos7Dias`, `clientesAtivos`
- `totalAgendado`, `totalConfirmado`, `totalConcluido`, `totalCancelado` via `Collectors.groupingBy`
- `proximosAgendamentos` — 3 próximos com status AGENDADO ou CONFIRMADO, ordem cronológica

**Dashboard (`dashboard/index.html`)** — 3 linhas:
1. 4 metric-cards com gradiente (Total, Hoje, Próximos 7 dias, Clientes ativos)
2. Card de distribuição por status + tabela de próximos agendamentos
3. Cards de acesso rápido (Clientes, Profissionais, Serviços)

**Visualização de distribuição por status — decisão final:**  
Foram tentadas três abordagens de visualização:
- Chart.js via CDN → falhou por CDN inacessível no ambiente de desenvolvimento
- `conic-gradient` CSS com Thymeleaf → falhou por cache de templates na IDE
- **Solução adotada:** 4 mini-cards Bootstrap em grid 2×2 + barra de progresso empilhada (stacked), 100% Thymeleaf server-side, zero JavaScript, zero dependência externa

---

## 4. TESTES REALIZADOS

### Testes automatizados
- `mvn test` executado após cada grupo de alterações — **0 falhas**
- `mvn compile` executado após cada arquivo alterado — **0 erros**
- Qodana CI/CD — **0 avisos** após correções das etapas 1 e 2

### Testes manuais de interface
Foram realizados testes manuais nas seguintes telas (resolução 1280px e 375px):

| Tela | Fluxo testado |
|---|---|
| `/web/login` | Credenciais corretas/erradas, toggle de senha, redirecionamento |
| `/dashboard` | Métricas com dados reais, cards de status, próximos agendamentos |
| `/web/clientes` | Listagem, busca em tempo real, modal de exclusão |
| `/web/clientes/novo` | Cadastro, validação HTML5, redirecionamento com sucesso |
| `/web/clientes/{id}/editar` | Edição, pré-preenchimento de campos |
| `/web/profissionais` | Listagem, badges de especialidade, modal de remoção |
| `/web/servicos` | Listagem, preço formatado, duração em minutos |
| `/web/agendamentos/listar` | Filtro por status e data, modal de status, modal de exclusão |
| `/web/agendamentos/novo` | Selects populados, info dinâmica do serviço |
| `/portal` | Identificação/login, hero card, stat cards |
| `/portal/step1` até `/portal/step4` | Fluxo completo do wizard |

### Fluxos de regressão
- Admin: login → dashboard → criar cliente → criar profissional → criar serviço → criar agendamento → alterar status → excluir
- Portal: home → wizard step1 → step2 → step3 → confirmar → step4 → meus agendamentos

---

## 5. CONTROLE DE VERSÃO E PULL REQUESTS

| PR | Descrição | Status |
|---|---|---|
| #19 | Correção PasswordEncoder + Qodana bulk operation | Merged |
| #20 | Melhorias UI/UX completas + Qodana nullability | Merged |
| #21–#23 | Tentativas de gráfico Chart.js / CSS (descontinuadas) | Merged |
| #24 | Cards de status com barra empilhada (solução final) | Em revisão |

---

## 6. DECISÕES TÉCNICAS RELEVANTES

### Server-Side Rendering vs. SPA
Optei por Thymeleaf (SSR) em vez de um framework SPA (React, Angular) porque o domínio do sistema — agendamentos com fluxos lineares — não exige reatividade complexa. O SSR simplifica a implantação, elimina uma API REST separada para as telas internas e facilita a validação de formulários com Spring MVC `BindingResult`.

### Autenticação dupla
O Spring Security gerencia a autenticação administrativa via formulário com sessão no servidor. O portal do cliente usa `HttpSession` manual com verificação de CPF/senha BCrypt — uma camada mais leve adequada ao público não-administrativo.

### Separação painel × portal
Dois layouts Thymeleaf independentes (`layout/base.html` para o painel, `portal/layout.html` para o portal) garantem que alterações visuais em um ambiente não afetem o outro, e que as restrições de segurança fiquem explicitamente separadas no `SecurityConfig`.

### Cálculos de dashboard em memória
As métricas do dashboard são calculadas sobre a lista completa de agendamentos carregada uma única vez (`agendamentoService.listarTodos()`). Para o volume esperado do sistema (centenas a poucos milhares de registros), essa abordagem é eficiente. Para escala maior, seria preferível queries agregadas no banco.

---

## 7. LIMITAÇÕES RECONHECIDAS

- **Testes de interface automatizados ausentes** — a cobertura de UI foi manual; Selenium/Playwright aumentaria a rastreabilidade
- **Sem testes de integração nos controllers** — MockMvc não foi utilizado; apenas testes de unidade nos serviços
- **CDN externo no layout** — Bootstrap CSS/JS e Bootstrap Icons são carregados via CDN; em ambientes sem internet, o visual quebra (exceto a lógica do sistema)
- **Métricas do dashboard em memória** — eficiente para poucos registros, mas sem paginação ou query agregada para escala
- **Internacionalização ausente** — sistema fixo em português; `MessageSource` do Spring não foi implementado
- **Acessibilidade não auditada formalmente** — contraste e semântica HTML foram considerados, mas sem auditoria Axe/Lighthouse
- **Sem notificações** — não há envio de e-mail ou SMS para lembrete de agendamentos

---

## 8. TRABALHOS FUTUROS

- Notificações por e-mail (Spring Mail) e WhatsApp (API) para lembrete de agendamentos
- API REST com autenticação JWT para integração com aplicativo móvel
- Relatórios exportáveis em PDF/CSV por período, profissional ou serviço
- Testes de integração com MockMvc e testes de UI com Playwright
- Suporte multi-tenant (múltiplos estabelecimentos na mesma instalação)
- Deploy em nuvem com CI/CD completo (build + test + deploy automatizados)
- Auditoria de acessibilidade (WCAG 2.1 AA) com Axe

---

## 9. TECNOLOGIAS — RESUMO PARA FUNDAMENTAÇÃO TEÓRICA

| Tecnologia | Versão | Uso no projeto |
|---|---|---|
| Spring Boot | 4.0.5 | Framework base, auto-configuração, servidor embutido |
| Spring MVC | (incluso) | Padrão MVC, roteamento HTTP, binding de formulários |
| Spring Security | (incluso) | Autenticação admin, proteção de rotas, CSRF |
| Spring Data JPA | (incluso) | Repositórios, mapeamento objeto-relacional |
| Hibernate | (incluso) | Implementação JPA, geração de DDL |
| PostgreSQL | — | Banco de dados relacional de produção |
| Thymeleaf | 3.x | Motor de templates SSR, fragmentos, layouts |
| Bootstrap | 5.3.3 | Grid responsivo, componentes UI, utilitários CSS |
| Bootstrap Icons | 1.11.3 | Ícones SVG via font, usados em menus e botões |
| Maven | 3.x | Gerenciamento de dependências, build, testes |
| Qodana | — | Análise estática de código, integrado ao GitHub Actions |
| GitHub Actions | — | CI/CD: build, test, Qodana em cada PR |

---

## 10. PALETA DE CORES E IDENTIDADE VISUAL

| Elemento | Cor | Hex |
|---|---|---|
| Status AGENDADO | Azul-violeta | `#667eea` |
| Status CONFIRMADO | Verde | `#28a745` |
| Status CONCLUÍDO | Cinza | `#6c757d` |
| Status CANCELADO | Vermelho | `#dc3545` |
| Gradiente navbar/hero | Violeta → roxo | `#667eea → #764ba2` |
| Metric card Total | Roxo | `linear-gradient(135deg, #667eea, #764ba2)` |
| Metric card Hoje | Laranja | `linear-gradient(135deg, #f093fb, #f5576c)` |
| Metric card 7 dias | Verde | `linear-gradient(135deg, #4facfe, #00f2fe)` |
| Metric card Clientes | Azul | `linear-gradient(135deg, #43e97b, #38f9d7)` |

---

## 11. ESTRUTURA DE ARQUIVOS RELEVANTES

```
src/main/
├── java/.../
│   ├── config/SecurityConfig.java
│   ├── controller/
│   │   ├── web/controller/
│   │   │   ├── DashboardWebController.java    ← métricas dashboard
│   │   │   ├── ClienteWebController.java
│   │   │   ├── ProfissionalWebController.java
│   │   │   ├── ServicoWebController.java
│   │   │   └── AgendamentoWebController.java
│   │   ├── portal/
│   │   │   ├── PortalHomeController.java       ← login, cadastro, home
│   │   │   ├── PortalAgendamentoController.java ← wizard step1-4
│   │   │   └── PortalMeusAgendamentosController.java
│   │   └── api/                               ← endpoints REST
│   ├── service/
│   │   ├── AgendamentoService.java
│   │   ├── ClienteService.java                ← BCrypt, autenticação portal
│   │   ├── ProfissionalService.java
│   │   └── ServicoService.java
│   ├── entity/
│   │   ├── Agendamento.java
│   │   ├── Cliente.java
│   │   ├── Profissional.java
│   │   ├── Servico.java
│   │   └── StatusAgendamento.java             ← enum AGENDADO/CONFIRMADO/CONCLUIDO/CANCELADO
│   ├── dto/
│   │   └── response/AgendamentoResponse.java
│   └── exception/
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java        ← Qodana fix: stream collect
└── resources/
    ├── templates/
    │   ├── layout/base.html                   ← navbar admin + layout base
    │   ├── dashboard/index.html               ← métricas + cards de status
    │   ├── clientes/ profissionais/ servicos/ agendamentos/
    │   ├── portal/
    │   │   ├── layout.html
    │   │   ├── home.html
    │   │   ├── wizard/step1.html … step4.html
    │   │   └── meusAgendamentos.html
    │   └── web/login.html
    ├── static/css/style.css                   ← variáveis CSS, metric-card, badge-status
    └── application.properties
```

---

## 12. TRECHOS DE CÓDIGO PARA O TCC

### DashboardWebController — cálculo das métricas
```java
List<AgendamentoResponse> todos = agendamentoService.listarTodos();

// Agendamentos de hoje
long agendamentosHoje = todos.stream()
    .filter(a -> !a.getDataHora().isBefore(inicioHoje)
              &&  a.getDataHora().isBefore(fimHoje))
    .count();

// Distribuição por status com LinkedHashMap ordenado
Map<String, Long> porStatus = Arrays.stream(StatusAgendamento.values())
    .collect(Collectors.toMap(
        StatusAgendamento::name, s -> 0L, (a, b) -> a, LinkedHashMap::new));
todos.stream()
    .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()))
    .forEach(porStatus::put);

// 3 próximos agendamentos futuros, ordem cronológica
List<AgendamentoResponse> proximos = todos.stream()
    .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO
              || a.getStatus() == StatusAgendamento.CONFIRMADO)
    .filter(a -> !a.getDataHora().isBefore(inicioHoje))
    .sorted(Comparator.comparing(AgendamentoResponse::getDataHora))
    .limit(3)
    .toList();
```

### Dashboard — cards de status com barra empilhada (Thymeleaf)
```html
<!-- Mini-card AGENDADO -->
<div class="p-3 rounded-3" style="background:#eef0ff; border-left:4px solid #667eea;">
    <div class="fw-bold fs-3 lh-1" style="color:#667eea;"
         th:text="${totalAgendado}">0</div>
    <div class="small fw-semibold mt-1" style="color:#667eea;">AGENDADO</div>
    <div class="text-muted" style="font-size:.7rem;"
         th:text="${totalAgendamentos > 0}
                  ? (${totalAgendado * 100 / totalAgendamentos} + '%') : '-'">-</div>
</div>

<!-- Barra empilhada -->
<div class="progress" style="height:12px; border-radius:6px;">
    <div class="progress-bar" style="background:#667eea;"
         th:style="'background:#667eea; width:'
                   + (${totalAgendamentos > 0 ? totalAgendado * 100 / totalAgendamentos : 0})
                   + '%'"></div>
    <div class="progress-bar bg-success"
         th:style="'width:'
                   + (${totalAgendamentos > 0 ? totalConfirmado * 100 / totalAgendamentos : 0})
                   + '%'"></div>
    <!-- ... CONCLUIDO (bg-secondary) e CANCELADO (bg-danger) -->
</div>
```

### GlobalExceptionHandler — correção Qodana
```java
// Antes (aviso "bulk operation"):
Map<String, String> erros = new HashMap<>();
for (FieldError error : ex.getBindingResult().getFieldErrors()) {
    erros.put(error.getField(), error.getDefaultMessage());
}

// Depois:
Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
    .collect(Collectors.toMap(
        FieldError::getField,
        FieldError::getDefaultMessage));
```

---

## 13. FRASES-CHAVE PARA ESCRITA DO TCC

- "O desenvolvimento seguiu o padrão GitHub Flow, com cada conjunto de alterações em uma branch de funcionalidade submetida como Pull Request, revisada pelo Qodana e mesclada após aprovação do CI."
- "A análise estática com Qodana identificou dois grupos de avisos — operações de coleção ineficientes e risco de nullability — que foram corrigidos antes da integração ao branch principal."
- "A camada de apresentação foi construída inteiramente com Thymeleaf e Bootstrap 5, sem frameworks SPA, aproveitando o mecanismo de fragmentos para garantir consistência visual entre as mais de quinze telas do sistema."
- "O painel administrativo e o portal do cliente compartilham a mesma base de dados e camada de serviço, mas possuem layouts, rotas e mecanismos de autenticação independentes, isolados no SecurityConfig."
- "A visualização de distribuição de agendamentos por status foi implementada com componentes nativos do Bootstrap — grid de cards e barra de progresso empilhada — calculados pelo Thymeleaf no servidor, eliminando qualquer dependência de JavaScript ou CDN externo."
- "As métricas do dashboard são calculadas em memória sobre a lista completa de agendamentos, uma abordagem adequada ao volume esperado do sistema e que evita a complexidade de queries agregadas adicionais."
