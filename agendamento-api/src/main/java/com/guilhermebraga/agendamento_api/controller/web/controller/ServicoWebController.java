package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
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
 * Controller MVC responsável pelas páginas web de Serviço.
 * <p>
 * Usa @Controller e retorna nomes de templates Thymeleaf.
 * Os endpoints REST de Serviço estão em controller/ServicoController.java.
 * <p>
 * Base URL: /web/servicos
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/web/servicos")
@RequiredArgsConstructor
@Hidden
public class ServicoWebController {

    private final ServicoService servicoService;

    /**
     * GET /web/servicos — Exibe a listagem de serviços.
     */
    @GetMapping
    public String listar(Model model) {
        List<ServicoResponse> servicos = servicoService.listarTodos();
        model.addAttribute("servicos", servicos);
        model.addAttribute("tituloPagina", "Serviços");
        return "servicos/listar";
    }

    /**
     * GET /web/servicos/novo — Exibe o formulário de cadastro.
     */
    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("servicoRequest", new ServicoRequest());
        model.addAttribute("tituloPagina", "Novo Serviço");
        model.addAttribute("acao", "cadastro");
        return "servicos/formulario";
    }

    /**
     * POST /web/servicos — Processa o cadastro.
     */
    @PostMapping
    public String salvar(@Valid @ModelAttribute("servicoRequest") ServicoRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Novo Serviço");
            model.addAttribute("acao", "cadastro");
            return "servicos/formulario";
        }

        try {
            servicoService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Serviço cadastrado com sucesso!");
            return "redirect:/web/servicos";

        } catch (BusinessException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("tituloPagina", "Novo Serviço");
            model.addAttribute("acao", "cadastro");
            return "servicos/formulario";
        }
    }

    /**
     * GET /web/servicos/{id}/editar — Exibe formulário preenchido.
     */
    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        try {
            ServicoResponse servico = servicoService.buscarPorId(id);
            ServicoRequest request = new ServicoRequest(
                    servico.getNome(),
                    servico.getDescricao(),
                    servico.getDuracaoMinutos(),
                    servico.getPreco()
            );
            model.addAttribute("servicoRequest", request);
            model.addAttribute("servicoId", id);
            model.addAttribute("tituloPagina", "Editar Serviço");
            model.addAttribute("acao", "edicao");
            return "servicos/formulario";

        } catch (ResourceNotFoundException e) {
            return "redirect:/web/servicos";
        }
    }

    /**
     * POST /web/servicos/{id}/editar — Processa a edição.
     */
    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("servicoRequest") ServicoRequest request,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("servicoId", id);
            model.addAttribute("tituloPagina", "Editar Serviço");
            model.addAttribute("acao", "edicao");
            return "servicos/formulario";
        }

        try {
            servicoService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Serviço atualizado com sucesso!");
            return "redirect:/web/servicos";

        } catch (BusinessException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("servicoId", id);
            model.addAttribute("tituloPagina", "Editar Serviço");
            model.addAttribute("acao", "edicao");
            return "servicos/formulario";
        }
    }

    /**
     * POST /web/servicos/{id}/excluir — Remove o serviço.
     */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicoService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Serviço removido com sucesso!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Serviço não encontrado.");
        }
        return "redirect:/web/servicos";
    }
}
