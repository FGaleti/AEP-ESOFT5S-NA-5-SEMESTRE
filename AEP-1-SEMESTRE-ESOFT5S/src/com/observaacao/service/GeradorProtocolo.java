package com.observaacao.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GeradorProtocolo {
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String gerar() {
        String data = LocalDate.now().format(FORMATO_DATA);
        int sequencia = 1000 + random.nextInt(9000);
        return "OBS-" + data + "-" + sequencia;
    }
}
