import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import ScreenTitle from '../../components/ScreenTitle';
import { ErrorMessage } from '../../components/Feedback';

export default function BuscarSolicitacao() {
  const navigate = useNavigate();
  const [protocolo, setProtocolo] = useState('');
  const [erro, setErro] = useState<string | null>(null);

  function buscar(e: FormEvent) {
    e.preventDefault();
    const valor = protocolo.trim();
    if (valor === '') {
      setErro('Informe o número do protocolo.');
      return;
    }
    navigate(`/comprovante/${encodeURIComponent(valor)}`);
  }

  return (
    <div className="page">
      <ScreenTitle>Buscar Solicitação</ScreenTitle>
      <p className="page-hint">
        Digite o protocolo recebido ao registrar a solicitação para acompanhar o andamento.
      </p>

      <form className="form" onSubmit={buscar}>
        <label className="field">
          <span>Protocolo</span>
          <input
            type="text"
            value={protocolo}
            onChange={(e) => setProtocolo(e.target.value)}
            placeholder="Ex.: PROT-20260605092132-1001"
            autoFocus
          />
        </label>

        {erro && <ErrorMessage>{erro}</ErrorMessage>}

        <button type="submit" className="btn btn-primary btn-block">
          🔍 Buscar
        </button>
      </form>
    </div>
  );
}
