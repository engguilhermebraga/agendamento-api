package com.guilhermebraga.agendamento_api.mapper;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.Agendamento;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.entity.Profissional;
import com.guilhermebraga.agendamento_api.entity.Servico;
import org.springframework.stereotype.Component;

/**
 * Converte entre a entidade Agendamento e seus DTOs.
 * <p>
 * O Mapper do Agendamento recebe as entidades relacionadas
 * como parâmetro, pois a conversão depende de dados de
 * Cliente, Profissional e Serviço já carregados pelo Service.
 *
 * @author Guilherme Braga
 */
@Component
public class AgendamentoMapper {

    /**
     * Converte AgendamentoRequest → entidade Agendamento.
     * Recebe as entidades relacionadas já validadas pelo Service.
     */
    public Agendamento toEntity(AgendamentoRequest request,
                                Cliente cliente,
                                Profissional profissional,
                                Servico servico) {
        return Agendamento.builder()
                .cliente(cliente)
                .profissional(profissional)
                .servico(servico)
                .dataHora(request.getDataHora())
                // Calcula e persiste o horário de término
                .dataHoraFim(request.getDataHora()
                        .plusMinutes(servico.getDuracaoMinutos()))
                .status(request.getStatus())
                .build();
    }

    /**
     * Converte entidade Agendamento → AgendamentoResponse.
     * Expõe dados resumidos das entidades relacionadas.
     */
    public AgendamentoResponse toResponse(Agendamento agendamento) {
        return AgendamentoResponse.builder()
                .id(agendamento.getId())
                .clienteId(agendamento.getCliente().getId())
                .clienteNome(agendamento.getCliente().getNome())
                .profissionalId(agendamento.getProfissional().getId())
                .profissionalNome(agendamento.getProfissional().getNome())
                .profissionalEspecialidade(agendamento.getProfissional().getEspecialidade())
                .servicoId(agendamento.getServico().getId())
                .servicoNome(agendamento.getServico().getNome())
                .servicoDuracaoMinutos(agendamento.getServico().getDuracaoMinutos())
                .dataHora(agendamento.getDataHora())
                .status(agendamento.getStatus())
                .criadoEm(agendamento.getCriadoEm())
                .atualizadoEm(agendamento.getAtualizadoEm())
                .build();
    }

    /**
     * Atualiza entidade existente com dados do request (usado no PUT).
     */
    public void updateEntityFromRequest(AgendamentoRequest request,
                                        Agendamento agendamento,
                                        Cliente cliente,
                                        Profissional profissional,
                                        Servico servico) {
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDataHora(request.getDataHora());
        // Recalcula o horário de término com o novo serviço/horário
        agendamento.setDataHoraFim(request.getDataHora()
                .plusMinutes(servico.getDuracaoMinutos()));
        if (request.getStatus() != null) {
            agendamento.setStatus(request.getStatus());
        }
    }
}


