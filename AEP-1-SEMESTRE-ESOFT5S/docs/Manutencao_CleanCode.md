# RELATÓRIO DE CLEAN CODE — Projeto ObservaAção
## Manutenção de Software — Análise de 3 Funções/Métodos

---

## 1. Enum `StatusSolicitacao` — Máquina de Estados no Enum

**Localização:** `src/com/observaacao/model/StatusSolicitacao.java`

### Práticas de Clean Code aplicadas:

**a) Encapsulamento da regra de transição no próprio tipo (Tell, Don't Ask)**
- **O que foi feito:** Cada constante do enum implementa o método abstrato `proximosPermitidos()`, declarando quais transições são válidas a partir de si mesma. O método `podeTransicionarPara(destino)` encapsula a consulta.
- **Como aplicou:** Antes, a lógica de transição estava espalhada no `SolicitacaoService` com `if/else`. Agora, `ABERTO.podeTransicionarPara(TRIAGEM)` retorna `true` por definição do próprio enum — sem lógica externa.
- **Por que melhora manutenção:** Se um novo status surgir (ex.: `AGUARDANDO_MATERIAL`), basta adicioná-lo ao enum com seus `proximosPermitidos()`. Nenhuma outra classe precisa ser alterada. A regra de negócio vive junto ao dado, eliminando o risco de dessincronia.

**b) Self-documenting code (código autodocumentado)**
- **O que foi feito:** O fluxo `ABERTO → TRIAGEM → EM_EXECUCAO → RESOLVIDO → ENCERRADO` fica legível diretamente no enum, sem necessidade de diagrama externo.
- **Como aplicou:** Cada constante é um bloco que declara `Set.of(...)` — qualquer desenvolvedor lê e entende o fluxo sem comentário.
- **Por que melhora manutenção:** Em auditoria ou revisão de código, o revisor valida o fluxo inteiro em um único arquivo. Reduz documentação externa que desatualiza.

**c) Imutabilidade via `Set.of()` (Defensive Return)**
- **O que foi feito:** Os conjuntos retornados são `Set.of()` (imutáveis). Nenhum código externo pode adicionar transições indevidas.
- **Como aplicou:** `Set.of(TRIAGEM, CANCELADO)` retorna uma coleção que lança exceção se alguém tentar modificá-la.
- **Por que melhora manutenção:** Previne bugs sutis onde código externo poderia alterar as regras de transição em tempo de execução, garantindo integridade da máquina de estados.

---

## 2. Método `avancarStatus` — Classe `Solicitacao`

**Localização:** `src/com/observaacao/model/Solicitacao.java`

### Práticas de Clean Code aplicadas:

**a) Fail Fast com mensagem rica de contexto**
- **O que foi feito:** O método valida a transição antes de alterar qualquer estado. Se inválida, lança `IllegalStateException` com mensagem que inclui: de onde veio, para onde tentou ir, e o que era permitido.
- **Como aplicou:** `String.format("Transição inválida: %s → %s. Permitidos: %s", ...)` — a exceção é autoexplicativa.
- **Por que melhora manutenção:** Quando um gestor tenta avançar para status errado, o erro no log/tela explica o problema completo. Zero necessidade de debug.

**b) Responsabilidade Única (SRP) — transição + auditoria numa operação atômica**
- **O que foi feito:** `avancarStatus()` faz duas coisas inseparáveis: valida+transiciona **e** registra no histórico. São logicamente uma operação, não duas.
- **Como aplicou:** Chamada interna `registrarMovimentacao()` sempre acontece junto com a mudança de status. Impossível alterar status sem gerar histStatus.
- **Por que melhora manutenção:** Elimina a classe de bug mais comum em sistemas de workflow: "o status mudou mas o histórico não foi atualizado". A auditoria é garantida por design.

**c) Campos `final` e `Collections.unmodifiableList` (Imutabilidade defensiva)**
- **O que foi feito:** `protocolo`, `solicitante`, `dataCriacao`, e as listas internas são `final`. Os getters de lista retornam `Collections.unmodifiableList()`.
- **Como aplicou:** Nenhum código externo pode alterar o protocolo ou injetar elementos na lista de histórico diretamente.
- **Por que melhora manutenção:** Em projetos com múltiplos desenvolvedores, impede que alguém faça `solicitacao.getHistoricoStatus().add(...)` por fora, quebrando a integridade da auditoria.

---

## 3. Classe `FilaAtendimento` — Fila com `PriorityQueue`

**Localização:** `src/com/observaacao/model/FilaAtendimento.java`

### Práticas de Clean Code aplicadas:

**a) Composição de Comparator com API fluente**
- **O que foi feito:** O comparador é declarado como constante estática com `Comparator.comparingInt(...).thenComparing(...)` — primeiro prioridade, depois data de criação (FIFO dentro da mesma prioridade).
- **Como aplicou:** `COMPARADOR_FILA` é um `Comparator<Solicitacao>` reutilizado pela `PriorityQueue` e pelos métodos de listagem. Não há lógica de comparação duplicada.
- **Por que melhora manutenção:** Se o critério de ordenação mudar (ex.: adicionar peso por categoria), há um único ponto de alteração. Todos os métodos recebem a mudança automaticamente.

**b) Nomes de método orientados ao domínio**
- **O que foi feito:** Métodos como `listarAguardandoAtendimento()`, `listarEmExecucao()`, `proximaDaFila()`, `contarAguardando()` usam vocabulário do domínio público, não termos técnicos.
- **Como aplicou:** Em vez de `getPending()` ou `filterByStatus()`, o código lê como o gestor pensa: "listar aguardando atendimento", "próxima da fila".
- **Por que melhora manutenção:** Um gestor público ou analista de negócio consegue validar o código lendo os nomes dos métodos. Reduz traduções mentais e erros de interpretação.

**c) Extração de predicado privado `isAguardandoAtendimento` (DRY)**
- **O que foi feito:** O filtro que define "solicitação aguardando" — `ABERTO || TRIAGEM` — foi extraído para um método privado reutilizado em `listarAguardandoAtendimento()`, `contarAguardando()` e `adicionar()`.
- **Como aplicou:** Se o conceito de "aguardando" mudar (ex.: incluir um futuro status `EM_ESPERA`), altera-se **um único método**.
- **Por que melhora manutenção:** Elimina duplicação de lógica condicional que, quando espalhada, inevitavelmente evolui de forma inconsistente em diferentes partes do código.

---

## Conclusão

As práticas aplicadas convergem em 3 pilares da manutenibilidade:

| Pilar | Práticas aplicadas | Benefício |
|-------|-------------------|-----------|
| **Legibilidade** | Enum autodocumentado, nomes de domínio, comparator fluente | O fluxo do sistema é legível direto no código |
| **Modificabilidade** | Máquina de estados no enum, predicado extraído, comparator centralizado | Novo status = 1 ponto de alteração |
| **Confiabilidade** | Fail fast, imutabilidade, auditoria atômica | Impossível transição sem registro, impossível modificação externa |

Essas práticas são essenciais em um sistema voltado ao cidadão (ODS 16), onde rastreabilidade e evolução contínua são requisitos, não luxos.
