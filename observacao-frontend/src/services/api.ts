import type {
  Categoria,
  Usuario,
  Solicitacao,
  CadastrarSolicitacao,
  AtualizarStatus,
  AdicionarComentario,
  Status,
} from '../types';

// Em desenvolvimento o caminho relativo "/api" é redirecionado para o
// back-end (http://localhost:8080) pelo proxy do Vite (vite.config.ts).
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

/** Erro com a mensagem amigável vinda do back-end (ErrorResponseDTO). */
export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new ApiError(
      'Não foi possível conectar ao servidor. Verifique se o back-end está em execução.',
      0,
    );
  }

  if (!response.ok) {
    let mensagem = `Erro ${response.status}`;
    try {
      const corpo = await response.json();
      mensagem = corpo.detalhes || corpo.mensagem || corpo.message || mensagem;
    } catch {
      /* corpo não-JSON: mantém a mensagem padrão */
    }
    throw new ApiError(mensagem, response.status);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

// ─── Endpoints ───

export const categoriasApi = {
  listar: () => request<Categoria[]>('/categorias'),
  obter: (id: number) => request<Categoria>(`/categorias/${id}`),
};

export const usuariosApi = {
  listar: () => request<Usuario[]>('/usuarios'),
  contar: () => request<{ total: number }>('/usuarios/count'),
};

export const solicitacoesApi = {
  listar: () => request<Solicitacao[]>('/solicitacoes'),

  consultar: (protocolo: string) =>
    request<Solicitacao>(`/solicitacoes/${encodeURIComponent(protocolo)}`),

  cadastrar: (dados: CadastrarSolicitacao) =>
    request<Solicitacao>('/solicitacoes', {
      method: 'POST',
      body: JSON.stringify(dados),
    }),

  atualizarStatus: (protocolo: string, dados: AtualizarStatus) =>
    request<Solicitacao>(`/solicitacoes/${encodeURIComponent(protocolo)}/status`, {
      method: 'PATCH',
      body: JSON.stringify(dados),
    }),

  adicionarComentario: (protocolo: string, dados: AdicionarComentario) =>
    request<Solicitacao>(`/solicitacoes/${encodeURIComponent(protocolo)}/comentarios`, {
      method: 'POST',
      body: JSON.stringify(dados),
    }),

  listarPorStatus: (status: Status) =>
    request<Solicitacao[]>(`/solicitacoes?status=${status}`),
};
