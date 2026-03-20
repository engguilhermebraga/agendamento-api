package com.guilhermebraga.agendamento_api.service;

import com.guilhermebraga.agendamento_api.dto.request.ProfissionalRequest;
import com.guilhermebraga.agendamento_api.dto.response.ProfissionalResponse;
import com.guilhermebraga.agendamento_api.entity.Profissional;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.ProfissionalMapper;
import com.guilhermebraga.agendamento_api.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalMapper profissionalMapper;

    /**
     * Cadastra novo profissional após validar duplicidade de e-mail.
     */
    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {

        // Verifica duplicidade de e-mail
        if (profissionalRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(
                    "Já existe um profissional cadastrado com o e-mail: " + request.getEmail());
        }

        Profissional profissional = profissionalMapper.toEntity(request);
        Profissional salvo = profissionalRepository.save(profissional);
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

        Profissional profissional = buscarOuLancarExcecao(id);

        // Verifica se outro profissional já usa o novo e-mail
        if (profissionalRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException(
                    "Já existe outro profissional com o e-mail: " + request.getEmail());
        }

        profissionalMapper.updateEntityFromRequest(request, profissional);
        return profissionalMapper.toResponse(profissionalRepository.save(profissional));
    }

    /**
     * Remove um profissional pelo ID.
     */
    @Transactional
    public void deletar(Long id) {
        buscarOuLancarExcecao(id);
        profissionalRepository.deleteById(id);
    }

    /**
     * Método auxiliar — busca o profissional ou lança ResourceNotFoundException.
     */
    private Profissional buscarOuLancarExcecao(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
    }
}