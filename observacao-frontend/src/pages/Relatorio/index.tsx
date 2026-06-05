import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { solicitacoesApi, ApiError } from '../../services/api';
import type { Solicitacao } from '../../types';
import { resumir } from '../../stats';
import { formatarData } from '../../constants';
import ScreenTitle from '../../components/ScreenTitle';
import { Loading, ErrorMessage, EmptyState } from '../../components/Feedback';

export default function Relatorio() {
  const [solicitacoes, setSolicitacoes] = useState<Solicitacao[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    solicitacoesApi
      .listar()
      .then(setSolicitacoes)
      .catch((e: ApiError) => setErro(e.message));
  }, []);

  if (erro) {
    return (
      <div className="page">
        <ScreenTitle>Relatório</ScreenTitle>
        <ErrorMessage>{erro}</ErrorMessage>
      </div>
    );
  }

  if (!solicitacoes) {
    return (
      <div className="page">
        <ScreenTitle>Relatório</ScreenTitle>
        <Loading>Gerando relatório…</Loading>
      </div>
    );
  }

  const resumo = resumir(solicitacoes);

  return (
    <div className="page relatorio">
      <ScreenTitle>Relatório</ScreenTitle>

      <div className="relatorio-cabecalho">
        <strong>ObservaAção — Relatório Consolidado</strong>
        <span>Gerado em {formatarData(new Date().toISOString())}</span>
      </div>

      <div className="relatorio-resumo">
        <span>Total: <strong>{resumo.total}</strong></span>
        <span>Abertas: <strong>{resumo.abertas}</strong></span>
        <span>Em andamento: <strong>{resumo.emAndamento}</strong></span>
        <span>Finalizadas: <strong>{resumo.finalizadas}</strong></span>
        <span>Canceladas: <strong>{resumo.canceladas}</strong></span>
        <span className={resumo.atrasadas > 0 ? 'texto-alerta' : ''}>
          Atrasadas: <strong>{resumo.atrasadas}</strong>
        </span>
      </div>

      {solicitacoes.length === 0 ? (
        <EmptyState>Nenhuma solicitação para relatar.</EmptyState>
      ) : (
        <div className="tabela-wrapper relatorio-tabela">
          <table className="tabela">
            <thead>
              <tr>
                <th>Protocolo</th>
                <th>Categoria</th>
                <th>Prioridade</th>
                <th>Status</th>
                <th>Bairro</th>
                <th>Prazo</th>
              </tr>
            </thead>
            <tbody>
              {solicitacoes.map((s) => (
                <tr key={s.protocolo}>
                  <td className="td-protocolo">{s.protocolo}</td>
                  <td>{s.categoria}</td>
                  <td>{s.prioridadeLabel}</td>
                  <td>
                    {s.statusLabel}
                    {s.atrasada ? ' ⚠' : ''}
                  </td>
                  <td>{s.bairro}</td>
                  <td>{formatarData(s.prazoEstimado)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="form-actions no-print">
        <Link className="btn btn-ghost" to="/admin">
          ← Painel
        </Link>
        <button className="btn btn-primary" onClick={() => window.print()}>
          🖨 Imprimir / PDF
        </button>
      </div>
    </div>
  );
}
