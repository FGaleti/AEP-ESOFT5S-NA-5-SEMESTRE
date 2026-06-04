package com.observaacao.dto;

import com.observaacao.model.Prioridade;
import com.observaacao.model.StatusSolicitacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para resposta de consulta de solicitação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoResponseDTO {
    private Long id;
    private String protocolo;
    private String categoria;
    private String descricao;
    private String bairro;
    private String localizacao;
    private Prioridade prioridade;
    private StatusSolicitacao status;
    private String statusFormatado;
    private String solicitante;
    private boolean anonimo;
    private LocalDateTime dataCriacao;
    private LocalDateTime prazoEstimado;
    private long diasRestantes;
    private long diasAtraso;
    private boolean atrasada;
    private List<String> historico;
    private List<String> comentarios;
}
