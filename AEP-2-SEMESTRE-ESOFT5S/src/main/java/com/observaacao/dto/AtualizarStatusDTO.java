package com.observaacao.dto;

import com.observaacao.model.StatusSolicitacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de atualização de status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarStatusDTO {
    private StatusSolicitacao novoStatus;
    private String observacao;
    private String nomeResponsavel;
}
