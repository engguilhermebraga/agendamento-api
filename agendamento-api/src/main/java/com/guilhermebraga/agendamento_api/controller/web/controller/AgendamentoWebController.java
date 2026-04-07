package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC responsável pelas páginas web de Agendamento.
 * <p>
 * Usa @Controller (não @RestController) e retorna nomes de templates
 * Thymeleaf. Os endpoints REST de Agendamento estão em
 * controller/AgendamentoController.java.
 * <p>
 * Base URL: /web/agendamentos
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/web/agendamentos")
@RequiredArgsConstructor
@Hidden
public class AgendamentoWebController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    /**
     * GET /web/agendamentos — exibe a listagem de todos os agendamentos.
     */
    @GetMapping
    public String listar(Model model) {
        List<AgendamentoResponse> agendamentos = agendamentoService.listarTodos();
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("tituloPagina", "Agendamentos");
        return "agendamentos/listar";
    }

    /**
     * GET /web/agendamentos/novo — exibe o formulário de novo agendamento.
     * Carrega as listas de clientes, profissionais e serviços para popular os selects.
     */
    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("agendamentoRequest", new AgendamentoRequest());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("tituloPagina", "Novo Agendamento");
        return "agendamentos/formulario";
    }

    /**
     * POST /web/agendamentos — processa o envio do formulário de novo agendamento.
     * Em caso de conflito de horário (BusinessException), retorna ao formulário
     * com a mensagem de erro para que o usuário escolha outro horário.
     */
    @PostMapping
    public String salvar(@Valid @ModelAttribute("agendamentoRequest") AgendamentoRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Erros de validação (@Valid) — retorna ao formulário recarregando os selects
        if (bindingResult.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("profissionais", profissionalService.listarTodos());
            model.addAttribute("servicos", servicoService.listarTodos());
            model.addAttribute("tituloPagina", "Novo Agendamento");
            return "agendamentos/formulario";
        }

        try {
            agendamentoService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Agendamento criado com sucesso!");
            return "redirect:/web/agendamentos";

        } catch (BusinessException | ResourceNotFoundException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("profissionais", profissionalService.listarTodos());
            model.addAttribute("servicos", servicoService.listarTodos());
            model.addAttribute("tituloPagina", "Novo Agendamento");
            return "agendamentos/formulario";
        }
    }

    /**
     * POST /web/agendamentos/{id}/status — atualiza o status de um agendamento.
     * Chamado pelos botões Confirmar / Concluir na listagem.
     */
    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable Long id,
                                  @RequestParam StatusAgendamento status,
                                  RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Status atualizado para: " + status);
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos";
    }

    /**
     * POST /web/agendamentos/{id}/cancelar — cancela um agendamento ativo.
     * Chamado pelo botão Cancelar na listagem.
     */
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.cancelar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Agendamento cancelado com sucesso.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/agendamentos";
    }
}