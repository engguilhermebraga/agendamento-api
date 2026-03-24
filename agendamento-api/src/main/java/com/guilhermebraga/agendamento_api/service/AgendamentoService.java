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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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