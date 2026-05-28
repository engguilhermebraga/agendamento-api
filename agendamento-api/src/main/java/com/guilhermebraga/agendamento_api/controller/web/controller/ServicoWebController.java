package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/servicos")
@RequiredArgsConstructor
public class ServicoWebController {

    private final ServicoService servicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("titulo", "Servicos");
        model.addAttribute("servicos", servicoService.listarTodos());
        return "servicos/listar";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("titulo", "Novo Servico");
        model.addAttribute("servico", new ServicoRequest());
        return "servicos/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute ServicoRequest request, RedirectAttributes redirectAttributes) {
        try {
            servicoService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Servico cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/servicos";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        var servico = servicoService.buscarPorId(id);
        var request = ServicoRequest.builder()
                .nome(servico.getNome())
                .descricao(servico.getDescricao())
                .duracaoMinutos(servico.getDuracaoMinutos())
                .preco(servico.getPreco())
                .build();
        model.addAttribute("titulo", "Editar Servico");
        model.addAttribute("servico", request);
        model.addAttribute("servicoId", id);
        return "servicos/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute ServicoRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            servicoService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Servico atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/servicos";
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicoService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço removido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/servicos";
    }
}
