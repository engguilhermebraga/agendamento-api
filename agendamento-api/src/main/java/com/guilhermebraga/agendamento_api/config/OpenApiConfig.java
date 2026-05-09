package com.guilhermebraga.agendamento_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc OpenAPI para geração automática de documentação Swagger.
 * 
 * Define metadados, servidores e informações da API que aparecem no Swagger UI.
 * 
 * Acesso: http://localhost:8081/swagger-ui.html
 * JSON: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura os metadados da API (título, descrição, versão, contato, licença).
     * 
     * @return Objeto OpenAPI com as configurações
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Agendamento de Serviços")
                        .description("API REST para gestão de agendamentos de serviços com aplicação de " +
                                "arquitetura em camadas, padrões de projeto e documentação OpenAPI. " +
                                "Trabalho de Conclusão de Curso (TCC) — Engenharia de Software, UNDB.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Guilherme Braga")
                                .email("guilhermebraga@email.com")
                                .url("https://github.com/engguilhermebraga/agendamento-api"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server()
                        .url("http://localhost:8081")
                        .description("Servidor de Desenvolvimento"))
                .addServersItem(new Server()
                        .url("https://api.agendamento.prod.com")
                        .description("Servidor de Produção"));
    }
}
