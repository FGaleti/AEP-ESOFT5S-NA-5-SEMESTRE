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

        // 3. Descrição (mínimo maior para anônimos — compensar ausência de dados)
        String descricao = coletarDescricao(solicitante.isAnonimo());

        // 4. Bairro
        String bairro = coletarBairro();

        // 5. Localização (endereço / referência)
        String localizacao = coletarLocalizacao();

        // 6. Prioridade (com SLA e impacto social visíveis)
        Prioridade prioridade = coletarPrioridade();

        // 7. Anexo (opcional)
        Anexo anexo = coletarAnexo();

        // 8. Confirmação
        if (!confirmarCadastro(categoria, descricao, bairro, localizacao, prioridade, solicitante, anexo)) {
            System.out.println("\n⚠ Cadastro cancelado pelo usuário.");
            return;
        }

        // 9. Cadastrar
        try {
            Solicitacao solicitacao = service.cadastrar(categoria, descricao, bairro,
                    localizacao, prioridade, solicitante, anexo);

            System.out.println("\n✔ Solicitação registrada com sucesso!\n");
            System.out.println(solicitacao);

            if (solicitante.isAnonimo()) {
                System.out.println("\n╔══════════════════════════════════════════════════╗");
                System.out.println("║  ⚠ IMPORTANTE — SOLICITAÇÃO ANÔNIMA             ║");
                System.out.println("║  Anote seu protocolo! Este é o ÚNICO meio de     ║");
                System.out.println("║  acompanhar o andamento da sua solicitação.      ║");
                System.out.println("║  Seus dados pessoais NÃO foram registrados.      ║");
                System.out.printf("║  Protocolo: %-37s║%n", solicitacao.getProtocolo());
                System.out.println("╚══════════════════════════════════════════════════╝");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\n✘ Erro ao cadastrar: " + e.getMessage());
        }
    }

    private Usuario coletarIdentificacao() {
        System.out.println("─── Identificação ───");
        System.out.println("[1] Me identificar (nome e e-mail serão registrados)");
        System.out.println("[2] Registrar ANONIMAMENTE (nenhum dado pessoal será coletado)");
        System.out.print("Opção: ");
        int opcao = lerOpcaoInteira(1, 2);

        if (opcao == 2) {
            System.out.println();
            System.out.println("  ╔══════════════════════════════════════════════════╗");
            System.out.println("  ║  MODO ANÔNIMO ATIVADO                           ║");
            System.out.println("  ║  → Nenhum dado pessoal será coletado ou salvo.   ║");
            System.out.println("  ║  → Sua identidade NÃO poderá ser descoberta.     ║");
            System.out.println("  ║  → A descrição precisará ser mais detalhada      ║");
            System.out.println("  ║    (mínimo 20 caracteres) para viabilizar         ║");
            System.out.println("  ║    o atendimento sem contato.                    ║");
            System.out.println("  ╚══════════════════════════════════════════════════╝");
            System.out.println();
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

    private String coletarDescricao(boolean isAnonimo) {
        int minimoChars = isAnonimo ? 20 : 10;
        System.out.println("─── Descrição ───");
        if (isAnonimo) {
            System.out.println("⚠ Modo anônimo: descreva com mais detalhes (mínimo " + minimoChars + " caracteres).");
            System.out.println("  Inclua referências visuais, datas e detalhes que ajudem");
            System.out.println("  a equipe a entender o problema sem contato com você.");
        } else {
            System.out.println("Descreva o problema com detalhes (mínimo " + minimoChars + " caracteres):");
        }
        System.out.print("> ");
        String descricao;
        do {
            descricao = scanner.nextLine().trim();
            if (descricao.length() < minimoChars) {
                System.out.printf("Descrição muito curta (%d/%d). Tente novamente:%n> ",
                        descricao.length(), minimoChars);
            }
        } while (descricao.length() < minimoChars);
        System.out.println();
        return descricao;
    }

    private String coletarBairro() {
        System.out.println("─── Bairro ───");
        System.out.print("Nome do bairro: ");
        String bairro;
        do {
            bairro = scanner.nextLine().trim();
            if (bairro.isEmpty()) {
                System.out.print("Bairro é obrigatório. Tente novamente: ");
            }
        } while (bairro.isEmpty());
        System.out.println();
        return bairro;
    }

    private String coletarLocalizacao() {
        System.out.println("─── Localização ───");
        System.out.print("Endereço / Referência: ");
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
        System.out.println("─── Prioridade (SLA — Acordo de Nível de Serviço) ───");
        Prioridade[] prioridades = Prioridade.values();
        for (int i = 0; i < prioridades.length; i++) {
            System.out.printf("[%d] %s%n", i + 1, prioridades[i].getSlaFormatado());
            System.out.printf("    Ex.: %s%n", prioridades[i].getExemploContexto());
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

    private boolean confirmarCadastro(Categoria categoria, String descricao, String bairro,
                                       String localizacao, Prioridade prioridade,
                                       Usuario solicitante, Anexo anexo) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("           CONFIRME OS DADOS DA SOLICITAÇÃO       ");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.printf("  Tipo:         %s%n", solicitante.isAnonimo() ? "ANÔNIMA (dados protegidos)" : "IDENTIFICADA");
        if (!solicitante.isAnonimo()) {
            System.out.printf("  Solicitante:  %s%n", solicitante.getNome());
        }
        System.out.printf("  Categoria:    %s%n", categoria.getDescricao());
        System.out.printf("  Descrição:    %s%n", descricao);
        System.out.printf("  Bairro:       %s%n", bairro);
        System.out.printf("  Localização:  %s%n", localizacao);
        System.out.printf("  Prioridade:   %s (SLA: %d dias)%n", prioridade.getDescricao(), prioridade.getPrazoEmDias());
        System.out.printf("  Impacto:      %s%n", prioridade.getImpactoSocial());
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
