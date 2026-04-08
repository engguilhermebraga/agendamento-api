package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/profissionais")
@RequiredArgsConstructor
public class ProfissionalWebController {

    private final ProfissionalService profissionalService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("titulo", "Profissionais");
        model.addAttribute("profissionais", profissionalService.listarTodos());
        return "profissionais/listar";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("titulo", "Novo Profissional");
        model.addAttribute("profissional", new ProfissionalRequest());
        return "profissionais/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute ProfissionalRequest request, RedirectAttributes redirectAttributes) {
        try {
            profissionalService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Profissional cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/profissionais";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        var profissional = profissionalService.buscarPorId(id);
        var request = ProfissionalRequest.builder()
                .nome(profissional.getNome())
                .especialidade(profissional.getEspecialidade())
                .email(profissional.getEmail())
                .telefone(profissional.getTelefone())
                .build();
        model.addAttribute("titulo", "Editar Profissional");
        model.addAttribute("profissional", request);
        model.addAttribute("profissionalId", id);
        return "profissionais/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute ProfissionalRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            profissionalService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Profissional atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/profissionais";
    }

    @GetMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            profissionalService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Profissional removido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/profissionais";
    }
}
