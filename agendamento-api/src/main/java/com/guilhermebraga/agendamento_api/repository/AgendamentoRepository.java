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
     * Encontra agendamentos de um profissional dentro de um intervalo de tempo,
     * filtrados por uma lista de status específicos.
     * 
     * Utiliza JPQL para detectar conflitos/sobreposições de horários,
     * considerando dataHoraFim para verificar a disponibilidade.
     * 
     * @param profissionalId ID do profissional
     * @param dataHoraInicio Data/hora de início do intervalo
     * @param dataHoraFim Data/hora de fim do intervalo
     * @param statusList Lista de status a filtrar (ex: CONFIRMADO, PENDENTE)
     * @return Lista de agendamentos que se sobrepõem ao intervalo
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.profissional.id = :profissionalId " +
           "AND a.dataHora < :dataHoraFim " +
           "AND a.dataHoraFim > :dataHoraInicio " +
           "AND a.status IN :statusList")
    List<Agendamento> findByProfissionalIdAndData(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("statusList") List<StatusAgendamento> statusList
    );

    /**
     * Encontra agendamentos de um cliente ordenados por data/hora descendente.
     * 
     * @param clienteId ID do cliente
     * @return Lista de agendamentos do cliente ordenada por data/hora (mais recente primeiro)
     */
    List<Agendamento> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    /**
     * Verifica se existe um agendamento para um profissional em um intervalo específico.
     * 
     * @param profissionalId ID do profissional
     * @param dataHora Data/hora do agendamento
     * @param dataHoraFim Data/hora de fim do agendamento
     * @return true se existe conflito, false caso contrário
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Agendamento a " +
           "WHERE a.profissional.id = :profissionalId " +
           "AND a.dataHora < :dataHoraFim " +
           "AND a.dataHoraFim > :dataHora " +
           "AND a.status IN ('CONFIRMADO', 'PENDENTE')")
    boolean existsConflict(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHora") LocalDateTime dataHora,
            @Param("dataHoraFim") LocalDateTime dataHoraFim
    );

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
     * Encontra conflitos de horário para um profissional em um intervalo de tempo.
     * 
     * Verifica se existe algum agendamento que se sobrepõe ao horário solicitado,
     * levando em conta apenas agendamentos com status CONFIRMADO ou PENDENTE.
     * 
     * @param profissionalId ID do profissional
     * @param dataHoraInicio Data/hora de início do agendamento desejado
     * @param dataHoraFim Data/hora de fim do agendamento desejado
     * @param statusList Lista de status a considerar (ex: CONFIRMADO, PENDENTE)
     * @return Lista de agendamentos conflitantes
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
     * Encontra conflitos de horário excluindo um agendamento específico.
     * 
     * Útil para validação ao atualizar um agendamento existente,
     * evitando que o agendamento sendo editado seja considerado como conflito.
     * 
     * @param profissionalId ID do profissional
     * @param dataHoraInicio Data/hora de início do agendamento desejado
     * @param dataHoraFim Data/hora de fim do agendamento desejado
     * @param statusList Lista de status a considerar (ex: CONFIRMADO, PENDENTE)
     * @param agendamentoIdExcluir ID do agendamento a excluir da busca
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
