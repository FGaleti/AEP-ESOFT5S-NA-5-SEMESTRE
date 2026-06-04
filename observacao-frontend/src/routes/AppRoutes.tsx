import { Routes, Route } from 'react-router-dom';
import AppLayout from '../layout/AppLayout';
import HomeAdmin from '../pages/HomeAdmin';
import CategoriaServico from '../pages/CategoriaServico';
import ListagemSolicitacoes from '../pages/ListagemSolicitacoes';
import AtualizacaoStatus from '../pages/AtualizacaoStatus';
import ComprovanteSolicitacao from '../pages/ComprovanteSolicitacao';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout><HomeAdmin /></AppLayout>} path="/" />
      <Route element={<AppLayout><CategoriaServico /></AppLayout>} path="/categorias" />
      <Route element={<AppLayout><ListagemSolicitacoes /></AppLayout>} path="/listagem" />
      <Route element={<AppLayout><AtualizacaoStatus /></AppLayout>} path="/atualizacao/:id" />
      <Route element={<AppLayout><ComprovanteSolicitacao /></AppLayout>} path="/comprovante/:id" />
    </Routes>
  );
}
