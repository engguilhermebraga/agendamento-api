package com.guilhermebraga.agendamento_api.controller.portal;

import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ClienteService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller do portal — home, identificação e cadastro do cliente.
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalHomeController {

    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final ProfissionalService profissionalService;
    private final AgendamentoService agendamentoService;

    /**
     * Página inicial do portal.
     * Se o cliente não está logado, redireciona para identificação.
     */
    @GetMapping
    public String home(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        if (cliente == null) {
            return "portal/identificacao";
        }

        model.addAttribute("titulo", "Início");
        model.addAttribute("menuAtivo", "home");
        model.addAttribute("totalServicos", servicoService.listarTodos().size());
        model.addAttribute("totalProfissionais", profissionalService.listarTodos().size());
        model.addAttribute("meusAgendamentos", agendamentoService.listarPorCliente(cliente.getId()).size());
        return "portal/home";
    }

    /**
     * Catálogo de serviços disponíveis.
     */
    @GetMapping("/servicos")
    public String servicos(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        if (cliente == null) return "redirect:/portal";

        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("menuAtivo", "servicos");
        return "portal/servicos";
    }

    /**
     * Login por e-mail + senha (BCrypt).
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        HttpSession session,
                        RedirectAttributes redirect) {
        Cliente cliente = clienteService.autenticarPorEmailESenha(email, senha);
        if (cliente == null) {
            redirect.addFlashAttribute("mensagemErro", "E-mail ou senha inválidos.");
            return "redirect:/portal";
        }
        session.setAttribute("clienteLogado", cliente);
        redirect.addFlashAttribute("mensagemSucesso", "Bem-vindo(a), " + cliente.getNome() + "!");
        return "redirect:/portal";
    }

    /**
     * Exibe formulário de cadastro.
     */
    @GetMapping("/cadastro")
    public String exibirCadastro() {
        return "portal/cadastro";
    }

    /**
     * Processa o cadastro do cliente e faz login automático.
     */
    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String telefone,
                            @RequestParam String cpf,
                            @RequestParam String senha,
                            @RequestParam String confirmarSenha,
                            HttpSession session,
                            RedirectAttributes redirect,
                            Model model) {
        if (senha.length() < 6) {
            model.addAttribute("mensagemErro", "A senha deve ter pelo menos 6 caracteres.");
            return "portal/cadastro";
        }
        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("mensagemErro", "As senhas não conferem.");
            return "portal/cadastro";
        }
        try {
            Cliente cliente = clienteService.criarPeloPortal(nome, email, telefone, cpf, senha);
            session.setAttribute("clienteLogado", cliente);
            redirect.addFlashAttribute("mensagemSucesso", "Cadastro realizado com sucesso! Bem-vindo(a), " + cliente.getNome() + "!");
            return "redirect:/portal";
        } catch (BusinessException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "portal/cadastro";
        }
    }

    /**
     * Logout — encerra a sessão.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/portal";
    }
}
