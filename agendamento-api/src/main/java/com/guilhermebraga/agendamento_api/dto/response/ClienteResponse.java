package com.guilhermebraga.agendamento_api.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de saída com os dados do Cliente retornados pela API.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}