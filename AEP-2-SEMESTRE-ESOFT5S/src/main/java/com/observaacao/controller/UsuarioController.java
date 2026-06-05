package com.observaacao.controller;

import com.observaacao.model.Usuario;
import com.observaacao.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para Usuários (área administrativa).
 * Fornece total e lista de usuários cadastrados para o painel Admin.
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * GET /api/usuarios
     * Lista todos os usuários cadastrados.
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    /**
     * GET /api/usuarios/count
     * Total de usuários cadastrados.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contar() {
        return ResponseEntity.ok(Map.of("total", usuarioRepository.count()));
    }
}
