# ESTRUTURA DO PROJETO — ObservaAção Spring Boot

## 📁 Organização de Diretórios

```
AEP-2-SEMESTRE-ESOFT5S/
├── pom.xml                                    # Configuração Maven com dependências
├── README.md                                  # Documentação do projeto
├── STRUCTURE.md                               # Este arquivo
├── .gitignore                                 # Exclusões Git
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/observaacao/
│   │   │       ├── ObservacaoApplication.java # Classe main Spring Boot
│   │   │       │
│   │   │       ├── controller/                # CAMADA 1: APRESENTAÇÃO (REST)
│   │   │       │   ├── SolicitacaoController.java
│   │   │       │   └── CategoriaController.java
│   │   │       │
│   │   │       ├── service/                   # CAMADA 2: LÓGICA DE NEGÓCIO
│   │   │       │   ├── SolicitacaoService.java       (PRINCIPAL)
│   │   │       │   ├── CategoriaService.java
│   │   │       │   └── GeradorProtocoloService.java
│   │   │       │
│   │   │       ├── repository/                # CAMADA 3: PERSISTÊNCIA
│   │   │       │   ├── SolicitacaoRepository.java
│   │   │       │   ├── CategoriaRepository.java
│   │   │       │   ├── UsuarioRepository.java
│   │   │       │   └── LogAuditoriaRepository.java
│   │   │       │
│   │   │       ├── model/                     # CAMADA 4: DOMÍNIO
│   │   │       │   ├── Solicitacao.java
│   │   │       │   ├── Usuario.java
│   │   │       │   ├── Categoria.java
│   │   │       │   ├── Prioridade.java
│   │   │       │   ├── StatusSolicitacao.java
│   │   │       │   ├── HistoricoStatus.java
│   │   │       │   ├── Comentario.java
│   │   │       │   └── LogAuditoria.java
│   │   │       │
│   │   │       ├── dto/                       # DATA TRANSFER OBJECTS
│   │   │       │   ├── CadastrarSolicitacaoDTO.java
│   │   │       │   ├── AtualizarStatusDTO.java
│   │   │       │   ├── AdicionarComentarioDTO.java
│   │   │       │   ├── SolicitacaoResponseDTO.java
│   │   │       │   └── ErrorResponseDTO.java
│   │   │       │
│   │   │       └── config/                    # CONFIGURAÇÃO
│   │   │           ├── DataLoader.java
│   │   │           └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties          # Configuração Spring Boot
│   │       └── application-dev.properties      # Configuração de desenvolvimento
│   │
│   └── test/
│       ├── java/
│       │   └── com/observaacao/               (Testes unitários — futuro)
│       │
│       └── resources/
│           └── application-test.properties    (Configuração de testes)
│
└── target/                                     (Gerado pelo Maven)
    ├── classes/                                (Classes compiladas)
    ├── observacao-spring-boot-2.0.0-BETA.jar  (JAR final)
    └── ...
```

## 🎯 Responsabilidade de Cada Camada

### 1. **Controller** (Apresentação)
- Recebe requisições HTTP
- Valida formato JSON/entrada básica
- **NÃO valida lógica de negócio**
- Delega tudo ao Service
- Retorna ResponseEntity com status apropriado

**Exemplo:**
```java
@PostMapping
public ResponseEntity<?> cadastrar(@RequestBody CadastrarSolicitacaoDTO dto) {
    Solicitacao solicitacao = solicitacaoService.cadastrar(dto); // Delega
    return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(solicitacao));
}
```

### 2. **Service** (Lógica de Negócio) ⭐ MAIS IMPORTANTE
- Contém TODA lógica de negócio
- Validações de regras
- Transações ACID
- Auditoria
- Coordena chamadas aos repositories
- **NÃO conhece HTTP** (pode ser reutilizado por CLI, gRPC, etc.)

**Exemplo:**
```java
@Transactional
public Solicitacao cadastrar(CadastrarSolicitacaoDTO dto) {
    validarCamposObrigatorios(dto);           // Validação
    validarRegraAnonimato(solicitante, desc); // Regra de negócio
    verificarDuplicidade(...);                // Prevenção de abuso
    // ... cria solicitação ...
    registrarLog(...);                        // Auditoria
    return solicitacaoRepository.save(...);   // Persiste
}
```

### 3. **Repository** (Persistência)
- Interface com banco de dados
- CRUD simples
- Queries específicas
- **SEM lógica de negócio** (apenas consultas)

**Exemplo:**
```java
@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    Optional<Solicitacao> findByProtocolo(String protocolo);
    List<Solicitacao> findByStatus(StatusSolicitacao status);
}
```

