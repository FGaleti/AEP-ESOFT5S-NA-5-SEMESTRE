package com.observaacao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class HistoricoStatus {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final StatusSolicitacao statusAnterior;
    private final StatusSolicitacao statusNovo;
    private final String responsavel;
    private final String observacao;
    private final LocalDateTime dataAlteracao;

    public HistoricoStatus(StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo,
                           String responsavel, String observacao) {
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.responsavel = responsavel;
        this.observacao = (observacao != null) ? observacao : "";
        this.dataAlteracao = LocalDateTime.now();
    }

    public StatusSolicitacao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSolicitacao getStatusNovo() {
        return statusNovo;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(dataAlteracao.format(FORMATTER)).append("] ");

        if (statusAnterior == null) {
            sb.append("● Criação → ").append(statusNovo.getDescricao());
        } else {
            sb.append(statusAnterior.getDescricao()).append(" → ").append(statusNovo.getDescricao());
        }

        sb.append(" | Por: ").append(responsavel);

        if (!observacao.isEmpty()) {
            sb.append(" | Obs: ").append(observacao);
        }

        return sb.toString();
    }
}
