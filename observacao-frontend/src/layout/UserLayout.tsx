import { Outlet } from 'react-router-dom';
import Navbar, { type NavItem } from './Navbar';

const ITENS: NavItem[] = [
  { to: '/', label: 'Início', end: true },
  { to: '/categorias', label: 'Nova Solicitação' },
  { to: '/buscar', label: 'Buscar' },
];

export default function UserLayout() {
  return (
    <div className="app-shell">
      <div className="phone">
        <Navbar homeTo="/" brand="ObservaAção" items={ITENS} />
        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
