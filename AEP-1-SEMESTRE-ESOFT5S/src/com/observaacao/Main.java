package com.observaacao;

import com.observaacao.repository.SolicitacaoRepository;
import com.observaacao.service.SolicitacaoService;
import com.observaacao.view.CadastroSolicitacaoView;
import com.observaacao.view.ConsultaSolicitacaoView;
import com.observaacao.view.GestaoSolicitacaoView;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        SolicitacaoRepository repository = new SolicitacaoRepository();
        SolicitacaoService service = new SolicitacaoService(repository);

        CadastroSolicitacaoView cadastroView = new CadastroSolicitacaoView(service, scanner);
        ConsultaSolicitacaoView consultaView = new ConsultaSolicitacaoView(service, scanner);
        GestaoSolicitacaoView gestaoView = new GestaoSolicitacaoView(service, scanner);

        boolean executando = true;

        while (executando) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> cadastroView.exibirFormulario();
                case "2" -> consultaView.exibirConsulta();
                case "3" -> consultaView.exibirListagem();
                case "4" -> gestaoView.exibirMenuGestao();
                case "0" -> {
                    executando = false;
                    System.out.println("\nObrigado por usar o ObservaAção. Até logo!");
                }
                default -> System.out.println("\nOpção inválida. Tente novamente.");
            }
        }

        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              OBSERVAAÇÃO - Menu                 ║");
        System.out.println("║    Sistema de Solicitações ao Cidadão           ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  [1] Nova Solicitação                           ║");
        System.out.println("║  [2] Consultar Solicitação (por protocolo)      ║");
        System.out.println("║  [3] Listar Todas as Solicitações               ║");
        System.out.println("║  [4] Painel do Servidor / Gestor                ║");
        System.out.println("║  [0] Sair                                       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.print("Opção: ");
    }
}
