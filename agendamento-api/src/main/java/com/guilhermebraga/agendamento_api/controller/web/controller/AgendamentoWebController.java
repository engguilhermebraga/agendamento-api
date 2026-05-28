package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/web/agendamentos")
@RequiredArgsConstructor
@Slf4j
public class AgendamentoWebController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    @GetMapping
    public String raiz() {
        return "redirect:/web/agendamentos/listar";
    }

    @GetMapping("/listar")
    public String listar(
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Model model) {

        List<AgendamentoResponse> agendamentos = agendamentoService.listarTodos();

        if (status != null) {
            agendamentos = agendamentos.stream()
                    .filter(a -> a.getStatus() == status)
                    .toList();
        }
        if (dataInicio != null) {
            LocalDateTime inicio = dataInicio.atStartOfDay();
            agendamentos = agendamentos.stream()
                    .filter(a -> !a.getDataHora().isBefore(inicio))
                    .toList();
        }
        if (dataFim != null) {
            LocalDateTime fim = dataFim.atTime(23, 59, 59);
            agendamentos = agendamentos.stream()
                    .filter(a -> !a.getDataHora().isAfter(fim))
                    .toList();
        }

        model.addAttribute("titulo", "Agendamentos");
        model.addAttribute("menuAtivo", "agendamentos");
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("statusList", StatusAgendamento.values());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        return "agendamentos/listar";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("titulo", "Novo Agendamento");
        model.addAttribute("menuAtivo", "agendamentos");
        model.addAttribute("agendamento", new AgendamentoRequest());
        popularSelects(model);
        return "agendamentos/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("agendamento") AgendamentoRequest request,
                         Model model, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento criado com sucesso!");
            return "redirect:/web/agendamentos/listar";
        } catch (BusinessException e) {
            log.warn("Conflito ao criar agendamento: {}", e.getMessage());
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("titulo", "Novo Agendamento");
            model.addAttribute("menuAtivo", "agendamentos");
            popularSelects(model);
            return "agendamentos/formulario";
        } catch (Exception e) {
            log.error("Erro inesperado ao criar agendamento", e);
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado. Tente novamente.");
            return "redirect:/web/agendamentos/listar";
        }
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            AgendamentoResponse agendamento = agendamentoService.buscarPorId(id);
            StatusAgendamento statusAtual = agendamento.getStatus();
            boolean bloqueado = statusAtual == StatusAgendamento.CONCLUIDO
                    || statusAtual == StatusAgendamento.CANCELADO;

            AgendamentoRequest request = AgendamentoRequest.builder()
                    .clienteId(agendamento.getClienteId())
                    .profissionalId(agendamento.getProfissionalId())
                    .servicoId(agendamento.getServicoId())
                    .dataHora(agendamento.getDataHora())
                    .status(agendamento.getStatus())
                    .build();

            model.addAttribute("titulo", "Editar Agendamento");
            model.addAttribute("menuAtivo", "agendamentos");
            model.addAttribute("agendamento", request);
            model.addAttribute("agendamentoId", id);
            model.addAttribute("statusAtual", statusAtual);
            model.addAttribute("bloqueado", bloqueado);
            model.addAttribute("statusList", StatusAgendamento.values());
            popularSelects(model);
            return "agendamentos/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Agendamento não encontrado.");
            return "redirect:/web/agendamentos/listar";
        }
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @ModelAttribute("agendamento") AgendamentoRequest request,
                            Model model, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento atualizado com sucesso!");
            return "redirect:/web/agendamentos/listar";
        } catch (BusinessException e) {
            log.warn("Conflito ao atualizar agendamento id={}: {}", id, e.getMessage());
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("titulo", "Editar Agendamento");
            model.addAttribute("menuAtivo", "agendamentos");
            model.addAttribute("agendamentoId", id);
            model.addAttribute("statusAtual", request.getStatus());
            model.addAttribute("bloqueado", false);
            model.addAttribute("statusList", StatusAgendamento.values());
            popularSelects(model);
            return "agendamentos/editar";
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar agendamento id={}", id, e);
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado. Tente novamente.");
            return "redirect:/web/agendamentos/listar";
        }
    }

    @PostMapping("/status/{id}")
    public String mudarStatus(@PathVariable Long id,
                              @RequestParam StatusAgendamento novoStatus,
                              RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.atualizarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status alterado para " + novoStatus + " com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos/listar";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.cancelar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos/listar";
    }

    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento removido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos/listar";
    }

    private void popularSelects(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
    }
}
