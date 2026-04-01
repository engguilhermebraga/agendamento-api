package com.guilhermebraga.agendamento_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração da documentação OpenAPI 3.0 (Swagger UI).
 * <p>
 * Define as informações que aparecem no cabeçalho do Swagger UI:
 * título, versão, descrição, contato e servidor ativo.
 * <p>
 * Acesso: http://localhost:8081/swagger-ui.html
 *
 * @author Guilherme Braga
 */
@Configuration
public class OpenApiConfig {

    /**
     * Cria e registra o bean OpenAPI com as informações do projeto.
     * O SpringDoc lê este bean e popula a interface do Swagger UI.
     */
    @Bean
    public OpenAPI agendamentoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agendamento API")
                        .description(
                                "API REST para Gestão de Agendamentos de Serviços. " +
                                        "Desenvolvida como Trabalho de Conclusão de Curso (TCC) " +
                                        "do Curso de Administração — Centro Universitário UNDB (2026). " +
                                        "Aplica Arquitetura em Camadas, padrões REST e documentação OpenAPI 3.0.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Guilherme Braga")
                                .url("https://github.com/engguilhermebraga"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor de desenvolvimento local")
                ));
    }
}