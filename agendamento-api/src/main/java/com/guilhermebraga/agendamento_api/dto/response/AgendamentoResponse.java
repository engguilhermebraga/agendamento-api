package com.guilhermebraga.agendamento_api.dto.response;

import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de saída com os dados completos do Agendamento.
 * Expõe os dados resumidos de Cliente, Profissional e Serviço
 * sem precisar retornar as entidades completas.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoResponse {

    private Long id;

    // Dados resumidos do Cliente
    private Long clienteId;
    private String clienteNome;

    // Dados resumidos do Profissional
    private Long profissionalId;
    private String profissionalNome;
    private String profissionalEspecialidade;

    // Dados resumidos do Serviço
    private Long servicoId;
    private String servicoNome;
    private Integer servicoDuracaoMinutos;

    private LocalDateTime dataHora;
    private StatusAgendamento status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}