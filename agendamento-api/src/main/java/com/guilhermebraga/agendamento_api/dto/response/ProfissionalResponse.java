package com.guilhermebraga.agendamento_api.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de saída com os dados do Profissional retornados pela API.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfissionalResponse {

    private Long id;
    private String nome;
    private String especialidade;
    private String email;
    private String telefone;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}