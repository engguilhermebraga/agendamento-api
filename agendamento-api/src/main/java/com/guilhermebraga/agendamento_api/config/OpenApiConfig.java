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
 * Disponível em: http://localhost:8080/swagger-ui.html
 *
 * @author Guilherme Braga
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agendamentoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agendamento API")
                        .description(
                                "API REST para Gestão de Agendamentos de Serviços. " +
                                        "Desenvolvida como Trabalho de Conclusão de Curso (TCC) " +
                                        "do Curso de Administração — UNDB.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Guilherme Braga")
                                .url("https://github.com/engguilhermebraga"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desenvolvimento local")
                ));
    }
}