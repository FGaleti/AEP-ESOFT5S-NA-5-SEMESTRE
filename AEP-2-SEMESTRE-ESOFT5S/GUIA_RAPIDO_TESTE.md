# 🚀 GUIA RÁPIDO — Como Compilar e Testar

## 📦 Pré-requisitos

✅ Java 17+ instalado  
✅ Maven 3.8+ instalado  
✅ Git instalado  

### Verificar Versões
```bash
java -version          # Deve ser 17+
mvn -version          # Deve ser 3.8+
git --version
```

---

## 🛠️ Passo 1: Clonar e Navegar

```bash
# Ir para o diretório do projeto
cd "c:\Users\seu_usuario\Documents\AEP - 2B"

# Entrar no diretório da 2ª entrega
cd AEP-ESOFT5S-NA-5-SEMESTRE\AEP-2-SEMESTRE-ESOFT5S

# Verificar que está no local correto
dir
# Deve listar: pom.xml, README.md, src/
```

---

## 🔨 Passo 2: Compilar o Projeto

### Opção A: Build Completo (Recomendado)
```bash
mvn clean compile
```

**O que acontece:**
- ✅ Remove compilações anteriores (`clean`)
- ✅ Baixa dependências (primeira vez)
- ✅ Compila código Java
- ✅ Detecta erros de sintaxe

**Esperado:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 15-30 segundos
```

### Opção B: Apenas Compilar (Mais rápido)
```bash
mvn compile
```

### Se der erro

**"Maven not found"**
```bash
# Adicionar Maven ao PATH (Windows PowerShell)
$env:Path += ";C:\Program Files\Apache\maven\bin"

# Ou usar Maven wrapper (se existir)
.\mvnw clean compile
```

---

## 🏃 Passo 3: Executar a Aplicação

### Comando Principal
```bash
mvn spring-boot:run
```

**Esperado (último output):**
```
2026-06-03 14:05:23,123 - Started ObservacaoApplication
2026-06-03 14:05:23,456 - ✓ Categorias carregadas com sucesso!
2026-06-03 14:05:23,789 - Tomcat started on port(s): 8080 (http)
```

### ✅ Parou por aqui? A aplicação está RODANDO!

---

## 🧪 Passo 4: Testar Endpoints (Em outro terminal)

### Verificar se está rodando
```bash
# Simples teste
curl http://localhost:8080/observacao/api/categorias
```

**Resposta esperada:** Lista de categorias em JSON

### Teste 1: Listar Categorias
```bash
curl http://localhost:8080/observacao/api/categorias
```

**Resposta:**
```json
[
  {
    "id": 1,
    "descricao": "Iluminação Pública",
    "exemploProblema": "Lâmpadas queimadas, postes apagados"
  },
  ...
]
```

### Teste 2: Cadastrar Solicitação
```bash
curl -X POST http://localhost:8080/observacao/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{
    "categoriaId": 1,
    "descricao": "Lâmpada queimada há 2 semanas na Avenida Principal",
    "bairro": "Centro",
    "localizacao": "Avenida Principal, próximo ao semáforo",
    "prioridade": "ALTA",
    "nomeUsuario": "João Silva",
    "emailUsuario": "joao@email.com",
    "telefoneUsuario": "11987654321",
    "anonimo": false
  }'
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "protocolo": "PROT-20260603140523-1001",
  "categoria": "Iluminação Pública",
  "descricao": "Lâmpada queimada há 2 semanas...",
  "status": "ABERTO",
  "statusFormatado": "Aberto (5 dia(s) restante(s))",
  "solicitante": "João Silva",
  "anonimo": false,
  "diasRestantes": 5,
  "atrasada": false,
  "historico": ["null → ABERTO (Sistema) - Solicitação registrada"],
  "comentarios": []
}
```

**📌 Copiar o protocolo para os próximos testes!**

### Teste 3: Consultar por Protocolo
```bash
curl http://localhost:8080/observacao/api/solicitacoes/PROT-20260603140523-1001
```

### Teste 4: Listar com Filtro (status)
```bash
curl "http://localhost:8080/observacao/api/solicitacoes?status=ABERTO"
```

### Teste 5: Atualizar Status
```bash
curl -X PATCH http://localhost:8080/observacao/api/solicitacoes/PROT-20260603140523-1001/status \
  -H "Content-Type: application/json" \
  -d '{
    "novoStatus": "TRIAGEM",
    "observacao": "Solicitação recebida e categor

izada",
    "nomeResponsavel": "Carlos - Atendente"
  }'
```

**Resposta:** Status mudou para TRIAGEM

### Teste 6: Adicionar Comentário
```bash
curl -X POST http://localhost:8080/observacao/api/solicitacoes/PROT-20260603140523-1001/comentarios \
  -H "Content-Type: application/json" \
  -d '{
    "texto": "Equipe de manutenção acionada. Previsão de conserto em 2 dias",
    "autor": "Fernanda - Gestora"
  }'
