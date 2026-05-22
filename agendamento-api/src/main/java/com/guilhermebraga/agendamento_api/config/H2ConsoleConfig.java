package com.guilhermebraga.agendamento_api.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registra manualmente o console web do H2 no Spring Boot 4.x.
 *
 * No Spring Boot 3.x, isso era feito automaticamente por H2ConsoleAutoConfiguration.
 * No Spring Boot 4.x, essa auto-configuração foi removida — o servlet precisa
 * ser registrado explicitamente quando a propriedade spring.h2.console.enabled=true.
 *
 * Ativo apenas no perfil "dev" para garantir que o console não seja exposto
 * em produção. O path padrão é /h2-console/*, conforme application.properties.
 *
 * @author Guilherme Braga
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServlet() {
        ServletRegistrationBean<JakartaWebServlet> registration =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
        registration.setName("H2Console");
        registration.addInitParameter("webAllowOthers", "false");
        registration.addInitParameter("trace", "false");
        return registration;
    }
}
