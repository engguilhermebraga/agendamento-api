package com.guilhermebraga.agendamento_api.repository;

import com.guilhermebraga.agendamento_api.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de acesso aos dados da entidade Serviço.
 * Métodos customizados gerados automaticamente pelo Spring Data JPA.
 *
 * @author Guilherme Braga
 */
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    /**
     * Busca um serviço pelo nome — verifica duplicidade no cadastro.
     * Dois serviços não podem ter o mesmo nome no sistema.
     */
    Optional<Servico> findByNome(String nome);

    /**
     * Verifica se outro serviço já usa esse nome (ignora o próprio ID).
     * Usado na validação do fluxo de atualização (PUT).
     */
    boolean existsByNomeAndIdNot(String nome, Long id);
}