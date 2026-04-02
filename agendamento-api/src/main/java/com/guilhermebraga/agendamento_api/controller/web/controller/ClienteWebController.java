package com.guilhermebraga.agendamento_api.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
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
 * Controller MVC responsável pelas páginas web de Profissional.
 * <p>
 * Usa @Controller (não @RestController) e retorna nomes de templates
 * Thymeleaf. Os endpoints REST de Profissional estão em
 * controller/ProfissionalController.java.
 * <p>
 * Base URL: /web/profissionais
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/web/profissionais")
@RequiredArgsConstructor
@Hidden
public class ProfissionalWebController {

    private final ProfissionalService profissionalService;

    /**
     * GET /web/profissionais — exibe a listagem de todos os profissionais.
     */
    @GetMapping
    public String listar(Model model) {
        List<ProfissionalResponse> profissionais = profissionalService.listarTodos();
        model.addAttribute("profissionais", profissionais);
        model.addAttribute("tituloPagina", "Profissionais");
        return "profissionais/listar";
    }

    /**
     * GET /web/profissionais/novo — exibe o formulário de cadastro.
     * Injeta um ProfissionalRequest vazio no Model para o th:object do formulário.
     */
    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("profissionalRequest", new ProfissionalRequest());
        model.addAttribute("tituloPagina", "Novo Profissional");
        model.addAttribute("acao", "cadastro");
        return "profissionais/formulario";
    }

    /**
     * POST /web/profissionais — processa o cadastro do profissional.
     * Em caso de erros de validação, retorna ao formulário com as mensagens.
     */
    @PostMapping
    public String salvar(@Valid @ModelAttribute("profissionalRequest") ProfissionalRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Novo Profissional");
            model.addAttribute("acao", "cadastro");
            return "profissionais/formulario";
        }

        try {
            profissionalService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Profissional cadastrado com sucesso!");
            return "redirect:/web/profissionais";

        } catch (BusinessException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("tituloPagina", "Novo Profissional");
            model.addAttribute("acao", "cadastro");
            return "profissionais/formulario";
        }
    }

    /**
     * GET /web/profissionais/{id}/editar — exibe o formulário de edição preenchido.
     * Converte o ProfissionalResponse em ProfissionalRequest para popular os campos.
     */
    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        try {
            ProfissionalResponse profissional = profissionalService.buscarPorId(id);
            ProfissionalRequest request = new ProfissionalRequest(
                    profissional.getNome(),
                    profissional.getEspecialidade(),
                    profissional.getEmail(),
                    profissional.getTelefone()
            );
            model.addAttribute("profissionalRequest", request);
            model.addAttribute("profissionalId", id);
            model.addAttribute("tituloPagina", "Editar Profissional");
            model.addAttribute("acao", "edicao");
            return "profissionais/formulario";

        } catch (ResourceNotFoundException e) {
            return "redirect:/web/profissionais";
        }
    }

    /**
     * POST /web/profissionais/{id}/editar — processa a edição do profissional.
     * Usa POST pois formulários HTML não suportam PUT nativamente.
     */
    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("profissionalRequest") ProfissionalRequest request,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("profissionalId", id);
            model.addAttribute("tituloPagina", "Editar Profissional");
            model.addAttribute("acao", "edicao");
            return "profissionais/formulario";
        }

        try {
            profissionalService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Profissional atualizado com sucesso!");
            return "redirect:/web/profissionais";

        } catch (BusinessException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("profissionalId", id);
            model.addAttribute("tituloPagina", "Editar Profissional");
            model.addAttribute("acao", "edicao");
            return "profissionais/formulario";
        }
    }

    /**
     * POST /web/profissionais/{id}/excluir — remove o profissional.
     * Usa POST pois formulários HTML não suportam DELETE nativamente.
     */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            profissionalService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Profissional removido com sucesso!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Profissional não encontrado.");
        }
        return "redirect:/web/profissionais";
    }
}