package com.observaacao.model;

import java.time.LocalDateTime;

public class Anexo {
    private String nomeArquivo;
    private String caminhoArquivo;
    private LocalDateTime dataEnvio;

    public Anexo(String nomeArquivo, String caminhoArquivo) {
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.dataEnvio = LocalDateTime.now();
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    @Override
    public String toString() {
        return "Anexo: " + nomeArquivo + " (enviado em " + dataEnvio + ")";
    }
}
