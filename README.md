# 📅 Agendamento API

> API REST para Gestão de Agendamentos de Serviços desenvolvida como Trabalho de Conclusão de Curso (TCC) do Curso de Engenharia de Software do Centro Universitário UNDB.

---

## 📋 Sobre o Projeto

O **Agendamento API** é um sistema backend desenvolvido em Java com Spring Boot que permite o gerenciamento completo de agendamentos de serviços. O sistema possibilita o cadastro de clientes, profissionais e serviços, além do controle de agendamentos com validações de regras de negócio.

O projeto foi desenvolvido com foco na aplicação de boas práticas de engenharia de software, como arquitetura em camadas, padrões de projeto e documentação automatizada via OpenAPI 3.0.

---

## 🚀 Tecnologias Utilizadas

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 25 LTS | Linguagem principal |
| Spring Boot | 3.5.9 | Framework base |
| Spring Data JPA | — | Acesso ao banco de dados |
| Spring Validation | — | Validação dos dados de entrada |
| Hibernate | 7.x | ORM / Mapeamento objeto-relacional |
| H2 Database | — | Banco de dados em memória (desenvolvimento) |
| MySQL | 8.x | Banco de dados relacional (produção) |
| Lombok | — | Redução de código boilerplate |
| SpringDoc OpenAPI | 2.8.8 | Documentação automática Swagger UI |
| Maven | — | Gerenciamento de dependências |

---

## 🏗️ Arquitetura do Projeto

O projeto segue a **Arquitetura em Camadas** (*Layered Architecture*), com separação clara de responsabilidades entre os pacotes:

```
src/main/java/com/guilhermebraga/agendamento_api/
│
├── controller/       → Endpoints REST com anotações OpenAPI
├── service/          → Regras de negócio e validações
├── repository/       → Acesso ao banco via Spring Data JPA
├── entity/           → Mapeamento JPA das tabelas
├── dto/
│   ├── request/      → Dados de entrada da API
│   └── response/     → Dados de saída da API
├── mapper/           → Conversão Entity ↔ DTO
├── exception/        → Tratamento global de erros
└── config/           → Configurações (OpenAPI, etc.)
```

---

## 📦 Entidades do Sistema

```
Cliente
├── id, nome, email, telefone, cpf
├── criadoEm, atualizadoEm
└── relacionamento: 1 Cliente → N Agendamentos

Profissional
├── id, nome, especialidade, email, telefone
├── criadoEm, atualizadoEm
└── relacionamento: 1 Profissional → N Agendamentos

Serviço
├── id, nome, descrição, duração (min), preço
├── criadoEm, atualizadoEm
└── relacionamento: 1 Serviço → N Agendamentos

Agendamento
├── id, dataHora, status
├── cliente (FK), profissional (FK), serviço (FK)
└── criadoEm, atualizadoEm
```

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos

- Java 25+
- Maven 3.8+

### Clonando o repositório

```bash
git clone https://github.com/engguilhermebraga/agendamento-api.git
cd agendamento-api
```

### Executando em modo desenvolvimento (H2)

```bash
mvn spring-boot:run
```

O banco H2 em memória é iniciado automaticamente. Os dados são perdidos ao reiniciar a aplicação.

### Executando em modo produção (MySQL)

Configure as variáveis de ambiente antes de executar:

```bash
export DB_URL=jdbc:mysql://localhost:3306/agendamentodb
export DB_USERNAME=seu_usuario
export DB_PASSWORD=sua_senha

mvn spring-boot:run -Dspring.profiles.active=prod
```

---

## 📖 Documentação da API

Com a aplicação em execução, acesse:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| JSON OpenAPI | http://localhost:8081/api-docs |
| Console H2 | http://localhost:8081/h2-console |

### Credenciais do Console H2 (desenvolvimento)

```
JDBC URL: jdbc:h2:mem:agendamentodb
User:     sa
Password: (deixar em branco)
```

---

## 🔗 Endpoints Disponíveis

### Clientes — `/api/v1/clientes`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| POST | `/api/v1/clientes` | Cadastrar novo cliente | 201 |
| GET | `/api/v1/clientes` | Listar todos os clientes | 200 |
| GET | `/api/v1/clientes/{id}` | Buscar cliente por ID | 200 |
| PUT | `/api/v1/clientes/{id}` | Atualizar cliente | 200 |
| DELETE | `/api/v1/clientes/{id}` | Remover cliente | 204 |

### Profissionais — `/api/v1/profissionais`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| POST | `/api/v1/profissionais` | Cadastrar novo profissional | 201 |
| GET | `/api/v1/profissionais` | Listar todos os profissionais | 200 |
| GET | `/api/v1/profissionais/{id}` | Buscar profissional por ID | 200 |
| PUT | `/api/v1/profissionais/{id}` | Atualizar profissional | 200 |
| DELETE | `/api/v1/profissionais/{id}` | Remover profissional | 204 |

