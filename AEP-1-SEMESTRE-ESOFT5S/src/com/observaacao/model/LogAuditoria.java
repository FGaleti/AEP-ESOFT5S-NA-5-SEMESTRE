package com.observaacao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro imutável de auditoria para prevenção de abuso e rastreabilidade.
 * Cada ação relevante no sistema gera uma entrada de log que não pode ser alterada,
 * garantindo conformidade com ODS 16 (transparência e prestação de contas).
 *
 * Tipos de evento:
 * - CADASTRO: nova solicitação criada
 * - TRANSICAO_STATUS: mudança de estado
 * - COMENTARIO: comentário registrado
 * - TENTATIVA_ABUSO: ação bloqueada por regra de prevenção
 * - CONSULTA: acesso a dados de solicitação
 */
public final class LogAuditoria {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public enum TipoEvento {
        CADASTRO("Cadastro"),
        TRANSICAO_STATUS("Transição de Status"),
        COMENTARIO("Comentário"),
        TENTATIVA_ABUSO("Tentativa de Abuso"),
        CONSULTA("Consulta");

        private final String descricao;

        TipoEvento(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private final TipoEvento tipo;
    private final String protocolo;
    private final String ator;
    private final String detalhes;
    private final boolean anonimo;
    private final LocalDateTime dataEvento;

    public LogAuditoria(TipoEvento tipo, String protocolo, String ator,
                        String detalhes, boolean anonimo) {
        this.tipo = tipo;
        this.protocolo = protocolo;
        this.ator = ator;
        this.detalhes = detalhes;
        this.anonimo = anonimo;
        this.dataEvento = LocalDateTime.now();
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public String getAtor() {
        return ator;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public boolean isAnonimo() {
        return anonimo;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-22s | Proto: %-20s | Ator: %-15s | Anônimo: %-3s | %s",
                dataEvento.format(FORMATTER),
                tipo.getDescricao(),
                protocolo != null ? protocolo : "N/A",
                anonimo ? "***" : ator,
                anonimo ? "Sim" : "Não",
                detalhes);
    }
}
