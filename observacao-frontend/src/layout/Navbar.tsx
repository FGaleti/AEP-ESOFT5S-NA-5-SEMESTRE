import { Link } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-container">
        <h1 className="navbar-brand">🔍 ObservaAção</h1>
        <ul className="navbar-links">
          <li><Link to="/">Home</Link></li>
          <li><Link to="/categorias">Categorias</Link></li>
          <li><Link to="/listagem">Listagem</Link></li>
        </ul>
      </div>
    </nav>
  );
}
