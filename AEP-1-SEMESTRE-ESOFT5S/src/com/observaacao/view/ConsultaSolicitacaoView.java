package com.observaacao.view;

import com.observaacao.model.Comentario;
import com.observaacao.model.HistoricoStatus;
import com.observaacao.model.Solicitacao;
import com.observaacao.service.SolicitacaoService;

import java.util.List;
import java.util.Scanner;

public class ConsultaSolicitacaoView {
    private final SolicitacaoService service;
    private final Scanner scanner;

    public ConsultaSolicitacaoView(SolicitacaoService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void exibirConsulta() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         CONSULTAR SOLICITAÇÃO POR PROTOCOLO     ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.print("Digite o número do protocolo: ");
        String protocolo = scanner.nextLine().trim();

        try {
            Solicitacao solicitacao = service.consultarPorProtocolo(protocolo);
            System.out.println();
            System.out.println(solicitacao);

            exibirHistoricoStatus(solicitacao.getHistoricoStatus());
            exibirComentarios(solicitacao.getComentarios());
        } catch (IllegalArgumentException e) {
            System.out.println("\n✘ " + e.getMessage());
        }
    }

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
        for (Solicitacao s : todas) {
            System.out.printf("  [%s] %s | %s | %s | %s%n",
                    s.getProtocolo(),
                    s.getCategoria().getDescricao(),
                    s.getStatus().getDescricao(),
                    s.getPrioridade().getDescricao(),
                    s.getLocalizacao());
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
