# ObservaAção — Front-end (React + Vite + TypeScript)

Interface web (mobile-first) da Ouvidoria Digital **ObservaAção**, integrada ao
back-end Spring Boot (`AEP-2-SEMESTRE-ESOFT5S`). Todas as ações do usuário são
persistidas no banco **H2** através da API REST.

## Telas

| Rota | Tela | Ação |
|------|------|------|
| `/` | Início (painel) | Atalhos + contadores (abertas / total) |
| `/categorias` | Categoria de Serviço | Lista categorias da API |
| `/nova/:categoriaId` | Nova Solicitação | **Cadastra** a solicitação (POST) |
| `/comprovante/:protocolo` | Comprovante | Detalhes, prazo/SLA, histórico e **comentários** |
| `/listagem` | Listagem | Tabela de todas as solicitações |
| `/atualizacao/:protocolo` | Atualização de Status | **Muda o status** respeitando a máquina de estados |

## Como executar

Você precisa de **dois** terminais: um para o back-end e outro para o front-end.

### 1. Back-end (porta 8080)

```bash
cd AEP-2-SEMESTRE-ESOFT5S
mvn spring-boot:run
```

A API fica em `http://localhost:8080/api` e o console do H2 em
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:observacao_db`,
usuário `sa`, sem senha).

> Sem Maven instalado? Use o Maven que acompanha o IntelliJ IDEA, por exemplo:
> `"C:\Program Files\JetBrains\IntelliJ IDEA <versão>\plugins\maven\lib\maven3\bin\mvn" spring-boot:run`

### 2. Front-end (porta 5173)

```bash
cd observacao-frontend
npm install      # apenas na primeira vez
npm run dev
```

Abra `http://localhost:5173`. O Vite redireciona as chamadas `/api/...` para o
back-end automaticamente (proxy configurado em `vite.config.ts`), evitando CORS.

## Configuração

A URL da API pode ser ajustada via variável de ambiente (arquivo `.env`):

```
VITE_API_URL=/api          # padrão (usa o proxy do Vite)
# VITE_API_URL=http://localhost:8080/api   # chamada direta ao back-end
```
