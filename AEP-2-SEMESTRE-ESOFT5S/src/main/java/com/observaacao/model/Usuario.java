package com.observaacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

/**
 * Entidade que representa um usuário do sistema.
 * Pode ser um cidadão (anônimo ou identificado) ou um servidor público.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String email;

    private String telefone;

    @Column(nullable = false)
    private boolean anonimo;

    private String tipo; // "CIDADAO" ou "SERVIDOR"

    public Usuario(String nome, String email, String telefone, boolean anonimo) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.anonimo = anonimo;
        this.tipo = "CIDADAO";
    }

    public static Usuario criarAnonimo() {
        Usuario usuario = new Usuario();
        usuario.setNome("Anônimo");
        usuario.setAnonimo(true);
        usuario.setTipo("CIDADAO");
        return usuario;
    }

    public static Usuario criarServidor(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setAnonimo(false);
        usuario.setTipo("SERVIDOR");
        return usuario;
    }
}
