package com.guilhermebraga.agendamento_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um Serviço oferecido no sistema.
 * Mapeada para a tabela "servicos" no banco de dados.
 * Cada serviço pode estar associado a múltiplos agendamentos.
 *
 * @author Guilherme Braga
 */
@Entity
@Table(name = "servicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servico {

    /**
     * Identificador único gerado automaticamente pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do serviço (ex: Corte de Cabelo, Massagem Relaxante).
     */
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /**
     * Descrição detalhada do serviço oferecido.
     */
    @Column(name = "descricao", length = 255)
    private String descricao;

    /**
     * Duração do serviço em minutos.
     * Usado para calcular o horário de término do agendamento.
     */
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    /**
     * Preço do serviço.
     * Utiliza BigDecimal para garantir precisão em valores monetários.
     */
    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

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