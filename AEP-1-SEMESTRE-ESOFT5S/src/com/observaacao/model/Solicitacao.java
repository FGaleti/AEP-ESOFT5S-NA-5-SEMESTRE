package com.observaacao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Solicitacao {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String protocolo;
    private Categoria categoria;
    private String descricao;
    private String localizacao;
    private Prioridade prioridade;
    private StatusSolicitacao status;
    private final Usuario solicitante;
    private Anexo anexo;
    private final LocalDateTime dataCriacao;
    private LocalDateTime prazoEstimado;
    private final List<HistoricoStatus> historicoStatus;
    private final List<Comentario> comentarios;

    public Solicitacao(String protocolo, Categoria categoria, String descricao,
                       String localizacao, Prioridade prioridade, Usuario solicitante) {
        this.protocolo = protocolo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.prioridade = prioridade;
        this.solicitante = solicitante;
        this.status = StatusSolicitacao.ABERTO;
        this.dataCriacao = LocalDateTime.now();
        this.prazoEstimado = calcularPrazo(prioridade);
        this.historicoStatus = new ArrayList<>();
        this.comentarios = new ArrayList<>();
        registrarMovimentacao(null, StatusSolicitacao.ABERTO, "Sistema", "Solicitação registrada");
    }

    // --- Máquina de estados ---

    public void avancarStatus(StatusSolicitacao novoStatus, String responsavel, String observacao) {
        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(
                    String.format("Transição inválida: %s → %s. Permitidos: %s",
                            this.status.getDescricao(),
                            novoStatus.getDescricao(),
                            formatarPermitidos()));
        }
        registrarMovimentacao(this.status, novoStatus, responsavel, observacao);
        this.status = novoStatus;
    }

    // --- Histórico ---

    private void registrarMovimentacao(StatusSolicitacao anterior, StatusSolicitacao novo,
                                        String responsavel, String observacao) {
        historicoStatus.add(new HistoricoStatus(anterior, novo, responsavel, observacao));
    }

    public List<HistoricoStatus> getHistoricoStatus() {
        return Collections.unmodifiableList(historicoStatus);
    }

    // --- Comentários ---

    public void adicionarComentario(String autor, String texto) {
        comentarios.add(new Comentario(autor, texto));
    }

    public List<Comentario> getComentarios() {
        return Collections.unmodifiableList(comentarios);
    }

    // --- Cálculo de prazo ---

    private LocalDateTime calcularPrazo(Prioridade prioridade) {
        return switch (prioridade) {
            case CRITICA -> dataCriacao.plusDays(2);
            case ALTA    -> dataCriacao.plusDays(5);
            case MEDIA   -> dataCriacao.plusDays(15);
            case BAIXA   -> dataCriacao.plusDays(30);
        };
    }

    // --- Getters ---

    public String getProtocolo()            { return protocolo; }
    public Categoria getCategoria()         { return categoria; }
    public String getDescricao()            { return descricao; }
    public String getLocalizacao()          { return localizacao; }
    public Prioridade getPrioridade()       { return prioridade; }
    public StatusSolicitacao getStatus()    { return status; }
    public Usuario getSolicitante()         { return solicitante; }
    public Anexo getAnexo()                 { return anexo; }
    public LocalDateTime getDataCriacao()   { return dataCriacao; }
    public LocalDateTime getPrazoEstimado() { return prazoEstimado; }

    // --- Setters controlados ---

    public void setCategoria(Categoria categoria)       { this.categoria = categoria; }
    public void setDescricao(String descricao)          { this.descricao = descricao; }
    public void setLocalizacao(String localizacao)      { this.localizacao = localizacao; }
    public void setAnexo(Anexo anexo)                   { this.anexo = anexo; }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
        this.prazoEstimado = calcularPrazo(prioridade);
    }

    // --- Utilidades ---

    private String formatarPermitidos() {
        var permitidos = status.proximosPermitidos();
        if (permitidos.isEmpty()) return "nenhum (status terminal)";
        return permitidos.stream()
                .map(StatusSolicitacao::getDescricao)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════╗\n");
        sb.append("║              COMPROVANTE DE SOLICITAÇÃO                 ║\n");
        sb.append("╠══════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("  Protocolo:    %s%n", protocolo));
        sb.append(String.format("  Categoria:    %s%n", categoria.getDescricao()));
        sb.append(String.format("  Descrição:    %s%n", descricao));
        sb.append(String.format("  Localização:  %s%n", localizacao));
        sb.append(String.format("  Prioridade:   %s%n", prioridade.getDescricao()));
        sb.append(String.format("  Status:       %s%n", status.getDescricao()));
        sb.append(String.format("  Criado em:    %s%n", dataCriacao.format(FORMATTER)));
        sb.append(String.format("  Prazo:        %s%n", prazoEstimado.format(FORMATTER)));
        sb.append(String.format("  Solicitante:  %s%n", solicitante.isAnonimo() ? "ANÔNIMO" : solicitante.getNome()));
        if (anexo != null) {
            sb.append(String.format("  Anexo:        %s%n", anexo.getNomeArquivo()));
        }
        sb.append("╚══════════════════════════════════════════════════════════╝");
        return sb.toString();
    }
}
