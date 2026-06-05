package com.observaacao.controller;

import com.observaacao.model.Categoria;
import com.observaacao.service.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller REST para Categorias.
 */
@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * GET /api/categorias
     * Lista todas as categorias
     */
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    /**
     * GET /api/categorias/{id}
     * Obtém uma categoria por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obter(@PathVariable Long id) {
        try {
            Categoria categoria = categoriaService.obterPorId(id);
            return ResponseEntity.ok(categoria);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Categoria não encontrada");
        }
    }

    /**
     * POST /api/categorias
     * Cria uma nova categoria
     */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Categoria categoria) {
        try {
            Categoria novaCategoria = categoriaService.criar(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaCategoria);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
