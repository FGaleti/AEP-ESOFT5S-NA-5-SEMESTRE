# ObservaAção - Sistema de Ouvidoria Digital Pública

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-blue?style=for-the-badge)

**Uma plataforma inclusiva de ouvidoria digital para democratizar o acesso à transparência e participação cidadã nos processos públicos.**

[Sobre](#-sobre) • [Funcionalidades](#-funcionalidades) • [Arquitetura](#-arquitetura) • [Instalação](#-instalação) • [Como Usar](#-como-usar) • [Contribuidores](#-contribuidores) • [Licença](#-licença)

</div>

---

## 🎯 Sobre

**ObservaAção** é um sistema de ouvidoria digital desenvolvido como Avaliação Experiencial Prática (AEP) para o 5º semestre de Engenharia de Software, alinhado com os princípios da **Agenda 2030 das Nações Unidas**, especificamente o **ODS 16 — Paz, Justiça e Instituições Eficazes**.

O projeto materializa as metas:
- **Meta 16.6:** Desenvolver instituições eficazes, responsáveis e transparentes
- **Meta 16.7:** Garantir tomada de decisão responsiva, inclusiva e participativa

### Por que ObservaAção?

O sistema democratiza o acesso ao poder público, permitindo que cidadãos de todos os níveis de letramento digital possam:
- 📢 Registrar solicitações, reclamações e sugestões
- 📊 Acompanhar o status em tempo real
- 🔐 Fazer denúncias anônimas protegidas
- 🎯 Entender prioridades com SLAs transparentes
- ♿ Usar uma interface acessível e intuitiva

---

## ✨ Funcionalidades

### Core
- ✅ **Cadastro de Solicitações** — Formulário interativo com coleta de dados essenciais
- ✅ **Consulta e Acompanhamento** — Status em tempo real com protocolo único
- ✅ **Gestão de Prioridades** — SLA vinculado com cálculo automático de prazos
- ✅ **Log de Auditoria** — Rastreabilidade completa de operações (CRUD + tentativas de abuso)
- ✅ **Suporte a Anonimato** — Proteção de denunciantes conforme ODS 16.10
- ✅ **Limite de Cadastros** — Prevenção de spam com verificação de duplicatas
- ✅ **Interface CLI Acessível** — Menus responsivos e linguagem clara

### Entidades Principais
- **Solicitação** — NCT (número de identificação), categoria, descrição, status
- **Usuário** — Cidadão (anônimo/identificado) e Gestor
- **Prioridade** — CRÍTICA (2d), ALTA (5d), MÉDIA (15d), BAIXA (30d) com descrição de impacto
- **Histórico de Status** — Trilha de alterações com timestamp
- **Comentário** — Feedback de gestores aos cidadãos
- **Log de Auditoria** — Eventos de acesso, modificação e segurança

---

## 🏗️ Arquitetura

```
com/observaacao/
├── Main.java                          # Ponto de entrada
├── view/                              # Camada de Apresentação (CLI)
│   ├── CadastroSolicitacaoView.java  # Formulário de novo protocolo
│   ├── ConsultaSolicitacaoView.java  # Acompanhamento e listagem
│   └── GestaoSolicitacaoView.java    # Painel gerencial
├── service/                           # Lógica de Negócio
│   ├── SolicitacaoService.java       # Regras de CRUD, validações, SLA
│   └── GeradorProtocolo.java         # Geração de número único
├── model/                             # Entidades do Domínio
│   ├── Solicitacao.java
│   ├── Usuario.java
│   ├── Prioridade.java
│   ├── StatusSolicitacao.java
│   ├── Categoria.java
│   ├── FilaAtendimento.java
│   ├── HistoricoStatus.java
│   ├── Comentario.java
│   ├── Anexo.java
│   └── LogAuditoria.java
└── repository/                        # Persistência (em memória)
    └── SolicitacaoRepository.java     # CRUD e consultas
```

### Padrões Aplicados
- **MVC** — Separação clara entre views, services e modelos
- **Repository Pattern** — Abstração da camada de dados
- **Clean Code** — Enum com comportamento, métodos com responsabilidade única, DRY
- **Domain-Driven Design** — Entidades ricas em lógica de negócio
- **Fail Fast** — Validações no início com audit logging integrado

---

## 📋 Pré-requisitos

Before you begin, ensure you have the following installed:

- **Java 17+**
- **Maven 3.8+** (opcional, para build)
- **Git**
- **IDE recomendada:** IntelliJ IDEA Community / VS Code + Extension Pack for Java

### Verificar versão Java
```bash
java -version
```

---

## 🚀 Instalação

### 1. Clonar o Repositório
```bash
git clone https://github.com/seu-usuario/AEP-ESOFT5S-NA-5-SEMESTRE.git
cd AEP-ESOFT5S-NA-5-SEMESTRE/AEP-1-SEMESTRE-ESOFT5S
```

### 2. Compilar o Projeto
```bash
# Com Maven
mvn clean compile

# Ou manualmente com o IDE (Build Project)
```

### 3. Executar
```bash
# Via Maven
mvn exec:java -Dexec.mainClass="com.observaacao.Main"

# Ou execute direto via IDE: Right-click Main.java > Run
```

---

## 📖 Como Usar

### Menu Principal

Ao iniciar, você verá:

```
╔══════════════════════════════════════════════════╗
║              OBSERVAAÇÃO - Menu                  ║
║    Sistema de Solicitações ao Cidadão            ║
║              ODS 16 — Paz e Justiça              ║
╠══════════════════════════════════════════════════╣
║  [1] Nova Solicitação                            ║
║  [2] Acompanhar Solicitação                      ║
║  [3] Listar Todas as Solicitações                ║
║  [4] Menu de Gestão (Gestor)                     ║
║  [0] Sair                                        ║
╚══════════════════════════════════════════════════╝
```

### Fluxos Principais

#### 📝 Registrar Nova Solicitação
1. Escolha **[1]** no menu
2. Selecione a **categoria** (Infraestrutura, Saúde, Educação, etc)
3. Escolha o **bairro/localização**
4. Digite a **descrição** do problema
5. Defina a **prioridade** (1=Crítica, 4=Baixa)
6. Escolha se deseja ser **anônimo** (s/n)
7. Receba o **número de protocolo** para rastreamento

#### 🔍 Acompanhar Solicitação
1. Escolha **[2]** no menu
2. Digite seu **protocolo** (ex: `AEP-2024-001`)
3. Visualize status, prazos de SLA e histórico

#### 👤 Painel de Gestão
1. Escolha **[4]** (acesso com permissão de gestor)
2. Visualize **fila de atendimento** ordenada por prioridade
3. **Altere status** de solicitações
4. **Deixe comentários** para o cidadão
5. **Consulte logs de auditoria** para compliance

---

## 🔐 Atributos de Segurança e Conformidade

### Anonimato Seguro
- Cidadãos podem denunciar sem identificação
- Sistema protege contra retaliação (ODS 16.10)
- Gestor visualiza apenas "Anônimo" nos dados públicos
- Log interno mantém IP/timestamp para auditoria interna

### Limite de Cadastros
- Máximo 5 solicitações por IP/dia (prevenção de spam)
- Detecção de possíveis duplicatas por similaridade de texto
- Evento `TENTATIVA_ABUSO` registrado em auditoria

### Rastreabilidade Completa
- Cada operação (CREATE, UPDATE, DELETE) registrada com:
  - Data/hora
  - Ator (usuário ou anônimo)
  - Tipo de evento
  - Dados anterior/novo (diff)

---

## 📚 Documentação Adicional

Veja os arquivos de documentação no diretório `docs/`:

- [IHC_Personas.md](docs/IHC_Personas.md) — Personas, casos de uso e acessibilidade
- [Manutencao_CleanCode.md](docs/Manutencao_CleanCode.md) — Análise de Clean Code aplicado
- [LICENSE](LICENSE) — Licença MIT

---

## 🤝 Contribuidores

| Nome                    | RA |
|-------------------------|-----|
| Breno Bertaglia Nosima  | 24113673-2 |
| Felipe Galeti Gôngora   | 24036480-2 |
| Gustavo Mazeto Pasquini | 24183078-2 |
| Henrique Kendi Ikeda    | 24039456-2 |



---

## 📊 Status do Projeto

- [x] Modelagem de entidades
- [x] Camada de repository
- [x] Lógica de negócio (service)
- [x] Interface CLI
- [x] Log de auditoria
- [ ] Integração com banco de dados (fase 2)
- [ ] Conversão para o Spring Boot (fase 2)
- [ ] API RESTFull (fase 2)
- [ ] Frontend web responsivo (fase 2)

---

## 📜 Licença

Este projeto é licenciado sob a **Licença MIT** — veja o arquivo [LICENSE](LICENSE) para detalhes.

```
MIT License

Copyright (c) 2024 Equipe ObservaAção

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
...
```

---

## 🌍 Alinhamento com ODS 16

ObservaAção contribui diretamente para:

| Meta | Contribuição | Métrica |
|------|---|---|
| **16.6** | Transparência de decisões públicas | % de solicitações com status atualizado |
| **16.7** | Participação cidadã inclusiva | Acessibilidade WCAG AA |
| **16.10** | Proteção de denunciantes | Suporte a anonimato seguro |
| **17.8** | Transferência de tecnologia | Código aberto, documentado e reutilizável |

---

<div align="center">

[⬆ Voltar ao topo](#observaação---sistema-de-ouvidoria-digital-pública)

</div>
