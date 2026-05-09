package com.guilhermebraga.agendamento_api.service;

import com.guilhermebraga.agendamento_api.dto.request.ServicoRequest;
import com.guilhermebraga.agendamento_api.dto.response.ServicoResponse;
import com.guilhermebraga.agendamento_api.entity.Servico;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.ServicoMapper;
import com.guilhermebraga.agendamento_api.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço com as regras de negócio do Serviço.
 * Valida duplicidade de nome antes de persistir.
 *
 * @author Guilherme Braga
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    /**
     * Cadastra novo serviço após validar duplicidade de nome.
     */
    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        log.info("Criando serviço: {}", request.getNome());

        // Verifica duplicidade de nome
        if (servicoRepository.findByNome(request.getNome()).isPresent()) {
            log.warn("Tentativa de cadastro duplicado de serviço — nome já existe: {}", request.getNome());
            throw new BusinessException(
                    "Já existe um serviço cadastrado com o nome: " + request.getNome());
        }

        Servico servico = servicoMapper.toEntity(request);
        Servico salvo = servicoRepository.save(servico);
        log.info("Serviço criado com sucesso: id={}, nome={}", salvo.getId(), salvo.getNome());
        return servicoMapper.toResponse(salvo);
    }

    /**
     * Retorna a lista completa de serviços cadastrados.
     */
    @Transactional(readOnly = true)
    public List<ServicoResponse> listarTodos() {
        return servicoRepository.findAll()
                .stream()
                .map(servicoMapper::toResponse)
                .toList();
    }

    /**
     * Busca um serviço pelo ID ou lança 404.
     */
    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(Long id) {
        return servicoMapper.toResponse(buscarOuLancarExcecao(id));
    }

    /**
     * Atualiza os dados de um serviço existente.
     */
    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        log.info("Atualizando serviço: id={}", id);

        Servico servico = buscarOuLancarExcecao(id);

        // Verifica se outro serviço já usa o novo nome
        if (servicoRepository.existsByNomeAndIdNot(request.getNome(), id)) {
            log.warn("Atualização de serviço id={} bloqueada — nome em uso: {}", id, request.getNome());
            throw new BusinessException(
                    "Já existe outro serviço com o nome: " + request.getNome());
        }

        servicoMapper.updateEntityFromRequest(request, servico);
        Servico atualizado = servicoRepository.save(servico);
        log.info("Serviço atualizado: id={}", atualizado.getId());
        return servicoMapper.toResponse(atualizado);
    }

    /**
     * Remove um serviço pelo ID.
     */
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando serviço: id={}", id);
        buscarOuLancarExcecao(id);
        servicoRepository.deleteById(id);
        log.info("Serviço deletado: id={}", id);
    }

    /**
     * Método auxiliar — busca o serviço ou lança ResourceNotFoundException.
     */
    private Servico buscarOuLancarExcecao(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
    }
}