### 4. **Model** (Domínio)
- Entidades do negócio
- Mapeamento com banco
- Comportamento do domínio
- Imutáveis/defensivas

**Exemplo:**
```java
@Entity
public class Solicitacao {
    public void avancarStatus(StatusSolicitacao novoStatus, ...) {
        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(...); // Protege domínio
        }
    }
}
```

### 5. **DTO** (Transferência de Dados)
- Formatos de entrada/saída
- Isolam Model da API
- Permitem evolução sem quebrar clients

**Exemplo:**
```java
@Data
public class CadastrarSolicitacaoDTO {
    private Long categoriaId;
    private String descricao;
    // ... sem incluir campos internos do Model
}
```

### 6. **Config** (Configuração)
- Inicialização de dados (DataLoader)
- Tratamento global de erros
- Beans personalizados

---

## 🔄 Fluxo de Execução — Exemplo: Cadastro

```
POST /api/solicitacoes
   │
   ├─→ [SolicitacaoController]
   │   │ Recebe JSON
   │   ├─ Valida formato básico
   │   └─ Chama service
   │
   ├─→ [SolicitacaoService.cadastrar()]
   │   ├─ validarCamposObrigatorios() ← Regra de negócio
   │   ├─ validarRegraAnonimato()     ← Regra de negócio
   │   ├─ verificarLimiteCadastros()  ← Regra de negócio
   │   ├─ verificarDuplicidade()      ← Regra de negócio (Jaccard)
   │   ├─ recuperarOuCriarUsuario()
   │   ├─ new Solicitacao(...)
   │   ├─ registrarLog(...)           ← Auditoria
   │   └─ solicitacaoRepository.save()
   │
   ├─→ [SolicitacaoRepository]
   │   ├─ Converte para SQL
   │   ├─ Persiste em H2
   │   └─ Retorna entidade salva
   │
   └─→ [SolicitacaoController]
       ├─ Converte para DTO
       └─ Retorna 201 Created + JSON
```

---

## 📊 Estrutura de Dados — Banco H2

### Tabela: `solicitacoes`
```sql
CREATE TABLE solicitacoes (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    protocolo VARCHAR(50) UNIQUE NOT NULL,
    categoria_id LONG NOT NULL,
    descricao TEXT NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    localizacao VARCHAR(200) NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    usuario_id LONG NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    prazo_estimado TIMESTAMP NOT NULL,
    url_anexo VARCHAR(500)
);
```

### Tabela: `categorias`
```sql
CREATE TABLE categorias (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    descricao VARCHAR(100) UNIQUE NOT NULL,
    exemplo_problema TEXT
);
```

### Tabela: `usuarios`
```sql
CREATE TABLE usuarios (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefone VARCHAR(20),
    anonimo BOOLEAN NOT NULL,
    tipo VARCHAR(20)
);
```

### Tabela: `log_auditoria`
```sql
CREATE TABLE log_auditoria (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    tipo_evento VARCHAR(50) NOT NULL,
    protocolo VARCHAR(50),
    ator VARCHAR(100) NOT NULL,
    descricao TEXT,
    data_hora TIMESTAMP NOT NULL,
    envolve_anonimato BOOLEAN
);
```

---

## 🚀 Como Compilar e Executar

### 1. Compilar
```bash
mvn clean compile
```

### 2. Executar
```bash
mvn spring-boot:run
```

### 3. Parar
```bash
Ctrl + C
```

### 4. Testar via cURL

**Cadastrar:**
```bash
curl -X POST http://localhost:8080/observacao/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{"categoriaId": 1, "descricao": "Buraco grande", ...}'
```

**Listar:**
```bash
curl http://localhost:8080/observacao/api/solicitacoes
```

**Acessar H2 Console:**
```
http://localhost:8080/observacao/h2-console
```

---

## 🔧 Configuração (application.properties)

```properties
# Servidor
server.port=8080
server.servlet.context-path=/observacao

# Banco (H2)
spring.datasource.url=jdbc:h2:mem:observacao_db
spring.jpa.hibernate.ddl-auto=create-drop

# Logging
logging.level.com.observaacao=DEBUG
```

---

## ✅ Checklist — Boas Práticas POO + Spring Boot

- ✅ Injeção de dependência (DI) via Spring
- ✅ Separação clara de responsabilidades (SoC)
- ✅ Controllers magros (sem lógica)
- ✅ Services gordos (lógica centralizada)
- ✅ Transações gerenciadas (@Transactional)
- ✅ DTOs para isolar models
- ✅ Exceções customizadas
- ✅ Logging estruturado
- ✅ Auditoria de operações
- ✅ Clean Code em métodos privados

---

**Última atualização:** Junho 2026
