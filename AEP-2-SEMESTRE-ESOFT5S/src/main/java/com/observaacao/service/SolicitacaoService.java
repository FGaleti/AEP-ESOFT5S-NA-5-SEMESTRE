package com.observaacao.service;

import com.observaacao.model.*;
import com.observaacao.repository.SolicitacaoRepository;
import com.observaacao.repository.CategoriaRepository;
import com.observaacao.repository.UsuarioRepository;
import com.observaacao.repository.LogAuditoriaRepository;
import com.observaacao.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço principal de Solicitações.
 * Centraliza toda lógica de negócio (validações, regras, auditoria).
 * 
 * Responsabilidades:
 * - Validação de dados de entrada
 * - Prevenção de abuso e duplicatas
 * - Geração de protocolo único
 * - Auditoria de todas as operações
 * - Gestão de anonimato e proteção de denunciantes
 */
@Service
public class SolicitacaoService {

    private static final int LIMITE_CADASTROS_POR_HORA = 10;
    private static final int DESCRICAO_MINIMA_ANONIMO = 20;
    private static final int DESCRICAO_MINIMA_IDENTIFICADO = 10;
    private static final double THRESHOLD_DUPLICATA = 0.8;

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final GeradorProtocoloService geradorProtocoloService;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                             CategoriaRepository categoriaRepository,
                             UsuarioRepository usuarioRepository,
                             LogAuditoriaRepository logAuditoriaRepository,
                             GeradorProtocoloService geradorProtocoloService) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.geradorProtocoloService = geradorProtocoloService;
    }

    // ─── CADASTRO COM VALIDAÇÃO COMPLETA ───

    @Transactional
    public Solicitacao cadastrar(CadastrarSolicitacaoDTO dto) {
        
        // 1. Validar campos obrigatórios
        validarCamposObrigatorios(dto);

        // 2. Recuperar ou criar usuário
        Usuario solicitante = recuperarOuCriarUsuario(dto);

        // 3. Validar regras de anonimato
        validarRegraAnonimato(solicitante, dto.getDescricao());

        // 4. Verificar limite de cadastros
        verificarLimiteCadastros(solicitante);

        // 5. Verificar duplicidade
        verificarDuplicidade(dto.getCategoriaId(), dto.getBairro(), 
                           dto.getDescricao(), solicitante);

        // 6. Recuperar categoria
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        // 7. Gerar protocolo único
        String protocolo = geradorProtocoloService.gerarProtocolo();

        // 8. Criar solicitação
        Solicitacao solicitacao = new Solicitacao(
                protocolo,
                categoria,
                dto.getDescricao(),
                dto.getBairro(),
                dto.getLocalizacao(),
                dto.getPrioridade(),
                solicitante
        );

        // 9. Salvar
        solicitacao = solicitacaoRepository.save(solicitacao);

        // 10. Registrar auditoria
        registrarLog(LogAuditoria.TipoEvento.CADASTRO, protocolo,
                solicitante.isAnonimo() ? "Anônimo" : solicitante.getNome(),
                String.format("Categoria: %s | Prioridade: %s (SLA: %d dias) | Bairro: %s",
                        categoria.getDescricao(), dto.getPrioridade().getDescricao(),
                        dto.getPrioridade().getPrazoEmDias(), dto.getBairro()),
                solicitante.isAnonimo());

        return solicitacao;
    }

    // ─── CONSULTA ───

    @Transactional(readOnly = true)
    public Solicitacao consultarPorProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            throw new IllegalArgumentException("Protocolo não pode ser vazio.");
        }

        Solicitacao solicitacao = solicitacaoRepository.findByProtocolo(protocolo.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação não encontrada para o protocolo: " + protocolo));

        // Registrar consulta em auditoria
        registrarLog(LogAuditoria.TipoEvento.CONSULTA, protocolo, "Cidadão",
                "Consulta de protocolo", solicitacao.getSolicitante().isAnonimo());

        return solicitacao;
    }

    // ─── LISTAR COM FILTROS ───

    @Transactional(readOnly = true)
    public List<Solicitacao> listarTodas() {
        return solicitacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Solicitacao> listarPorStatus(StatusSolicitacao status) {
        return solicitacaoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Solicitacao> listarPorPrioridade(Prioridade prioridade) {
        return solicitacaoRepository.findByPrioridade(prioridade);
    }

    @Transactional(readOnly = true)
    public List<Solicitacao> listarPorBairro(String bairro) {
        return solicitacaoRepository.findByBairro(bairro);
    }

    // ─── ATUALIZAR STATUS ───

    @Transactional
    public Solicitacao atualizarStatus(String protocolo, AtualizarStatusDTO dto) {
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);

        // Validar transição
        if (!solicitacao.getStatus().podeTransicionarPara(dto.getNovoStatus())) {
            throw new IllegalStateException(
                    String.format("Transição inválida: %s → %s",
                            solicitacao.getStatus().getDescricao(),
                            dto.getNovoStatus().getDescricao()));
        }

        // Atualizar status
        solicitacao.avancarStatus(dto.getNovoStatus(), dto.getNomeResponsavel(), 
                                 dto.getObservacao());

        solicitacao = solicitacaoRepository.save(solicitacao);

        // Registrar auditoria
        registrarLog(LogAuditoria.TipoEvento.ATUALIZACAO_STATUS, protocolo,
                dto.getNomeResponsavel(),
                String.format("Status: %s → %s | Observação: %s",
                        solicitacao.getStatus().getDescricao(),
                        dto.getNovoStatus().getDescricao(),
                        dto.getObservacao()),
                solicitacao.getSolicitante().isAnonimo());

        return solicitacao;
    }

    // ─── ADICIONAR COMENTÁRIO ───

    @Transactional
    public Solicitacao adicionarComentario(String protocolo, AdicionarComentarioDTO dto) {
        Solicitacao solicitacao = consultarPorProtocolo(protocolo);

        if (dto.getTexto() == null || dto.getTexto().trim().isEmpty()) {
            throw new IllegalArgumentException("Comentário não pode ser vazio.");
        }

        solicitacao.adicionarComentario(dto.getAutor(), dto.getTexto());
        solicitacao = solicitacaoRepository.save(solicitacao);

        // Registrar auditoria
        registrarLog(LogAuditoria.TipoEvento.ADICIONAR_COMENTARIO, protocolo,
                dto.getAutor(),
                String.format("Novo comentário: %s", dto.getTexto()),
                solicitacao.getSolicitante().isAnonimo());

        return solicitacao;
    }

    // ─── VALIDAÇÕES PRIVADAS ───

    private void validarCamposObrigatorios(CadastrarSolicitacaoDTO dto) {
        if (dto.getCategoriaId() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (dto.getDescricao() == null || dto.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (dto.getBairro() == null || dto.getBairro().trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório.");
        }
        if (dto.getLocalizacao() == null || dto.getLocalizacao().trim().isEmpty()) {
            throw new IllegalArgumentException("Localização é obrigatória.");
        }
        if (dto.getPrioridade() == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória.");
        }
    }

    private void validarRegraAnonimato(Usuario usuario, String descricao) {
        if (usuario.isAnonimo()) {
            if (descricao.trim().length() < DESCRICAO_MINIMA_ANONIMO) {
                throw new IllegalArgumentException(
                        String.format("Solicitação anônima exige descrição mais detalhada " +
                                "(mínimo %d caracteres). Você informou %d.",
                                DESCRICAO_MINIMA_ANONIMO, descricao.trim().length()));
            }
        } else {
            if (descricao.trim().length() < DESCRICAO_MINIMA_IDENTIFICADO) {
                throw new IllegalArgumentException(
                        String.format("Descrição é obrigatória (mínimo %d caracteres).",
                                DESCRICAO_MINIMA_IDENTIFICADO));
            }
        }
    }

    private void verificarLimiteCadastros(Usuario usuario) {
        List<Solicitacao> recentes = solicitacaoRepository.findAll().stream()
                .filter(s -> s.getSolicitante().getId().equals(usuario.getId()))
                .collect(Collectors.toList());

        if (recentes.size() >= LIMITE_CADASTROS_POR_HORA) {
            registrarLog(LogAuditoria.TipoEvento.TENTATIVA_ABUSO, null,
                    usuario.isAnonimo() ? "Anônimo" : usuario.getNome(),
                    "Tentativa de cadastro acima do limite",
                    usuario.isAnonimo());

            throw new IllegalArgumentException(
                    "Limite de cadastros atingido. Tente novamente mais tarde.");
        }
    }

    private void verificarDuplicidade(Long categoriaId, String bairro, String descricao,
                                     Usuario usuario) {
        List<Solicitacao> similares = solicitacaoRepository.findByCategoria_Id(categoriaId)
                .stream()
                .filter(s -> s.getBairro().equals(bairro))
                .filter(s -> calcularSimilaridade(s.getDescricao(), descricao) > THRESHOLD_DUPLICATA)
                .collect(Collectors.toList());

        if (!similares.isEmpty()) {
            registrarLog(LogAuditoria.TipoEvento.TENTATIVA_ABUSO, null,
                    usuario.isAnonimo() ? "Anônimo" : usuario.getNome(),
                    "Possível duplicata detectada",
                    usuario.isAnonimo());

            throw new IllegalArgumentException(
                    "Solicitação similar já existe. Verifique protocolos anteriores.");
        }
    }

    private double calcularSimilaridade(String a, String b) {
        Set<String> palavrasA = new HashSet<>(Arrays.asList(a.toLowerCase().split("\\s+")));
        Set<String> palavrasB = new HashSet<>(Arrays.asList(b.toLowerCase().split("\\s+")));

        Set<String> intersecao = new HashSet<>(palavrasA);
        intersecao.retainAll(palavrasB);

        Set<String> uniao = new HashSet<>(palavrasA);
        uniao.addAll(palavrasB);

        return uniao.isEmpty() ? 0 : (double) intersecao.size() / uniao.size();
    }

    private Usuario recuperarOuCriarUsuario(CadastrarSolicitacaoDTO dto) {
        if (dto.isAnonimo()) {
            // Persiste o usuário anônimo: a Solicitação possui um @ManyToOne
            // obrigatório para o solicitante, então ele não pode ser transiente.
            return usuarioRepository.save(Usuario.criarAnonimo());
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmailUsuario())
                .orElse(new Usuario(dto.getNomeUsuario(), dto.getEmailUsuario(),
                        dto.getTelefoneUsuario(), false));

        return usuarioRepository.save(usuario);
    }

    // ─── AUDITORIA ───

    private void registrarLog(LogAuditoria.TipoEvento tipo, String protocolo, String ator,
                             String descricao, boolean envolveAnonimato) {
        LogAuditoria log = new LogAuditoria(tipo, protocolo, ator, descricao, envolveAnonimato);
        logAuditoriaRepository.save(log);
    }
}
