# 📖 FLUXOS E CASOS DE USO — ObservaAção Spring Boot

## 🎯 Casos de Uso Principais

### Caso 1: Cidadão Registra Solicitação de Iluminação
**Ator:** Cidadão Identificado  
**Objetivo:** Solicitar conserto de iluminação pública

#### Fluxo Principal
```
1. Cidadão acessa: POST /api/solicitacoes
2. Envia dados:
   - categoria: "Iluminação Pública" (ID=1)
   - descrição: "Lâmpada da esquina está apagada há 3 semanas"
   - bairro: "Centro"
   - localização: "Rua A, esquina com Rua B"
   - prioridade: "ALTA" (SLA: 5 dias)
   - identificado: nome, email, telefone

3. Service valida:
   ✅ Campos obrigatórios OK
   ✅ Descrição > 10 chars OK
   ✅ Limite de cadastros OK
   ✅ Sem duplicatas OK

4. Sistema cria Solicitacao:
   - Gera protocolo único: PROT-20260603140523-1001
   - Status: ABERTO
   - Prazo estimado: 5 dias (hoje + 5)

5. Registra em Log de Auditoria:
   - Evento: CADASTRO
   - Ator: João Silva
   - Descrição: Categoria Iluminação | Prioridade ALTA | Bairro Centro

6. Retorna 201 Created com protocolo
```

#### Respostas Possíveis
```json
// ✅ Sucesso (201)
{
  "protocolo": "PROT-20260603140523-1001",
  "categoria": "Iluminação Pública",
  "status": "ABERTO",
  "solicitante": "João Silva",
  "diasRestantes": 5,
  "atrasada": false
}

// ❌ Descrição muito curta (400)
{
  "mensagem": "Erro de validação",
  "detalhes": "Descrição é obrigatória (mínimo 10 caracteres).",
  "codigoHttp": 400
}

// ❌ Limite excedido (400)
{
  "mensagem": "Erro de validação",
  "detalhes": "Limite de cadastros atingido. Tente novamente mais tarde.",
  "codigoHttp": 400
}
```

---

### Caso 2: Denúncia Anônima de Assédio
**Ator:** Cidadão em Vulnerabilidade (Rafael, 19 anos)  
**Objetivo:** Denunciar assédio na escola SEM revelar identidade

#### Fluxo Principal
```
1. Cidadão acessa: POST /api/solicitacoes
2. Envia dados:
   - categoria: "Segurança" (ID=6)
   - descrição: "Presenciei e sofri assédio de professor na aula de educação física. 
                 Situação humilhante e discriminatória. Preciso que seja investigado com urgência"
   - bairro: "Vila Esperança"
   - localização: "Escola Municipal Vila Esperança"
   - prioridade: "CRITICA" (SLA: 2 dias)
   - anonimo: TRUE (⚠️ IMPORTANTE)

3. Service valida REGRA DE ANONIMATO:
   ✅ Descrição > 20 chars? SIM (tem ~180 chars)
   ✅ Sem identificação? SIM
   ✅ Flag anonimato ativada? SIM

4. Sistema cria Solicitacao com Usuario anônimo:
   - Nome do solicitante: "Anônimo"
   - Sem email, telefone
   - Flag anonimo=true

5. Registra em Log de Auditoria:
   - Evento: CADASTRO
   - Ator: "Anônimo"
   - Descrição: Categoria Segurança | Prioridade CRITICA
   - envolveAnonimato: TRUE ← Marca para proteção especial

6. Retorna protocolo APENAS (sem ligar dados pessoais)
```

#### Respostas Possíveis
```json
// ✅ Sucesso (201) - Sem identificação
{
  "protocolo": "PROT-20260603142000-1002",
  "categoria": "Segurança",
  "status": "ABERTO",
  "solicitante": "Anônimo",        // ← Anônimo!
  "anonimo": true,                 // ← Flag ativo
  "diasRestantes": 2,              // ← SLA CRITICA
  "atrasada": false
}

// ❌ Descrição muito curta para anônimo (400)
{
  "mensagem": "Erro de validação",
  "detalhes": "Solicitação anônima exige descrição mais detalhada (mínimo 20 caracteres). Você informou 15.",
  "codigoHttp": 400
}
```

#### Proteções Ativas
- ✅ Sem rastreamento de IP
- ✅ Sem dados pessoais vinculados
- ✅ Auditoria registra "Anônimo"
- ✅ SLA crítico (2 dias)
- ✅ Apenas gestor pode ver no painel com contexto

---

### Caso 3: Atendente Consulta Solicitação
**Ator:** Carlos (34 anos, Atendente da Ouvidoria)  
**Objetivo:** Conferir status de solicitação de cidadão

#### Fluxo Principal
```
1. Cidadão liga: "Qual é o status da minha solicitação?"
2. Carlos acessa: GET /api/solicitacoes/PROT-20260603140523-1001

3. Service executa:
   ✅ Valida se protocolo existe
   ✅ Recupera solicitacao do banco
   ✅ Calcula dias restantes/atraso
   ✅ Formata resposta com histórico completo

4. Registra em Log de Auditoria:
   - Evento: CONSULTA
   - Ator: "Cidadão"
   - Protocolo: PROT-20260603140523-1001

5. Retorna dados completos
```

