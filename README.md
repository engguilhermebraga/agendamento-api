# 📅 Agendamento API

> **Trabalho de Conclusão de Curso (TCC) — UNDB**
>
> Desenvolvimento de uma API REST para Gestão de Agendamentos de Serviços com Aplicação de Arquitetura em Camadas,
> Padrões de Projeto e Documentação OpenAPI

---

## 👨‍🎓 Informações Acadêmicas

| Campo           | Detalhe                                                    |
|-----------------|------------------------------------------------------------|
| **Aluno**       | Guilherme Braga                                            |
| **Curso**       | Engenharia de Software                                     |
| **Instituição** | Centro Universitário UNDB                                  |
| **Ano**         | 2026                                                       |
| **GitHub**      | [@engguilhermebraga](https://github.com/engguilhermebraga) |

---

## 📌 Sobre o Projeto

O **Agendamento API** é um sistema de gestão de agendamentos de serviços desenvolvido como artefato técnico do TCC. O
projeto demonstra a aplicação prática de conceitos como **arquitetura em camadas**, **padrões de projeto REST**, *
*documentação OpenAPI 3.0** e **interface com.guilhermebraga.agendamento_api.controller.web server-side com Thymeleaf**,
integrando
backend e frontend em uma única
aplicação Spring Boot.

O sistema permite o gerenciamento completo do ciclo de vida de um agendamento — desde o cadastro de clientes,
profissionais e serviços, até a criação, confirmação, conclusão ou cancelamento de agendamentos, com validação de
conflito de horário em tempo real.

---

## ⚙️ Stack Tecnológica

| Camada           | Tecnologia                         |
|------------------|------------------------------------|
| **Linguagem**    | Java 25                            |
| **Framework**    | Spring Boot 4.0.5                  |
| **ORM**          | Spring Data JPA + Hibernate        |
| **Banco (dev)**  | H2 (em memória)                    |
| **Banco (prod)** | MySQL                              |
| **Frontend**     | Thymeleaf (server-side rendering)  |
| **Documentação** | SpringDoc OpenAPI 3.0 + Swagger UI |
| **Build**        | Maven                              |
| **Utilitários**  | Lombok, Bean Validation            |

---

## 🏗️ Arquitetura do Projeto

O projeto adota a **Arquitetura em Camadas** (*Layered Architecture*), conforme descrita por Fowler (2002), na qual cada
camada possui uma responsabilidade bem definida e se comunica apenas com a camada imediatamente inferior.

```
src/main/java/com/guilhermebraga/agendamento_api/
│
├── config/                    # Configurações globais (OpenAPI)
├── controller/                # Controllers REST — endpoints da API
├── com.guilhermebraga.agendamento_api.controller.web/
│   └── controller/            # Controllers MVC — páginas Thymeleaf
├── service/                   # Regras de negócio
├── repository/                # Acesso ao banco via Spring Data JPA
├── entity/                    # Entidades JPA mapeadas
├── dto/
│   ├── request/               # DTOs de entrada (validações @Valid)
│   └── response/              # DTOs de saída
├── mapper/                    # Conversão Entity ↔ DTO
└── exception/                 # Tratamento global de erros

src/main/resources/
├── templates/                 # Páginas HTML (Thymeleaf)
│   ├── clientes/
│   ├── profissionais/
│   ├── servicos/
│   ├── agendamentos/
│   └── layout/
├── static/
│   ├── css/
│   └── js/
├── application.properties
└── application-prod.properties
```

### Fluxo de uma requisição REST

```
Cliente HTTP → Controller REST → Service → Repository → Entity → Banco de Dados
                    ↑                ↑
                  OpenAPI       Exception Handler
```

### Fluxo de uma requisição Web (Thymeleaf)

```
Navegador → Controller MVC (com.guilhermebraga.agendamento_api.controller.web/) → Service → Repository → Entity → Banco
                    ↓
              Template HTML (Thymeleaf) → Resposta HTML renderizada
```

> **Importante:** os Controllers REST (`controller/`) e os Controllers MVC (
`com.guilhermebraga.agendamento_api.controller.web/controller/`) são **completamente
separados**. Os REST respondem JSON; os MVC respondem páginas HTML. Eles compartilham a mesma camada de Service.

---

## 🧩 Entidades do Sistema

| Entidade       | Descrição                              | Campos principais                                |
|----------------|----------------------------------------|--------------------------------------------------|
| `Cliente`      | Usuário que realiza o agendamento      | nome, email, telefone, cpf                       |
| `Profissional` | Prestador do serviço                   | nome, especialidade, email, telefone             |
| `Servico`      | Tipo de serviço oferecido              | nome, descrição, duração (min), preço            |
| `Agendamento`  | Evento que relaciona as três entidades | cliente, profissional, serviço, dataHora, status |

### Ciclo de vida do Agendamento (`StatusAgendamento`)

```
AGENDADO → CONFIRMADO → CONCLUIDO
    ↓            ↓
 CANCELADO   CANCELADO
```

---

## 🔌 Endpoints da API REST

Base URL: `http://localhost:8081/api/v1`

### Clientes — `/clientes`

| Método   | Rota             | Descrição               | Status                |
|----------|------------------|-------------------------|-----------------------|
| `POST`   | `/clientes`      | Cadastra novo cliente   | 201 / 400 / 409       |
| `GET`    | `/clientes`      | Lista todos os clientes | 200                   |
| `GET`    | `/clientes/{id}` | Busca cliente por ID    | 200 / 404             |
| `PUT`    | `/clientes/{id}` | Atualiza cliente        | 200 / 400 / 404 / 409 |
| `DELETE` | `/clientes/{id}` | Remove cliente          | 204 / 404             |

### Profissionais — `/profissionais`

| Método   | Rota                  | Descrição                    | Status                |
|----------|-----------------------|------------------------------|-----------------------|
| `POST`   | `/profissionais`      | Cadastra novo profissional   | 201 / 400 / 409       |
| `GET`    | `/profissionais`      | Lista todos os profissionais | 200                   |
| `GET`    | `/profissionais/{id}` | Busca profissional por ID    | 200 / 404             |
| `PUT`    | `/profissionais/{id}` | Atualiza profissional        | 200 / 400 / 404 / 409 |
| `DELETE` | `/profissionais/{id}` | Remove profissional          | 204 / 404             |

### Serviços — `/servicos`

| Método   | Rota             | Descrição               | Status                |
|----------|------------------|-------------------------|-----------------------|
| `POST`   | `/servicos`      | Cadastra novo serviço   | 201 / 400 / 409       |
| `GET`    | `/servicos`      | Lista todos os serviços | 200                   |
| `GET`    | `/servicos/{id}` | Busca serviço por ID    | 200 / 404             |
| `PUT`    | `/servicos/{id}` | Atualiza serviço        | 200 / 400 / 404 / 409 |
| `DELETE` | `/servicos/{id}` | Remove serviço          | 204 / 404             |

### Agendamentos — `/agendamentos`

| Método   | Rota                        | Descrição                   | Status                |
|----------|-----------------------------|-----------------------------|-----------------------|
| `POST`   | `/agendamentos`             | Cria novo agendamento       | 201 / 400 / 404 / 409 |
| `GET`    | `/agendamentos`             | Lista todos os agendamentos | 200                   |
| `GET`    | `/agendamentos/{id}`        | Busca agendamento por ID    | 200 / 404             |
| `PUT`    | `/agendamentos/{id}`        | Atualiza agendamento        | 200 / 400 / 404 / 409 |
| `PATCH`  | `/agendamentos/{id}/status` | Atualiza status             | 200 / 404 / 409       |
| `DELETE` | `/agendamentos/{id}`        | Cancela agendamento         | 204 / 404 / 409       |

---

## 🌐 Páginas Web (Thymeleaf)

Base URL: `http://localhost:8081`

| Rota                                                                      | Descrição                              |
|---------------------------------------------------------------------------|----------------------------------------|
| `/`                                                                       | Dashboard principal                    |
| `/com.guilhermebraga.agendamento_api.controller.web/clientes`             | Listagem de clientes                   |
| `/com.guilhermebraga.agendamento_api.controller.web/clientes/novo`        | Formulário de cadastro de cliente      |
| `/com.guilhermebraga.agendamento_api.controller.web/clientes/{id}/editar` | Formulário de edição de cliente        |
| `/com.guilhermebraga.agendamento_api.controller.web/profissionais`        | Listagem de profissionais              |
| `/com.guilhermebraga.agendamento_api.controller.web/profissionais/novo`   | Formulário de cadastro de profissional |
| `/com.guilhermebraga.agendamento_api.controller.web/servicos`             | Listagem de serviços                   |
| `/com.guilhermebraga.agendamento_api.controller.web/servicos/novo`        | Formulário de cadastro de serviço      |
| `/com.guilhermebraga.agendamento_api.controller.web/agendamentos`         | Listagem de agendamentos               |
| `/com.guilhermebraga.agendamento_api.controller.web/agendamentos/novo`    | Formulário de novo agendamento         |

---

## 🚀 Como Executar

### Pré-requisitos

- Java 25+
- Maven 3.9+

### Execução (desenvolvimento — H2)

```bash
# Clone o repositório
git clone https://github.com/engguilhermebraga/agendamento-api.git
cd agendamento-api/agendamento-api

# Execute com Maven
mvn spring-boot:run
```

### Acessos após subir a aplicação

| Recurso           | URL                                   |
|-------------------|---------------------------------------|
| **Interface Web** | http://localhost:8081                 |
| **Swagger UI**    | http://localhost:8081/swagger-ui.html |
| **OpenAPI JSON**  | http://localhost:8081/api-docs        |
| **Console H2**    | http://localhost:8081/h2-console      |

> **Console H2:** JDBC URL → `jdbc:h2:mem:agendamentodb` · Usuário: `sa` · Senha: *(vazio)*

---

## 🔒 Regras de Negócio

1. **CPF único** — não é permitido cadastrar dois clientes com o mesmo CPF
2. **E-mail único** — não é permitido e-mail duplicado para clientes ou profissionais
3. **Nome único de serviço** — cada serviço deve ter um nome distinto
4. **Data futura** — agendamentos só podem ser criados para datas futuras
5. **Conflito de horário** — um profissional não pode ter dois agendamentos ativos com horários sobrepostos; a
   sobreposição é calculada com base na duração do serviço (`dataHora` a `dataHoraFim`)
6. **Imutabilidade pós-conclusão** — agendamentos com status `CONCLUIDO` ou `CANCELADO` não podem ser alterados
7. **Fluxo de status** — as transições seguem a máquina de estados: `AGENDADO → CONFIRMADO → CONCLUIDO` ou
   `AGENDADO/CONFIRMADO → CANCELADO`

---

## 📦 Estrutura do `pom.xml`

| Dependência                                                             | Finalidade                                 |
|-------------------------------------------------------------------------|--------------------------------------------|
| `spring-boot-starter-com.guilhermebraga.agendamento_api.controller.web` | Servidor Tomcat embutido + REST            |
| `spring-boot-starter-data-jpa`                                          | ORM com Hibernate                          |
| `spring-boot-starter-validation`                                        | Bean Validation (`@Valid`, `@NotBlank`...) |
| `spring-boot-starter-thymeleaf`                                         | Motor de templates HTML server-side        |
| `springdoc-openapi-starter-webmvc-ui:2.8.6`                             | Swagger UI + OpenAPI 3.0                   |
| `spring-boot-devtools`                                                  | Hot reload em desenvolvimento              |
| `h2`                                                                    | Banco em memória (desenvolvimento)         |
| `mysql-connector-j`                                                     | Driver JDBC MySQL (produção)               |
| `lombok`                                                                | Geração de código boilerplate              |
| `spring-boot-starter-test`                                              | JUnit 5 + Mockito + MockMvc                |

---

## 📚 Referências Bibliográficas

- FIELDING, Roy Thomas. **Architectural styles and the design of network-based software architectures**. 2000. Tese (
  Doutorado) — University of California, Irvine, 2000.
- FOWLER, Martin. **Patterns of enterprise application architecture**. Boston: Addison-Wesley, 2002.
- MARTIN, Robert C. **Arquitetura limpa: o guia do artesão para estrutura e design de software**. Rio de Janeiro: Alta
  Books, 2019.
- SOMMERVILLE, Ian. **Engenharia de software**. 9. ed. São Paulo: Pearson Prentice Hall, 2011.
- RICHARDSON, Leonard; RUBY, Sam. **RESTful Web Services**. Sebastopol: O'Reilly Media, 2007.
- SMARTBEAR SOFTWARE. **OpenAPI Specification**. Version 3.0.3. 2020. Disponível
  em: https://spec.openapis.org/oas/v3.0.3.
- VMWARE. **Spring Boot Reference Documentation**. 2024. Disponível
  em: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/.

---

*Projeto desenvolvido para fins acadêmicos — TCC UNDB 2026.*
