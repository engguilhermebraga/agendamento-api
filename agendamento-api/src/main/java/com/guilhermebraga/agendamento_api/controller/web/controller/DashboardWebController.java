package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dashboard administrativo — exibe métricas em tempo real:
 *   total de agendamentos, agendamentos do dia, próximos 7 dias e
 *   clientes com agendamentos ativos (AGENDADO ou CONFIRMADO).
 *
 * @author Guilherme Braga
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardWebController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    @GetMapping("")
    public String index(Model model) {
        log.info("Renderizando dashboard administrativo");

        List<AgendamentoResponse> todos = agendamentoService.listarTodos();

        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje    = inicioHoje.plusDays(1);
        LocalDateTime fim7Dias   = inicioHoje.plusDays(7);

        long agendamentosHoje = todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioHoje)
                          &&  a.getDataHora().isBefore(fimHoje))
                .count();

        long proximos7Dias = todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioHoje)
                          &&  a.getDataHora().isBefore(fim7Dias))
                .count();

        Set<Long> clientesAtivosIds = todos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO
                          || a.getStatus() == StatusAgendamento.CONFIRMADO)
                .map(AgendamentoResponse::getClienteId)
                .collect(Collectors.toSet());

        // Distribuição por status — mapa ordenado com todos os status
        Map<String, Long> porStatus = Arrays.stream(StatusAgendamento.values())
                .collect(Collectors.toMap(StatusAgendamento::name, s -> 0L, (a, b) -> a, LinkedHashMap::new));
        todos.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()))
                .forEach(porStatus::put);

        // Próximos 3 agendamentos futuros (AGENDADO ou CONFIRMADO), em ordem cronológica
        List<AgendamentoResponse> proximosAgendamentos = todos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO
                          || a.getStatus() == StatusAgendamento.CONFIRMADO)
                .filter(a -> !a.getDataHora().isBefore(inicioHoje))
                .sorted(Comparator.comparing(AgendamentoResponse::getDataHora))
                .limit(3)
                .toList();

        model.addAttribute("titulo",               "Dashboard");
        model.addAttribute("totalAgendamentos",    todos.size());
        model.addAttribute("agendamentosHoje",     agendamentosHoje);
        model.addAttribute("proximos7Dias",        proximos7Dias);
        model.addAttribute("clientesAtivos",       clientesAtivosIds.size());
        model.addAttribute("totalClientes",        clienteService.listarTodos().size());
        model.addAttribute("totalProfissionais",   profissionalService.listarTodos().size());
        model.addAttribute("totalServicos",        servicoService.listarTodos().size());
        model.addAttribute("porStatus",            porStatus);
        model.addAttribute("proximosAgendamentos", proximosAgendamentos);
        model.addAttribute("totalAgendado",        porStatus.getOrDefault("AGENDADO",   0L));
        model.addAttribute("totalConfirmado",      porStatus.getOrDefault("CONFIRMADO", 0L));
        model.addAttribute("totalConcluido",       porStatus.getOrDefault("CONCLUIDO",  0L));
        model.addAttribute("totalCancelado",       porStatus.getOrDefault("CANCELADO",  0L));

        return "dashboard/index";
    }
}
