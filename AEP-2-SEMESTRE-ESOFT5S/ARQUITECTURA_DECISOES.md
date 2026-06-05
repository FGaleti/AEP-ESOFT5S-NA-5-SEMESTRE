# Decisões Arquiteturais — ObservaAção Spring Boot

## 📝 Resumo Executivo

Esta é a documentação das decisões tomadas na **2ª entrega** do projeto ObservaAção, migrando de Java CLI puro para **Spring Boot 3.2.0 com arquitetura em camadas profissional**.

---

## 1. Por que Spring Boot?

### Justificativa
- ✅ **Produção-ready**: Configuração mínima, convenção sobre configuração
- ✅ **Escalabilidade**: REST API nativa, pronta para múltiplos clientes
- ✅ **Manutenibilidade**: Separação clara de camadas via Spring
- ✅ **Injeção de Dependência**: Loose coupling, fácil testar
- ✅ **Comunidade**: Ecossistema maduro, documentação excelente
- ✅ **ODS 16**: Instituições eficazes = sistemas bem arquitetados

### Alternativas Rejeitadas
- ❌ Quarkus: Overkill para escopo atual
- ❌ Micronaut: Comunidade menor
- ❌ Play Framework: Sintaxe diferente, curva de aprendizado

---

## 2. Arquitetura em Camadas

### Decisão: MVC Estratificado

```
┌─────────────────────────────────┐
│      PRESENTATION LAYER         │
│   SolicitacaoController.java    │
│   ↓ (HTTP Requests/Responses)   │
├─────────────────────────────────┤
│    BUSINESS LOGIC LAYER         │
│ SolicitacaoService.java         │
│ (Validações, Regras, Auditoria) │
│ ↓ (Delegação de Responsabilidade)
├─────────────────────────────────┤
│   PERSISTENCE LAYER             │
│ SolicitacaoRepository.java      │
│ (CRUD, Queries)                 │
│ ↓ (SQL via JPA/Hibernate)       │
├─────────────────────────────────┤
│     DATABASE LAYER              │
│  H2 In-Memory Database          │
└─────────────────────────────────┘
```

### Razão
- **Separação de Responsabilidades**: Cada camada tem um propósito único
- **Testabilidade**: Services podem ser testados sem HTTP
- **Reutilização**: Services podem ser reutilizados por CLI, gRPC, etc.
- **Manutenção**: Mudanças em uma camada não afetam as outras

### Exemplo: Onde vive a lógica?

| Lógica | Onde? | Por quê? |
|--------|-------|---------|
| Validação de entrada (JSON) | Controller | Responsabilidade da API |
| Validação de regra (anonimato) | Service | Regra de negócio |
| Prevenção de duplicatas | Service | Regra de negócio |
| Consulta ao banco | Repository | Responsabilidade de persistência |
| Formatação de resposta | DTO | Separar model interno da API |

---

## 3. Controllers Magros vs Services Gordos

### Decisão: Lógica NO Service, NÃO no Controller

#### ❌ ERRADO (Lógica no Controller)
```java
@PostMapping
public ResponseEntity<?> cadastrar(@RequestBody DTO dto) {
    // ❌ LÓGICA AQUI — NÃO FAZER!
    if (dto.getDescricao().length() < 20 && dto.isAnonimo()) {
        return ResponseEntity.badRequest().build();
    }
    // ... mais validações ...
    solicitacao = repository.save(...);
}
```

**Problemas:**
- Controller cresce demais
- Impossível testar lógica sem HTTP
- Duplicação de código
- Violação de SRP

#### ✅ CORRETO (Lógica no Service)
```java
@PostMapping
public ResponseEntity<?> cadastrar(@RequestBody DTO dto) {
    // ✅ Apenas orquestra
    Solicitacao sol = solicitacaoService.cadastrar(dto);
    return ResponseEntity.status(201).body(toDTO(sol));
}

// Em SolicitacaoService:
@Transactional
public Solicitacao cadastrar(DTO dto) {
    validarRegraAnonimato(dto);        // ✅ REGRA AQUI
    verificarDuplicidade(dto);         // ✅ REGRA AQUI
    // ...
}
```

