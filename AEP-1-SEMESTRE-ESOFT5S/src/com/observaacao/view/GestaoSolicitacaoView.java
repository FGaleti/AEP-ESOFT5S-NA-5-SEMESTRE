package com.observaacao.view;

import com.observaacao.model.Categoria;
import com.observaacao.model.Prioridade;
import com.observaacao.model.Solicitacao;
import com.observaacao.model.StatusSolicitacao;
import com.observaacao.service.SolicitacaoService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

public class GestaoSolicitacaoView {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SolicitacaoService service;
    private final Scanner scanner;

    public GestaoSolicitacaoView(SolicitacaoService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void exibirMenuGestao() {
        boolean continuar = true;
        while (continuar) {
            exibirResumo();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║             PAINEL DO SERVIDOR / GESTOR                 ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║  Fluxo: Aberto → Triagem → Em Execução → Resolvido     ║");
            System.out.println("║                                          → Encerrado    ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] Avançar status (comentário obrigatório)            ║");
            System.out.println("║  [2] Registrar comentário em solicitação                ║");
            System.out.println("║  [3] Fila de atendimento (aguardando)                   ║");
            System.out.println("║  [4] Solicitações em execução                           ║");
            System.out.println("║  [5] Próxima da fila (maior prioridade)                 ║");
            System.out.println("║  ──── Filtros ─────────────────────────────────────────  ║");
            System.out.println("║  [6] Listar por PRIORIDADE                              ║");
            System.out.println("║  [7] Listar por BAIRRO                                  ║");
            System.out.println("║  [8] Listar por CATEGORIA                               ║");
            System.out.println("║  [9] Listar ATRASADAS                                   ║");
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
                case "6" -> filtrarPorPrioridade();
                case "7" -> filtrarPorBairro();
                case "8" -> filtrarPorCategoria();
                case "9" -> verAtrasadas();
                case "0" -> continuar = false;
                default -> System.out.println("\nOpção inválida.");
            }
        }
    }

    // ─── Resumo / Dashboard ───

    private void exibirResumo() {
        long abertas = service.contarPorStatus(StatusSolicitacao.ABERTO);
        long triagem = service.contarPorStatus(StatusSolicitacao.TRIAGEM);
        long execucao = service.contarPorStatus(StatusSolicitacao.EM_EXECUCAO);
        long resolvidas = service.contarPorStatus(StatusSolicitacao.RESOLVIDO);
        long encerradas = service.contarPorStatus(StatusSolicitacao.ENCERRADO);
        long atrasadas = service.contarAtrasadas();
        long total = service.listarTodas().size();

        System.out.println("\n┌──────────────────────────────────────────────────────────┐");
        System.out.println("│                    RESUMO DO PAINEL                      │");
        System.out.println("├──────────────────────────────────────────────────────────┤");
        System.out.printf("│  Total: %-5d │ Abertas: %-4d │ Triagem: %-4d            │%n", total, abertas, triagem);
        System.out.printf("│  Em Execução: %-4d │ Resolvidas: %-4d │ Encerradas: %-4d │%n", execucao, resolvidas, encerradas);
        if (atrasadas > 0) {
            System.out.printf("│  ⚠ ATRASADAS: %-4d                                       │%n", atrasadas);
        }
        System.out.println("└──────────────────────────────────────────────────────────┘\n");
    }

    // ─── Avançar Status (comentário OBRIGATÓRIO) ───

    private void avancarStatus() {
        System.out.println("\n─── Avançar Status (comentário obrigatório) ───");
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

        if (solicitacao.isAtrasada()) {
            System.out.printf("  ⚠ ATENÇÃO: Solicitação ATRASADA em %d dia(s)!%n", solicitacao.getDiasAtraso());
        }

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
        if (responsavel.isEmpty()) {
            System.out.println("\n✘ Nome do responsável é obrigatório.");
            return;
        }

        String observacao;
        do {
            System.out.print("Comentário/justificativa (OBRIGATÓRIO): ");
            observacao = scanner.nextLine().trim();
            if (observacao.isEmpty()) {
                System.out.println("  ⚠ Comentário não pode ser vazio. É obrigatório justificar a mudança de status.");
            }
        } while (observacao.isEmpty());

        try {
            service.avancarStatus(protocolo, novoStatus, responsavel, observacao);
            System.out.printf("%n✔ Status avançado: %s → %s%n", atual.getDescricao(), novoStatus.getDescricao());
            System.out.printf("  Responsável: %s | Justificativa: %s%n", responsavel, observacao);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("\n✘ " + e.getMessage());
        }
    }

    // ─── Registrar Comentário ───

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

    // ─── Filas e Próxima ───

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

