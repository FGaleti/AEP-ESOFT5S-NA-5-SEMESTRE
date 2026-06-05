import { useNavigate } from 'react-router-dom';

export default function UserHome() {
  const navigate = useNavigate();

  return (
    <div className="page page-home">
      <div className="home-header">
        <h1 className="home-brand">ObservaAção</h1>
        <p className="home-subtitle">Ouvidoria do Cidadão</p>
        <div className="home-avatar" aria-hidden="true">📣</div>
      </div>

      <p className="page-hint">
        Registre um problema na sua cidade ou acompanhe uma solicitação já aberta.
      </p>

      <div className="card-grid">
        <button className="tile tile-primary" onClick={() => navigate('/categorias')}>
          <span className="tile-icon">📝</span>
          <span className="tile-label">Nova Solicitação</span>
        </button>

        <button className="tile" onClick={() => navigate('/buscar')}>
          <span className="tile-icon">🔍</span>
          <span className="tile-label">Buscar Solicitação</span>
        </button>
      </div>
    </div>
  );
}
