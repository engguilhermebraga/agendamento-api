package com.guilhermebraga.agendamento_api.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de formulário para o fluxo de agendamento em etapas no portal.
 * Mantido em sessão via @SessionAttributes enquanto o cliente avança pelas etapas.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovoAgendamentoForm implements Serializable {

    // Etapa 1 — Serviço
    private Long servicoId;
    private String servicoNome;

    // Etapa 2 — Profissional
    private Long profissionalId;
    private String profissionalNome;

    // Etapa 3 — Data e Horário
    private LocalDate data;
    private LocalTime horario;

    // Dados do cliente logado
    private Long clienteId;
}
