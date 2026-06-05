package com.observaacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um histórico de mudança de status.
 * Rastreabilidade completa das movimentações.
 */
@Entity
@Table(name = "historico_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao statusNovo;

    @Column(nullable = false)
    private String responsavel;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao;

    public HistoricoStatus(StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo,
                          String responsavel, String observacao) {
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.responsavel = responsavel;
        this.observacao = observacao;
        this.dataMovimentacao = LocalDateTime.now();
    }
}
