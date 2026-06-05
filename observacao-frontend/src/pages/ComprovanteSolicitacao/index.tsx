import { useEffect, useState, type FormEvent } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { solicitacoesApi, ApiError } from '../../services/api';
import type { Solicitacao } from '../../types';
import { formatarData } from '../../constants';
import ScreenTitle from '../../components/ScreenTitle';
import StatusBadge from '../../components/StatusBadge';
import { Loading, ErrorMessage } from '../../components/Feedback';

export default function ComprovanteSolicitacao() {
  const { protocolo } = useParams<{ protocolo: string }>();
  const navigate = useNavigate();
  const isAdmin = useLocation().pathname.startsWith('/admin');

  const [solicitacao, setSolicitacao] = useState<Solicitacao | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  const [autor, setAutor] = useState('');
  const [texto, setTexto] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erroComentario, setErroComentario] = useState<string | null>(null);

  useEffect(() => {
    if (!protocolo) return;
    solicitacoesApi
      .consultar(protocolo)
      .then(setSolicitacao)
      .catch((e: ApiError) => setErro(e.message));
  }, [protocolo]);

  async function enviarComentario(e: FormEvent) {
    e.preventDefault();
    if (!protocolo || texto.trim() === '') return;
    setEnviando(true);
    setErroComentario(null);
    try {
      const atualizada = await solicitacoesApi.adicionarComentario(protocolo, {
        autor: autor.trim() || 'Cidadão',
        texto: texto.trim(),
      });
      setSolicitacao(atualizada);
      setTexto('');
    } catch (err) {
      setErroComentario(err instanceof ApiError ? err.message : 'Erro ao comentar.');
    } finally {
      setEnviando(false);
    }
  }

  if (erro) {
    return (
      <div className="page">
        <ScreenTitle>Comprovante de Solicitação</ScreenTitle>
        <ErrorMessage>{erro}</ErrorMessage>
        <Link className="btn btn-ghost" to={isAdmin ? '/admin/listagem' : '/buscar'}>
          {isAdmin ? '← Voltar à listagem' : '← Nova busca'}
        </Link>
      </div>
    );
  }

  if (!solicitacao) {
    return (
      <div className="page">
        <ScreenTitle>Comprovante de Solicitação</ScreenTitle>
        <Loading />
      </div>
    );
  }

  const s = solicitacao;

  return (
    <div className="page">
      <ScreenTitle>Comprovante de Solicitação</ScreenTitle>

      {/* Resumo */}
      <div className="comprovante-box">
        <div className="comprovante-row">
          <span>Protocolo:</span>
          <strong>{s.protocolo}</strong>
        </div>
        <div className="comprovante-row">
          <span>Solicitante:</span>
          <strong>{s.solicitante}</strong>
        </div>
        <div className="comprovante-row">
          <span>Categoria:</span>
          <strong>{s.categoria}</strong>
        </div>
        <div className="comprovante-row">
          <span>Prioridade:</span>
          <strong>
            {s.prioridadeLabel} (SLA: {s.slaDias} dias)
          </strong>
        </div>
        <div className="comprovante-row">
          <span>Status:</span>
          <StatusBadge status={s.status} label={s.statusLabel} />
        </div>
        <div className="comprovante-descricao">{s.descricao}</div>
      </div>

      {/* Situação do prazo */}
      <h3 className="section-title">Situação do Prazo</h3>
      <div className={`prazo-box ${s.atrasada ? 'prazo-atrasado' : 'prazo-ok'}`}>
        {s.atrasada ? (
          <>
            <div className="prazo-status">⚠ Atrasado há {s.diasAtraso} dia(s)</div>
            <div className="prazo-linha">
              <span>Prazo limite:</span>
              <span>{formatarData(s.prazoEstimado)}</span>
            </div>
          </>
        ) : (
          <>
            <div className="prazo-status">✓ Dentro do prazo</div>
            <div className="prazo-linha">
              <span>Prazo limite:</span>
              <span>{formatarData(s.prazoEstimado)}</span>
            </div>
            <div className="prazo-linha">
              <span>Dias restantes:</span>
              <span>{s.diasRestantes} dia(s)</span>
            </div>
          </>
        )}
      </div>

      {/* Histórico */}
      <h3 className="section-title">Histórico de Status</h3>
      <div className="historico">
        {s.historico.map((h, i) => (
          <div key={i} className="historico-item">
            <div className="historico-cabecalho">
              <span className="historico-data">[{formatarData(h.dataMovimentacao)}]</span>
              <span className="historico-transicao">
                {h.statusAnteriorLabel} → {h.statusNovoLabel}
              </span>
            </div>
            <div className="historico-por">Por: {h.responsavel}</div>
            {h.observacao && (
              <div className="historico-obs">Observação: {h.observacao}</div>
            )}
          </div>
        ))}
      </div>

      {/* Comentários */}
      <h3 className="section-title">Comentários</h3>
      <div className="comentarios">
        {s.comentarios.length === 0 ? (
          <p className="comentario-vazio">Nenhum comentário registrado…</p>
        ) : (
          s.comentarios.map((c, i) => (
            <div key={i} className="comentario-item">
              <div className="comentario-cabecalho">
                <strong>{c.autor}</strong>
                <span>{formatarData(c.dataCriacao)}</span>
              </div>
              <div className="comentario-texto">{c.texto}</div>
            </div>
          ))
        )}
      </div>

      <form className="form comentario-form" onSubmit={enviarComentario}>
        <input
          type="text"
          value={autor}
          onChange={(e) => setAutor(e.target.value)}
          placeholder="Seu nome (opcional)"
        />
        <textarea
          rows={2}
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="Escreva um comentário…"
          required
        />
        {erroComentario && <ErrorMessage>{erroComentario}</ErrorMessage>}
        <button type="submit" className="btn btn-primary" disabled={enviando}>
          {enviando ? 'Enviando…' : 'Adicionar comentário'}
        </button>
      </form>

      <div className="form-actions">
        {isAdmin ? (
          <>
            <Link className="btn btn-ghost" to="/admin/listagem">
              ← Listagem
            </Link>
            <button
              className="btn btn-primary"
              onClick={() => navigate(`/admin/atualizacao/${encodeURIComponent(s.protocolo)}`)}
            >
              Atualizar status
            </button>
          </>
        ) : (
          <>
            <Link className="btn btn-ghost" to="/buscar">
              🔍 Nova busca
            </Link>
            <Link className="btn btn-primary" to="/">
              Início
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
