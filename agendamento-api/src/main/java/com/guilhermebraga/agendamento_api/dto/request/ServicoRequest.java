package com.guilhermebraga.agendamento_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO de entrada para criação e atualização de Serviço.
 * Contém as validações dos dados recebidos pela API.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String descricao;

    @NotNull(message = "A duração em minutos é obrigatória.")
    @Min(value = 1, message = "A duração mínima é de 1 minuto.")
    @Max(value = 480, message = "A duração máxima é de 480 minutos (8 horas).")
    private Integer duracaoMinutos;

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço mínimo é R$ 0,01.")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos inteiros e 2 decimais.")
    private BigDecimal preco;
}