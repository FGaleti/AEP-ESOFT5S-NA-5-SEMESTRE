import { useEffect, useState, type FormEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { solicitacoesApi, ApiError } from '../../services/api';
import type { Solicitacao, Status } from '../../types';
import { STATUS_LABEL } from '../../constants';
import ScreenTitle from '../../components/ScreenTitle';
import StatusBadge from '../../components/StatusBadge';
import { Loading, ErrorMessage } from '../../components/Feedback';

export default function AtualizacaoStatus() {
  const { protocolo } = useParams<{ protocolo: string }>();
  const navigate = useNavigate();

  const [solicitacao, setSolicitacao] = useState<Solicitacao | null>(null);
  const [erroCarregar, setErroCarregar] = useState<string | null>(null);

  const [novoStatus, setNovoStatus] = useState<Status | null>(null);
  const [responsavel, setResponsavel] = useState('');
  const [observacao, setObservacao] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!protocolo) return;
    solicitacoesApi
      .consultar(protocolo)
      .then(setSolicitacao)
      .catch((e: ApiError) => setErroCarregar(e.message));
  }, [protocolo]);

  async function confirmar(e: FormEvent) {
    e.preventDefault();
    if (!protocolo || !novoStatus) return;
    if (responsavel.trim() === '') {
      setErro('Informe o nome do responsável.');
      return;
    }
    if (observacao.trim() === '') {
      setErro('A justificativa (observação) é obrigatória.');
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await solicitacoesApi.atualizarStatus(protocolo, {
        novoStatus,
        nomeResponsavel: responsavel.trim(),
        observacao: observacao.trim(),
      });
      navigate(`/admin/comprovante/${encodeURIComponent(protocolo)}`);
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Erro ao atualizar status.');
      setEnviando(false);
    }
  }

  if (erroCarregar) {
    return (
      <div className="page">
        <ScreenTitle>Atualização de Status</ScreenTitle>
        <ErrorMessage>{erroCarregar}</ErrorMessage>
        <Link className="btn btn-ghost" to="/admin/listagem">
          ← Voltar à listagem
        </Link>
      </div>
    );
  }

  if (!solicitacao) {
    return (
      <div className="page">
        <ScreenTitle>Atualização de Status</ScreenTitle>
        <Loading />
      </div>
    );
  }

  const s = solicitacao;
  const terminal = s.transicoesPermitidas.length === 0;

  return (
    <div className="page page-atualizacao">
      <ScreenTitle>Atualização de Status</ScreenTitle>

      <div className="atualizacao-protocolo">
        <span>Protocolo da solicitação</span>
        <strong>{s.protocolo}</strong>
      </div>

      <div className="status-atual">
        <span>Status Atual</span>
        <StatusBadge status={s.status} label={s.statusLabel} />
      </div>

      {terminal ? (
        <ErrorMessage>
          Esta solicitação está em status terminal ({s.statusLabel}) e não pode mais ser
          alterada.
        </ErrorMessage>
      ) : (
        <>
          <h3 className="section-title centro">Novo Status</h3>
          <div className="novo-status-opcoes">
            {s.transicoesPermitidas.map((st) => (
              <button
                key={st}
                type="button"
                className={`status-opcao ${novoStatus === st ? 'selecionado' : ''}`}
                onClick={() => setNovoStatus(st)}
              >
                {STATUS_LABEL[st]}
              </button>
            ))}
          </div>

          {novoStatus && (
            <form className="form" onSubmit={confirmar}>
              <label className="field">
                <span>Responsável *</span>
                <input
                  type="text"
                  value={responsavel}
                  onChange={(e) => setResponsavel(e.target.value)}
                  placeholder="Seu nome"
                  required
                />
              </label>
              <label className="field">
                <span>Justificativa / Observação *</span>
                <textarea
                  rows={3}
                  value={observacao}
                  onChange={(e) => setObservacao(e.target.value)}
                  placeholder="Explique o motivo da mudança de status"
                  required
                />
              </label>

              {erro && <ErrorMessage>{erro}</ErrorMessage>}

              <div className="form-actions">
                <Link className="btn btn-ghost" to={`/admin/comprovante/${encodeURIComponent(s.protocolo)}`}>
                  Cancelar
                </Link>
                <button type="submit" className="btn btn-primary" disabled={enviando}>
                  {enviando ? 'Salvando…' : `Confirmar: ${STATUS_LABEL[novoStatus]}`}
                </button>
              </div>
            </form>
          )}
        </>
      )}
    </div>
  );
}
