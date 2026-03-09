package com.observaacao.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class FilaAtendimento {

    private static final Comparator<Solicitacao> COMPARADOR_FILA =
            Comparator.comparingInt((Solicitacao s) -> s.getPrioridade().ordinal())
                      .thenComparing(Solicitacao::getDataCriacao);

    private final PriorityQueue<Solicitacao> filaPrioridade;
    private final List<Solicitacao> todas;

    public FilaAtendimento() {
        this.filaPrioridade = new PriorityQueue<>(COMPARADOR_FILA);
        this.todas = new ArrayList<>();
    }

    public void adicionar(Solicitacao solicitacao) {
        todas.add(solicitacao);
        if (isAguardandoAtendimento(solicitacao)) {
            filaPrioridade.offer(solicitacao);
        }
    }

    public void reordenar() {
        filaPrioridade.clear();
        todas.stream()
             .filter(this::isAguardandoAtendimento)
             .forEach(filaPrioridade::offer);
    }

    public Solicitacao proximaDaFila() {
        reordenar();
        return filaPrioridade.peek();
    }

    public List<Solicitacao> listarAguardandoAtendimento() {
        return todas.stream()
                .filter(this::isAguardandoAtendimento)
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarEmExecucao() {
        return todas.stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.EM_EXECUCAO)
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarPorStatus(StatusSolicitacao status) {
        return todas.stream()
                .filter(s -> s.getStatus() == status)
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarPorCategoria(Categoria categoria) {
        return todas.stream()
                .filter(s -> s.getCategoria() == categoria)
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarPorBairro(String bairro) {
        return todas.stream()
                .filter(s -> s.getBairro() != null
                        && s.getBairro().equalsIgnoreCase(bairro))
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarPorPrioridade(Prioridade prioridade) {
        return todas.stream()
                .filter(s -> s.getPrioridade() == prioridade)
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarAtrasadas() {
        return todas.stream()
                .filter(Solicitacao::isAtrasada)
                .sorted(COMPARADOR_FILA)
                .collect(Collectors.toList());
    }

    public List<String> listarBairrosDistintos() {
        return todas.stream()
                .map(Solicitacao::getBairro)
                .filter(b -> b != null && !b.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Solicitacao> listarTodas() {
        return new ArrayList<>(todas);
    }

    public int contarAguardando() {
        return (int) todas.stream().filter(this::isAguardandoAtendimento).count();
    }

    public int contarEmExecucao() {
        return (int) todas.stream().filter(s -> s.getStatus() == StatusSolicitacao.EM_EXECUCAO).count();
    }

    public int tamanhoTotal() {
        return todas.size();
    }

    private boolean isAguardandoAtendimento(Solicitacao s) {
        return s.getStatus() == StatusSolicitacao.ABERTO
            || s.getStatus() == StatusSolicitacao.TRIAGEM;
    }
}
