package com.observaacao.dto;

import com.observaacao.model.StatusSolicitacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para um item do histórico de status de uma solicitação.
 * Estruturado para facilitar a renderização no front-end (Tela "Comprovante").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoItemDTO {
    private StatusSolicitacao statusAnterior;
    private String statusAnteriorLabel;
    private StatusSolicitacao statusNovo;
    private String statusNovoLabel;
    private String responsavel;
    private String observacao;
    private LocalDateTime dataMovimentacao;
}
