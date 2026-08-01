import React from 'react';
import { Outlet, NavLink, useLocation } from 'react-router-dom';
import './MainLayout.css';

export default function MainLayout() {
  const location = useLocation();

  return (
    <div className="app-layout">
      <main className="app-content"><Outlet /></main>
      <nav className="bottom-nav" aria-label="Điều hướng chính">
        <div className="bottom-nav-container">
          <NavLink to="/" end className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}>
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M6 3h12l2 4v14H4V7l2-4Z" />
              <path d="M4 8h16M9 12h6M12 10v4" />
            </svg>
            <span className="tab-label">Gửi hàng</span>
          </NavLink>

          <NavLink to="/profile" className={() => `nav-tab ${location.pathname === '/profile' || location.pathname === '/enroll' ? 'active' : ''}`}>
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <circle cx="12" cy="8" r="4" />
              <path d="M4 21a8 8 0 0 1 16 0" />
            </svg>
            <span className="tab-label">Hồ sơ</span>
          </NavLink>
        </div>
      </nav>
    </div>
  );
}
