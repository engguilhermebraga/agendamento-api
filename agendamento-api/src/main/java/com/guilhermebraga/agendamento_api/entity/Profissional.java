package com.guilhermebraga.agendamento_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa um Profissional no sistema.
 * Mapeada para a tabela "profissionais" no banco de dados.
 * Cada profissional pode possuir múltiplos agendamentos.
 *
 * @author Guilherme Braga
 */
@Entity
@Table(name = "profissionais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profissional {

    /**
     * Identificador único gerado automaticamente pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do profissional.
     */
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /**
     * Especialidade do profissional (ex: Cabeleireiro, Fisioterapeuta).
     */
    @Column(name = "especialidade", nullable = false, length = 100)
    private String especialidade;

    /**
     * E-mail único do profissional para contato.
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Telefone do profissional para contato.
     */
    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    /**
     * Data e hora de criação do registro — preenchido automaticamente.
     */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Data e hora da última atualização — atualizado automaticamente.
     */
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    /**
     * Executado antes do INSERT — define as datas de criação.
     */
    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Executado antes do UPDATE — atualiza a data de modificação.
     */
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}