#### Resposta (200 OK)
```json
{
  "protocolo": "PROT-20260603140523-1001",
  "categoria": "Iluminação Pública",
  "status": "TRIAGEM",
  "statusFormatado": "Em Triagem (3 dia(s) restante(s))",
  "descricao": "Lâmpada da esquina está apagada há 3 semanas",
  "bairro": "Centro",
  "prioridade": "ALTA",
  "solicitante": "João Silva",
  "anonimo": false,
  "dataCriacao": "2026-06-03T14:05:23",
  "prazoEstimado": "2026-06-08T14:05:23",
  "diasRestantes": 3,
  "diasAtraso": 0,
  "atrasada": false,
  "historico": [
    "null → ABERTO (Sistema) - Solicitação registrada",
    "ABERTO → TRIAGEM (Carlos - Atendente) - Solicitação recebida e categorizada"
  ],
  "comentarios": [
    "[Carlos - Atendente] Solicitação recebida. Acionando equipe de manutenção - 2026-06-03T14:30:00"
  ]
}
```

---

### Caso 4: Gestor Atualiza Status para "Em Execução"
**Ator:** Fernanda (41 anos, Gestora de Serviços Urbanos)  
**Objetivo:** Informar que equipe de campo saiu para corrigir iluminação

#### Fluxo Principal
```
1. Fernanda acessa: PATCH /api/solicitacoes/PROT-20260603140523-1001/status
2. Envia:
   {
     "novoStatus": "EM_EXECUCAO",
     "observacao": "Equipe enviada para local. ETA: 2 horas. Técnico: José Silva",
     "nomeResponsavel": "Fernanda - Gestora"
   }

3. Service valida MÁQUINA DE ESTADOS:
   ✅ Status atual: TRIAGEM
   ✅ Transição para EM_EXECUÇÃO permitida? SIM
   
4. Atualiza:
   - status = EM_EXECUCAO
   - Registra no histórico com timestamp
   - Commita transação

5. Registra auditoria:
   - Evento: ATUALIZACAO_STATUS
   - Ator: Fernanda - Gestora
   - Descrição: TRIAGEM → EM_EXECUÇÃO

6. Retorna atualizado
```

#### Fluxo de Erro (Transição Inválida)
```
1. Alguém tenta: PATCH /api/solicitacoes/PROT-xxxxx/status
   {
     "novoStatus": "ABERTO",  // ← Inválido! (só descer, não subir)
     ...
   }

2. Service valida:
   ✅ Status atual: EM_EXECUÇÃO
   ✅ Transição para ABERTO permitida? NÃO
   
3. Lança IllegalStateException

4. GlobalExceptionHandler captura:
   {
     "mensagem": "Operação inválida",
     "detalhes": "Transição inválida: Em Execução → Aberto",
     "codigoHttp": 409
   }
```

---

### Caso 5: Filtrar por Bairro
**Ator:** Gestor de Política Pública  
**Objetivo:** Ver todas as solicitações de um bairro específico

#### Fluxo Principal
```
1. Acessa: GET /api/solicitacoes?bairro=Centro

2. Service executa:
   repository.findByBairro("Centro")

3. JPA executa SQL:
   SELECT * FROM solicitacoes WHERE bairro = 'Centro'

4. Retorna array de solicitações do bairro
```

#### Respostas
```json
// ✅ Sucesso com múltiplos resultados
[
  {
    "protocolo": "PROT-20260603140523-1001",
    "categoria": "Iluminação Pública",
    "status": "EM_EXECUCAO",
    "bairro": "Centro",
    "diasRestantes": 3
  },
  {
    "protocolo": "PROT-20260603141500-1003",
    "categoria": "Buracos e Pavimento",
    "status": "ABERTO",
    "bairro": "Centro",
    "diasRestantes": 12
  }
]

// ✅ Sucesso com array vazio
[]
```

---

### Caso 6: Detecção de Duplicata
**Ator:** Cidadão (cadastrando segunda vez)  
**Objetivo:** Tentar registrar problema similar no mesmo bairro (bloqueado)

#### Fluxo Principal
```
1. Cidadão tenta: POST /api/solicitacoes
   {
     "categoriaId": 1,  // Iluminação
     "descricao": "Poste apagado na esquina da Rua A",  // ← Semelhante
     "bairro": "Centro",  // ← Mesmo bairro
     ...
   }

2. Service executa verificarDuplicidade():
   - Busca solicitações de Iluminação no Centro
   - Encontra: "Lâmpada da esquina apagada há 3 semanas"
   - Calcula Jaccard (palavras em comum): 0.85 > 0.80
   
3. Detecta duplicata! ⚠️

4. Registra Log de Auditoria:
   - Evento: TENTATIVA_ABUSO
   - Ator: João Silva
   - Descrição: Possível duplicata detectada

5. Lança IllegalArgumentException

6. Retorna 400
```

