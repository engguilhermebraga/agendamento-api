package com.guilhermebraga.agendamento_api.mapper;

import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.dto.response.ClienteResponse;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import org.springframework.stereotype.Component;

/**
 * Converte entre a entidade Cliente e seus DTOs.
 *
 * @author Guilherme Braga
 */
@Component
public class ClienteMapper {

    /**
     * Converte ClienteRequest → entidade Cliente.
     */
    public Cliente toEntity(ClienteRequest request) {
        return Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .cpf(request.getCpf())
                .build();
    }

    /**
     * Converte entidade Cliente → ClienteResponse.
     */
    public ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .cpf(cliente.getCpf())
                .criadoEm(cliente.getCriadoEm())
                .atualizadoEm(cliente.getAtualizadoEm())
                .build();
    }

    /**
     * Atualiza entidade existente com dados do request (usado no PUT).
     */
    public void updateEntityFromRequest(ClienteRequest request, Cliente cliente) {
        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setTelefone(request.getTelefone());
        cliente.setCpf(request.getCpf());
    }
}