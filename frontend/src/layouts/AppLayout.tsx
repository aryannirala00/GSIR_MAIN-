import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from  '../app/AuthContext';
import './AppLayout.css';

export function AppLayout() {
  const { isAuthenticated, logout } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <main className="gsir-app-layout-public">
        <Outlet />
      </main>
    );
  }

  const navLinks = [
    { path: '/', label: 'Dashboard' },
    { path: '/chat', label: 'Chat' },
    { path: '/projects', label: 'Projects' },
    { path: '/notes', label: 'Notes' },
    { path: '/research', label: 'Research' }
  ];

  return (
    <div className="gsir-app-layout">
      <header className="gsir-header">
        <div className="gsir-header__brand">
          <h1>GSIR</h1>
        </div>
        <nav className="gsir-nav" aria-label="Main navigation">
          <ul className="gsir-nav__list">
            {navLinks.map(link => (
              <li key={link.path}>
                <Link 
                  to={link.path} 
                  className={'gsir-nav__link ' + (location.pathname === link.path ? 'gsir-nav__link--active' : '')}
                  aria-current={location.pathname === link.path ? 'page' : undefined}
                >
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        <div className="gsir-header__actions">
          <button onClick={logout} className="gsir-button gsir-button--ghost gsir-button--sm">Logout</button>
        </div>
      </header>
      <main className="gsir-main" id="main-content">
        <Outlet />
      </main>
    </div>
  );
}