#### Resposta
```json
{
  "mensagem": "Erro de validação",
  "detalhes": "Solicitação similar já existe. Verifique protocolos anteriores.",
  "codigoHttp": 400
}
```

---

### Caso 7: Limite de Cadastros (Rate Limiting)
**Ator:** Spammer  
**Objetivo:** Tentar fazer 11 cadastros em uma hora (bloqueado)

#### Fluxo Principal
```
1-10. Spammer registra 10 solicitações (OK)
      Cada uma gera protocolo único

11. Tentativa de 11ª:
    Service valida verificarLimiteCadastros()
    Conta solicitações do usuário: 10 ≥ LIMITE (10)
    
    Bloqueia! ⚠️

    Registra Log:
    - Evento: TENTATIVA_ABUSO
    - Detalhes: "Tentativa de cadastro acima do limite"

12. Retorna 400
```

#### Resposta
```json
{
  "mensagem": "Erro de validação",
  "detalhes": "Limite de cadastros atingido. Tente novamente mais tarde.",
  "codigoHttp": 400
}
```

---

## 📊 Estados e Transições

### Diagrama de Estados
```
        ┌─────────┐
        │ ABERTO  │
        └────┬────┘
             │
        ┌────v────────────┐
        │   TRIAGEM       │
        └────┬─────┬──────┘
             │     │
        ┌────v─┐   │
        │EM_   │   │
        │EXEC  │   │
        └────┬─┘   │
             │     │
        ┌────v──┐  │
        │RESOLV │  │
        │IDO    │  │
        └────┬──┘  │
             │     │
        ┌────v──┬──v──┐
        │ENCERR │CANCE│
        │ADO    │LADO │
        └───────┴─────┘
```

### Regras
```
✅ ABERTO → TRIAGEM
✅ ABERTO → CANCELADO
✅ TRIAGEM → EM_EXECUÇÃO
✅ TRIAGEM → CANCELADO
✅ EM_EXECUÇÃO → RESOLVIDO
✅ EM_EXECUÇÃO → CANCELADO
✅ RESOLVIDO → ENCERRADO
✅ RESOLVIDO → EM_EXECUÇÃO (reabrir)
❌ Qualquer terminal → nada (ENCERRADO e CANCELADO são finais)
```

---

## 🔒 Regras de Negócio em Ação

### SLA por Prioridade
```
Solicitação criada em 03/06/2026 14:05:23

CRITICA    → Prazo: 05/06/2026 14:05:23 (2 dias)
ALTA       → Prazo: 08/06/2026 14:05:23 (5 dias)
MEDIA      → Prazo: 18/06/2026 14:05:23 (15 dias)
BAIXA      → Prazo: 03/07/2026 14:05:23 (30 dias)

Na consulta:
- Se hoje < prazo: "X dias restantes"
- Se hoje > prazo: "ATRASADO há X dias"
```

### Auditoria Completa
```
Log de Auditoria contém:
- Cada cadastro
- Cada consulta
- Cada atualização de status
- Cada comentário adicionado
- Cada tentativa de abuso bloqueada

Permite:
✅ Auditoria externa
✅ Detecção de padrões de abuso
✅ Conformidade LGPD
✅ Proteção de denunciantes
```

---

## 🎬 Fluxo Completo: Do Cidadão até Resolução

### Timeline Realista

```
06/03 14:05 - João registra iluminação apagada (PROT-001)
              Status: ABERTO
              SLA: 5 dias (até 08/06)

06/03 14:30 - Carlos consulta e muda para TRIAGEM
              Adiciona comentário: "Recebido"

06/04 09:00 - Fernanda muda para EM_EXECUÇÃO
              Comentário: "Equipe enviada"

06/04 11:30 - Técnico conserta lâmpada

06/04 13:00 - Fernanda muda para RESOLVIDO
              Comentário: "Lâmpada trocada com sucesso"

06/05 09:00 - Sistema auto-encerra (ou servidor confirma ENCERRADO)
              Histórico completo com 5 eventos

06/05 14:00 - João consulta protocolo:
              Status: ENCERRADO
              Histórico mostra toda jornada
              Comentários com feedbacks
```

---

## 📈 Relatório para Gestor (Futuro)

```
Com dados em memória, já temos base para gerar:

✅ Solicitações por bairro
✅ Tempo médio de resolução
✅ Taxa de atraso
✅ Prioridades mais recorrentes
✅ Detecção de tentativas de abuso
✅ Denúncias anônimas (sem exposição)
✅ Conformidade com SLA

Exemplo:
- Total: 150 solicitações
- Resolvidas: 120 (80%)
- Atrasadas: 15 (10%)
- Tempo médio: 7 dias
- Taxa de anonimato: 12%
- Tentativas de abuso bloqueadas: 3
```

---

## 🎯 Conclusão

A arquitetura permite fluxos **reais, robustos e auditados**, com:

✅ Validações em camadas  
✅ Máquina de estados segura  
✅ Proteção de anonimato efetiva  
✅ Auditoria completa  
✅ Prevenção de abuso  
✅ SLA rastreável  
✅ ODS 16 implementado na prática  

**Pronto para demonstração! 🚀**
