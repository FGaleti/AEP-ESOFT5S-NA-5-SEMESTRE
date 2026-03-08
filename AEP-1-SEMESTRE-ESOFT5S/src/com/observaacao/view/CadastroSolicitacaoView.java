package com.observaacao.view;

import com.observaacao.model.*;
import com.observaacao.service.SolicitacaoService;

import java.util.Scanner;

public class CadastroSolicitacaoView {
    private final SolicitacaoService service;
    private final Scanner scanner;

    public CadastroSolicitacaoView(SolicitacaoService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void exibirFormulario() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         CADASTRO DE NOVA SOLICITAÇÃO            ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // 1. Identificação ou Anônimo
        Usuario solicitante = coletarIdentificacao();

        // 2. Categoria
        Categoria categoria = coletarCategoria();

        // 3. Descrição
        String descricao = coletarDescricao();

        // 4. Localização
        String localizacao = coletarLocalizacao();

        // 5. Prioridade
        Prioridade prioridade = coletarPrioridade();

        // 6. Anexo (opcional)
        Anexo anexo = coletarAnexo();

        // 7. Confirmação
        if (!confirmarCadastro(categoria, descricao, localizacao, prioridade, solicitante, anexo)) {
            System.out.println("\n⚠ Cadastro cancelado pelo usuário.");
            return;
        }

        // 8. Cadastrar
        try {
            Solicitacao solicitacao = service.cadastrar(categoria, descricao, localizacao,
                    prioridade, solicitante, anexo);

            System.out.println("\n✔ Solicitação registrada com sucesso!\n");
            System.out.println(solicitacao);

            if (solicitante.isAnonimo()) {
                System.out.println("\n⚠ IMPORTANTE: Anote seu protocolo! Como solicitação anônima,");
                System.out.println("  este é o único meio de acompanhar o andamento.");
                System.out.println("  Protocolo: " + solicitacao.getProtocolo());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n✘ Erro ao cadastrar: " + e.getMessage());
        }
    }

    private Usuario coletarIdentificacao() {
        System.out.println("─── Identificação ───");
        System.out.println("[1] Me identificar");
        System.out.println("[2] Registrar anonimamente");
        System.out.print("Opção: ");
        int opcao = lerOpcaoInteira(1, 2);

        if (opcao == 2) {
            System.out.println("→ Modo anônimo selecionado. Seus dados não serão registrados.\n");
            return Usuario.criarAnonimo();
        }

        System.out.print("Nome completo: ");
        String nome = scanner.nextLine().trim();

        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();

        System.out.print("Telefone (opcional, pressione Enter para pular): ");
        String telefone = scanner.nextLine().trim();
        if (telefone.isEmpty()) {
            telefone = null;
        }

        System.out.println();
        return new Usuario(nome, email, telefone);
    }

    private Categoria coletarCategoria() {
        System.out.println("─── Categoria do Serviço ───");
        Categoria[] categorias = Categoria.values();
        for (int i = 0; i < categorias.length; i++) {
            System.out.printf("[%d] %s%n", i + 1, categorias[i].getDescricao());
        }
        System.out.print("Opção: ");
        int opcao = lerOpcaoInteira(1, categorias.length);
        System.out.println();
        return categorias[opcao - 1];
    }

    private String coletarDescricao() {
        System.out.println("─── Descrição ───");
        System.out.println("Descreva o problema com detalhes (mínimo 10 caracteres):");
        System.out.print("> ");
        String descricao;
        do {
            descricao = scanner.nextLine().trim();
            if (descricao.length() < 10) {
                System.out.print("Descrição muito curta. Tente novamente:\n> ");
            }
        } while (descricao.length() < 10);
        System.out.println();
        return descricao;
    }

    private String coletarLocalizacao() {
        System.out.println("─── Localização ───");
        System.out.print("Bairro / Endereço / Referência: ");
        String localizacao;
        do {
            localizacao = scanner.nextLine().trim();
            if (localizacao.isEmpty()) {
                System.out.print("Localização é obrigatória. Tente novamente: ");
            }
        } while (localizacao.isEmpty());
        System.out.println();
        return localizacao;
    }

    private Prioridade coletarPrioridade() {
        System.out.println("─── Prioridade ───");
        Prioridade[] prioridades = Prioridade.values();
        for (int i = 0; i < prioridades.length; i++) {
            String prazo = switch (prioridades[i]) {
                case BAIXA -> "(prazo: 30 dias)";
                case MEDIA -> "(prazo: 15 dias)";
                case ALTA -> "(prazo: 5 dias)";
                case CRITICA -> "(prazo: 2 dias)";
            };
            System.out.printf("[%d] %s %s%n", i + 1, prioridades[i].getDescricao(), prazo);
        }
        System.out.print("Opção: ");
        int opcao = lerOpcaoInteira(1, prioridades.length);
        System.out.println();
        return prioridades[opcao - 1];
    }

    private Anexo coletarAnexo() {
        System.out.println("─── Anexo (opcional) ───");
        System.out.print("Deseja anexar um arquivo? [S/N]: ");
        String resposta = scanner.nextLine().trim().toUpperCase();

        if (resposta.equals("S")) {
            System.out.print("Nome do arquivo (ex: foto_buraco.jpg): ");
            String nome = scanner.nextLine().trim();
            System.out.print("Caminho do arquivo: ");
            String caminho = scanner.nextLine().trim();

            if (!nome.isEmpty() && !caminho.isEmpty()) {
                System.out.println("→ Anexo registrado.\n");
                return new Anexo(nome, caminho);
            }
            System.out.println("→ Dados inválidos. Anexo ignorado.\n");
        }
        System.out.println();
        return null;
    }

    private boolean confirmarCadastro(Categoria categoria, String descricao, String localizacao,
                                       Prioridade prioridade, Usuario solicitante, Anexo anexo) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("           CONFIRME OS DADOS DA SOLICITAÇÃO       ");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.printf("  Tipo:         %s%n", solicitante.isAnonimo() ? "ANÔNIMA" : "IDENTIFICADA");
        if (!solicitante.isAnonimo()) {
            System.out.printf("  Solicitante:  %s%n", solicitante.getNome());
        }
        System.out.printf("  Categoria:    %s%n", categoria.getDescricao());
        System.out.printf("  Descrição:    %s%n", descricao);
        System.out.printf("  Localização:  %s%n", localizacao);
        System.out.printf("  Prioridade:   %s%n", prioridade.getDescricao());
        System.out.printf("  Anexo:        %s%n", anexo != null ? anexo.getNomeArquivo() : "Nenhum");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.print("Confirmar envio? [S/N]: ");
        return scanner.nextLine().trim().toUpperCase().equals("S");
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
