# RELATÓRIO DE CLEAN CODE — Projeto ObservaAção
## Manutenção de Software — Análise de 3 Funções/Métodos

---

## 1. Enum `Prioridade` — SLA e Impacto Social no Próprio Tipo

**Localização:** `src/com/observaacao/model/Prioridade.java`

### Práticas de Clean Code aplicadas:

**a) Eliminação de dados hardcoded (Magic Numbers)**
- **O que foi feito:** Antes, os prazos de SLA eram definidos em um `switch` dentro de `Solicitacao.calcularPrazo()` com valores soltos (`2`, `5`, `15`, `30`). Agora, cada constante do enum carrega `prazoEmDias`, `impactoSocial` e `exemploContexto` como campos finais.
- **Como aplicou:** `CRITICA("Crítica", 2, "Risco iminente à vida...", "Desabamento...")` — o prazo vive junto ao dado, não espalhado em lógica externa. O método `calcularPrazo()` em `Solicitacao` ficou uma linha: `dataCriacao.plusDays(prioridade.getPrazoEmDias())`.
- **Por que melhora manutenção:** Se o gestor decidir alterar o SLA de "Alta" de 5 para 7 dias, há **um único ponto de alteração** (o enum). Zero risco de esquecer de atualizar a View ou o Service.

**b) Self-documenting code com getSlaFormatado()**
- **O que foi feito:** O método `getSlaFormatado()` retorna uma string legível: `"Alta — SLA: 5 dia(s) | Impacto: Risco à saúde..."`. A View usa esse método diretamente, sem formatação manual.
- **Como aplicou:** A `CadastroSolicitacaoView.coletarPrioridade()` lista as prioridades chamando `getSlaFormatado()` — qualquer desenvolvedor entende o que será exibido sem ler a View.
- **Por que melhora manutenção:** O enum auto-descreve suas regras de negócio. Em auditoria, basta ler `Prioridade.java` para validar todos os SLAs do sistema.

**c) Coesão: dados + comportamento juntos (Tell, Don't Ask)**
- **O que foi feito:** `impactoSocial` e `exemploContexto` são campos do enum, não de uma classe auxiliar ou configuração externa. A prioridade **sabe** seu impacto.
- **Como aplicou:** Em vez de a View perguntar "qual o impacto da prioridade X?" e montar a string, o enum responde com `getImpactoSocial()`.
- **Por que melhora manutenção:** Adicionar uma nova prioridade (ex.: `URGENTE`) exige apenas adicionar uma constante com todos os dados. Nenhuma classe externa precisa ser modificada.

---

## 2. Método `verificarDuplicidade` — Classe `SolicitacaoService`

**Localização:** `src/com/observaacao/service/SolicitacaoService.java`

### Práticas de Clean Code aplicadas:

**a) Método com responsabilidade única e nome descritivo**
- **O que foi feito:** A detecção de possíveis duplicatas foi isolada em `verificarDuplicidade(categoria, bairro, descricao, solicitante)` — método privado que é chamado dentro de `cadastrar()` junto com as demais validações.
- **Como aplicou:** Antes, o cadastro não verificava duplicatas. Agora, `cadastrar()` lê como uma narrativa: `validarCamposObrigatorios(...)`, `validarRegraAnonimato(...)`, `verificarLimiteCadastros(...)`, `verificarDuplicidade(...)`. Cada validação tem seu método.
- **Por que melhora manutenção:** Se o critério de duplicata mudar (ex.: incluir localização no filtro), há um único método para alterar. Os demais métodos de validação não são afetados.

**b) Fail Fast com log de auditoria integrado**
- **O que foi feito:** Quando uma possível duplicata é detectada, o método registra um `LogAuditoria` do tipo `TENTATIVA_ABUSO` **antes** de lançar a exceção. Isso garante rastreabilidade mesmo quando a operação é negada.
- **Como aplicou:** `registrarLog(TipoEvento.TENTATIVA_ABUSO, null, ator, "Possível duplicata...", anonimo)` seguido de `throw new IllegalArgumentException(...)`. O gestor pode consultar as tentativas bloqueadas no painel de auditoria.
- **Por que melhora manutenção:** Efeitos colaterais (log) e pré-condições (validação) são explícitos. Nenhum desenvolvedor precisará adivinhar onde as tentativas de abuso são registradas — está dentro do próprio método de verificação.

**c) Extração de `calcularSimilaridade()` como primitiva reutilizável (DRY)**
- **O que foi feito:** A comparação entre descrições usa coeficiente de Jaccard por palavras, extraído em método privado `calcularSimilaridade(a, b)` que retorna `double` entre 0.0 e 1.0.
- **Como aplicou:** `verificarDuplicidade()` chama `calcularSimilaridade(descExistente, descNova) > 0.8`. Se amanhã o método for necessário em outro contexto (ex.: sugerir solicitações similares ao cidadão), basta torná-lo `public`.
- **Por que melhora manutenção:** O threshold (`0.8`) e o algoritmo estão isolados. Trocar por Levenshtein ou TF-IDF no futuro requer alterar apenas `calcularSimilaridade()`, sem tocar na lógica de duplicate detection.

