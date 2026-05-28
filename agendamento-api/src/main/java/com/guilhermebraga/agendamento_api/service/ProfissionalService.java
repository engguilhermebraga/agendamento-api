package com.guilhermebraga.agendamento_api.service;

import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.entity.Profissional;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.ProfissionalMapper;
import com.guilhermebraga.agendamento_api.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço com as regras de negócio do Profissional.
 * Valida duplicidade de e-mail antes de persistir.
 *
 * @author Guilherme Braga
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalMapper profissionalMapper;

    /**
     * Cadastra novo profissional após validar duplicidade de e-mail.
     */
    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {
        log.info("Criando profissional: {}", request.getEmail());

        // Verifica duplicidade de e-mail
        if (profissionalRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Tentativa de cadastro duplicado de profissional — e-mail já existe: {}", request.getEmail());
            throw new BusinessException(
                    "Já existe um profissional cadastrado com o e-mail: " + request.getEmail());
        }

        Profissional profissional = profissionalMapper.toEntity(request);
        Profissional salvo = profissionalRepository.save(profissional);
        log.info("Profissional criado com sucesso: id={}, email={}", salvo.getId(), salvo.getEmail());
        return profissionalMapper.toResponse(salvo);
    }

    /**
     * Retorna a lista completa de profissionais cadastrados.
     */
    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listarTodos() {
        return profissionalRepository.findAll()
                .stream()
                .map(profissionalMapper::toResponse)
                .toList();
    }

    /**
     * Busca um profissional pelo ID ou lança 404.
     */
    @Transactional(readOnly = true)
    public ProfissionalResponse buscarPorId(Long id) {
        return profissionalMapper.toResponse(buscarOuLancarExcecao(id));
    }

    /**
     * Atualiza os dados de um profissional existente.
     */
    @Transactional
    public ProfissionalResponse atualizar(Long id, ProfissionalRequest request) {
        log.info("Atualizando profissional: id={}", id);

        Profissional profissional = buscarOuLancarExcecao(id);

        // Verifica se outro profissional já usa o novo e-mail
        if (profissionalRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            log.warn("Atualização de profissional id={} bloqueada — e-mail em uso: {}", id, request.getEmail());
            throw new BusinessException(
                    "Já existe outro profissional com o e-mail: " + request.getEmail());
        }

        profissionalMapper.updateEntityFromRequest(request, profissional);
        Profissional atualizado = profissionalRepository.save(profissional);
        log.info("Profissional atualizado: id={}", atualizado.getId());
        return profissionalMapper.toResponse(atualizado);
    }

    /**
     * Remove um profissional pelo ID.
     */
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando profissional: id={}", id);
        buscarOuLancarExcecao(id);
        profissionalRepository.deleteById(id);
        log.info("Profissional deletado: id={}", id);
    }

    /**
     * Método auxiliar — busca o profissional ou lança ResourceNotFoundException.
     */
    private Profissional buscarOuLancarExcecao(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
    }
}