import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import './MainLayout.css';

export default function MainLayout() {
  return (
    <div className="app-layout">
      {/* Main Content Area */}
      <main className="app-content">
        <Outlet />
      </main>

      {/* Fixed Bottom Navigation Menu */}
      <nav className="bottom-nav">
        <div className="bottom-nav-container">
          <NavLink 
            to="/" 
            className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}
            end
          >
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            <span className="tab-label">Trang chủ</span>
          </NavLink>

          <NavLink 
            to="/enroll" 
            className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}
          >
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
              <circle cx="12" cy="13" r="4"></circle>
            </svg>
            <span className="tab-label">Face ID</span>
          </NavLink>

          <NavLink 
            to="/lockers" 
            className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}
          >
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="9" y1="3" x2="9" y2="21"></line>
              <path d="M14 12h2"></path>
            </svg>
            <span className="tab-label">Quản lý Tủ</span>
          </NavLink>
        </div>
      </nav>
    </div>
  );
}