---

## 3. Método `validarRegraAnonimato` — Classe `SolicitacaoService`

**Localização:** `src/com/observaacao/service/SolicitacaoService.java`

### Práticas de Clean Code aplicadas:

**a) Regra de negócio com constantes nomeadas (evitar Magic Numbers)**
- **O que foi feito:** Os mínimos de caracteres para descrição são constantes de classe: `DESCRICAO_MINIMA_ANONIMO = 20` e `DESCRICAO_MINIMA_IDENTIFICADO = 10`. A diferença materializa a decisão técnica: anônimos precisam compensar a ausência de identificação com mais detalhes.
- **Como aplicou:** `if (descricao.trim().length() < DESCRICAO_MINIMA_ANONIMO)` — o nome da constante explica **por quê** o mínimo é maior. Sem constante, seria um `20` mágico que ninguém saberia justificar.
- **Por que melhora manutenção:** O gestor pode ajustar os mínimos em um único lugar. A constante nomeada serve como documentação inline — não precisa de comentário para explicar o `20`.

**b) Bifurcação clara por perfil de usuário (Guard Clause / Cláusula de guarda)**
- **O que foi feito:** O método valida diferente dependendo de `solicitante.isAnonimo()`. Para anônimo: exige descrição detalhada. Para identificado: exige nome e e-mail.
- **Como aplicou:** A cláusula `if (solicitante.isAnonimo())` vem primeiro (guard clause). Se anônimo, valida descrição e retorna. Se identificado, segue para validação de dados pessoais. Fluxo linear, sem `else` aninhado.
- **Por que melhora manutenção:** Cada perfil tem seu bloco isolado. Adicionar uma regra para um novo perfil (ex.: servidor autenticado) é adicionar um `else if` sem mexer nos demais.

**c) Mensagem de exceção com contexto (ajuda o cidadão)**
- **O que foi feito:** Quando o anônimo não atinge o mínimo, a mensagem inclui o mínimo esperado **e** quantos caracteres foram informados: `"Solicitação anônima exige descrição mais detalhada (mínimo 20 caracteres). Você informou 12."`.
- **Como aplicou:** `String.format("...mínimo %d caracteres. Você informou %d.", DESCRICAO_MINIMA_ANONIMO, descricao.trim().length())`.
- **Por que melhora manutenção:** A mensagem é autoexplicativa. O atendente ou cidadão entende exatamente o que corrigir. Zero necessidade de consultar documentação externa ou debug.

---

## Conclusão

As práticas aplicadas convergem em 3 pilares da manutenibilidade:

| Pilar | Práticas aplicadas | Benefício |
|-------|-------------------|-----------|
| **Legibilidade** | Enum com SLA autodescritivo, constantes nomeadas, mensagens ricas | O sistema é legível como especificação |
| **Modificabilidade** | SLA no enum, similaridade extraída, guard clauses por perfil | Novo SLA ou regra = 1 ponto de alteração |
| **Confiabilidade** | Fail fast com log, auditoria integrada, imutabilidade | Impossível abuso sem registro; impossível modificação externa |

Essas práticas são essenciais em um sistema voltado ao cidadão (ODS 16), onde rastreabilidade e evolução contínua são requisitos, não luxos.

---

## Adendo — Clean Code nas demais funcionalidades

| Método/Classe | Prática aplicada | Onde |
|---------------|------------------|------|
| `Prioridade.getSlaFormatado()` | Self-documenting — exibe SLA sem lógica na View | `Prioridade.java` |
| `Solicitacao.calcularPrazo()` | Eliminação de Magic Numbers — usa `prioridade.getPrazoEmDias()` | `Solicitacao.java` |
| `SolicitacaoService.verificarLimiteCadastros()` | Rate limiting com log de auditoria — previne spam | `SolicitacaoService.java` |
| `SolicitacaoService.cadastrar()` | Narrativa de validação — cada regra é um método com nome descritivo | `SolicitacaoService.java` |
| `LogAuditoria` (classe) | Imutabilidade total (`final`) — registro de auditoria não pode ser alterado | `LogAuditoria.java` |
| `StatusSolicitacao.podeTransicionarPara()` | Máquina de estados no enum — transições declarativas | `StatusSolicitacao.java` |
| `FilaAtendimento.COMPARADOR_FILA` | Composição fluente de Comparator — critério centralizado | `FilaAtendimento.java` |
| `GestaoSolicitacaoView.verLogAuditoria()` | Auditoria acessível — getor consulta tentativas de abuso | `GestaoSolicitacaoView.java` |
