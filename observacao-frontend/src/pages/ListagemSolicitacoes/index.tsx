import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { solicitacoesApi, ApiError } from '../../services/api';
import type { Solicitacao } from '../../types';
import ScreenTitle from '../../components/ScreenTitle';
import StatusBadge from '../../components/StatusBadge';
import { Loading, ErrorMessage, EmptyState } from '../../components/Feedback';

export default function ListagemSolicitacoes() {
  const navigate = useNavigate();
  const [solicitacoes, setSolicitacoes] = useState<Solicitacao[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    solicitacoesApi
      .listar()
      .then(setSolicitacoes)
      .catch((e: ApiError) => setErro(e.message));
  }, []);

  return (
    <div className="page">
      <ScreenTitle>Listagem de Solicitações</ScreenTitle>

      {erro && <ErrorMessage>{erro}</ErrorMessage>}
      {!solicitacoes && !erro && <Loading>Carregando solicitações…</Loading>}

      {solicitacoes && (
        <>
          <p className="listagem-total">
            TOTAL: {solicitacoes.length} solicitação{solicitacoes.length === 1 ? '' : 'ões'}
          </p>

          {solicitacoes.length === 0 ? (
            <EmptyState>Nenhuma solicitação cadastrada.</EmptyState>
          ) : (
            <div className="tabela-wrapper">
              <table className="tabela">
                <thead>
                  <tr>
                    <th>protocolo</th>
                    <th>categoria</th>
                    <th>status</th>
                  </tr>
                </thead>
                <tbody>
                  {solicitacoes.map((s) => (
                    <tr
                      key={s.protocolo}
                      onClick={() => navigate(`/admin/comprovante/${encodeURIComponent(s.protocolo)}`)}
                    >
                      <td className="td-protocolo">{s.protocolo}</td>
                      <td>{s.categoria}</td>
                      <td>
                        <StatusBadge status={s.status} label={s.statusLabel} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      <div className="form-actions">
        <Link className="btn btn-ghost" to="/admin">
          ← Painel
        </Link>
      </div>
    </div>
  );
}