```

### Teste 7: Anonimato (Cadastro anônimo)
```bash
curl -X POST http://localhost:8080/observacao/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{
    "categoriaId": 6,
    "descricao": "Presenciei situação de assédio na escola do bairro. Preciso denunciar de forma anônima e segura",
    "bairro": "Vila Esperança",
    "localizacao": "Escola Municipal - Vila Esperança",
    "prioridade": "CRITICA",
    "anonimo": true
  }'
```

**Resposta:** Protocolo de denúncia sem identificação

---

## 🌐 Acessar Interface Web (H2 Console)

Se quiser ver o banco de dados diretamente:

```
http://localhost:8080/observacao/h2-console
```

**Login:**
- JDBC URL: `jdbc:h2:mem:observacao_db`
- User: `sa`
- Password: (deixar vazio)

**Clique em Connect**

---

## 🛑 Parar a Aplicação

No terminal onde rodou `mvn spring-boot:run`:

```bash
Ctrl + C
```

**Esperado:**
```
[INFO] BUILD SUCCESS
Process terminated
```

---

## 📋 Fluxo Completo de Teste (10 minutos)

```bash
# 1. Terminal 1: Compilar e executar
mvn clean compile
mvn spring-boot:run
# Esperar "Started ObservacaoApplication"

# 2. Terminal 2: Testar
# Teste 1: Listar categorias
curl http://localhost:8080/observacao/api/categorias

# Teste 2: Cadastrar
curl -X POST http://localhost:8080/observacao/api/solicitacoes \
  -H "Content-Type: application/json" \
  -d '{"categoriaId": 1, "descricao": "Problema", ...}'
# ✅ Copiar protocolo

# Teste 3: Consultar
curl http://localhost:8080/observacao/api/solicitacoes/PROT-xxxxx

# Teste 4: Listar
curl "http://localhost:8080/observacao/api/solicitacoes?status=ABERTO"

# Teste 5: Atualizar status
curl -X PATCH http://localhost:8080/observacao/api/solicitacoes/PROT-xxxxx/status \
  -H "Content-Type: application/json" \
  -d '{"novoStatus": "TRIAGEM", "observacao": "x", "nomeResponsavel": "Carlos"}'

# Teste 6: Comentário
curl -X POST http://localhost:8080/observacao/api/solicitacoes/PROT-xxxxx/comentarios \
  -H "Content-Type: application/json" \
  -d '{"texto": "Comentário", "autor": "Servidor"}'

# 3. Terminal 1: Parar (Ctrl+C)
```

---

## ⚠️ Troubleshooting

### Erro: "Connection refused" ao testar endpoints
**Causa:** Aplicação não está rodando  
**Solução:**
```bash
# Verificar se está compilada
mvn clean compile

# Executar
mvn spring-boot:run

# Esperar a mensagem "Started ObservacaoApplication"
```

### Erro: "Port 8080 already in use"
**Causa:** Outra aplicação usando porta 8080  
**Solução:**
```bash
# Matar processo (Windows PowerShell)
Stop-Process -Port 8080

# Ou mudar porta em application.properties
# server.port=8081
```

### Erro: "No main manifest attribute"
**Causa:** JAR sem classe main  
**Solução:** Usar `mvn spring-boot:run` (não `java -jar`)

### Erro de compilação "cannot find symbol"
**Causa:** Dependências não baixadas  
**Solução:**
```bash
mvn clean install -U
```

---

## ✅ Checklist de Validação

- [ ] Projeto compila sem erros (`mvn clean compile`)
- [ ] Aplicação inicia (`mvn spring-boot:run`)
- [ ] Endpoints respondem (curl de categorias)
- [ ] Cadastro de solicitação funciona (201 Created)
- [ ] Consulta por protocolo funciona
- [ ] Atualização de status funciona
- [ ] Comentários funcionam
- [ ] Anonimato funciona (sem erro de descrição curta)
- [ ] H2 Console acessível
- [ ] Sem exceções não tratadas

---

## 📊 Monitoramento

### Verificar Logs Detalhados
```bash
# Em application.properties, mudar:
logging.level.com.observaacao=DEBUG

# Depois executar novamente
mvn spring-boot:run
```

### Ver Queries SQL
```bash
# Em application.properties:
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 🎓 Estrutura do Banco H2

Tabelas criadas automaticamente:

```sql
-- Verificar tabelas (no H2 Console)
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC'

-- Ver dados
SELECT * FROM SOLICITACOES;
SELECT * FROM CATEGORIAS;
SELECT * FROM USUARIOS;
SELECT * FROM LOG_AUDITORIA;
```

---

## 🎯 Próximo Passo

Após validar que tudo funciona:

1. ✅ Certificar que código compila
2. ✅ Certificar que endpoints respondem
3. ⏳ Criar Wireframes (IHC)
4. ⏳ Extrair Métricas (Manutenção)
5. ⏳ Gravar Vídeo (até 5 min)

---

## 💡 Dicas

- **Usar Postman**: Interface gráfica para testar endpoints
- **VS Code REST Extension**: Testar direto no editor
- **curl com pretty print**:
  ```bash
  curl http://localhost:8080/observacao/api/categorias | python -m json.tool
  ```

---

**Tudo pronto! Boa sorte com os testes! 🚀**
