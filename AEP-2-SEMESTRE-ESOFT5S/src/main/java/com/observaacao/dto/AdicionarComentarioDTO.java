package com.observaacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de adicionar comentário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdicionarComentarioDTO {
    private String texto;
    private String autor;
}
