package com.guilhermebraga.agendamento_api.mapper;

import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.entity.Profissional;
import org.springframework.stereotype.Component;

/**
 * Converte entre a entidade Profissional e seus DTOs.
 *
 * @author Guilherme Braga
 */
@Component
public class ProfissionalMapper {

    /**
     * Converte ProfissionalRequest → entidade Profissional.
     */
    public Profissional toEntity(ProfissionalRequest request) {
        return Profissional.builder()
                .nome(request.getNome())
                .especialidade(request.getEspecialidade())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .build();
    }

    /**
     * Converte entidade Profissional → ProfissionalResponse.
     */
    public ProfissionalResponse toResponse(Profissional profissional) {
        return ProfissionalResponse.builder()
                .id(profissional.getId())
                .nome(profissional.getNome())
                .especialidade(profissional.getEspecialidade())
                .email(profissional.getEmail())
                .telefone(profissional.getTelefone())
                .criadoEm(profissional.getCriadoEm())
                .atualizadoEm(profissional.getAtualizadoEm())
                .build();
    }

    /**
     * Atualiza entidade existente com dados do request (usado no PUT).
     */
    public void updateEntityFromRequest(ProfissionalRequest request, Profissional profissional) {
        profissional.setNome(request.getNome());
        profissional.setEspecialidade(request.getEspecialidade());
        profissional.setEmail(request.getEmail());
        profissional.setTelefone(request.getTelefone());
    }
}