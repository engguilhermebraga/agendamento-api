package com.guilhermebraga.agendamento_api.repository;

import com.guilhermebraga.agendamento_api.entity.Agendamento;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade Agendamento.
 * Fornece operações de persistência e consultas customizadas.
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Encontra agendamentos por ID do cliente.
     * 
     * @param clienteId ID do cliente
     * @return Lista de agendamentos do cliente
     */
    List<Agendamento> findByClienteId(Long clienteId);

    /**
     * Encontra agendamentos por ID do profissional.
     * 
     * @param profissionalId ID do profissional
     * @return Lista de agendamentos do profissional
     */
    List<Agendamento> findByProfissionalId(Long profissionalId);

    /**
     * Encontra agendamentos por ID do serviço.
     * 
     * @param servicoId ID do serviço
     * @return Lista de agendamentos do serviço
     */
    List<Agendamento> findByServicoId(Long servicoId);

    /**
     * Encontra agendamentos por status.
     * 
     * @param status Status do agendamento
     * @return Lista de agendamentos com o status fornecido
     */
    List<Agendamento> findByStatus(StatusAgendamento status);

    /**
     * Encontra agendamentos de um cliente ordenados por data/hora descendente.
     *
     * @param clienteId ID do cliente
     * @return Lista de agendamentos do cliente ordenada por data/hora (mais recente primeiro)
     */
    List<Agendamento> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    /**
     * Encontra agendamentos dentro de um intervalo de datas.
     * 
     * @param dataInicio Data/hora de início
     * @param dataFim Data/hora de fim
     * @return Lista de agendamentos no intervalo
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.dataHora BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findByDataInterval(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Encontra agendamentos de um profissional que se sobrepõem ao intervalo informado,
     * filtrados pelos status fornecidos (deve receber os status ativos: AGENDADO, CONFIRMADO).
     *
     * @param profissionalId ID do profissional
     * @param dataHoraInicio Data/hora de início do intervalo
     * @param dataHoraFim    Data/hora de fim do intervalo
     * @param statusList     Status ativos a considerar (ex: AGENDADO, CONFIRMADO)
     * @return Lista de agendamentos que se sobrepõem ao intervalo
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.profissional.id = :profissionalId " +
           "AND a.dataHora < :dataHoraFim " +
           "AND a.dataHoraFim > :dataHoraInicio " +
           "AND a.status IN :statusList")
    List<Agendamento> findConflitosHorario(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("statusList") List<StatusAgendamento> statusList
    );

    /**
     * Igual a {@link #findConflitosHorario}, mas exclui um agendamento específico da busca.
     * Usado ao atualizar um agendamento para que ele não conflite consigo mesmo.
     *
     * @param profissionalId       ID do profissional
     * @param dataHoraInicio       Data/hora de início do intervalo
     * @param dataHoraFim          Data/hora de fim do intervalo
     * @param statusList           Status ativos a considerar (ex: AGENDADO, CONFIRMADO)
     * @param agendamentoIdExcluir ID do agendamento a ignorar na busca
     * @return Lista de agendamentos conflitantes (excluindo o ID fornecido)
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.profissional.id = :profissionalId " +
           "AND a.dataHora < :dataHoraFim " +
           "AND a.dataHoraFim > :dataHoraInicio " +
           "AND a.status IN :statusList " +
           "AND a.id != :agendamentoIdExcluir")
    List<Agendamento> findConflitosHorarioExcluindoId(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("statusList") List<StatusAgendamento> statusList,
            @Param("agendamentoIdExcluir") Long agendamentoIdExcluir
    );
}
