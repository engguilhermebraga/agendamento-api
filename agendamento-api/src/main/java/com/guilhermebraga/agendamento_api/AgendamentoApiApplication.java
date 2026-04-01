package com.guilhermebraga.agendamento_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Agendamento API.
 * <p>
 * A anotação @SpringBootApplication combina três anotações:
 * - @Configuration: marca como fonte de beans Spring
 * - @EnableAutoConfiguration: ativa a configuração automática
 * - @ComponentScan: escaneia os componentes do pacote raiz
 *
 * @author Guilherme Braga
 */
@SpringBootApplication
public class AgendamentoApiApplication {


    static void main(String[] args) {
        SpringApplication.run(AgendamentoApiApplication.class, args);
    }
}