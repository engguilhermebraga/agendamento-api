package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class DashboardWebController {

    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("titulo", "Dashboard");
        model.addAttribute("totalClientes", clienteService.listarTodos().size());
        model.addAttribute("totalProfissionais", profissionalService.listarTodos().size());
        model.addAttribute("totalServicos", servicoService.listarTodos().size());
        model.addAttribute("totalAgendamentos", agendamentoService.listarTodos().size());
        return "layout/dashboard";
    }
}
