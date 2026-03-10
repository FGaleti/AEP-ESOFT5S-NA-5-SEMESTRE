package com.observaacao.service;

import com.observaacao.model.*;
import com.observaacao.model.LogAuditoria.TipoEvento;
import com.observaacao.repository.SolicitacaoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço principal de solicitações com regras críticas:
 * - Anonimato: limita campos pessoais, mas exige detalhes mínimos
 * - Prioridade: SLA (prazo alvo) definido no enum, com impacto social
 * - Prevenção de abuso: validações, detecção de duplicatas, log de auditoria
 */
public class SolicitacaoService {

    private static final int LIMITE_CADASTROS_POR_HORA = 10;
    private static final int DESCRICAO_MINIMA_ANONIMO = 20;
    private static final int DESCRICAO_MINIMA_IDENTIFICADO = 10;

    private final SolicitacaoRepository repository;
    private final FilaAtendimento filaAtendimento;
    private final List<LogAuditoria> logsAuditoria;

    public SolicitacaoService(SolicitacaoRepository repository) {
        this.repository = repository;
        this.filaAtendimento = new FilaAtendimento();
        this.logsAuditoria = new ArrayList<>();
    }

    // ─── Cadastro com validação completa e prevenção de abuso ───

    public Solicitacao cadastrar(Categoria categoria, String descricao, String bairro,
                                  String localizacao, Prioridade prioridade,
                                  Usuario solicitante, Anexo anexo) {

        validarCamposObrigatorios(categoria, descricao, bairro, localizacao, prioridade);
        validarRegraAnonimato(solicitante, descricao);
        verificarLimiteCadastros(solicitante);
        verificarDuplicidade(categoria, bairro, descricao, solicitante);

        String protocolo = gerarProtocoloUnico();

        Solicitacao solicitacao = new Solicitacao(protocolo, categoria, descricao,
                bairro, localizacao, prioridade, solicitante);

        if (anexo != null) {
            solicitacao.setAnexo(anexo);
        }

        repository.salvar(solicitacao);
        filaAtendimento.adicionar(solicitacao);

        registrarLog(TipoEvento.CADASTRO, protocolo,
                solicitante.isAnonimo() ? "Anônimo" : solicitante.getNome(),
                String.format("Categoria: %s | Prioridade: %s (SLA: %d dias) | Bairro: %s",
                        categoria.getDescricao(), prioridade.getDescricao(),
                        prioridade.getPrazoEmDias(), bairro),
                solicitante.isAnonimo());

        return solicitacao;
    }

