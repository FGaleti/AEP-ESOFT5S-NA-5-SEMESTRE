import type { Prioridade, Status } from './types';

// ─── Prioridades (espelha o enum Prioridade do back-end) ───

export interface PrioridadeInfo {
  valor: Prioridade;
  label: string;
  slaDias: number;
  descricao: string;
}

export const PRIORIDADES: PrioridadeInfo[] = [
  { valor: 'BAIXA', label: 'Baixa', slaDias: 30, descricao: 'Impacto localizado, sem risco à segurança' },
  { valor: 'MEDIA', label: 'Média', slaDias: 15, descricao: 'Afeta conforto ou mobilidade de um grupo' },
  { valor: 'ALTA', label: 'Alta', slaDias: 5, descricao: 'Risco à saúde ou segurança da comunidade' },
  { valor: 'CRITICA', label: 'Crítica', slaDias: 2, descricao: 'Risco iminente à vida ou direitos fundamentais' },
];

// ─── Status (espelha o enum StatusSolicitacao do back-end) ───

export const STATUS_LABEL: Record<Status, string> = {
  ABERTO: 'Aberto',
  TRIAGEM: 'Em Triagem',
  EM_EXECUCAO: 'Em Execução',
  RESOLVIDO: 'Resolvido',
  ENCERRADO: 'Encerrado',
  CANCELADO: 'Cancelado',
};

/** Classe CSS (cor do badge) por status. */
export const STATUS_CLASSE: Record<Status, string> = {
  ABERTO: 'badge-aberto',
  TRIAGEM: 'badge-triagem',
  EM_EXECUCAO: 'badge-execucao',
  RESOLVIDO: 'badge-resolvido',
  ENCERRADO: 'badge-encerrado',
  CANCELADO: 'badge-cancelado',
};

// ─── Ícones por categoria (correspondência por palavra-chave) ───

const ICONES_CATEGORIA: Array<[RegExp, string]> = [
  [/ilumin/i, '💡'],
  [/buraco|pavimento|via/i, '🕳️'],
  [/poda|árvore|arvore/i, '🌳'],
  [/limpeza|lixo/i, '🧹'],
  [/saúde|saude/i, '🏥'],
  [/segur/i, '🛡️'],
  [/acessib/i, '♿'],
  [/água|agua|esgoto|saneamento/i, '🚰'],
  [/educa|escola/i, '🏫'],
];

export function iconeCategoria(descricao: string): string {
  const match = ICONES_CATEGORIA.find(([regex]) => regex.test(descricao));
  return match ? match[1] : '📋';
}

// ─── Formatação ───

export function formatarData(iso?: string | null): string {
  if (!iso) return '—';
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return iso;
  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
