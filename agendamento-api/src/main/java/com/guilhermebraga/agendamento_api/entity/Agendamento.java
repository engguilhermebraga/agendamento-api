package com.guilhermebraga.agendamento_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa um Agendamento no sistema.
 * Mapeada para a tabela "agendamentos" no banco de dados.
 * <p>
 * Relaciona Cliente, Profissional e Serviço em uma data/hora
 * específica, controlando o status pelo enum StatusAgendamento.
 *
 * @author Guilherme Braga
 */
@Entity
@Table(name = "agendamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    /**
     * Identificador único gerado automaticamente pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente que realizou o agendamento.
     * Relacionamento ManyToOne — um cliente pode ter vários agendamentos.
     * LAZY: o cliente só é carregado quando acessado explicitamente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Profissional responsável pelo atendimento.
     * Relacionamento ManyToOne — um profissional pode ter vários agendamentos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    /**
     * Serviço a ser realizado no agendamento.
     * Relacionamento ManyToOne — um serviço pode estar em vários agendamentos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /**
     * Data e hora de início do atendimento.
     * Usado em conjunto com a duração do serviço para detectar conflitos.
     */
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /**
     * Status atual do agendamento.
     * Armazenado como String no banco para facilitar leitura.
     * Valor padrão: AGENDADO.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusAgendamento status;

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

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    /**
     * Executado antes do INSERT — define status inicial e datas.
     */
    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        // Define AGENDADO como status padrão se não informado
        if (this.status == null) {
            this.status = StatusAgendamento.AGENDADO;
        }
    }

    /**
     * Executado antes do UPDATE — atualiza a data de modificação.
     */
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}