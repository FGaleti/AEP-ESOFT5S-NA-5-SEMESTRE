package com.observaacao.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma categoria de solicitação.
 * Ex: Iluminação, Buraco, Poda, Saúde, etc.
 */
@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String descricao;

    private String exemploProblema;

    public Categoria(String descricao, String exemploProblema) {
        this.descricao = descricao;
        this.exemploProblema = exemploProblema;
    }
}
