import { Outlet } from 'react-router-dom';
import Navbar, { type NavItem } from './Navbar';

const ITENS: NavItem[] = [
  { to: '/admin', label: 'Painel', end: true },
  { to: '/admin/listagem', label: 'Solicitações' },
  { to: '/admin/dashboard', label: 'Dashboard' },
  { to: '/admin/relatorio', label: 'Relatório' },
  { to: '/', label: '↩ Área do cidadão' },
];

export default function AdminLayout() {
  return (
    <div className="app-shell">
      <div className="phone">
        <Navbar homeTo="/admin" brand="ObservaAção" subtitle="Admin" items={ITENS} />
        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
