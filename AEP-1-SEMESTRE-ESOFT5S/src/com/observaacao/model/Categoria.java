package com.observaacao.model;

public enum Categoria {
    ILUMINACAO("Iluminação"),
    BURACO("Buraco"),
    LIMPEZA("Limpeza"),
    SAUDE("Saúde"),
    SEGURANCA_ESCOLAR("Segurança Escolar"),
    PODA_ARVORE("Poda de Árvore"),
    SANEAMENTO("Saneamento"),
    TRANSPORTE("Transporte"),
    OUTROS("Outros");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
