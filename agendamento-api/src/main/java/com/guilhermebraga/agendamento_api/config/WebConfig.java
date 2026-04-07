package com.guilhermebraga.agendamento_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC.
 * Garante que recursos estáticos sejam servidos corretamente
 * sem interferir no servlet do H2 Console.
 *
 * @author Guilherme Braga
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve arquivos CSS, JS e imagens de /static/
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}