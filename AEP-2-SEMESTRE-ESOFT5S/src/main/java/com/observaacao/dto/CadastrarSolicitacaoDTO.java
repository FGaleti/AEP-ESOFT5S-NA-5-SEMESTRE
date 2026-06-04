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
 * DTO para requisição de cadastro de solicitação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CadastrarSolicitacaoDTO {
    private Long categoriaId;
    private String descricao;
    private String bairro;
    private String localizacao;
    private Prioridade prioridade;
    private String nomeUsuario;
    private String emailUsuario;
    private String telefoneUsuario;
    private boolean anonimo;
}