    public Solicitacao consultarPorProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            throw new IllegalArgumentException("Protocolo não pode ser vazio.");
        }
        Solicitacao solicitacao = repository.buscarPorProtocolo(protocolo.trim());
        if (solicitacao == null) {
            throw new IllegalArgumentException("Solicitação não encontrada para o protocolo: " + protocolo);
        }
        return solicitacao;
    }

    public List<Solicitacao> listarTodas() {
        return repository.listarTodas();
    }

    public List<Solicitacao> listarPorPrioridade(Prioridade prioridade) {
        return filaAtendimento.listarPorPrioridade(prioridade);
    }

    public List<Solicitacao> listarPorBairro(String bairro) {
        return filaAtendimento.listarPorBairro(bairro);
    }

    public List<Solicitacao> listarPorCategoria(Categoria categoria) {
        return filaAtendimento.listarPorCategoria(categoria);
    }

    public List<Solicitacao> listarAtrasadas() {
        return filaAtendimento.listarAtrasadas();
    }

    public List<String> listarBairrosDistintos() {
        return filaAtendimento.listarBairrosDistintos();
    }

    // ─── Avançar status (comentário OBRIGATÓRIO — auditoria) ───

    public void avancarStatus(String protocolo, StatusSolicitacao novoStatus,
                               String responsavel, String observacao) {
        if (responsavel == null || responsavel.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do responsável é OBRIGATÓRIO.");
        }
        if (observacao == null || observacao.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Comentário/observação é OBRIGATÓRIO ao atualizar o status.");
        }
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);

        StatusSolicitacao statusAnterior = solicitacao.getStatus();
        solicitacao.avancarStatus(novoStatus, responsavel, observacao);
        filaAtendimento.reordenar();
        repository.salvar(solicitacao);

        registrarLog(TipoEvento.TRANSICAO_STATUS, protocolo, responsavel,
                String.format("%s → %s | Obs: %s",
                        statusAnterior.getDescricao(), novoStatus.getDescricao(), observacao),
                false);
    }

    // ─── Registrar comentário (com log) ───

    public void registrarComentario(String protocolo, String autor, String texto) {
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do autor é obrigatório.");
        }
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Texto do comentário não pode ser vazio.");
        }
        if (texto.trim().length() < 5) {
            throw new IllegalArgumentException("Comentário deve ter no mínimo 5 caracteres.");
        }
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);
        solicitacao.adicionarComentario(autor, texto);
        repository.salvar(solicitacao);

        registrarLog(TipoEvento.COMENTARIO, protocolo, autor,
                "Comentário registrado (" + texto.length() + " caracteres)", false);
    }

    public FilaAtendimento getFilaAtendimento() {
        return filaAtendimento;
    }

    // ─── Estatísticas para o painel ───

    public long contarPorStatus(StatusSolicitacao status) {
        return repository.listarTodas().stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }

    public long contarAtrasadas() {
        return repository.listarTodas().stream()
                .filter(Solicitacao::isAtrasada)
                .count();
    }

    // ─── Log de auditoria (imutável, acessível ao gestor) ───

    public List<LogAuditoria> getLogsAuditoria() {
        return Collections.unmodifiableList(logsAuditoria);
    }

    public List<LogAuditoria> getLogsPorProtocolo(String protocolo) {
        return logsAuditoria.stream()
                .filter(l -> protocolo.equals(l.getProtocolo()))
                .collect(Collectors.toList());
    }

    public List<LogAuditoria> getLogsPorTipo(TipoEvento tipo) {
        return logsAuditoria.stream()
                .filter(l -> l.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public long contarTentativasAbuso() {
        return logsAuditoria.stream()
                .filter(l -> l.getTipo() == TipoEvento.TENTATIVA_ABUSO)
                .count();
    }

    private void registrarLog(TipoEvento tipo, String protocolo,
                               String ator, String detalhes, boolean anonimo) {
        logsAuditoria.add(new LogAuditoria(tipo, protocolo, ator, detalhes, anonimo));
    }

    // ─── Regras de negócio: validações ───

    private void validarCamposObrigatorios(Categoria categoria, String descricao,
                                            String bairro, String localizacao,
                                            Prioridade prioridade) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (descricao.trim().length() < DESCRICAO_MINIMA_IDENTIFICADO) {
            throw new IllegalArgumentException(
                    "Descrição deve ter no mínimo " + DESCRICAO_MINIMA_IDENTIFICADO + " caracteres.");
        }
        if (bairro == null || bairro.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório.");
        }
        if (localizacao == null || localizacao.trim().isEmpty()) {
            throw new IllegalArgumentException("Localização é obrigatória.");
        }
        if (prioridade == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória.");
        }
    }

    /**
     * Regra de anonimato: anônimos NÃO fornecem dados pessoais,
     * mas DEVEM fornecer uma descrição mais detalhada (mín. 20 chars)
     * para compensar a ausência de identificação.
     */
    private void validarRegraAnonimato(Usuario solicitante, String descricao) {
        if (solicitante == null) {
            throw new IllegalArgumentException("Solicitante é obrigatório.");
        }
        if (solicitante.isAnonimo()) {
            if (descricao.trim().length() < DESCRICAO_MINIMA_ANONIMO) {
                throw new IllegalArgumentException(
                        String.format("Solicitação anônima exige descrição mais detalhada " +
                                "(mínimo %d caracteres). Você informou %d.",
                                DESCRICAO_MINIMA_ANONIMO, descricao.trim().length()));
            }
        } else {
            if (solicitante.getNome() == null || solicitante.getNome().trim().isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório para usuário identificado.");
            }
            if (solicitante.getEmail() == null || solicitante.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("E-mail é obrigatório para usuário identificado.");
            }
        }
    }

    /**
     * Prevenção de abuso: limita cadastros por hora.
     * Em memória, conta logs de CADASTRO na última hora.
     * Se exceder o limite, registra TENTATIVA_ABUSO e bloqueia.
     */
    private void verificarLimiteCadastros(Usuario solicitante) {
        LocalDateTime umaHoraAtras = LocalDateTime.now().minusHours(1);
        long cadastrosRecentes = logsAuditoria.stream()
                .filter(l -> l.getTipo() == TipoEvento.CADASTRO)
                .filter(l -> l.getDataEvento().isAfter(umaHoraAtras))
                .count();

        if (cadastrosRecentes >= LIMITE_CADASTROS_POR_HORA) {
            registrarLog(TipoEvento.TENTATIVA_ABUSO, null,
                    solicitante.isAnonimo() ? "Anônimo" : solicitante.getNome(),
                    "Limite de " + LIMITE_CADASTROS_POR_HORA + " cadastros/hora excedido",
                    solicitante.isAnonimo());
            throw new IllegalStateException(
                    "Limite de cadastros por hora atingido (" + LIMITE_CADASTROS_POR_HORA +
                    "). Aguarde antes de registrar novas solicitações.");
        }
    }

    /**
     * Prevenção de abuso: detecta possíveis duplicatas.
     * Verifica se já existe solicitação ABERTA com mesma categoria + bairro
     * e descrição muito semelhante nos últimos registros.
     */
    private void verificarDuplicidade(Categoria categoria, String bairro,
                                       String descricao, Usuario solicitante) {
        String descNormalizada = descricao.trim().toLowerCase();
        boolean possivelDuplicata = repository.listarTodas().stream()
                .filter(s -> !s.getStatus().isTerminal())
                .filter(s -> s.getCategoria() == categoria)
                .filter(s -> s.getBairro().equalsIgnoreCase(bairro))
                .anyMatch(s -> calcularSimilaridade(
                        s.getDescricao().toLowerCase(), descNormalizada) > 0.8);

        if (possivelDuplicata) {
            registrarLog(TipoEvento.TENTATIVA_ABUSO, null,
                    solicitante.isAnonimo() ? "Anônimo" : solicitante.getNome(),
                    "Possível duplicata detectada: " + categoria + " em " + bairro,
                    solicitante.isAnonimo());
            throw new IllegalArgumentException(
                    "Já existe uma solicitação em aberto com descrição muito semelhante " +
                    "para esta categoria e bairro. Verifique antes de cadastrar novamente.");
        }
    }

    /**
     * Similaridade simples entre duas strings (coeficiente de Jaccard por palavras).
     * Retorna valor entre 0.0 (nada em comum) e 1.0 (idênticas).
     */
    private double calcularSimilaridade(String a, String b) {
        var palavrasA = java.util.Set.of(a.split("\\s+"));
        var palavrasB = java.util.Set.of(b.split("\\s+"));

        long intersecao = palavrasA.stream().filter(palavrasB::contains).count();
        long uniao = palavrasA.size() + palavrasB.size() - intersecao;

        return uniao == 0 ? 0.0 : (double) intersecao / uniao;
    }

    private String gerarProtocoloUnico() {
        String protocolo;
        do {
            protocolo = GeradorProtocolo.gerar();
        } while (repository.existeProtocolo(protocolo));
        return protocolo;
    }
}
