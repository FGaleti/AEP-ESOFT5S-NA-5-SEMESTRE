package com.observaacao.controller;

import com.observaacao.model.Solicitacao;
import com.observaacao.model.StatusSolicitacao;
import com.observaacao.model.Prioridade;
import com.observaacao.service.SolicitacaoService;
import com.observaacao.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para Solicitações.
 * 
 * Responsabilidade: Expor endpoints HTTP
 * Não contém lógica de negócio (delegada ao Service)
 */
@RestController
@RequestMapping("/api/solicitacoes")
@CrossOrigin(origins = "*")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    /**
     * POST /api/solicitacoes
     * Cadastra uma nova solicitação
     */
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody CadastrarSolicitacaoDTO dto) {
        try {
            Solicitacao solicitacao = solicitacaoService.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toDTO(solicitacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDTO(
                            "Erro ao cadastrar solicitação",
                            e.getMessage(),
                            400));
        }
    }

    /**
     * GET /api/solicitacoes/{protocolo}
     * Consulta uma solicitação por protocolo
     */
    @GetMapping("/{protocolo}")
    public ResponseEntity<?> consultar(@PathVariable String protocolo) {
        try {
            Solicitacao solicitacao = solicitacaoService.consultarPorProtocolo(protocolo);
            return ResponseEntity.ok(toDTO(solicitacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDTO(
                            "Solicitação não encontrada",
                            e.getMessage(),
                            404));
        }
    }

    /**
     * GET /api/solicitacoes
     * Lista todas as solicitações (com filtros opcionais)
     */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) StatusSolicitacao status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) String bairro) {

        List<Solicitacao> solicitacoes;

        if (status != null) {
            solicitacoes = solicitacaoService.listarPorStatus(status);
        } else if (prioridade != null) {
            solicitacoes = solicitacaoService.listarPorPrioridade(prioridade);
        } else if (bairro != null && !bairro.isEmpty()) {
            solicitacoes = solicitacaoService.listarPorBairro(bairro);
        } else {
            solicitacoes = solicitacaoService.listarTodas();
        }

        List<SolicitacaoResponseDTO> dtos = solicitacoes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * PATCH /api/solicitacoes/{protocolo}/status
     * Atualiza o status de uma solicitação
     */
    @PatchMapping("/{protocolo}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable String protocolo,
            @RequestBody AtualizarStatusDTO dto) {

        try {
            Solicitacao solicitacao = solicitacaoService.atualizarStatus(protocolo, dto);
            return ResponseEntity.ok(toDTO(solicitacao));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponseDTO(
                            "Transição de status inválida",
                            e.getMessage(),
                            409));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDTO(
                            "Solicitação não encontrada",
                            e.getMessage(),
                            404));
        }
    }

    /**
     * POST /api/solicitacoes/{protocolo}/comentarios
     * Adiciona um comentário a uma solicitação
     */
    @PostMapping("/{protocolo}/comentarios")
    public ResponseEntity<?> adicionarComentario(
            @PathVariable String protocolo,
            @RequestBody AdicionarComentarioDTO dto) {

        try {
            Solicitacao solicitacao = solicitacaoService.adicionarComentario(protocolo, dto);
            return ResponseEntity.ok(toDTO(solicitacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDTO(
                            "Erro ao adicionar comentário",
                            e.getMessage(),
                            400));
        }
    }

    // ─── HELPER METHOD ───

    private SolicitacaoResponseDTO toDTO(Solicitacao solicitacao) {
        return SolicitacaoResponseDTO.builder()
                .id(solicitacao.getId())
                .protocolo(solicitacao.getProtocolo())
                .categoria(solicitacao.getCategoria().getDescricao())
                .descricao(solicitacao.getDescricao())
                .bairro(solicitacao.getBairro())
                .localizacao(solicitacao.getLocalizacao())
                .prioridade(solicitacao.getPrioridade())
                .status(solicitacao.getStatus())
                .statusFormatado(solicitacao.getStatusFormatado())
                .solicitante(solicitacao.getSolicitante().isAnonimo() 
                        ? "Anônimo" 
                        : solicitacao.getSolicitante().getNome())
                .anonimo(solicitacao.getSolicitante().isAnonimo())
                .dataCriacao(solicitacao.getDataCriacao())
                .prazoEstimado(solicitacao.getPrazoEstimado())
                .diasRestantes(solicitacao.getDiasRestantes())
                .diasAtraso(solicitacao.getDiasAtraso())
                .atrasada(solicitacao.isAtrasada())
                .historico(solicitacao.getHistoricoStatus().stream()
                        .map(h -> String.format("%s → %s (%s) - %s",
                                h.getStatusAnterior() == null ? "INÍCIO" : h.getStatusAnterior(),
                                h.getStatusNovo(),
                                h.getResponsavel(),
                                h.getObservacao()))
                        .collect(Collectors.toList()))
                .comentarios(solicitacao.getComentarios().stream()
                        .map(c -> String.format("[%s] %s - %s",
                                c.getAutor(),
                                c.getTexto(),
                                c.getDataCriacao()))
                        .collect(Collectors.toList()))
                .build();
    }
}
