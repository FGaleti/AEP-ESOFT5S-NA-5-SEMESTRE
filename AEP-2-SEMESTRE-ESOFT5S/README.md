# ObservaAção — Spring Boot 2ª Entrega

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green?style=for-the-badge&logo=spring-boot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-In%20Memory-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**Sistema de Ouvidoria Digital com Spring Boot — Arquitetura em Camadas Profissional**

[Sobre](#-sobre) • [Arquitetura](#-arquitetura) • [Instalação](#-instalação) • [Como Usar](#-como-usar) • [Endpoints](#-endpoints) • [Contribuidores](#-contribuidores)

</div>

---

## 📋 Sobre

**ObservaAção Spring Boot** é a evolução da primeira entrega, migrando de Java CLI puro para uma arquitetura robusta com **Spring Boot 3.2.0**.

### Destaques da Evolução

✅ **Arquitetura em Camadas Profissional**
- Controllers → Services → Repository
- Separação clara de responsabilidades
- Injeção de dependência com Spring

✅ **REST API moderna**
- Endpoints JSON com documentação
- Tratamento global de erros
- DTOs para transferência de dados

✅ **Persistência em Memória**
- H2 Database (em memória)
- JPA/Hibernate para ORM
- Inicialização automática de dados

✅ **Manutenibilidade**
- Clean Code aplicado
- Transações gerenciadas automaticamente
- Logging centralizado

---

## 🏗️ Arquitetura

### Estrutura de Camadas

```
com/observaacao/
│
├── controller/
│   ├── SolicitacaoController.java      # Endpoints de Solicitações
│   └── CategoriaController.java         # Endpoints de Categorias
│
├── service/
│   ├── SolicitacaoService.java          # Lógica de negócio
│   ├── CategoriaService.java            # Gestão de categorias
│   └── GeradorProtocoloService.java     # Geração de protocolos
│
├── repository/
│   ├── SolicitacaoRepository.java       # CRUD de Solicitações
│   ├── CategoriaRepository.java         # CRUD de Categorias
│   ├── UsuarioRepository.java           # CRUD de Usuários
│   └── LogAuditoriaRepository.java      # CRUD de Logs
│
├── model/
│   ├── Solicitacao.java                 # Entidade principal
│   ├── Usuario.java                     # Usuário (cidadão/servidor)
│   ├── Categoria.java                   # Categoria de solicitação
│   ├── Prioridade.java                  # Enum de prioridades
│   ├── StatusSolicitacao.java           # Máquina de estados
│   ├── HistoricoStatus.java             # Rastreabilidade
│   ├── Comentario.java                  # Comentários
│   └── LogAuditoria.java                # Auditoria
│
├── dto/
│   ├── CadastrarSolicitacaoDTO.java     # Requisição de cadastro
│   ├── AtualizarStatusDTO.java          # Requisição de atualização
│   ├── AdicionarComentarioDTO.java      # Requisição de comentário
│   ├── SolicitacaoResponseDTO.java      # Resposta formatada
│   └── ErrorResponseDTO.java            # Resposta de erro
│
├── config/
│   ├── DataLoader.java                  # Inicialização de dados
│   └── GlobalExceptionHandler.java      # Tratamento global de erros
│
└── ObservacaoApplication.java           # Classe principal Spring Boot
```

### Padrões Aplicados

| Padrão | Implementação |
|--------|---------------|
| **MVC** | Controllers + Services + Models |
| **Repository** | JpaRepository para persistência |
| **DTO** | Camada de transferência de dados |
| **Dependency Injection** | @Autowired e construtor |
| **Transactional** | @Transactional para ACID |
| **Exception Handling** | GlobalExceptionHandler |

---

## 🚀 Instalação

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Git

### Passos

1. **Clonar o repositório**
   ```bash
   git clone https://github.com/FGaleti/AEP-ESOFT5S-NA-5-SEMESTRE.git
   cd AEP-2-SEMESTRE-ESOFT5S
   ```

2. **Compilar o projeto**
   ```bash
   mvn clean compile
   ```

3. **Executar a aplicação**
   ```bash
   mvn spring-boot:run
   ```

4. **Acessar a aplicação**
   - API: [http://localhost:8080/observacao](http://localhost:8080/observacao)
   - H2 Console: [http://localhost:8080/observacao/h2-console](http://localhost:8080/observacao/h2-console)

---

## 📡 Endpoints

### Solicitações

#### 1. **Cadastrar nova solicitação**
```http
POST /observacao/api/solicitacoes
Content-Type: application/json

{
  "categoriaId": 1,
  "descricao": "Buraco grande na Rua Principal, criando risco para veículos",
  "bairro": "Centro",
  "localizacao": "Rua Principal, próximo ao semáforo",
  "prioridade": "ALTA",
  "nomeUsuario": "João Silva",
  "emailUsuario": "joao@email.com",
  "telefoneUsuario": "11999999999",
  "anonimo": false
}
```

**Resposta (201 Created):**
```json
{
  "protocolo": "PROT-20260603140523-1001",
  "categoria": "Buracos e Pavimento",
  "status": "ABERTO",
  "diasRestantes": 5,
  "atrasada": false
}
```

#### 2. **Consultar solicitação por protocolo**
```http
GET /observacao/api/solicitacoes/{protocolo}
```

#### 3. **Listar solicitações com filtros**
```http
GET /observacao/api/solicitacoes?status=ABERTO
GET /observacao/api/solicitacoes?prioridade=ALTA
GET /observacao/api/solicitacoes?bairro=Centro
```

#### 4. **Atualizar status**
```http
PATCH /observacao/api/solicitacoes/{protocolo}/status
Content-Type: application/json

{
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Equipe enviada para análise local",
  "nomeResponsavel": "Carlos - Atendente"
}
```

#### 5. **Adicionar comentário**
```http
POST /observacao/api/solicitacoes/{protocolo}/comentarios
Content-Type: application/json

{
  "texto": "Solicitação recebida e priorizada",
  "autor": "Sistema"
}
```

### Categorias

#### 1. **Listar categorias**
```http
GET /observacao/api/categorias
```

#### 2. **Obter categoria por ID**
```http
GET /observacao/api/categorias/{id}
```

#### 3. **Criar categoria**
```http
POST /observacao/api/categorias
Content-Type: application/json

{
  "descricao": "Saúde Mental",
  "exemploProblema": "Falta de atendimento especializado"
}
```

---

## ⚙️ Configuração

### application.properties

```properties
# Porta
server.port=8080
server.servlet.context-path=/observacao

# Banco de Dados (H2 em Memória)
spring.datasource.url=jdbc:h2:mem:observacao_db
spring.jpa.hibernate.ddl-auto=create-drop

# Logging
logging.level.com.observaacao=DEBUG
```

---

## 🔄 Fluxo de Operação

### 1. Cadastro de Solicitação

```
POST /api/solicitacoes
         ↓
[SolicitacaoController.cadastrar()]
         ↓
[SolicitacaoService.cadastrar()]
├── validarCamposObrigatorios()
├── validarRegraAnonimato()
├── verificarLimiteCadastros()
├── verificarDuplicidade()
├── recuperarOuCriarUsuario()
└── registrarLog() → [LogAuditoriaRepository.save()]
         ↓
[SolicitacaoRepository.save()] → H2 Database
         ↓
Response: SolicitacaoResponseDTO (201 Created)
```

### 2. Consulta por Protocolo

```
GET /api/solicitacoes/{protocolo}
         ↓
[SolicitacaoController.consultar()]
         ↓
[SolicitacaoService.consultarPorProtocolo()]
├── validarProtocolo()
├── registrarLog(CONSULTA)
└── retornar Solicitacao
         ↓
Response: SolicitacaoResponseDTO (200 OK)
```

### 3. Atualizar Status

```
PATCH /api/solicitacoes/{protocolo}/status
         ↓
[SolicitacaoController.atualizarStatus()]
         ↓
[SolicitacaoService.atualizarStatus()]
├── consultarPorProtocolo()
├── validarTransição()
├── solicitacao.avancarStatus()
├── registrarLog(ATUALIZACAO_STATUS)
└── repository.save()
         ↓
Response: SolicitacaoResponseDTO (200 OK)
```

---

## 📊 Regras de Negócio Implementadas

| Regra | Implementação |
|-------|--------------|
| **SLA por Prioridade** | CRITICA=2d, ALTA=5d, MEDIA=15d, BAIXA=30d |
| **Anonimato** | Descrição mínima 20 chars para anônimos |
| **Limite de Cadastros** | Máx 10 por hora (prevenção de abuso) |
| **Detecção de Duplicatas** | Similaridade Jaccard > 0.8 |
| **Máquina de Estados** | Transições validadas per status |
| **Auditoria Completa** | Todas as operações registradas |

---

## 🧪 Exemplos de Uso

### cURL - Cadastrar

```bash
curl -X POST http://localhost:8080/observacao/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{
    "categoriaId": 1,
    "descricao": "Luz apagada na Avenida Principal há 3 dias",
    "bairro": "Centro",
    "localizacao": "Avenida Principal, próximo ao mercado",
    "prioridade": "ALTA",
    "nomeUsuario": "Maria",
    "emailUsuario": "maria@email.com",
    "telefoneUsuario": "11988888888",
    "anonimo": false
  }'
```

### cURL - Consultar

```bash
curl -X GET http://localhost:8080/observacao/api/solicitacoes/PROT-20260603140523-1001
```

### cURL - Listar por Status

```bash
curl -X GET "http://localhost:8080/observacao/api/solicitacoes?status=ABERTO"
```

---

## 📚 Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|-----------|--------|----------|
| Java | 17+ | Linguagem principal |
| Spring Boot | 3.2.0 | Framework web |
| Spring Data JPA | 3.2.0 | ORM |
| H2 Database | Latest | Persistência em memória |
| Lombok | 1.18.30 | Reduzir boilerplate |
| Maven | 3.8+ | Build tool |

---

## 🔐 Segurança

- ✅ Validação de entrada centralizada no Service
- ✅ Auditoria de todas as operações
- ✅ Proteção de anonimato com regras específicas
- ✅ Tratamento global de exceções
- ✅ Prevenção de abuso (rate limiting)

---

## 📈 Próximos Passos

- [ ] Adicionar autenticação JWT
- [ ] Implementar persistência em banco real (PostgreSQL)
- [ ] Criar testes unitários com JUnit 5
- [ ] Integração com Swagger/OpenAPI
- [ ] Cache com Redis
- [ ] Fila de processamento assíncrono

---

## 👥 Equipe

**Projeto:** ObservaAção - Sistema de Ouvidoria Digital  
**Alinhamento:** ODS 16 — Paz, Justiça e Instituições Eficazes  
**Semestre:** 5º Engenharia de Software  

---

## 📄 Licença

Distribuído sob a licença MIT. Veja `LICENSE` para detalhes.

---

**Última atualização:** Junho 2026
