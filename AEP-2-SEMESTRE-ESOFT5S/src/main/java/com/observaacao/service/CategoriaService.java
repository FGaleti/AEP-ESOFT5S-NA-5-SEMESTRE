package com.observaacao.service;

import com.observaacao.model.Categoria;
import com.observaacao.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Serviço para gestão de categorias.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria obterPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
    }

    @Transactional
    public Categoria criar(Categoria categoria) {
        if (categoria.getDescricao() == null || categoria.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da categoria é obrigatória");
        }
        return categoriaRepository.save(categoria);
    }
}