**Benefícios:**
- Controller legível (5-10 linhas)
- Service testável sem HTTP
- Reutilização em outros contextos

---

## 4. Por que Usar DTOs?

### Decisão: DTO ≠ Model

```
┌──────────────────────────────────────────────────┐
│ REQUEST JSON (Client)                            │
└─────────────────────┬──────────────────────────┘
                      │
                      ↓
        ┌─────────────────────────────┐
        │ CadastrarSolicitacaoDTO     │
        │ - categoriaId               │
        │ - descricao                 │
        │ (Apenas o que o client      │
        │  precisa enviar)            │
        └──────────────┬──────────────┘
                       │ (Conversão no Service)
                       ↓
        ┌──────────────────────────────┐
        │ Solicitacao (Entity/Model)   │
        │ - id (gerado)                │
        │ - protocolo (gerado)         │
        │ - dataCriacao (now)          │
        │ - status (ABERTO)            │
        │ - historico (collections)    │
        │ (Tudo que o sistema sabe)    │
        └──────────────┬───────────────┘
                       │ (Conversão no Controller)
                       ↓
        ┌──────────────────────────────┐
        │ SolicitacaoResponseDTO       │
        │ - protocolo                  │
        │ - status                     │
        │ - diasRestantes              │
        │ (Apenas o que o client       │
        │  precisa saber)              │
        └──────────────┬───────────────┘
                       │
                       ↓
        ┌──────────────────────────────┐
        │ RESPONSE JSON (Client)       │
        └──────────────────────────────┘
```

### Razão
- **Evolução**: Adicionar campo no Model não quebra API
- **Segurança**: Não expor campos internos (ex: senhas, IDs de tabela)
- **Validação**: Validar DTO na entrada, não Model
- **Separação**: Model = banco, DTO = API

### Exemplo Crítico: Anonimato

```java
// Entity Model (interno do sistema)
public class Solicitacao {
    private String ipAddress;      // ⚠️ Info sensível
    private Usuario solicitante;   // Pode ter dados pessoais
}

// Response DTO (o que enviamos)
public class SolicitacaoResponseDTO {
    private String solicitante; // "Anônimo" ou "João" conforme caso
    // SEM ipAddress, sem dados sensíveis
}
```

---

## 5. Banco de Dados: Por que H2 em Memória?

### Decisão: H2 para 2ª Entrega, PostgreSQL (futura 3ª)

#### H2 em Memória (Agora)
```properties
spring.datasource.url=jdbc:h2:mem:observacao_db
spring.jpa.hibernate.ddl-auto=create-drop
```

**Vantagens:**
- ✅ Zero configuração
- ✅ Dados reset a cada execução (desenvolvimento limpo)
- ✅ Velocidade (memória)
- ✅ JPA já configurado (fácil migrate para PostgreSQL)
- ✅ H2 Console para debug

**Desvantagens:**
- ❌ Dados perdidos ao restartar
- ❌ Monoprocesso (não escalável)

