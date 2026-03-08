package com.observaacao.model;

public class Usuario {
    private String nome;
    private String email;
    private String telefone;
    private boolean anonimo;

    // Construtor para usuário identificado
    public Usuario(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.anonimo = false;
    }

    // Construtor para usuário anônimo
    private Usuario() {
        this.nome = "Anônimo";
        this.email = null;
        this.telefone = null;
        this.anonimo = true;
    }

    public static Usuario criarAnonimo() {
        return new Usuario();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (!this.anonimo) {
            this.nome = nome;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (!this.anonimo) {
            this.email = email;
        }
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        if (!this.anonimo) {
            this.telefone = telefone;
        }
    }

    public boolean isAnonimo() {
        return anonimo;
    }

    @Override
    public String toString() {
        if (anonimo) {
            return "Usuário Anônimo";
        }
        return "Usuário: " + nome + " | Email: " + email + " | Tel: " + telefone;
    }
}
