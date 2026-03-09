package com.observaacao.service;

import com.observaacao.model.*;
import com.observaacao.repository.SolicitacaoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SolicitacaoService {
    private final SolicitacaoRepository repository;
    private final FilaAtendimento filaAtendimento;

    public SolicitacaoService(SolicitacaoRepository repository) {
        this.repository = repository;
        this.filaAtendimento = new FilaAtendimento();
    }

    public Solicitacao cadastrar(Categoria categoria, String descricao, String bairro,
                                  String localizacao, Prioridade prioridade,
                                  Usuario solicitante, Anexo anexo) {

        validarCamposObrigatorios(categoria, descricao, bairro, localizacao, prioridade);
        validarRegraAnonimato(solicitante);

        String protocolo = gerarProtocoloUnico();

        Solicitacao solicitacao = new Solicitacao(protocolo, categoria, descricao,
                bairro, localizacao, prioridade, solicitante);

        if (anexo != null) {
            solicitacao.setAnexo(anexo);
        }

        repository.salvar(solicitacao);
        filaAtendimento.adicionar(solicitacao);
        return solicitacao;
    }

    public Solicitacao consultarPorProtocolo(String protocolo) {
        Solicitacao solicitacao = repository.buscarPorProtocolo(protocolo);
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

    public void avancarStatus(String protocolo, StatusSolicitacao novoStatus,
                               String responsavel, String observacao) {
        if (observacao == null || observacao.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Comentário/observação é OBRIGATÓRIO ao atualizar o status.");
        }
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);
        solicitacao.avancarStatus(novoStatus, responsavel, observacao);
        filaAtendimento.reordenar();
        repository.salvar(solicitacao);
    }

    public void registrarComentario(String protocolo, String autor, String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Texto do comentário não pode ser vazio.");
        }
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);
        solicitacao.adicionarComentario(autor, texto);
        repository.salvar(solicitacao);
    }

    public FilaAtendimento getFilaAtendimento() {
        return filaAtendimento;
    }

    // --- Estatísticas para o painel ---

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

    // --- Regras de negócio ---

    private void validarCamposObrigatorios(Categoria categoria, String descricao,
                                            String bairro, String localizacao,
                                            Prioridade prioridade) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (descricao.trim().length() < 10) {
            throw new IllegalArgumentException("Descrição deve ter no mínimo 10 caracteres.");
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

    private void validarRegraAnonimato(Usuario solicitante) {
        if (solicitante == null) {
            throw new IllegalArgumentException("Solicitante é obrigatório.");
        }
        if (!solicitante.isAnonimo()) {
            if (solicitante.getNome() == null || solicitante.getNome().trim().isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório para usuário identificado.");
            }
            if (solicitante.getEmail() == null || solicitante.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("E-mail é obrigatório para usuário identificado.");
            }
        }
    }

    private String gerarProtocoloUnico() {
        String protocolo;
        do {
            protocolo = GeradorProtocolo.gerar();
        } while (repository.existeProtocolo(protocolo));
        return protocolo;
    }
}
