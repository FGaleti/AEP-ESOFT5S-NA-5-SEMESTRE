package com.observaacao.model;

/**
 * Prioridade com SLA (Service Level Agreement) e impacto social.
 * Cada nível define prazos-alvo e o grau de impacto na comunidade,
 * alinhado à ODS 16 — transparência e prestação de contas.
 */
public enum Prioridade {

    BAIXA("Baixa", 30,
            "Impacto localizado, sem risco à segurança",
            "Manutenção preventiva, melhorias estéticas"),

    MEDIA("Média", 15,
            "Afeta conforto ou mobilidade de grupo de cidadãos",
            "Poda de árvore, limpeza de terreno baldio"),

    ALTA("Alta", 5,
            "Risco à saúde ou segurança de comunidade vulnerável",
            "Iluminação pública apagada, buraco em via principal"),

    CRITICA("Crítica", 2,
            "Risco iminente à vida ou direitos fundamentais",
            "Desabamento, falta de água em hospital, denúncia de violência");

    private final String descricao;
    private final int prazoEmDias;
    private final String impactoSocial;
    private final String exemploContexto;

    Prioridade(String descricao, int prazoEmDias, String impactoSocial, String exemploContexto) {
        this.descricao = descricao;
        this.prazoEmDias = prazoEmDias;
        this.impactoSocial = impactoSocial;
        this.exemploContexto = exemploContexto;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPrazoEmDias() {
        return prazoEmDias;
    }

    public String getImpactoSocial() {
        return impactoSocial;
    }

    public String getExemploContexto() {
        return exemploContexto;
    }

    public String getSlaFormatado() {
        return String.format("%s — SLA: %d dia(s) | Impacto: %s", descricao, prazoEmDias, impactoSocial);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
