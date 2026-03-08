package com.observaacao.repository;

import com.observaacao.model.Solicitacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolicitacaoRepository {
    private final Map<String, Solicitacao> solicitacoes;

    public SolicitacaoRepository() {
        this.solicitacoes = new HashMap<>();
    }

    public void salvar(Solicitacao solicitacao) {
        solicitacoes.put(solicitacao.getProtocolo(), solicitacao);
    }

    public Solicitacao buscarPorProtocolo(String protocolo) {
        return solicitacoes.get(protocolo);
    }

    public List<Solicitacao> listarTodas() {
        return new ArrayList<>(solicitacoes.values());
    }

    public boolean existeProtocolo(String protocolo) {
        return solicitacoes.containsKey(protocolo);
    }

    public int contarSolicitacoes() {
        return solicitacoes.size();
    }
}