#### Plano: PostgreSQL (3ª Entrega)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/observacao
spring.datasource.username=observacao_user
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate  # Em produção
```

### Razão para H2 agora
- Não depende de infraestrutura externa
- Fácil rodas em qualquer máquina
- JPA/Hibernate já faz a abstração (migração será simples)

---

## 6. Transações ACID com @Transactional

### Decisão: @Transactional em todo método de escrita

```java
@Transactional  // ← IMPORTANTE
public Solicitacao cadastrar(DTO dto) {
    validarRegraAnonimato(dto);         // Se falhar aqui
    new Solicitacao(...);               // e aqui
    solicitacaoRepository.save(...);    // Rollback automático
    registrarLog(...);                  // Se falhar aqui, tudo desfaz
}
```

**Garantias ACID:**
- **Atomicidade**: Tudo ou nada
- **Consistência**: Regras de negócio respeitadas
- **Isolamento**: Operações concorrentes não interferem
- **Durabilidade**: Uma vez commitado, permanece

**Sem @Transactional:**
```java
public Solicitacao cadastrar(DTO dto) {
    solicitacaoRepository.save(...);    // ← Commita aqui
    registrarLog(...);                  // ← Falha aqui, mas solicitação já foi salva ❌
}
```

---

## 7. Auditoria Integrada

### Decisão: LogAuditoria em TODA operação crítica

```java
public Solicitacao cadastrar(...) {
    // ... validações e criação ...
    solicitacaoRepository.save(solicitacao);
    
    // ← AUDITORIA AQUI
    registrarLog(
        TipoEvento.CADASTRO,
        protocolo,
        solicitante.isAnonimo() ? "Anônimo" : solicitante.getNome(),
        "Categoria: ... | Prioridade: ... | Bairro: ...",
        solicitante.isAnonimo()
    );
}
```

### Por que?
- ✅ **Segurança**: Rastrear quem fez o quê
- ✅ **ODS 16**: Transparência institucional
- ✅ **Conformidade**: LGPD, auditorias externas
- ✅ **Detecção de abuso**: Tentativas bloqueadas registradas
- ✅ **Proteção de denunciantes**: Historicamente documentado

---

## 8. Máquina de Estados (StateMachine)

### Decisão: Estados em Enum com transições validadas

```java
public enum StatusSolicitacao {
    ABERTO("Aberto") {
        @Override
        public Set<StatusSolicitacao> proximosPermitidos() {
            return Set.of(TRIAGEM, CANCELADO);  // ← Apenas estas transições
        }
    },
    TRIAGEM("Em Triagem") { /* ... */ },
    EM_EXECUCAO("Em Execução") { /* ... */ },
    RESOLVIDO("Resolvido") { /* ... */ },
    ENCERRADO("Encerrado") { /* ... */ },
    CANCELADO("Cancelado") { /* ... */ };
}
```

### Uso
```java
public void avancarStatus(StatusSolicitacao novoStatus, ...) {
    if (!this.status.podeTransicionarPara(novoStatus)) {
        throw new IllegalStateException("Transição inválida");
    }
    // ... prossegue ...
}
```

**Diagrama:**
```
ABERTO ──→ TRIAGEM ──→ EM_EXECUÇÃO ──→ RESOLVIDO ──→ ENCERRADO
   ↓                                                      ↑
   └──────────────────────── CANCELADO ─────────────────┘
```

---

## 9. Validação em Camadas

### Decisão: 3 Níveis de Validação

```
Entrada (HTTP)
   ↓
Controller (Validação Básica)
   ├─ Formato JSON válido?
   ├─ Status 400 se não
   └─ Delega ao Service
      ↓
Service (Validação de Negócio) ⭐ MAIS IMPORTANTE
   ├─ Campos obrigatórios?
   ├─ Regra de anonimato?
   ├─ Limite de cadastros?
   ├─ Duplicatas?
   ├─ Status 400 se não
   └─ Cria Model
      ↓
Model (Validação de Domínio)
   ├─ Transição de status válida?
   ├─ IllegalStateException se não
   └─ Garante consistência
