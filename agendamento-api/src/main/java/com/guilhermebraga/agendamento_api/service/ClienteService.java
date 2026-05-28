package com.guilhermebraga.agendamento_api.service;

import com.guilhermebraga.agendamento_api.dto.request.ClienteRequest;
import com.guilhermebraga.agendamento_api.dto.response.ClienteResponse;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.ClienteMapper;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de serviço com as regras de negócio do Cliente.
 * Valida duplicidade de CPF e e-mail antes de persistir.
 *
 * @author Guilherme Braga
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cadastra novo cliente após validar duplicidade de CPF e e-mail.
     */
    @Transactional
    public ClienteResponse criar(ClienteRequest request) {

        // Verifica duplicidade de e-mail
        if (clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(
                    "Já existe um cliente cadastrado com o e-mail: " + request.getEmail());
        }

        // Verifica duplicidade de CPF
        if (clienteRepository.findByCpf(request.getCpf()).isPresent()) {
            throw new BusinessException(
                    "Já existe um cliente cadastrado com o CPF: " + request.getCpf());
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente salvo = clienteRepository.save(cliente);
        return clienteMapper.toResponse(salvo);
    }

    /**
     * Retorna a lista completa de clientes cadastrados.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    /**
     * Busca um cliente pelo ID ou lança 404.
     */
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarOuLancarExcecao(id));
    }

    /**
     * Atualiza os dados de um cliente existente.
     */
    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {

        Cliente cliente = buscarOuLancarExcecao(id);

        // Verifica se outro cliente já usa o novo e-mail
        if (clienteRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException(
                    "Já existe outro cliente com o e-mail: " + request.getEmail());
        }

        // Verifica se outro cliente já usa o novo CPF
        if (clienteRepository.existsByCpfAndIdNot(request.getCpf(), id)) {
            throw new BusinessException(
                    "Já existe outro cliente com o CPF: " + request.getCpf());
        }

        clienteMapper.updateEntityFromRequest(request, cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    /**
     * Remove um cliente pelo ID.
     */
    @Transactional
    public void deletar(Long id) {
        buscarOuLancarExcecao(id);
        clienteRepository.deleteById(id);
    }

    /**
     * Autentica por e-mail + senha (BCrypt). Retorna null se inválido.
     */
    @Transactional(readOnly = true)
    public Cliente autenticarPorEmailESenha(String email, String senha) {
        return clienteRepository.findByEmail(email)
                .filter(c -> c.getSenha() != null && passwordEncoder.matches(senha, c.getSenha()))
                .orElse(null);
    }

    /**
     * Cadastra um novo cliente a partir dos dados básicos (usado no portal).
     * Recebe a senha em texto claro e a armazena hasheada com BCrypt.
     */
    @Transactional
    public Cliente criarPeloPortal(String nome, String email, String telefone, String cpf, String senha) {

        if (clienteRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(
                    "Já existe um cliente cadastrado com o e-mail: " + email);
        }

        if (clienteRepository.findByCpf(cpf).isPresent()) {
            throw new BusinessException(
                    "Já existe um cliente cadastrado com o CPF: " + cpf);
        }

        Cliente cliente = Cliente.builder()
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .cpf(cpf)
                .senha(passwordEncoder.encode(senha))
                .build();

        return clienteRepository.save(cliente);
    }

    /**
     * Método auxiliar — busca o cliente ou lança ResourceNotFoundException.
     */
    private Cliente buscarOuLancarExcecao(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}