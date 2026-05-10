package com.guilhermebraga.agendamento_api.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO mantido em sessão (@SessionAttributes("wizard")) durante o fluxo de
 * agendamento em 3 etapas no portal do cliente.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WizardForm implements Serializable {

    // Step 1 — Serviço
    private Long servicoId;
    private String servicoNome;
    private Integer servicoDuracaoMinutos;
    private BigDecimal servicoPreco;

    // Step 2 — Profissional + Data/Hora
    private Long profissionalId;
    private String profissionalNome;
    private String profissionalEspecialidade;
    private LocalDate data;
    private LocalTime hora;
}
