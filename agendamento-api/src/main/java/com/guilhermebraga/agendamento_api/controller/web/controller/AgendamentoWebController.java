package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/agendamentos")
@RequiredArgsConstructor
public class AgendamentoWebController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("titulo", "Agendamentos");
        model.addAttribute("agendamentos", agendamentoService.listarTodos());
        return "agendamentos/listar";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("titulo", "Novo Agendamento");
        model.addAttribute("agendamento", new AgendamentoRequest());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        return "agendamentos/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute AgendamentoRequest request, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        var agendamento = agendamentoService.buscarPorId(id);
        var request = AgendamentoRequest.builder()
                .clienteId(agendamento.getClienteId())
                .profissionalId(agendamento.getProfissionalId())
                .servicoId(agendamento.getServicoId())
                .dataHora(agendamento.getDataHora())
                .status(agendamento.getStatus())
                .build();
        model.addAttribute("titulo", "Editar Agendamento");
        model.addAttribute("agendamento", request);
        model.addAttribute("agendamentoId", id);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("statusList", StatusAgendamento.values());
        return "agendamentos/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute AgendamentoRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos";
    }

    @GetMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.cancelar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos";
    }
}
