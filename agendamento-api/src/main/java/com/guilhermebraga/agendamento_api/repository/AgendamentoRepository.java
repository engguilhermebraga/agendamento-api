package com.guilhermebraga.agendamento_api.repository;

import com.guilhermebraga.agendamento_api.entity.Agendamento;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório de acesso aos dados da entidade Agendamento.
 *
 * @author Guilherme Braga
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Verifica conflito de horário para um profissional.
     * <p>
     * Busca agendamentos ativos do profissional cujo intervalo
     * [dataHora, dataHoraFim] se sobrepõe ao horário solicitado.
     * <p>
     * A lógica de término é calculada no Service antes de chamar
     * este método, passando dataHoraFim já calculada.
     * <p>
     * Dois intervalos [A, B] e [C, D] se sobrepõem quando: A < D e B > C
     *
     * @param profissionalId  ID do profissional
     * @param dataHoraInicio  início do novo agendamento
     * @param dataHoraFim     fim do novo agendamento (calculado no Service)
     * @param statusIgnorados status a ignorar (CANCELADO e CONCLUIDO)
     * @return lista de agendamentos conflitantes
     */
    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.profissional.id = :profissionalId
            AND a.status NOT IN :statusIgnorados
            AND a.dataHora < :dataHoraFim
            AND :dataHoraInicio < a.dataHoraFim
            """)
    List<Agendamento> findConflitosHorario(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("statusIgnorados") List<StatusAgendamento> statusIgnorados
    );

    /**
     * Verifica conflito de horário excluindo o próprio agendamento.
     * Usado na validação do fluxo de atualização (PUT).
     */
    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.profissional.id = :profissionalId
            AND a.id <> :agendamentoId
            AND a.status NOT IN :statusIgnorados
            AND a.dataHora < :dataHoraFim
            AND :dataHoraInicio < a.dataHoraFim
            """)
    List<Agendamento> findConflitosHorarioExcluindoId(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("statusIgnorados") List<StatusAgendamento> statusIgnorados,
            @Param("agendamentoId") Long agendamentoId
    );
}