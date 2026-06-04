package com.observaacao.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço para geração de protocolos únicos.
 * Formato: PROTOCOLO-YYYYMMDD-HHMMSS-SEQUENCIAL
 */
@Service
public class GeradorProtocoloService {

    private final AtomicLong sequencial = new AtomicLong(1000);

    public String gerarProtocolo() {
        LocalDateTime agora = LocalDateTime.now();
        String data = agora.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = sequencial.incrementAndGet();
        
        return String.format("PROT-%s-%04d", data, seq);
    }
}
