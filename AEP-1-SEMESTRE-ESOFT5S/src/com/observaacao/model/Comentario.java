package com.observaacao.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comentario {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String autor;
    private String texto;
    private LocalDateTime dataCriacao;

    public Comentario(String autor, String texto) {
        this.autor = autor;
        this.texto = texto;
        this.dataCriacao = LocalDateTime.now();
    }

    public String getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public String toString() {
        return "[" + dataCriacao.format(FORMATTER) + "] " + autor + ": " + texto;
    }
}