### Serviços — `/api/v1/servicos`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| POST | `/api/v1/servicos` | Cadastrar novo serviço | 201 |
| GET | `/api/v1/servicos` | Listar todos os serviços | 200 |
| GET | `/api/v1/servicos/{id}` | Buscar serviço por ID | 200 |
| PUT | `/api/v1/servicos/{id}` | Atualizar serviço | 200 |
| DELETE | `/api/v1/servicos/{id}` | Remover serviço | 204 |

### Agendamentos — `/api/v1/agendamentos`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| POST | `/api/v1/agendamentos` | Criar novo agendamento | 201 |
| GET | `/api/v1/agendamentos` | Listar todos os agendamentos | 200 |
| GET | `/api/v1/agendamentos/{id}` | Buscar agendamento por ID | 200 |
| PUT | `/api/v1/agendamentos/{id}` | Atualizar agendamento | 200 |
| PATCH | `/api/v1/agendamentos/{id}/status` | Atualizar status do agendamento | 200 |
| DELETE | `/api/v1/agendamentos/{id}` | Cancelar agendamento | 204 |

---

## 🛡️ Tratamento de Erros

A API retorna respostas de erro padronizadas em todos os endpoints:

```json
{
  "timestamp": "2026-03-19T16:00:00",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Cliente com ID 1 não encontrado(a)."
}
```

| Status | Descrição |
|---|---|
| 400 | Dados inválidos na requisição |
| 404 | Recurso não encontrado |
| 409 | Conflito de regra de negócio (CPF/e-mail duplicado, horário ocupado) |
| 500 | Erro interno no servidor |

---

## 📁 Estrutura de Arquivos

```
agendamento-api/
│
├── src/
│   ├── main/
│   │   ├── java/com/guilhermebraga/agendamento_api/
│   │   │   ├── AgendamentoApiApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
├── pom.xml
└── README.md
```

---

## 📚 Contexto Acadêmico

Este projeto foi desenvolvido como **Trabalho de Conclusão de Curso (TCC)** do Curso de Administração do **Centro Universitário Unidade de Ensino Superior Dom Bosco (UNDB)**, São Luís — MA.

**Tema:** Desenvolvimento de uma API REST para Gestão de Agendamentos de Serviços com Aplicação de Arquitetura em Camadas, Padrões de Projeto e Documentação OpenAPI.

**Referências bibliográficas principais:**
- FIELDING, Roy Thomas. *Architectural styles and the design of network-based software architectures*. 2000.
- FOWLER, Martin. *Patterns of enterprise application architecture*. Boston: Addison-Wesley, 2002.
- WALLS, Craig. *Spring Boot in action*. 2. ed. Shelter Island: Manning Publications, 2019.
- WESTERMAN, George; BONNET, Didier; MCAFEE, Andrew.** _Liderando na Era Digital_.
- RICHARDSON, Leonard; RUBY, Sam.** _RESTful Web Services_.
- SOUZA, A. R. et al.** "A importância da automação de processos para a gestão organizacional". _Revista de Gestão e Tecnologia_
- MARTIN, Robert C. (Uncle Bob).** _Arquitetura Limpa: O guia do artesão para estrutura e design de software_.
- GAMMA, Erich et al. **Padrões de projeto: soluções reutilizáveis de software orientado a objetos**. Porto Alegre: Bookman, 2000.
- MARTIN, Robert C. **Código limpo: habilidades práticas do Agile Software**. Rio de Janeiro: Alta Books, 2009.
- SOMMERVILLE, Ian. **Engenharia de software**. 9. ed. São Paulo: Pearson Prentice Hall, 2011
- LAUDON, Kenneth C.; LAUDON, Jane P. **Sistemas de informação gerenciais**. 11. ed. São Paulo: Pearson Prentice Hall, 2014.
- TURBAN, Efraim; VOLONINO, Linda. **Tecnologia da informação para gestão: transformando os negócios na economia digital**. 8. ed. Porto Alegre: Bookman, 2013.
- BOAGLIO, Fernando. **Spring Boot: acelere o desenvolvimento de microsserviços**. São Paulo: Casa do Código, 2017. (ou edição mais recente).
- SMARTBEAR SOFTWARE. **OpenAPI Specification**. Version 3.0.3. 2020. Disponível em: [https://spec.openapis.org/oas/v3.0.3](https://spec.openapis.org/oas/v3.0.3). Acesso em: 24 mar. 2026.
- 

---

## 👨‍💻 Autor

**Guilherme Braga**

[![GitHub](https://img.shields.io/badge/GitHub-engguilhermebraga-181717?style=flat&logo=github)](https://github.com/engguilhermebraga)

---

## 📄 Licença

Este projeto está sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.
