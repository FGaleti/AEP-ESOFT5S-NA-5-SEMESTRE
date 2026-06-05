package com.observaacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um comentário de um servidor na solicitação.
 * Permite comunicação entre gestor e cidadão.
 */
@Entity
@Table(name = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    public Comentario(String texto, String autor) {
        this.texto = texto;
        this.autor = autor;
        this.dataCriacao = LocalDateTime.now();
    }
}
