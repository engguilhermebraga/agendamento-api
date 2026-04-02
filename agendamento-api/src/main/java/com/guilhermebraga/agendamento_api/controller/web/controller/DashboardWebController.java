package com.guilhermebraga.agendamento_api.web.controller;

import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller MVC responsável pela página inicial (dashboard).
 * Exibe um resumo com os totais de cada entidade cadastrada no sistema.
 *
 * @author Guilherme Braga
 */
@Controller
@RequiredArgsConstructor
@Hidden
public class DashboardWebController {

    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;

    /**
     * GET / — redireciona a raiz para o dashboard.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/web/dashboard";
    }

    /**
     * GET /web/dashboard — exibe o painel principal com os contadores de cada entidade.
     * Os valores são injetados no Model e exibidos pelos cards no template dashboard.html.
     */
    @GetMapping("/web/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalClientes",
                clienteService.listarTodos().size());
        model.addAttribute("totalProfissionais",
                profissionalService.listarTodos().size());
        model.addAttribute("totalServicos",
                servicoService.listarTodos().size());
        model.addAttribute("totalAgendamentos",
                agendamentoService.listarTodos().size());
        model.addAttribute("tituloPagina", "Dashboard");
        return "dashboard";
    }
}