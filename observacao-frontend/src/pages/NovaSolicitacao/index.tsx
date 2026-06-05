import { useEffect, useState, type FormEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { categoriasApi, solicitacoesApi, ApiError } from '../../services/api';
import type { Categoria, Prioridade } from '../../types';
import { PRIORIDADES, iconeCategoria } from '../../constants';
import ScreenTitle from '../../components/ScreenTitle';
import { Loading, ErrorMessage } from '../../components/Feedback';

export default function NovaSolicitacao() {
  const { categoriaId } = useParams<{ categoriaId: string }>();
  const navigate = useNavigate();

  const [categoria, setCategoria] = useState<Categoria | null>(null);
  const [erroCarregar, setErroCarregar] = useState<string | null>(null);

  const [anonimo, setAnonimo] = useState(false);
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [telefone, setTelefone] = useState('');
  const [descricao, setDescricao] = useState('');
  const [bairro, setBairro] = useState('');
  const [localizacao, setLocalizacao] = useState('');
  const [prioridade, setPrioridade] = useState<Prioridade>('MEDIA');

  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    const id = Number(categoriaId);
    if (!id) {
      setErroCarregar('Categoria inválida.');
      return;
    }
    categoriasApi
      .obter(id)
      .then(setCategoria)
      .catch((e: ApiError) => setErroCarregar(e.message));
  }, [categoriaId]);

  const minDescricao = anonimo ? 20 : 10;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (!categoria) return;
    if (!anonimo && nome.trim() === '') {
      setErro('Informe seu nome ou registre de forma anônima.');
      return;
    }
    if (descricao.trim().length < minDescricao) {
      setErro(`A descrição precisa ter ao menos ${minDescricao} caracteres.`);
      return;
    }

    setEnviando(true);
    try {
      const criada = await solicitacoesApi.cadastrar({
        categoriaId: categoria.id,
        descricao: descricao.trim(),
        bairro: bairro.trim(),
        localizacao: localizacao.trim(),
        prioridade,
        anonimo,
        nomeUsuario: anonimo ? undefined : nome.trim(),
        emailUsuario: anonimo ? undefined : email.trim(),
        telefoneUsuario: anonimo ? undefined : telefone.trim() || undefined,
      });
      navigate(`/comprovante/${encodeURIComponent(criada.protocolo)}`);
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Erro ao registrar solicitação.');
      setEnviando(false);
    }
  }

  if (erroCarregar) {
    return (
      <div className="page">
        <ScreenTitle>Nova Solicitação</ScreenTitle>
        <ErrorMessage>{erroCarregar}</ErrorMessage>
        <button className="btn btn-ghost" onClick={() => navigate('/categorias')}>
          ← Voltar às categorias
        </button>
      </div>
    );
  }

  if (!categoria) {
    return (
      <div className="page">
        <ScreenTitle>Nova Solicitação</ScreenTitle>
        <Loading />
      </div>
    );
  }

  return (
    <div className="page">
      <ScreenTitle>Nova Solicitação</ScreenTitle>

      <div className="categoria-selecionada">
        <span className="categoria-icone" aria-hidden="true">
          {iconeCategoria(categoria.descricao)}
        </span>
        <div>
          <span className="categoria-selecionada-label">Categoria</span>
          <strong>{categoria.descricao}</strong>
        </div>
      </div>

      <form className="form" onSubmit={handleSubmit}>
        <div className="form-toggle">
          <button
            type="button"
            className={!anonimo ? 'active' : ''}
            onClick={() => setAnonimo(false)}
          >
            Me identificar
          </button>
          <button
            type="button"
            className={anonimo ? 'active' : ''}
            onClick={() => setAnonimo(true)}
          >
            Anônimo
          </button>
        </div>

        {!anonimo && (
          <>
            <label className="field">
              <span>Nome completo *</span>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Seu nome"
                required={!anonimo}
              />
            </label>
            <label className="field">
              <span>E-mail</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="voce@email.com"
              />
            </label>
            <label className="field">
              <span>Telefone (opcional)</span>
              <input
                type="tel"
                value={telefone}
                onChange={(e) => setTelefone(e.target.value)}
                placeholder="(00) 00000-0000"
              />
            </label>
          </>
        )}

        {anonimo && (
          <p className="aviso-anonimo">
            🔒 Nenhum dado pessoal será coletado. Anote o protocolo: ele será o único meio
            de acompanhar a solicitação.
          </p>
        )}

        <label className="field">
          <span>Descrição do problema *</span>
          <textarea
            rows={4}
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
            placeholder="Descreva o problema com detalhes (local, referências, etc.)"
            required
          />
          <small className={descricao.trim().length < minDescricao ? 'hint-warn' : 'hint-ok'}>
            {descricao.trim().length}/{minDescricao} caracteres mínimos
          </small>
        </label>

        <label className="field">
          <span>Bairro *</span>
          <input
            type="text"
            value={bairro}
            onChange={(e) => setBairro(e.target.value)}
            placeholder="Nome do bairro"
            required
          />
        </label>

        <label className="field">
          <span>Localização / Referência *</span>
          <input
            type="text"
            value={localizacao}
            onChange={(e) => setLocalizacao(e.target.value)}
            placeholder="Endereço ou ponto de referência"
            required
          />
        </label>

        <label className="field">
          <span>Prioridade *</span>
          <select
            value={prioridade}
            onChange={(e) => setPrioridade(e.target.value as Prioridade)}
          >
            {PRIORIDADES.map((p) => (
              <option key={p.valor} value={p.valor}>
                {p.label} — SLA {p.slaDias} dias
              </option>
            ))}
          </select>
          <small className="hint-ok">
            {PRIORIDADES.find((p) => p.valor === prioridade)?.descricao}
          </small>
        </label>

        {erro && <ErrorMessage>{erro}</ErrorMessage>}

        <div className="form-actions">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/categorias')}>
            Cancelar
          </button>
          <button type="submit" className="btn btn-primary" disabled={enviando}>
            {enviando ? 'Enviando…' : 'Registrar solicitação'}
          </button>
        </div>
      </form>
    </div>
  );
}
