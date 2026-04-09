package com.guilhermebraga.agendamento_api.controller.portal;

import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller do portal — listagem e cancelamento de agendamentos do cliente.
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/portal/meus-agendamentos")
@RequiredArgsConstructor
public class PortalMeusAgendamentosController {

    private final AgendamentoService agendamentoService;

    @GetMapping
    public String listar(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        if (cliente == null) return "redirect:/portal";

        model.addAttribute("agendamentos", agendamentoService.listarPorCliente(cliente.getId()));
        model.addAttribute("menuAtivo", "meus-agendamentos");
        return "portal/meus-agendamentos";
    }

    @GetMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        if (cliente == null) return "redirect:/portal";

        try {
            agendamentoService.cancelarPeloCliente(id, cliente.getId());
            redirect.addFlashAttribute("mensagemSucesso", "Agendamento cancelado com sucesso.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("mensagemErro", e.getMessage());
        }

        return "redirect:/portal/meus-agendamentos";
    }
}
