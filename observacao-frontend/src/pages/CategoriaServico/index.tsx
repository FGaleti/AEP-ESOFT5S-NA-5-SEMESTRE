import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { categoriasApi, ApiError } from '../../services/api';
import type { Categoria } from '../../types';
import { iconeCategoria } from '../../constants';
import ScreenTitle from '../../components/ScreenTitle';
import { Loading, ErrorMessage, EmptyState } from '../../components/Feedback';

export default function CategoriaServico() {
  const navigate = useNavigate();
  const [categorias, setCategorias] = useState<Categoria[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    categoriasApi
      .listar()
      .then(setCategorias)
      .catch((e: ApiError) => setErro(e.message));
  }, []);

  return (
    <div className="page">
      <ScreenTitle>Categoria de Serviço</ScreenTitle>
      <p className="page-hint">Selecione a categoria do problema que deseja registrar.</p>

      {erro && <ErrorMessage>{erro}</ErrorMessage>}
      {!categorias && !erro && <Loading>Carregando categorias…</Loading>}
      {categorias && categorias.length === 0 && (
        <EmptyState>Nenhuma categoria disponível.</EmptyState>
      )}

      <div className="categoria-list">
        {categorias?.map((categoria) => (
          <button
            key={categoria.id}
            className="categoria-btn"
            onClick={() => navigate(`/nova/${categoria.id}`)}
          >
            <span className="categoria-nome">{categoria.descricao}</span>
            <span className="categoria-icone" aria-hidden="true">
              {iconeCategoria(categoria.descricao)}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
