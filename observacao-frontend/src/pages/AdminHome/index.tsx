import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { solicitacoesApi, usuariosApi, ApiError } from '../../services/api';
import type { Solicitacao } from '../../types';
import { ErrorMessage } from '../../components/Feedback';

export default function AdminHome() {
  const navigate = useNavigate();
  const [solicitacoes, setSolicitacoes] = useState<Solicitacao[] | null>(null);
  const [totalUsuarios, setTotalUsuarios] = useState<number | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    solicitacoesApi
      .listar()
      .then(setSolicitacoes)
      .catch((e: ApiError) => setErro(e.message));
    usuariosApi
      .contar()
      .then((r) => setTotalUsuarios(r.total))
      .catch(() => setTotalUsuarios(null));
  }, []);

  const abertas = solicitacoes?.filter((s) => s.status === 'ABERTO').length ?? null;

  return (
    <div className="page page-home">
      <div className="home-header">
        <h1 className="home-brand">ObservaAção</h1>
        <p className="home-subtitle">Admin</p>
        <div className="home-avatar" aria-hidden="true">👤</div>
      </div>

      {erro && <ErrorMessage>{erro}</ErrorMessage>}

      <div className="card-grid">
        <button className="tile tile-stat" onClick={() => navigate('/admin/listagem')}>
          <span className="tile-number">{abertas ?? '—'}</span>
          <span className="tile-label">Solicitações Abertas</span>
        </button>

        <button className="tile tile-stat" onClick={() => navigate('/admin/dashboard')}>
          <span className="tile-number">{totalUsuarios ?? '—'}</span>
          <span className="tile-label">Total de Usuários</span>
        </button>

        <button className="tile" onClick={() => navigate('/admin/dashboard')}>
          <span className="tile-icon">📊</span>
          <span className="tile-label">Dashboard Administrativo</span>
        </button>

        <button className="tile" onClick={() => navigate('/admin/relatorio')}>
          <span className="tile-icon">📄</span>
          <span className="tile-label">Relatório</span>
        </button>
      </div>
    </div>
  );
}
