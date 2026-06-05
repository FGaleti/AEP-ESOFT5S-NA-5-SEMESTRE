package com.observaacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para um comentário de uma solicitação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioDTO {
    private String autor;
    private String texto;
    private LocalDateTime dataCriacao;
}
