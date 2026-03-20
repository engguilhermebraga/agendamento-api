package com.guilhermebraga.agendamento_api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de saída com os dados do Serviço retornados pela API.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private Integer duracaoMinutos;
    private BigDecimal preco;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
