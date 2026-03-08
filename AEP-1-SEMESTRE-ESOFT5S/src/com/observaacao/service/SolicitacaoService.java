package com.observaacao.service;

import com.observaacao.model.*;
import com.observaacao.repository.SolicitacaoRepository;

import java.util.List;

public class SolicitacaoService {
    private final SolicitacaoRepository repository;
    private final FilaAtendimento filaAtendimento;

    public SolicitacaoService(SolicitacaoRepository repository) {
        this.repository = repository;
        this.filaAtendimento = new FilaAtendimento();
    }

    public Solicitacao cadastrar(Categoria categoria, String descricao, String localizacao,
                                  Prioridade prioridade, Usuario solicitante, Anexo anexo) {

        validarCamposObrigatorios(categoria, descricao, localizacao, prioridade);
        validarRegraAnonimato(solicitante);

        String protocolo = gerarProtocoloUnico();

        Solicitacao solicitacao = new Solicitacao(protocolo, categoria, descricao,
                localizacao, prioridade, solicitante);

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

    public void avancarStatus(String protocolo, StatusSolicitacao novoStatus,
                               String responsavel, String observacao) {
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);
        solicitacao.avancarStatus(novoStatus, responsavel, observacao);
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

    // --- Regras de negócio ---

    private void validarCamposObrigatorios(Categoria categoria, String descricao,
                                            String localizacao, Prioridade prioridade) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (descricao.trim().length() < 10) {
            throw new IllegalArgumentException("Descrição deve ter no mínimo 10 caracteres.");
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
