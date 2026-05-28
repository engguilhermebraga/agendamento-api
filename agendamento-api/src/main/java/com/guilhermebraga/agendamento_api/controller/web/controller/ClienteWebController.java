package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/clientes")
@RequiredArgsConstructor
public class ClienteWebController {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("titulo", "Clientes");
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/listar";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("titulo", "Novo Cliente");
        model.addAttribute("cliente", new ClienteRequest());
        return "clientes/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute ClienteRequest request, RedirectAttributes redirectAttributes) {
        try {
            clienteService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/clientes";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        var cliente = clienteService.buscarPorId(id);
        var request = ClienteRequest.builder()
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .cpf(cliente.getCpf())
                .build();
        model.addAttribute("titulo", "Editar Cliente");
        model.addAttribute("cliente", request);
        model.addAttribute("clienteId", id);
        return "clientes/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute ClienteRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            clienteService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/clientes";
    }

    @GetMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente removido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/web/clientes";
    }
}
