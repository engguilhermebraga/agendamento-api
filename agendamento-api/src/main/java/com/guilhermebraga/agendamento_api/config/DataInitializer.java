package com.guilhermebraga.agendamento_api.config;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.dto.response.ClienteResponse;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inicializador de dados de demonstração.
 * <p>
 * Executado automaticamente quando a aplicação sobe no perfil "dev".
 * Popula o banco H2 em memória com dados realistas para demonstração
 * no Swagger UI e na interface Thymeleaf.
 * <p>
 * Para desativar: remova @Profile("dev") ou mude o perfil ativo em
 * application.properties para "prod".
 *
 * @author Guilherme Braga
 */
@Component
@Profile("dev") // executa apenas no perfil de desenvolvimento
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;

    @Override
    public void run(String... args) {
        log.info("=================================================");
        log.info(" Inicializando dados de demonstração...");
        log.info("=================================================");

        try {
            // ---- CLIENTES ----
            // Clientes criados com senha "cliente123" para acesso ao portal
            var ent1 = clienteService.criarPeloPortal(
                    "Maria Fernanda Oliveira", "maria.fernanda@email.com",
                    "98988881111", "11122233344", "cliente123");
            ClienteResponse c1 = ClienteResponse.builder().id(ent1.getId()).build();

            var ent2 = clienteService.criarPeloPortal(
                    "João Pedro Santos", "joao.pedro@email.com",
                    "98988882222", "22233344455", "cliente123");
            ClienteResponse c2 = ClienteResponse.builder().id(ent2.getId()).build();

            var ent3 = clienteService.criarPeloPortal(
                    "Ana Carolina Lima", "ana.lima@email.com",
                    "98988883333", "33344455566", "cliente123");
            ClienteResponse c3 = ClienteResponse.builder().id(ent3.getId()).build();

            log.info(" [OK] {} clientes cadastrados.", 3);

            // ---- PROFISSIONAIS ----
            ProfissionalResponse p1 = profissionalService.criar(new ProfissionalRequest(
                    "Dra. Carla Mendes",
                    "Fisioterapeuta",
                    "carla.mendes@clinica.com",
                    "98977771111"
            ));

            ProfissionalResponse p2 = profissionalService.criar(new ProfissionalRequest(
                    "Renata Costa",
                    "Cabeleireira",
                    "renata.costa@salao.com",
                    "98977772222"
            ));

            ProfissionalResponse p3 = profissionalService.criar(new ProfissionalRequest(
                    "Dr. Lucas Pereira",
                    "Nutricionista",
                    "lucas.pereira@nutri.com",
                    "98977773333"
            ));

            log.info(" [OK] {} profissionais cadastrados.", 3);

            // ---- SERVIÇOS ----
            ServicoResponse s1 = servicoService.criar(new ServicoRequest(
                    "Sessão de Fisioterapia",
                    "Atendimento individual de fisioterapia com avaliação postural.",
                    60,
                    new BigDecimal("150.00")
            ));

            ServicoResponse s2 = servicoService.criar(new ServicoRequest(
                    "Corte de Cabelo Feminino",
                    "Corte, lavagem e finalização.",
                    90,
                    new BigDecimal("80.00")
            ));

            ServicoResponse s3 = servicoService.criar(new ServicoRequest(
                    "Consulta Nutricional",
                    "Consulta de avaliação nutricional com elaboração de plano alimentar.",
                    45,
                    new BigDecimal("200.00")
            ));

            ServicoResponse s4 = servicoService.criar(new ServicoRequest(
                    "Massagem Relaxante",
                    "Massagem relaxante corporal com óleos essenciais.",
                    60,
                    new BigDecimal("120.00")
            ));

            log.info(" [OK] {} serviços cadastrados.", 4);

            // ---- AGENDAMENTOS ----
            // Agendamento 1 — futuro, status AGENDADO (padrão)
            LocalDateTime amanha10h = LocalDateTime.now().plusDays(1)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);

            agendamentoService.criar(AgendamentoRequest.builder()
                    .clienteId(c1.getId())
                    .profissionalId(p1.getId())
                    .servicoId(s1.getId())
                    .dataHora(amanha10h)
                    .build());

            // Agendamento 2 — futuro, será confirmado
            LocalDateTime amanha14h = LocalDateTime.now().plusDays(1)
                    .withHour(14).withMinute(0).withSecond(0).withNano(0);

            var ag2 = agendamentoService.criar(AgendamentoRequest.builder()
                    .clienteId(c2.getId())
                    .profissionalId(p2.getId())
                    .servicoId(s2.getId())
                    .dataHora(amanha14h)
                    .build());

            // Confirma o segundo agendamento para demonstrar o fluxo
            agendamentoService.atualizarStatus(ag2.getId(), StatusAgendamento.CONFIRMADO);

            // Agendamento 3 — dois dias no futuro
            LocalDateTime doisDias9h = LocalDateTime.now().plusDays(2)
                    .withHour(9).withMinute(0).withSecond(0).withNano(0);

            agendamentoService.criar(AgendamentoRequest.builder()
                    .clienteId(c3.getId())
                    .profissionalId(p3.getId())
                    .servicoId(s3.getId())
                    .dataHora(doisDias9h)
                    .build());

            // Agendamento 4 — três dias no futuro com massagem
            LocalDateTime tresDias11h = LocalDateTime.now().plusDays(3)
                    .withHour(11).withMinute(0).withSecond(0).withNano(0);

            agendamentoService.criar(AgendamentoRequest.builder()
                    .clienteId(c1.getId())
                    .profissionalId(p1.getId())
                    .servicoId(s4.getId())
                    .dataHora(tresDias11h)
                    .build());

            log.info(" [OK] {} agendamentos criados (1 confirmado, 3 agendados).", 4);

        } catch (Exception e) {
            // Loga o erro mas não impede a subida da aplicação
            log.warn(" Falha ao inicializar dados de demo: {}", e.getMessage());
        }

        log.info("=================================================");
        log.info(" Dados de demonstração prontos!");
        log.info(" Interface web:  http://localhost:8081");
        log.info(" Swagger UI:     http://localhost:8081/swagger-ui.html");
        log.info(" H2 Console:     http://localhost:8081/h2-console");
        log.info("=================================================");
    }
}
