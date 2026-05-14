package com.guilhermebraga.agendamento_api.controller.web.controller;

import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteWebController {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("titulo", "Clientes");
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("titulo", "Novo Cliente");
        model.addAttribute("cliente", new ClienteRequest());
        model.addAttribute("modoEdicao", false);
        return "clientes/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ClienteRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            clienteService.criar(request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente cadastrado com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao salvar cliente", e);
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado ao salvar cliente.");
        }
        return "redirect:/web/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("titulo", "Editar Cliente");
            model.addAttribute("cliente", clienteService.buscarPorId(id));
            model.addAttribute("clienteId", id);
            model.addAttribute("modoEdicao", true);
            return "clientes/formulario";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/web/clientes";
        }
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @ModelAttribute ClienteRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            clienteService.atualizar(id, request);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente atualizado com sucesso!");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao atualizar cliente id={}", id, e);
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado ao atualizar cliente.");
        }
        return "redirect:/web/clientes";
    }

    @GetMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente removido com sucesso!");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao deletar cliente id={}", id, e);
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado ao remover cliente.");
        }
        return "redirect:/web/clientes";
    }
}
