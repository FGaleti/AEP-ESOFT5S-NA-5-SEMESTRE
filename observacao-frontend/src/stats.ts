import type { Solicitacao, Status, Prioridade } from './types';
import { STATUS_LABEL, PRIORIDADES } from './constants';

export const STATUS_ORDEM: Status[] = [
  'ABERTO',
  'TRIAGEM',
  'EM_EXECUCAO',
  'RESOLVIDO',
  'ENCERRADO',
  'CANCELADO',
];

export interface ContagemItem {
  chave: string;
  label: string;
  quantidade: number;
}

/** Resumo geral usado nos KPIs do painel administrativo. */
export interface Resumo {
  total: number;
  abertas: number;
  emAndamento: number; // TRIAGEM + EM_EXECUCAO
  finalizadas: number; // RESOLVIDO + ENCERRADO
  canceladas: number;
  atrasadas: number;
}

export function resumir(lista: Solicitacao[]): Resumo {
  const contar = (fn: (s: Solicitacao) => boolean) => lista.filter(fn).length;
  return {
    total: lista.length,
    abertas: contar((s) => s.status === 'ABERTO'),
    emAndamento: contar((s) => s.status === 'TRIAGEM' || s.status === 'EM_EXECUCAO'),
    finalizadas: contar((s) => s.status === 'RESOLVIDO' || s.status === 'ENCERRADO'),
    canceladas: contar((s) => s.status === 'CANCELADO'),
    atrasadas: contar((s) => s.atrasada),
  };
}

export function contarPorStatus(lista: Solicitacao[]): ContagemItem[] {
  return STATUS_ORDEM.map((status) => ({
    chave: status,
    label: STATUS_LABEL[status],
    quantidade: lista.filter((s) => s.status === status).length,
  })).filter((item) => item.quantidade > 0);
}

export function contarPorPrioridade(lista: Solicitacao[]): ContagemItem[] {
  return PRIORIDADES.map((p) => ({
    chave: p.valor as Prioridade,
    label: p.label,
    quantidade: lista.filter((s) => s.prioridade === p.valor).length,
  })).filter((item) => item.quantidade > 0);
}

export function contarPorCategoria(lista: Solicitacao[]): ContagemItem[] {
  const mapa = new Map<string, number>();
  for (const s of lista) {
    mapa.set(s.categoria, (mapa.get(s.categoria) ?? 0) + 1);
  }
  return [...mapa.entries()]
    .map(([categoria, quantidade]) => ({ chave: categoria, label: categoria, quantidade }))
    .sort((a, b) => b.quantidade - a.quantidade);
}
