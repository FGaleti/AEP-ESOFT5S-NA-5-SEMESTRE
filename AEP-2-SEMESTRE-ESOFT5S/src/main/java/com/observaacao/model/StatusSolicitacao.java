package com.observaacao.model;

import java.util.Set;

/**
 * Máquina de estados para o status de uma solicitação.
 * Define transições válidas entre estados.
 */
public enum StatusSolicitacao {

    ABERTO("Aberto") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of(TRIAGEM, CANCELADO);
        }
    },
    TRIAGEM("Em Triagem") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of(EM_EXECUCAO, CANCELADO);
        }
    },
    EM_EXECUCAO("Em Execução") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of(RESOLVIDO, CANCELADO);
        }
    },
    RESOLVIDO("Resolvido") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of(ENCERRADO, EM_EXECUCAO);
        }
    },
    ENCERRADO("Encerrado") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of();
        }
    },
    CANCELADO("Cancelado") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of();
        }
    };

    private final String descricao;

    StatusSolicitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public abstract Set<StatusSolicitacao> proximosPermitidos();

    public boolean podeTransicionarPara(StatusSolicitacao novoStatus) {
        return proximosPermitidos().contains(novoStatus);
    }

    public boolean isTerminal() {
        return this == ENCERRADO || this == CANCELADO;
    }
}
