package com.guilhermebraga.agendamento_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa um Cliente no sistema.
 * Mapeada para a tabela "clientes" no banco de dados.
 *
 * @author Guilherme Braga
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do cliente.
     */
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /**
     * E-mail único do cliente.
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Telefone para contato.
     */
    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    /**
     * CPF do cliente — somente números, único no sistema.
     */
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    /**
     * Data e hora de criação do registro.
     */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Data e hora da última atualização.
     */
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    /**
     * Preenchido automaticamente antes do INSERT.
     */
    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Atualizado automaticamente antes do UPDATE.
     */
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}