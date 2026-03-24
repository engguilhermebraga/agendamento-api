package com.guilhermebraga.agendamento_api.entity;

/**
 * Enum que representa o ciclo de vida de um Agendamento.
 * <p>
 * Fluxo permitido:
 * AGENDADO → CONFIRMADO → CONCLUIDO
 * AGENDADO → CANCELADO
 * CONFIRMADO → CANCELADO
 *
 * @author Guilherme Braga
 */
public enum StatusAgendamento {

    /**
     * Agendamento criado, aguardando confirmação.
     */
    AGENDADO,

    /**
     * Agendamento confirmado pelo profissional ou sistema.
     */
    CONFIRMADO,

    /**
     * Serviço realizado com sucesso.
     */
    CONCLUIDO,

    /**
     * Agendamento cancelado pelo cliente ou profissional.
     */
    CANCELADO
}