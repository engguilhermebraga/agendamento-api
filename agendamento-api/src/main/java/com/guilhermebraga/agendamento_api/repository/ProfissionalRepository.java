package com.guilhermebraga.agendamento_api.repository;

import com.guilhermebraga.agendamento_api.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de acesso aos dados da entidade Profissional.
 * Métodos customizados gerados automaticamente pelo Spring Data JPA.
 *
 * @author Guilherme Braga
 */
@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    /**
     * Busca profissional pelo e-mail — verifica duplicidade no cadastro.
     */
    Optional<Profissional> findByEmail(String email);

    /**
     * Verifica se outro profissional já usa esse e-mail (usado no PUT).
     */
    boolean existsByEmailAndIdNot(String email, Long id);
}