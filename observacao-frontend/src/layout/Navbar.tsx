import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';

export interface NavItem {
  to: string;
  label: string;
  end?: boolean;
}

interface NavbarProps {
  homeTo: string;
  brand: string;
  subtitle?: string;
  items: NavItem[];
}

export default function Navbar({ homeTo, brand, subtitle, items }: NavbarProps) {
  const [aberto, setAberto] = useState(false);
  const fechar = () => setAberto(false);

  return (
    <header className="navbar">
      <div className="navbar-bar">
        <Link to={homeTo} className="navbar-brand" onClick={fechar}>
          {brand}
          {subtitle && <span className="navbar-brand-suffix">{subtitle}</span>}
        </Link>
        <button
          type="button"
          className="navbar-toggle"
          aria-label="Abrir menu"
          aria-expanded={aberto}
          onClick={() => setAberto((v) => !v)}
        >
          ☰
        </button>
      </div>

      <nav className={`navbar-menu ${aberto ? 'is-open' : ''}`}>
        {items.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end} onClick={fechar}>
            {item.label}
          </NavLink>
        ))}
      </nav>
    </header>
  );
}
