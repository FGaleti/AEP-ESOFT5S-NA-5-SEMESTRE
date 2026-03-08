package com.observaacao.view;

import com.observaacao.model.Solicitacao;
import com.observaacao.model.StatusSolicitacao;
import com.observaacao.service.SolicitacaoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

public class GestaoSolicitacaoView {
    private final SolicitacaoService service;
    private final Scanner scanner;

    public GestaoSolicitacaoView(SolicitacaoService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void exibirMenuGestao() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║             PAINEL DO SERVIDOR / GESTOR                 ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  Fluxo: Aberto → Triagem → Em Execução → Resolvido     ║");
        System.out.println("║                                          → Encerrado    ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  [1] Avançar status de solicitação                      ║");
        System.out.println("║  [2] Registrar comentário em solicitação                ║");
        System.out.println("║  [3] Fila de atendimento (aguardando)                   ║");
        System.out.println("║  [4] Solicitações em execução                           ║");
        System.out.println("║  [5] Próxima da fila (maior prioridade)                 ║");
        System.out.println("║  [0] Voltar ao menu principal                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.print("Opção: ");

        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> avancarStatus();
            case "2" -> registrarComentario();
            case "3" -> verFilaAguardando();
            case "4" -> verEmExecucao();
            case "5" -> verProximaDaFila();
            case "0" -> { }
            default -> System.out.println("\nOpção inválida.");
        }
    }

    private void avancarStatus() {
        System.out.println("\n─── Avançar Status ───");
        System.out.print("Protocolo da solicitação: ");
        String protocolo = scanner.nextLine().trim();

        Solicitacao solicitacao;
        try {
            solicitacao = service.consultarPorProtocolo(protocolo);
        } catch (IllegalArgumentException e) {
            System.out.println("\n✘ " + e.getMessage());
            return;
        }

        StatusSolicitacao atual = solicitacao.getStatus();
        System.out.printf("%n  Status atual: %s%n", atual.getDescricao());

        if (atual.isTerminal()) {
            System.out.println("  ⚠ Esta solicitação está em status terminal. Não é possível avançar.");
            return;
        }

        Set<StatusSolicitacao> permitidos = atual.proximosPermitidos();
        List<StatusSolicitacao> opcoes = new ArrayList<>(permitidos);

        System.out.println("  Transições permitidas:");
        for (int i = 0; i < opcoes.size(); i++) {
            System.out.printf("    [%d] %s%n", i + 1, opcoes.get(i).getDescricao());
        }
        System.out.print("  Escolha: ");
        int escolha = lerOpcaoInteira(1, opcoes.size());
        StatusSolicitacao novoStatus = opcoes.get(escolha - 1);

        System.out.print("Seu nome (responsável): ");
        String responsavel = scanner.nextLine().trim();

        System.out.print("Observação (opcional, Enter para pular): ");
        String observacao = scanner.nextLine().trim();

        try {
            service.avancarStatus(protocolo, novoStatus, responsavel, observacao);
            System.out.printf("%n✔ Status avançado: %s → %s%n", atual.getDescricao(), novoStatus.getDescricao());
        } catch (IllegalStateException e) {
            System.out.println("\n✘ " + e.getMessage());
        }
    }

    private void registrarComentario() {
        System.out.println("\n─── Registrar Comentário ───");
        System.out.print("Protocolo da solicitação: ");
        String protocolo = scanner.nextLine().trim();

        System.out.print("Seu nome: ");
        String autor = scanner.nextLine().trim();

        System.out.print("Comentário: ");
        String texto = scanner.nextLine().trim();

        try {
            service.registrarComentario(protocolo, autor, texto);
            System.out.println("\n✔ Comentário registrado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("\n✘ " + e.getMessage());
        }
    }

    private void verFilaAguardando() {
        System.out.println("\n─── Fila de Atendimento (Aberto + Triagem) ───");
        var aguardando = service.getFilaAtendimento().listarAguardandoAtendimento();

        if (aguardando.isEmpty()) {
            System.out.println("  Nenhuma solicitação aguardando atendimento.");
            return;
        }

        System.out.printf("  Total aguardando: %d%n%n", aguardando.size());
        exibirTabelaSolicitacoes(aguardando);
    }

    private void verEmExecucao() {
        System.out.println("\n─── Solicitações Em Execução ───");
        var emExecucao = service.getFilaAtendimento().listarEmExecucao();

        if (emExecucao.isEmpty()) {
            System.out.println("  Nenhuma solicitação em execução no momento.");
            return;
        }

        System.out.printf("  Total em execução: %d%n%n", emExecucao.size());
        exibirTabelaSolicitacoes(emExecucao);
    }

    private void verProximaDaFila() {
        System.out.println("\n─── Próxima da Fila ───");
        Solicitacao proxima = service.getFilaAtendimento().proximaDaFila();

        if (proxima == null) {
            System.out.println("  Fila vazia. Nenhuma solicitação aguardando.");
            return;
        }

        System.out.println("  ▶ Próxima solicitação a ser atendida:\n");
        System.out.println(proxima);
    }

    private void exibirTabelaSolicitacoes(List<Solicitacao> lista) {
        System.out.printf("  %-20s %-10s %-15s %-14s %s%n",
                "PROTOCOLO", "PRIORID.", "CATEGORIA", "STATUS", "LOCAL");
        System.out.println("  " + "─".repeat(75));
        for (Solicitacao s : lista) {
            System.out.printf("  %-20s %-10s %-15s %-14s %s%n",
                    s.getProtocolo(),
                    s.getPrioridade().getDescricao(),
                    s.getCategoria().getDescricao(),
                    s.getStatus().getDescricao(),
                    s.getLocalizacao());
        }
    }

    private int lerOpcaoInteira(int min, int max) {
        while (true) {
            try {
                String linha = scanner.nextLine().trim();
                int valor = Integer.parseInt(linha);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.printf("Opção inválida. Escolha entre %d e %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Entrada inválida. Digite um número entre %d e %d: ", min, max);
            }
        }
    }
}
