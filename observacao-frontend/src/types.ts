// ─── Tipos compartilhados com a API Spring Boot (ObservaAção) ───

export type Prioridade = 'BAIXA' | 'MEDIA' | 'ALTA' | 'CRITICA';

export type Status =
  | 'ABERTO'
  | 'TRIAGEM'
  | 'EM_EXECUCAO'
  | 'RESOLVIDO'
  | 'ENCERRADO'
  | 'CANCELADO';

export interface Categoria {
  id: number;
  descricao: string;
  exemploProblema?: string;
}

export interface Usuario {
  id: number;
  nome: string;
  email?: string;
  telefone?: string;
  anonimo: boolean;
  tipo?: string;
}

export interface HistoricoItem {
  statusAnterior: Status | null;
  statusAnteriorLabel: string;
  statusNovo: Status;
  statusNovoLabel: string;
  responsavel: string;
  observacao: string;
  dataMovimentacao: string;
}

export interface Comentario {
  autor: string;
  texto: string;
  dataCriacao: string;
}

export interface Solicitacao {
  id: number;
  protocolo: string;
  categoria: string;
  descricao: string;
  bairro: string;
  localizacao: string;
  prioridade: Prioridade;
  prioridadeLabel: string;
  slaDias: number;
  status: Status;
  statusLabel: string;
  statusFormatado: string;
  solicitante: string;
  anonimo: boolean;
  dataCriacao: string;
  prazoEstimado: string;
  diasRestantes: number;
  diasAtraso: number;
  atrasada: boolean;
  transicoesPermitidas: Status[];
  historico: HistoricoItem[];
  comentarios: Comentario[];
}

// ─── Payloads de requisição ───

export interface CadastrarSolicitacao {
  categoriaId: number;
  descricao: string;
  bairro: string;
  localizacao: string;
  prioridade: Prioridade;
  nomeUsuario?: string;
  emailUsuario?: string;
  telefoneUsuario?: string;
  anonimo: boolean;
}

export interface AtualizarStatus {
  novoStatus: Status;
  observacao: string;
  nomeResponsavel: string;
}

export interface AdicionarComentario {
  texto: string;
  autor: string;
}
