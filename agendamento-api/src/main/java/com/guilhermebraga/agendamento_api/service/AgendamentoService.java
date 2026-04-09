package com.guilhermebraga.agendamento_api.service;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.*;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.AgendamentoMapper;
import com.guilhermebraga.agendamento_api.repository.AgendamentoRepository;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import com.guilhermebraga.agendamento_api.repository.ProfissionalRepository;
import com.guilhermebraga.agendamento_api.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de serviço com as regras de negócio do Agendamento.
 * <p>
 * Regras implementadas:
 * 1. Cliente, Profissional e Serviço devem existir no sistema
 * 2. A data/hora deve ser futura
 * 3. Não pode haver conflito de horário para o mesmo profissional
 * 4. Agendamentos CONCLUIDOS não podem ser alterados
 * 5. O status só pode seguir o fluxo permitido
 *
 * @author Guilherme Braga
 */
@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoMapper agendamentoMapper;

    @Value("${agendamento.cancelamento.antecedencia-horas:2}")
    private int antecedenciaCancelamentoHoras;

    @Value("${agendamento.horario.inicio:08:00}")
    private String horarioInicio;

    @Value("${agendamento.horario.fim:18:00}")
    private String horarioFim;

    // Status que indicam agendamento inativo — ignorados na verificação de conflito
    private static final List<StatusAgendamento> STATUS_INATIVOS =
            List.of(StatusAgendamento.CANCELADO, StatusAgendamento.CONCLUIDO);

    // ----------------------------------------------------------------
    // CRIAR AGENDAMENTO
    // ----------------------------------------------------------------

    /**
     * Cria um novo agendamento após validar todas as regras de negócio.
     */
    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {

        // Busca as entidades relacionadas — lança 404 se não existirem
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));

        Profissional profissional = profissionalRepository.findById(request.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", request.getProfissionalId()));

        Servico servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", request.getServicoId()));

        // Calcula o horário de término com base na duração do serviço
        LocalDateTime dataHoraFim = request.getDataHora()
                .plusMinutes(servico.getDuracaoMinutos());

        // Verifica conflito de horário para o profissional
        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                profissional.getId(),
                request.getDataHora(),
                dataHoraFim,
                STATUS_INATIVOS
        );

        if (!conflitos.isEmpty()) {
            throw new BusinessException(
                    "O profissional já possui um agendamento neste horário. " +
                            "Por favor, escolha outro horário ou profissional.");
        }

        // Monta e persiste o agendamento
        Agendamento agendamento = agendamentoMapper.toEntity(request, cliente, profissional, servico);
        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    // ----------------------------------------------------------------
    // LISTAR TODOS
    // ----------------------------------------------------------------

    /**
     * Retorna a lista completa de agendamentos.
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarTodos() {
        return agendamentoRepository.findAll()
                .stream()
                .map(agendamentoMapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // BUSCAR POR ID
    // ----------------------------------------------------------------

    /**
     * Busca um agendamento pelo ID ou lança 404.
     */
    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(Long id) {
        return agendamentoMapper.toResponse(buscarOuLancarExcecao(id));
    }

    // ----------------------------------------------------------------
    // ATUALIZAR
    // ----------------------------------------------------------------

    /**
     * Atualiza os dados de um agendamento existente.
     * Agendamentos CONCLUIDOS não podem ser alterados.
     */
    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoRequest request) {

        Agendamento agendamento = buscarOuLancarExcecao(id);

        // Agendamentos concluídos são imutáveis
        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException(
                    "Agendamentos concluídos não podem ser alterados.");
        }

        // Agendamentos cancelados não podem ser reativados
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException(
                    "Agendamentos cancelados não podem ser alterados.");
        }

        // Busca as entidades relacionadas
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));

        Profissional profissional = profissionalRepository.findById(request.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", request.getProfissionalId()));

        Servico servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", request.getServicoId()));

        // Calcula o novo horário de término
        LocalDateTime dataHoraFim = request.getDataHora()
                .plusMinutes(servico.getDuracaoMinutos());

        // Verifica conflito ignorando o próprio agendamento
        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorarioExcluindoId(
                profissional.getId(),
                request.getDataHora(),
                dataHoraFim,
                STATUS_INATIVOS,
                id
        );

        if (!conflitos.isEmpty()) {
            throw new BusinessException(
                    "O profissional já possui um agendamento neste horário. " +
                            "Por favor, escolha outro horário ou profissional.");
        }

        agendamentoMapper.updateEntityFromRequest(request, agendamento, cliente, profissional, servico);
        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    // ----------------------------------------------------------------
    // ATUALIZAR STATUS
    // ----------------------------------------------------------------

    /**
     * Atualiza apenas o status de um agendamento.
     * Valida o fluxo permitido de transições de status.
     */
    @Transactional
    public AgendamentoResponse atualizarStatus(Long id, StatusAgendamento novoStatus) {

        Agendamento agendamento = buscarOuLancarExcecao(id);
        StatusAgendamento statusAtual = agendamento.getStatus();

        // Valida as transições de status permitidas
        validarTransicaoStatus(statusAtual, novoStatus);

        agendamento.setStatus(novoStatus);
        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    // ----------------------------------------------------------------
    // CANCELAR
    // ----------------------------------------------------------------

    /**
     * Cancela um agendamento — equivale a setar status CANCELADO.
     * Agendamentos já concluídos não podem ser cancelados.
     */
    @Transactional
    public void cancelar(Long id) {

        Agendamento agendamento = buscarOuLancarExcecao(id);

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException(
                    "Agendamentos concluídos não podem ser cancelados.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException(
                    "Este agendamento já está cancelado.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    // ----------------------------------------------------------------
    // PORTAL — HORÁRIOS DISPONÍVEIS
    // ----------------------------------------------------------------

    /**
     * Calcula os horários disponíveis de um profissional para um serviço em uma data.
     * Gera slots a partir do horário de atendimento, remove os já ocupados.
     */
    @Transactional(readOnly = true)
    public List<LocalTime> buscarHorariosDisponiveis(Long profissionalId, Long servicoId, LocalDate data) {

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", profissionalId));

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", servicoId));

        LocalTime inicio = LocalTime.parse(horarioInicio);
        LocalTime fim = LocalTime.parse(horarioFim);
        int duracaoMinutos = servico.getDuracaoMinutos();

        // Busca agendamentos ativos do profissional nesse dia
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();
        List<Agendamento> agendamentosDoDia = agendamentoRepository
                .findByProfissionalIdAndData(profissionalId, inicioDia, fimDia, STATUS_INATIVOS);

        // Gera todos os slots possíveis e remove os ocupados
        List<LocalTime> slots = new ArrayList<>();
        LocalTime slot = inicio;

        while (slot.plusMinutes(duracaoMinutos).compareTo(fim) <= 0) {
            LocalDateTime slotInicio = data.atTime(slot);
            LocalDateTime slotFim = slotInicio.plusMinutes(duracaoMinutos);

            boolean ocupado = agendamentosDoDia.stream().anyMatch(a ->
                    a.getDataHora().isBefore(slotFim) && slotInicio.isBefore(a.getDataHoraFim())
            );

            // Se a data é hoje, ignora slots no passado
            if (data.equals(LocalDate.now()) && slot.isBefore(LocalTime.now())) {
                ocupado = true;
            }

            if (!ocupado) {
                slots.add(slot);
            }

            slot = slot.plusMinutes(30); // slots de 30 em 30 minutos
        }

        return slots;
    }

    // ----------------------------------------------------------------
    // PORTAL — LISTAR POR CLIENTE
    // ----------------------------------------------------------------

    /**
     * Retorna os agendamentos de um cliente, ordenados por data desc.
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findByClienteIdOrderByDataHoraDesc(clienteId)
                .stream()
                .map(agendamentoMapper::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // PORTAL — CANCELAMENTO PELO CLIENTE
    // ----------------------------------------------------------------

    /**
     * Cancela um agendamento validando posse do cliente e antecedência mínima.
     */
    @Transactional
    public void cancelarPeloCliente(Long agendamentoId, Long clienteId) {

        Agendamento agendamento = buscarOuLancarExcecao(agendamentoId);

        if (!agendamento.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Este agendamento não pertence ao cliente informado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException("Agendamentos concluídos não podem ser cancelados.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException("Este agendamento já está cancelado.");
        }

        // Valida antecedência mínima
        LocalDateTime limiteMin = agendamento.getDataHora().minusHours(antecedenciaCancelamentoHoras);
        if (LocalDateTime.now().isAfter(limiteMin)) {
            throw new BusinessException(
                    "O cancelamento deve ser feito com pelo menos " + antecedenciaCancelamentoHoras +
                            " hora(s) de antecedência.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    // ----------------------------------------------------------------
    // PORTAL — CRIAR AGENDAMENTO PELO PORTAL
    // ----------------------------------------------------------------

    /**
     * Cria um agendamento a partir dos dados do portal (IDs + dataHora).
     */
    @Transactional
    public AgendamentoResponse criarPeloPortal(Long clienteId, Long profissionalId, Long servicoId, LocalDateTime dataHora) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", profissionalId));

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", servicoId));

        LocalDateTime dataHoraFim = dataHora.plusMinutes(servico.getDuracaoMinutos());

        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                profissional.getId(), dataHora, dataHoraFim, STATUS_INATIVOS);

        if (!conflitos.isEmpty()) {
            throw new BusinessException(
                    "O profissional já possui um agendamento neste horário. " +
                            "Por favor, escolha outro horário ou profissional.");
        }

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .profissional(profissional)
                .servico(servico)
                .dataHora(dataHora)
                .dataHoraFim(dataHoraFim)
                .status(StatusAgendamento.AGENDADO)
                .build();

        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    // ----------------------------------------------------------------
    // MÉTODOS AUXILIARES
    // ----------------------------------------------------------------

    /**
     * Busca o agendamento ou lança ResourceNotFoundException.
     */
    private Agendamento buscarOuLancarExcecao(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
    }

    /**
     * Valida se a transição de status é permitida conforme o fluxo:
     * <p>
     * AGENDADO   → CONFIRMADO, CANCELADO
     * CONFIRMADO → CONCLUIDO, CANCELADO
     * CONCLUIDO  → (nenhuma transição permitida)
     * CANCELADO  → (nenhuma transição permitida)
     */
    private void validarTransicaoStatus(StatusAgendamento atual, StatusAgendamento novo) {

        boolean transicaoValida = switch (atual) {
            case AGENDADO -> novo == StatusAgendamento.CONFIRMADO
                    || novo == StatusAgendamento.CANCELADO;
            case CONFIRMADO -> novo == StatusAgendamento.CONCLUIDO
                    || novo == StatusAgendamento.CANCELADO;
            case CONCLUIDO, CANCELADO -> false;
        };

        if (!transicaoValida) {
            throw new BusinessException(
                    "Transição de status inválida: " + atual + " → " + novo + ". " +
                            "Verifique o fluxo permitido de status.");
        }
    }
}