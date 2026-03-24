package com.guilhermebraga.agendamento_api.dto.request;

import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de entrada para criação e atualização de Agendamento.
 * Recebe apenas os IDs das entidades relacionadas.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoRequest {

    @NotNull(message = "O ID do cliente é obrigatório.")
    private Long clienteId;

    @NotNull(message = "O ID do profissional é obrigatório.")
    private Long profissionalId;

    @NotNull(message = "O ID do serviço é obrigatório.")
    private Long servicoId;

    @NotNull(message = "A data e hora são obrigatórias.")
    @Future(message = "O agendamento deve ser para uma data futura.")
    private LocalDateTime dataHora;

    // Status é opcional na criação — padrão AGENDADO definido no @PrePersist
    private StatusAgendamento status;
}