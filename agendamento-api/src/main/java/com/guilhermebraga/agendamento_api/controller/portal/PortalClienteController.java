package com.guilhermebraga.agendamento_api.controller.portal;

import com.guilhermebraga.agendamento_api.dto.form.WizardForm;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Wizard de agendamento do portal do cliente, em 4 etapas.
 *
 * O {@link WizardForm} é mantido em sessão via {@code @SessionAttributes}
 * enquanto o cliente avança pelas etapas — assim os dados das etapas anteriores
 * permanecem disponíveis até a finalização (ou {@code SessionStatus.setComplete()}).
 *
 *   Step 1 (GET  /portal/step1): seleciona serviço
 *   Step 2 (POST /portal/step2): salva serviço, exibe form de profissional + data/hora
 *   Step 3 (POST /portal/step3): salva data/hora, exibe resumo para confirmação
 *   Step 4 (POST /portal/step4): cria o agendamento, limpa a sessão, exibe sucesso
 *
 * Pré-requisito: cliente identificado em sessão ({@code clienteLogado}).
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/portal")
@SessionAttributes("wizard")
@RequiredArgsConstructor
@Slf4j
public class PortalClienteController {

    private final ServicoService servicoService;
    private final ProfissionalService profissionalService;
    private final AgendamentoService agendamentoService;

    /**
     * Inicializa o WizardForm na sessão se ainda não existir.
     * Spring chama esse método antes de qualquer handler do controller.
     */
    @ModelAttribute("wizard")
    public WizardForm wizardForm() {
        return new WizardForm();
    }

    // ----------------------------------------------------------------
    // STEP 1 — Selecionar serviço
    // ----------------------------------------------------------------
    @GetMapping("/step1")
    public String step1(HttpSession session, Model model) {
        if (clienteNaoLogado(session)) return "redirect:/portal";

        log.info("Wizard step 1 — exibindo seleção de serviço");
        model.addAttribute("titulo", "Passo 1 — Selecione o Serviço");
        model.addAttribute("menuAtivo", "agendar");
        model.addAttribute("servicos", servicoService.listarTodos());
        return "portal/wizard/step1";
    }

    // ----------------------------------------------------------------
    // STEP 2 — Selecionar data/hora (e profissional)
    // ----------------------------------------------------------------
    @PostMapping("/step2")
    public String step2(@RequestParam Long servicoId,
                        @ModelAttribute("wizard") WizardForm wizard,
                        HttpSession session, Model model) {
        if (clienteNaoLogado(session)) return "redirect:/portal";

        ServicoResponse servico = servicoService.buscarPorId(servicoId);
        wizard.setServicoId(servico.getId());
        wizard.setServicoNome(servico.getNome());
        wizard.setServicoDuracaoMinutos(servico.getDuracaoMinutos());
        wizard.setServicoPreco(servico.getPreco());

        log.info("Wizard step 2 — serviço selecionado: id={}, nome={}", servico.getId(), servico.getNome());

        model.addAttribute("titulo", "Passo 2 — Data e Horário");
        model.addAttribute("menuAtivo", "agendar");
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("dataMinima", LocalDate.now());
        return "portal/wizard/step2";
    }

    // ----------------------------------------------------------------
    // STEP 3 — Confirmar dados
    // ----------------------------------------------------------------
    @PostMapping("/step3")
    public String step3(@RequestParam Long profissionalId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                        @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                        @ModelAttribute("wizard") WizardForm wizard,
                        HttpSession session, Model model,
                        RedirectAttributes redirectAttributes) {
        if (clienteNaoLogado(session)) return "redirect:/portal";

        if (wizard.getServicoId() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Sua sessão expirou. Por favor, comece novamente.");
            return "redirect:/portal/step1";
        }

        ProfissionalResponse profissional = profissionalService.buscarPorId(profissionalId);
        wizard.setProfissionalId(profissional.getId());
        wizard.setProfissionalNome(profissional.getNome());
        wizard.setProfissionalEspecialidade(profissional.getEspecialidade());
        wizard.setData(data);
        wizard.setHora(hora);

        log.info("Wizard step 3 — profissional={}, data={}, hora={}",
                profissional.getId(), data, hora);

        model.addAttribute("titulo", "Passo 3 — Confirmar Agendamento");
        model.addAttribute("menuAtivo", "agendar");
        return "portal/wizard/step3";
    }

    // ----------------------------------------------------------------
    // STEP 4 — Sucesso: cria o agendamento e exibe confirmação
    // ----------------------------------------------------------------
    @PostMapping("/step4")
    public String step4(@ModelAttribute("wizard") WizardForm wizard,
                        HttpSession session, SessionStatus sessionStatus,
                        Model model, RedirectAttributes redirectAttributes) {
        if (clienteNaoLogado(session)) return "redirect:/portal";

        if (wizard.getServicoId() == null || wizard.getProfissionalId() == null
                || wizard.getData() == null || wizard.getHora() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Dados incompletos. Por favor, refaça o agendamento.");
            return "redirect:/portal/step1";
        }

        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        try {
            LocalDateTime dataHora = LocalDateTime.of(wizard.getData(), wizard.getHora());
            var agendamentoCriado = agendamentoService.criarPeloPortal(
                    cliente.getId(),
                    wizard.getProfissionalId(),
                    wizard.getServicoId(),
                    dataHora
            );
            log.info("Agendamento criado via wizard — id={}, cliente={}, profissional={}, dataHora={}",
                    agendamentoCriado.getId(), cliente.getId(), wizard.getProfissionalId(), dataHora);

            model.addAttribute("agendamentoId", agendamentoCriado.getId());
            model.addAttribute("titulo", "Agendamento Confirmado!");
            model.addAttribute("menuAtivo", "agendar");
            model.addAttribute("wizardFinalizado", wizard);
            sessionStatus.setComplete(); // remove o "wizard" da sessão HTTP
            return "portal/wizard/step4";
        } catch (Exception e) {
            log.warn("Erro ao criar agendamento via wizard: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/portal/step1";
        }
    }

    // ----------------------------------------------------------------
    // FINALIZAR (mantido por compatibilidade — redireciona para step4)
    // ----------------------------------------------------------------
    @PostMapping("/finalizar")
    public String finalizar(@ModelAttribute("wizard") WizardForm wizard,
                            HttpSession session, SessionStatus sessionStatus,
                            RedirectAttributes redirectAttributes) {
        if (clienteNaoLogado(session)) return "redirect:/portal";

        if (wizard.getServicoId() == null || wizard.getProfissionalId() == null
                || wizard.getData() == null || wizard.getHora() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Dados incompletos. Por favor, refaça o agendamento.");
            return "redirect:/portal/step1";
        }

        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        try {
            LocalDateTime dataHora = LocalDateTime.of(wizard.getData(), wizard.getHora());
            agendamentoService.criarPeloPortal(
                    cliente.getId(),
                    wizard.getProfissionalId(),
                    wizard.getServicoId(),
                    dataHora
            );
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Agendamento criado com sucesso!");
            return "redirect:/portal/meus-agendamentos";
        } catch (Exception e) {
            log.warn("Erro ao finalizar agendamento via wizard: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/portal/step1";
        }
    }

    private boolean clienteNaoLogado(HttpSession session) {
        return session.getAttribute("clienteLogado") == null;
    }
}
