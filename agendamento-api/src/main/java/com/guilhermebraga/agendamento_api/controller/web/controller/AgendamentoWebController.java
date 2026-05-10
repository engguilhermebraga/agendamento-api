package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
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

    // Redireciona /web/agendamentos → /web/agendamentos/listar
    @GetMapping
    public String raiz() {
        return "redirect:/web/agendamentos/listar";
    }

    @GetMapping("/listar")
    public String listar(
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Model model) {

        log.info("Listando agendamentos — filtro status={}, data={}", status, data);

        List<AgendamentoResponse> agendamentos = agendamentoService.listarTodos();

        if (status != null) {
            agendamentos = agendamentos.stream()
                    .filter(a -> a.getStatus() == status)
                    .toList();
        }

        if (data != null) {
            LocalDateTime inicio = data.atStartOfDay();
            LocalDateTime fim = inicio.plusDays(1);
            agendamentos = agendamentos.stream()
                    .filter(a -> !a.getDataHora().isBefore(inicio) && a.getDataHora().isBefore(fim))
                    .toList();
        }

        model.addAttribute("titulo", "Agendamentos");
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("statusList", StatusAgendamento.values());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("dataSelecionada", data);
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

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute AgendamentoRequest request, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento criado com sucesso!");
        } catch (Exception e) {
            log.warn("Erro ao criar agendamento: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos/listar";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        AgendamentoResponse agendamento = agendamentoService.buscarPorId(id);
        AgendamentoRequest request = AgendamentoRequest.builder()
                .clienteId(agendamento.getClienteId())
                .profissionalId(agendamento.getProfissionalId())
                .servicoId(agendamento.getServicoId())
                .dataHora(agendamento.getDataHora())
                .status(agendamento.getStatus())
                .build();
        model.addAttribute("titulo", "Editar Agendamento");
        model.addAttribute("agendamento", request);
        model.addAttribute("agendamentoId", id);
        model.addAttribute("statusAtual", agendamento.getStatus());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("statusList", StatusAgendamento.values());
        return "agendamentos/formulario";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute AgendamentoRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento atualizado com sucesso!");
        } catch (Exception e) {
            log.warn("Erro ao atualizar agendamento id={}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos/listar";
    }

    @PostMapping("/status/{id}")
    public String mudarStatus(@PathVariable Long id,
                              @RequestParam StatusAgendamento novoStatus,
                              RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.atualizarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status alterado para " + novoStatus + " com sucesso!");
        } catch (Exception e) {
            log.warn("Erro ao atualizar status do agendamento id={}: {}", id, e.getMessage());
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
            log.warn("Erro ao cancelar agendamento id={}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos/listar";
    }
}
