# ObservaAção — Front-end (React + Vite + TypeScript)

Interface web **mobile-first** da Ouvidoria Digital **ObservaAção**, integrada ao
back-end Spring Boot (`AEP-2-SEMESTRE-ESOFT5S`). Todas as ações do usuário são
persistidas no banco **H2** através da API REST.

A aplicação é dividida em **duas áreas**, separadas por URL:

- 🧑 **Área do Cidadão** (`/`) — criar e buscar solicitações.
- 🔐 **Área Administrativa** (`/admin`) — painel, listagem, dashboard, relatório e
  atualização de status. *Acessada digitando `/admin` na URL* (não há link a partir
  da área do cidadão).

## Telas

### Área do Cidadão (`/`)

| Rota | Tela | Ação |
|------|------|------|
| `/` | Início do Cidadão | Atalhos: Nova Solicitação e Buscar |
| `/categorias` | Categoria de Serviço | Lista categorias da API |
| `/nova/:categoriaId` | Nova Solicitação | **Cadastra** a solicitação (`POST`) — identificada ou anônima |
| `/buscar` | Buscar Solicitação | Consulta por protocolo |
| `/comprovante/:protocolo` | Comprovante | Detalhes, prazo/SLA, histórico e **comentários** |

### Área Administrativa (`/admin`)

| Rota | Tela | Ação |
|------|------|------|
| `/admin` | Painel Admin | Atalhos + contadores (abertas / usuários) |
| `/admin/listagem` | Listagem | Tabela de todas as solicitações |
| `/admin/comprovante/:protocolo` | Comprovante | Igual ao do cidadão, **+ botão "Atualizar status"** |
| `/admin/atualizacao/:protocolo` | Atualização de Status | **Muda o status** respeitando a máquina de estados |
| `/admin/dashboard` | Dashboard Administrativo | KPIs + gráficos por status/categoria/prioridade |
| `/admin/relatorio` | Relatório | Relatório consolidado com impressão/PDF |

## Stack

- **React 18** + **React Router 6** (rotas aninhadas com layouts)
- **Vite 5** (dev server + build)
- **TypeScript** (modo estrito; sem dependências de UI externas)
- CSS próprio, mobile-first e responsivo (`src/styles/global.css`)

## Estrutura

```
src/
├── components/      # ScreenTitle, StatusBadge, BarList, Feedback
├── layout/          # UserLayout, AdminLayout, Navbar (parametrizada)
├── pages/           # uma pasta por tela
├── routes/          # AppRoutes (áreas cidadão e admin)
├── services/api.ts  # cliente HTTP tipado (get/post/patch)
├── constants.ts     # rótulos de status/prioridade, ícones, datas
├── stats.ts         # agregações usadas no dashboard/relatório
└── types.ts         # tipos compartilhados com a API
```

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

> Sem Maven no PATH? Use o Maven que acompanha o IntelliJ IDEA, por exemplo:
> `"C:\Program Files\JetBrains\IntelliJ IDEA <versão>\plugins\maven\lib\maven3\bin\mvn" spring-boot:run`

### 2. Front-end (porta 5173)

```bash
cd observacao-frontend
npm install      # apenas na primeira vez
npm run dev
```

Abra `http://localhost:5173` (cidadão) ou `http://localhost:5173/admin`
(administrativo). O Vite redireciona as chamadas `/api/...` para o back-end
automaticamente (proxy em `vite.config.ts`), evitando problemas de CORS.

### Build de produção

```bash
npm run build    # type-check (tsc) + bundle (vite) em dist/
npm run preview  # serve o build gerado
```

## Configuração

A URL da API pode ser ajustada via variável de ambiente (arquivo `.env`):

```
VITE_API_URL=/api          # padrão (usa o proxy do Vite)
# VITE_API_URL=http://localhost:8080/api   # chamada direta ao back-end
```
