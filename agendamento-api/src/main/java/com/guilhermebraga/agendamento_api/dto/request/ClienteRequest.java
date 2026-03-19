package com.guilhermebraga.agendamento_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO de entrada para criação e atualização de Cliente.
 * Contém as validações dos dados recebidos pela API.
 *
 * @author Guilherme Braga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "O telefone é obrigatório.")
    @Size(min = 10, max = 20, message = "O telefone deve ter entre 10 e 20 caracteres.")
    private String telefone;

    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos.")
    private String cpf;
}