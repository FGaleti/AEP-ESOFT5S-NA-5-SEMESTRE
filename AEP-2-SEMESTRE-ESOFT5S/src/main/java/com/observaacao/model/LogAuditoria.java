package com.observaacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um log de auditoria.
 * Rastreia todas as operações do sistema para segurança e conformidade.
 */
@Entity
@Table(name = "log_auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoria {

    public enum TipoEvento {
        CADASTRO,
        ATUALIZACAO_STATUS,
        ADICIONAR_COMENTARIO,
        CONSULTA,
        TENTATIVA_ABUSO,
        ACESSO_ANONIMATO,
        OUTROS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipoEvento;

    private String protocolo;

    @Column(nullable = false)
    private String ator;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private boolean envolveAnonimato;

    public LogAuditoria(TipoEvento tipoEvento, String protocolo, String ator,
                        String descricao, boolean envolveAnonimato) {
        this.tipoEvento = tipoEvento;
        this.protocolo = protocolo;
        this.ator = ator;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
        this.envolveAnonimato = envolveAnonimato;
    }
}
