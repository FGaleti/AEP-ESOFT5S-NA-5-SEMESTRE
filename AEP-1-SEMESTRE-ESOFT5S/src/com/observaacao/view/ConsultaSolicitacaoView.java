package com.observaacao.view;

import com.observaacao.model.Comentario;
import com.observaacao.model.HistoricoStatus;
import com.observaacao.model.Solicitacao;
import com.observaacao.service.SolicitacaoService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsultaSolicitacaoView {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SolicitacaoService service;
    private final Scanner scanner;

    public ConsultaSolicitacaoView(SolicitacaoService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    // ─── Menu de acompanhamento do cidadão ───

    public void exibirMenuAcompanhamento() {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║       ACOMPANHAMENTO PELO CIDADÃO               ║");
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.println("║  [1] Buscar por protocolo (visão completa)      ║");
            System.out.println("║  [2] Ver prazos e justificativas de atraso      ║");
            System.out.println("║  [0] Voltar ao menu principal                   ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            System.out.print("Opção: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> exibirConsultaCompleta();
                case "2" -> exibirConsultaPrazo();
                case "0" -> continuar = false;
                default -> System.out.println("\nOpção inválida. Tente novamente.");
            }
        }
    }

    // ─── Consulta completa por protocolo ───

    public void exibirConsultaCompleta() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║      CONSULTAR SOLICITAÇÃO POR PROTOCOLO        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.print("Digite o número do protocolo: ");
        String protocolo = scanner.nextLine().trim();

        try {
            Solicitacao solicitacao = service.consultarPorProtocolo(protocolo);
            System.out.println();
            System.out.println(solicitacao);

            exibirSituacaoPrazo(solicitacao);
            exibirHistoricoStatus(solicitacao.getHistoricoStatus());
            exibirComentarios(solicitacao.getComentarios());
        } catch (IllegalArgumentException e) {
            System.out.println("\n✘ " + e.getMessage());
        }
    }

    // ─── Consulta focada em prazos e justificativas ───

    public void exibirConsultaPrazo() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║        CONSULTA DE PRAZOS E JUSTIFICATIVAS      ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.print("Digite o número do protocolo: ");
        String protocolo = scanner.nextLine().trim();

        try {
            Solicitacao solicitacao = service.consultarPorProtocolo(protocolo);
            System.out.println();

            System.out.println("══════════════════════════════════════════════════════════");
            System.out.printf("  Protocolo:    %s%n", solicitacao.getProtocolo());
            System.out.printf("  Categoria:    %s%n", solicitacao.getCategoria().getDescricao());
            System.out.printf("  Status:       %s%n", solicitacao.getStatus().getDescricao());
            System.out.printf("  Prioridade:   %s%n", solicitacao.getPrioridade().getDescricao());
            System.out.printf("  Criado em:    %s%n", solicitacao.getDataCriacao().format(FORMATTER));
            System.out.printf("  Prazo limite: %s%n", solicitacao.getPrazoEstimado().format(FORMATTER));
            System.out.println("══════════════════════════════════════════════════════════");

            exibirSituacaoPrazo(solicitacao);

            if (solicitacao.isAtrasada()) {
                System.out.println("\n─── Últimas movimentações (justificativas) ───");
                List<HistoricoStatus> historico = solicitacao.getHistoricoStatus();
                int inicio = Math.max(0, historico.size() - 5);
                for (int i = inicio; i < historico.size(); i++) {
                    System.out.printf("  %s%n", historico.get(i));
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n✘ " + e.getMessage());
        }
    }

    // ─── Listagem geral ───

    public void exibirListagem() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║           LISTAGEM DE SOLICITAÇÕES              ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        List<Solicitacao> todas = service.listarTodas();

        if (todas.isEmpty()) {
            System.out.println("  Nenhuma solicitação cadastrada.");
            return;
        }

        System.out.printf("  Total: %d solicitação(ões)%n%n", todas.size());
        System.out.printf("  %-20s %-15s %-14s %-10s %-15s %s%n",
                "PROTOCOLO", "CATEGORIA", "STATUS", "PRIORID.", "BAIRRO", "PRAZO");
        System.out.println("  " + "─".repeat(90));

        for (Solicitacao s : todas) {
            String indicadorAtraso = s.isAtrasada() ? " ⚠ATRASO" : "";
            System.out.printf("  %-20s %-15s %-14s %-10s %-15s %s%s%n",
                    s.getProtocolo(),
                    s.getCategoria().getDescricao(),
                    s.getStatus().getDescricao(),
                    s.getPrioridade().getDescricao(),
                    s.getBairro(),
                    s.getPrazoEstimado().format(FORMATTER),
                    indicadorAtraso);
        }
    }

    // ─── Métodos auxiliares de exibição ───

    private void exibirSituacaoPrazo(Solicitacao solicitacao) {
        System.out.println("\n─── Situação do Prazo ───");

        if (solicitacao.getStatus().isTerminal()) {
            System.out.println("  ● Solicitação finalizada (" + solicitacao.getStatus().getDescricao() + ").");
            return;
        }

        if (solicitacao.isAtrasada()) {
            System.out.println("  ╔════════════════════════════════════════════════════╗");
            System.out.println("  ║  ⚠  ATENÇÃO: SOLICITAÇÃO COM PRAZO VENCIDO        ║");
            System.out.println("  ╚════════════════════════════════════════════════════╝");
            System.out.printf("  Prazo vencido há: %d dia(s)%n", solicitacao.getDiasAtraso());
            System.out.printf("  Data limite era:  %s%n", solicitacao.getPrazoEstimado().format(FORMATTER));
            System.out.println();
            System.out.println("  ─── Justificativa do órgão responsável ───");
            System.out.printf("  %s%n", solicitacao.getJustificativaAtraso());
        } else {
            System.out.println("  ✔ Dentro do prazo");
            System.out.printf("  Prazo limite:   %s%n", solicitacao.getPrazoEstimado().format(FORMATTER));
            System.out.printf("  Dias restantes: %d dia(s)%n", solicitacao.getDiasRestantes());
        }
    }

    private void exibirHistoricoStatus(List<HistoricoStatus> historico) {
        System.out.println("\n─── Histórico de Status ───");
        if (historico.isEmpty()) {
            System.out.println("  Nenhuma alteração registrada.");
        } else {
            for (HistoricoStatus h : historico) {
                System.out.println("  " + h);
            }
        }
    }

    private void exibirComentarios(List<Comentario> comentarios) {
        System.out.println("\n─── Comentários ───");
        if (comentarios.isEmpty()) {
            System.out.println("  Nenhum comentário registrado.");
        } else {
            for (Comentario c : comentarios) {
                System.out.println("  " + c);
            }
        }
    }
}
