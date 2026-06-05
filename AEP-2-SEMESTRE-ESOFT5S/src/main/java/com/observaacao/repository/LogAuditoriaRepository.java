package com.observaacao.repository;

import com.observaacao.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositório para Log de Auditoria.
 */
@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    List<LogAuditoria> findByProtocolo(String protocolo);

    List<LogAuditoria> findByAtor(String ator);
}
