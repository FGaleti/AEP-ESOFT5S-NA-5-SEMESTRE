package com.observaacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade principal que representa uma solicitação de serviço público.
 * Implementa máquina de estados e rastreabilidade completa.
 */
@Entity
@Table(name = "solicitacoes")
@Data
@NoArgsConstructor
public class Solicitacao {

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String protocolo;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario solicitante;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private LocalDateTime prazoEstimado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitacao_id")
    private List<HistoricoStatus> historicoStatus = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitacao_id")
    private List<Comentario> comentarios = new ArrayList<>();

    private String urlAnexo; // Caminho do arquivo anexado (se houver)

    public Solicitacao(String protocolo, Categoria categoria, String descricao,
                       String bairro, String localizacao, Prioridade prioridade,
                       Usuario solicitante) {
        this.protocolo = protocolo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.bairro = bairro;
        this.localizacao = localizacao;
        this.prioridade = prioridade;
        this.solicitante = solicitante;
        this.status = StatusSolicitacao.ABERTO;
        this.dataCriacao = LocalDateTime.now();
        this.prazoEstimado = calcularPrazo(prioridade);
        this.historicoStatus = new ArrayList<>();
        this.comentarios = new ArrayList<>();
        
        // Registra movimentação inicial
        registrarMovimentacao(null, StatusSolicitacao.ABERTO, "Sistema", "Solicitação registrada");
    }

    // ─── Máquina de estados ───

    public void avancarStatus(StatusSolicitacao novoStatus, String responsavel, String observacao) {
        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(
                    String.format("Transição inválida: %s → %s",
                            this.status.getDescricao(),
                            novoStatus.getDescricao()));
        }
        registrarMovimentacao(this.status, novoStatus, responsavel, observacao);
        this.status = novoStatus;
    }

    // ─── Histórico ───

    private void registrarMovimentacao(StatusSolicitacao anterior, StatusSolicitacao novo,
                                       String responsavel, String observacao) {
        historicoStatus.add(new HistoricoStatus(anterior, novo, responsavel, observacao));
    }

    public List<HistoricoStatus> getHistoricoStatus() {
        return Collections.unmodifiableList(historicoStatus);
    }

    // ─── Comentários ───

    public void adicionarComentario(String autor, String texto) {
        comentarios.add(new Comentario(texto, autor));
    }

    public List<Comentario> getComentarios() {
        return Collections.unmodifiableList(comentarios);
    }

    // ─── Cálculo de prazo ───

    private LocalDateTime calcularPrazo(Prioridade prioridade) {
        return dataCriacao.plusDays(prioridade.getPrazoEmDias());
    }

    // ─── Verificação de atraso ───

    public boolean isAtrasada() {
        if (status.isTerminal()) return false;
        return LocalDateTime.now().isAfter(prazoEstimado);
    }

    public long getDiasAtraso() {
        if (!isAtrasada()) return 0;
        return ChronoUnit.DAYS.between(prazoEstimado, LocalDateTime.now());
    }

    public long getDiasRestantes() {
        if (status.isTerminal()) return 0;
        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), prazoEstimado);
        return dias >= 0 ? dias : 0;
    }

    public String getStatusFormatado() {
        String statusStr = status.getDescricao();
        if (isAtrasada()) {
            return String.format("%s (ATRASADO por %d dia(s))", statusStr, getDiasAtraso());
        }
        if (!status.isTerminal()) {
            return String.format("%s (%d dia(s) restante(s))", statusStr, getDiasRestantes());
        }
        return statusStr;
    }

    private String formatarPermitidos() {
        return status.proximosPermitidos().isEmpty() ? 
            "Nenhum" : 
            status.proximosPermitidos().stream()
                .map(StatusSolicitacao::getDescricao)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Nenhum");
    }
}