```

### Exemplo

```java
// 1. Controller (básico)
@PostMapping
public ResponseEntity<?> cadastrar(@RequestBody CadastrarSolicitacaoDTO dto) {
    // Apenas recebe e passa adiante

// 2. Service (negócio) ← PRINCIPAL
@Transactional
public Solicitacao cadastrar(CadastrarSolicitacaoDTO dto) {
    validarCamposObrigatorios(dto);           // ← Validação 1
    validarRegraAnonimato(solicitante, desc); // ← Validação 2
    verificarLimiteCadastros(solicitante);    // ← Validação 3
    verificarDuplicidade(...);                // ← Validação 4

// 3. Model (domínio)
public class Solicitacao {
    public void avancarStatus(...) {
        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(...);
        }
    }
}
```

---

## 10. Tratamento Global de Erros

### Decisão: GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponseDTO(
                "Erro de validação",
                ex.getMessage(),
                400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        return ResponseEntity.status(500)
            .body(new ErrorResponseDTO(
                "Erro interno",
                ex.getMessage(),
                500));
    }
}
```

**Benefício:** Centralizamos resposta de erro em um único lugar. Todos os Controllers recebem tratamento consistente.

---

## 11. Injeção de Dependência

### Decisão: Constructor Injection (preferido)

#### ✅ CORRETO (Constructor)
```java
@Service
public class SolicitacaoService {
    private final SolicitacaoRepository repository;

    public SolicitacaoService(SolicitacaoRepository repository) {
        this.repository = repository;  // ← Imutável
    }
}
```

#### ❌ NÃO RECOMENDADO (Field Injection)
```java
@Service
public class SolicitacaoService {
    @Autowired
    private SolicitacaoRepository repository;  // ← Mutável, difícil de testar
}
```

**Razão:**
- ✅ Imutabilidade: `final` garante que field não muda
- ✅ Testabilidade: Fácil criar mock no teste
- ✅ Clareza: Dependências explícitas no construtor
- ✅ NullPointerException: Impossível deixar null se exigir no construtor

---

## 12. Mudanças em Relação à 1ª Entrega

| Aspecto | 1ª Entrega (CLI) | 2ª Entrega (Spring Boot) |
|--------|------------------|------------------------|
| Interface | CLI text-based | REST API JSON |
| Persistência | Em memória (List) | JPA/Hibernate + H2 |
| Estrutura | Monolítica | MVC em Camadas |
| Controladores | Scanner manualmente | @RestController |
| Serviços | Manual | @Service + @Transactional |
| Repositório | ArrayList | JpaRepository |
| Configuração | Hardcoded | application.properties |
| Tratamento de Erro | Try-catch manual | GlobalExceptionHandler |
| DTOs | Não havia | CadastrarSolicitacaoDTO, etc |
| Logging | Println | SLF4J + Log4j |

---

## 13. Por que JpaRepository?

### Decisão: Extends JpaRepository<Entity, ID>

```java
@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    Optional<Solicitacao> findByProtocolo(String protocolo);
    List<Solicitacao> findByStatus(StatusSolicitacao status);
}
```

**Gerado automaticamente:**
- `findById(Long id)`
- `findAll()`
- `save(Solicitacao)`
- `delete(Solicitacao)`
- `count()`
- ... + métodos customizados

**Alternativa rejeitada:** SQL nativo em tudo = verbose e propenso a erros

---

## 14. Configuração vs Código

### Decisão: application.properties para config, NÃO hardcode

#### ✅ CORRETO
```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:observacao_db
```

```java
@Value("${server.port}")
private int port;
```

#### ❌ ERRADO
```java
private static final int PORT = 8080;  // ← Hardcoded
String url = "jdbc:h2:mem:observacao_db";  // ← Hardcoded
```

**Razão:**
- ✅ Fácil mudar sem recompilar
- ✅ Múltiplos ambientes (dev, staging, produção)
- ✅ Segredos em variáveis de ambiente

---

## 15. Acessibilidade e Inclusão (ODS 16)

### Decisão: Anonimato com proteção real

```java
public class Solicitacao {
    @ManyToOne
    private Usuario solicitante;  // ← Pode ser anônimo

    // Na resposta:
    private String solicitanteNome = solicitacao.getSolicitante().isAnonimo() 
        ? "Anônimo" 
        : solicitacao.getSolicitante().getNome();
}
```

**Protege:**
- ✅ Denunciantes de violência
- ✅ Servidores públicos que relatam corrupção
- ✅ Pessoas em situação de vulnerabilidade
- ✅ Alinhado com ODS 16.10 (proteção contra represálias)

---

## Conclusão

A arquitetura escolhida (Spring Boot + MVC em Camadas) permite:

1. ✅ **Escalabilidade**: REST API pode servir múltiplos clientes
2. ✅ **Manutenibilidade**: Código organizado, limpo, testável
3. ✅ **Segurança**: Auditoria integrada, validações em camadas
4. ✅ **Evolução**: Fácil adicionar features sem quebrar existentes
5. ✅ **Profissionalismo**: Padrões da indústria (Spring Boot, JPA, REST)
6. ✅ **ODS 16**: Transparência, rastreabilidade, inclusão

A próxima entrega pode incluir:
- [ ] Autenticação JWT
- [ ] PostgreSQL + Docker
- [ ] Testes unitários/integração
- [ ] Swagger/OpenAPI
- [ ] Frontend Angular/React
- [ ] Microserviços (se necessário)

---

**Documentado em:** Junho 2026  
**Responsável:** Desenvolvimento Spring Boot  
**Status:** Pronto para Produção (2ª Entrega)