    // ─── Filtros: Prioridade, Bairro, Categoria ───

    private void filtrarPorPrioridade() {
        System.out.println("\n─── Filtrar por Prioridade ───");
        Prioridade[] prioridades = Prioridade.values();
        for (int i = 0; i < prioridades.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, prioridades[i].getDescricao());
        }
        System.out.print("Escolha a prioridade: ");
        int opcao = lerOpcaoInteira(1, prioridades.length);
        Prioridade selecionada = prioridades[opcao - 1];

        List<Solicitacao> resultado = service.listarPorPrioridade(selecionada);
        System.out.printf("%n─── Solicitações com prioridade: %s ──── (%d encontrada(s))%n%n",
                selecionada.getDescricao(), resultado.size());

        if (resultado.isEmpty()) {
            System.out.println("  Nenhuma solicitação encontrada.");
        } else {
            exibirTabelaSolicitacoes(resultado);
        }
    }

    private void filtrarPorBairro() {
        System.out.println("\n─── Filtrar por Bairro ───");

        List<String> bairros = service.listarBairrosDistintos();
        if (bairros.isEmpty()) {
            System.out.println("  Nenhuma solicitação cadastrada.");
            return;
        }

        System.out.println("  Bairros com solicitações:");
        for (int i = 0; i < bairros.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, bairros.get(i));
        }
        System.out.print("Escolha o bairro: ");
        int opcao = lerOpcaoInteira(1, bairros.size());
        String bairro = bairros.get(opcao - 1);

        List<Solicitacao> resultado = service.listarPorBairro(bairro);
        System.out.printf("%n─── Solicitações no bairro: %s ──── (%d encontrada(s))%n%n",
                bairro, resultado.size());

        if (resultado.isEmpty()) {
            System.out.println("  Nenhuma solicitação encontrada.");
        } else {
            exibirTabelaSolicitacoes(resultado);
        }
    }

    private void filtrarPorCategoria() {
        System.out.println("\n─── Filtrar por Categoria ───");
        Categoria[] categorias = Categoria.values();
        for (int i = 0; i < categorias.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, categorias[i].getDescricao());
        }
        System.out.print("Escolha a categoria: ");
        int opcao = lerOpcaoInteira(1, categorias.length);
        Categoria selecionada = categorias[opcao - 1];

        List<Solicitacao> resultado = service.listarPorCategoria(selecionada);
        System.out.printf("%n─── Solicitações na categoria: %s ──── (%d encontrada(s))%n%n",
                selecionada.getDescricao(), resultado.size());

        if (resultado.isEmpty()) {
            System.out.println("  Nenhuma solicitação encontrada.");
        } else {
            exibirTabelaSolicitacoes(resultado);
        }
    }

    private void verAtrasadas() {
        System.out.println("\n─── Solicitações ATRASADAS ───");
        List<Solicitacao> atrasadas = service.listarAtrasadas();

        if (atrasadas.isEmpty()) {
            System.out.println("  ✔ Nenhuma solicitação em atraso. Parabéns!");
            return;
        }

        System.out.printf("  ⚠ Total em atraso: %d%n%n", atrasadas.size());
        System.out.printf("  %-20s %-10s %-15s %-14s %-12s %s%n",
                "PROTOCOLO", "PRIORID.", "CATEGORIA", "STATUS", "BAIRRO", "DIAS ATRASO");
        System.out.println("  " + "─".repeat(90));

        for (Solicitacao s : atrasadas) {
            System.out.printf("  %-20s %-10s %-15s %-14s %-12s %d dia(s)%n",
                    s.getProtocolo(),
                    s.getPrioridade().getDescricao(),
                    s.getCategoria().getDescricao(),
                    s.getStatus().getDescricao(),
                    s.getBairro(),
                    s.getDiasAtraso());
        }
    }

    // ─── Tabela padrão de solicitações ───

    private void exibirTabelaSolicitacoes(List<Solicitacao> lista) {
        System.out.printf("  %-20s %-10s %-15s %-14s %-12s %s%n",
                "PROTOCOLO", "PRIORID.", "CATEGORIA", "STATUS", "BAIRRO", "PRAZO");
        System.out.println("  " + "─".repeat(90));
        for (Solicitacao s : lista) {
            String indicadorAtraso = s.isAtrasada() ? " ⚠" : "";
            System.out.printf("  %-20s %-10s %-15s %-14s %-12s %s%s%n",
                    s.getProtocolo(),
                    s.getPrioridade().getDescricao(),
                    s.getCategoria().getDescricao(),
                    s.getStatus().getDescricao(),
                    s.getBairro(),
                    s.getPrazoEstimado().format(FORMATTER),
                    indicadorAtraso);
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
