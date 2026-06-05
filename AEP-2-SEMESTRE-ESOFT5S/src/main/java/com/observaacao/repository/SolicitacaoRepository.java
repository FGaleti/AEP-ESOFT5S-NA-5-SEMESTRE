package com.observaacao.repository;

import com.observaacao.model.Solicitacao;
import com.observaacao.model.StatusSolicitacao;
import com.observaacao.model.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para Solicitação.
 * Inicialmente simulado em memória, pode ser estendido para JPA real.
 */
@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    Optional<Solicitacao> findByProtocolo(String protocolo);

    List<Solicitacao> findByStatus(StatusSolicitacao status);

    List<Solicitacao> findByPrioridade(Prioridade prioridade);

    List<Solicitacao> findByBairro(String bairro);

    List<Solicitacao> findByCategoria_Id(Long categoriaId);

    List<Solicitacao> findAll();
}
