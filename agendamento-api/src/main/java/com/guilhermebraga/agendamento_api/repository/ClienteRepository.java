package com.guilhermebraga.agendamento_api.repository;

import com.guilhermebraga.agendamento_api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de acesso aos dados da entidade Cliente.
 * Métodos customizados gerados automaticamente pelo Spring Data JPA.
 *
 * @author Guilherme Braga
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca cliente pelo e-mail — verifica duplicidade no cadastro.
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca cliente pelo CPF — verifica duplicidade no cadastro.
     */
    Optional<Cliente> findByCpf(String cpf);

    /**
     * Verifica se outro cliente já usa esse e-mail (ignora o próprio ID).
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Verifica se outro cliente já usa esse CPF (ignora o próprio ID).
     */
    boolean existsByCpfAndIdNot(String cpf, Long id);
}