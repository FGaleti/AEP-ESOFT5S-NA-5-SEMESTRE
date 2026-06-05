import { Routes, Route, Navigate } from 'react-router-dom';
import UserLayout from '../layout/UserLayout';
import AdminLayout from '../layout/AdminLayout';

// Área do cidadão
import UserHome from '../pages/UserHome';
import CategoriaServico from '../pages/CategoriaServico';
import NovaSolicitacao from '../pages/NovaSolicitacao';
import BuscarSolicitacao from '../pages/BuscarSolicitacao';
import ComprovanteSolicitacao from '../pages/ComprovanteSolicitacao';

// Área administrativa
import AdminHome from '../pages/AdminHome';
import ListagemSolicitacoes from '../pages/ListagemSolicitacoes';
import AtualizacaoStatus from '../pages/AtualizacaoStatus';
import Dashboard from '../pages/Dashboard';
import Relatorio from '../pages/Relatorio';

export default function AppRoutes() {
  return (
    <Routes>
      {/* ─── Área do cidadão (pública) ─── */}
      <Route element={<UserLayout />}>
        <Route path="/" element={<UserHome />} />
        <Route path="/categorias" element={<CategoriaServico />} />
        <Route path="/nova/:categoriaId" element={<NovaSolicitacao />} />
        <Route path="/buscar" element={<BuscarSolicitacao />} />
        <Route path="/comprovante/:protocolo" element={<ComprovanteSolicitacao />} />
      </Route>

      {/* ─── Área administrativa (acesse via /admin) ─── */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<AdminHome />} />
        <Route path="listagem" element={<ListagemSolicitacoes />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="relatorio" element={<Relatorio />} />
        <Route path="comprovante/:protocolo" element={<ComprovanteSolicitacao />} />
        <Route path="atualizacao/:protocolo" element={<AtualizacaoStatus />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
