package com.guilhermebraga.agendamento_api.controller.portal;

import com.guilhermebraga.agendamento_api.dto.form.NovoAgendamentoForm;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import com.guilhermebraga.agendamento_api.service.ProfissionalService;
import com.guilhermebraga.agendamento_api.service.ServicoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller do fluxo de agendamento em etapas no portal.
 * Usa @SessionAttributes para manter o NovoAgendamentoForm em sessão.
 *
 * @author Guilherme Braga
 */
@Controller
@RequestMapping("/portal/agendar")
@SessionAttributes("novoAgendamento")
@RequiredArgsConstructor
public class PortalAgendamentoController {

    private final ServicoService servicoService;
    private final ProfissionalService profissionalService;
    private final AgendamentoService agendamentoService;

    @ModelAttribute("novoAgendamento")
    public NovoAgendamentoForm novoAgendamentoForm() {
        return new NovoAgendamentoForm();
    }

    // ----------------------------------------------------------------
    // ETAPA 1 — ESCOLHER SERVIÇO
    // ----------------------------------------------------------------

    @GetMapping("/servico")
    public String escolherServico(@RequestParam(required = false) Long servicoId,
                                  @ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                  HttpSession session, Model model) {
        if (!isLogado(session)) return "redirect:/portal";

        if (servicoId != null) {
            ServicoResponse servico = servicoService.buscarPorId(servicoId);
            form.setServicoId(servico.getId());
            form.setServicoNome(servico.getNome());
            return "redirect:/portal/agendar/profissional";
        }

        model.addAttribute("titulo", "Escolher Serviço");
        model.addAttribute("menuAtivo", "agendar");
        model.addAttribute("servicos", servicoService.listarTodos());
        return "portal/agendar/servico";
    }

    @PostMapping("/servico")
    public String selecionarServico(@RequestParam Long servicoId,
                                    @ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                    HttpSession session) {
        if (!isLogado(session)) return "redirect:/portal";

        ServicoResponse servico = servicoService.buscarPorId(servicoId);
        form.setServicoId(servico.getId());
        form.setServicoNome(servico.getNome());
        return "redirect:/portal/agendar/profissional";
    }

    // ----------------------------------------------------------------
    // ETAPA 2 — ESCOLHER PROFISSIONAL
    // ----------------------------------------------------------------

    @GetMapping("/profissional")
    public String escolherProfissional(@ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                       HttpSession session, Model model) {
        if (!isLogado(session)) return "redirect:/portal";
        if (form.getServicoId() == null) return "redirect:/portal/agendar/servico";

        model.addAttribute("titulo", "Escolher Profissional");
        model.addAttribute("menuAtivo", "agendar");
        model.addAttribute("profissionais", profissionalService.listarTodos());
        return "portal/agendar/profissional";
    }

    @PostMapping("/profissional")
    public String selecionarProfissional(@RequestParam Long profissionalId,
                                         @ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                         HttpSession session) {
        if (!isLogado(session)) return "redirect:/portal";

        ProfissionalResponse profissional = profissionalService.buscarPorId(profissionalId);
        form.setProfissionalId(profissional.getId());
        form.setProfissionalNome(profissional.getNome());
        return "redirect:/portal/agendar/horario";
    }

    // ----------------------------------------------------------------
    // ETAPA 3 — ESCOLHER DATA E HORÁRIO
    // ----------------------------------------------------------------

    @GetMapping("/horario")
    public String escolherHorario(@RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                  @ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                  HttpSession session, Model model) {
        if (!isLogado(session)) return "redirect:/portal";
        if (form.getProfissionalId() == null) return "redirect:/portal/agendar/profissional";

        model.addAttribute("titulo", "Escolher Horário");
        model.addAttribute("menuAtivo", "agendar");
        model.addAttribute("dataMinima", LocalDate.now());

        if (data != null) {
            List<LocalTime> horarios = agendamentoService.buscarHorariosDisponiveis(
                    form.getProfissionalId(), form.getServicoId(), data);
            model.addAttribute("dataSelecionada", data);
            model.addAttribute("horarios", horarios);
        }

        return "portal/agendar/horario";
    }

    @PostMapping("/horario")
    public String selecionarHorario(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                    @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horario,
                                    @ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                    HttpSession session) {
        if (!isLogado(session)) return "redirect:/portal";

        form.setData(data);
        form.setHorario(horario);
        return "redirect:/portal/agendar/confirmar";
    }

    // ----------------------------------------------------------------
    // ETAPA 4 — REVISÃO E CONFIRMAÇÃO
    // ----------------------------------------------------------------

    @GetMapping("/confirmar")
    public String confirmar(@ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                            HttpSession session, Model model) {
        if (!isLogado(session)) return "redirect:/portal";
        if (form.getHorario() == null) return "redirect:/portal/agendar/horario";

        model.addAttribute("titulo", "Confirmar Agendamento");
        model.addAttribute("menuAtivo", "agendar");
        return "portal/agendar/confirmar";
    }

    @PostMapping("/confirmar")
    public String confirmarAgendamento(@ModelAttribute("novoAgendamento") NovoAgendamentoForm form,
                                       HttpSession session,
                                       RedirectAttributes redirect,
                                       SessionStatus sessionStatus) {
        if (!isLogado(session)) return "redirect:/portal";

        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
        LocalDateTime dataHora = LocalDateTime.of(form.getData(), form.getHorario());

        try {
            agendamentoService.criarPeloPortal(
                    cliente.getId(), form.getProfissionalId(),
                    form.getServicoId(), dataHora);

            sessionStatus.setComplete(); // limpa o form da sessão
            redirect.addFlashAttribute("mensagemSucesso",
                    "Agendamento realizado com sucesso! Serviço: " + form.getServicoNome() +
                            " em " + form.getData().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " às " + form.getHorario().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            return "redirect:/portal/meus-agendamentos";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/portal/agendar/horario";
        }
    }

    private boolean isLogado(HttpSession session) {
        return session.getAttribute("clienteLogado") != null;
    }
}
