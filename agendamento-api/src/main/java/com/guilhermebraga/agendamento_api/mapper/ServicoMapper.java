package com.guilhermebraga.agendamento_api.mapper;

import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.entity.Servico;
import org.springframework.stereotype.Component;

/**
 * Converte entre a entidade Serviço e seus DTOs.
 *
 * @author Guilherme Braga
 */
@Component
public class ServicoMapper {

    /**
     * Converte ServicoRequest → entidade Servico.
     */
    public Servico toEntity(ServicoRequest request) {
        return Servico.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .duracaoMinutos(request.getDuracaoMinutos())
                .preco(request.getPreco())
                .build();
    }

    /**
     * Converte entidade Servico → ServicoResponse.
     */
    public ServicoResponse toResponse(Servico servico) {
        return ServicoResponse.builder()
                .id(servico.getId())
                .nome(servico.getNome())
                .descricao(servico.getDescricao())
                .duracaoMinutos(servico.getDuracaoMinutos())
                .preco(servico.getPreco())
                .criadoEm(servico.getCriadoEm())
                .atualizadoEm(servico.getAtualizadoEm())
                .build();
    }

    /**
     * Atualiza entidade existente com dados do request (usado no PUT).
     */
    public void updateEntityFromRequest(ServicoRequest request, Servico servico) {
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setDuracaoMinutos(request.getDuracaoMinutos());
        servico.setPreco(request.getPreco());
    }
}