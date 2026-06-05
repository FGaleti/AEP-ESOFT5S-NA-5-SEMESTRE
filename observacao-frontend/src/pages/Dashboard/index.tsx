import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { solicitacoesApi, usuariosApi, ApiError } from '../../services/api';
import type { Solicitacao } from '../../types';
import {
  resumir,
  contarPorStatus,
  contarPorCategoria,
  contarPorPrioridade,
} from '../../stats';
import { STATUS_CLASSE } from '../../constants';
import ScreenTitle from '../../components/ScreenTitle';
import BarList from '../../components/BarList';
import { Loading, ErrorMessage } from '../../components/Feedback';

export default function Dashboard() {
  const [solicitacoes, setSolicitacoes] = useState<Solicitacao[] | null>(null);
  const [totalUsuarios, setTotalUsuarios] = useState<number | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    solicitacoesApi
      .listar()
      .then(setSolicitacoes)
      .catch((e: ApiError) => setErro(e.message));
    // Total de usuários é complementar: se falhar, o painel ainda funciona.
    usuariosApi
      .contar()
      .then((r) => setTotalUsuarios(r.total))
      .catch(() => setTotalUsuarios(null));
  }, []);

  if (erro) {
    return (
      <div className="page">
        <ScreenTitle>Dashboard Administrativo</ScreenTitle>
        <ErrorMessage>{erro}</ErrorMessage>
      </div>
    );
  }

  if (!solicitacoes) {
    return (
      <div className="page">
        <ScreenTitle>Dashboard Administrativo</ScreenTitle>
        <Loading>Carregando métricas…</Loading>
      </div>
    );
  }

  const resumo = resumir(solicitacoes);
  const porStatus = contarPorStatus(solicitacoes);
  const porCategoria = contarPorCategoria(solicitacoes);
  const porPrioridade = contarPorPrioridade(solicitacoes);

  return (
    <div className="page">
      <ScreenTitle>Dashboard Administrativo</ScreenTitle>

      <div className="kpi-grid">
        <div className="kpi">
          <span className="kpi-numero">{resumo.total}</span>
          <span className="kpi-label">Total</span>
        </div>
        <div className="kpi">
          <span className="kpi-numero">{resumo.abertas}</span>
          <span className="kpi-label">Abertas</span>
        </div>
        <div className="kpi">
          <span className="kpi-numero">{resumo.emAndamento}</span>
          <span className="kpi-label">Em andamento</span>
        </div>
        <div className="kpi">
          <span className="kpi-numero">{resumo.finalizadas}</span>
          <span className="kpi-label">Finalizadas</span>
        </div>
        <div className={`kpi ${resumo.atrasadas > 0 ? 'kpi-alerta' : ''}`}>
          <span className="kpi-numero">{resumo.atrasadas}</span>
          <span className="kpi-label">Atrasadas</span>
        </div>
        <div className="kpi">
          <span className="kpi-numero">{totalUsuarios ?? '—'}</span>
          <span className="kpi-label">Usuários</span>
        </div>
      </div>

      <h3 className="section-title">Por Status</h3>
      <BarList
        itens={porStatus}
        classePorItem={(i) => STATUS_CLASSE[i.chave as keyof typeof STATUS_CLASSE]}
      />

      <h3 className="section-title">Por Categoria</h3>
      <BarList itens={porCategoria} />

      <h3 className="section-title">Por Prioridade</h3>
      <BarList itens={porPrioridade} />

      <div className="form-actions">
        <Link className="btn btn-ghost" to="/admin">
          ← Painel
        </Link>
        <Link className="btn btn-primary" to="/admin/relatorio">
          Ver relatório
        </Link>
      </div>
    </div>
  );
}
