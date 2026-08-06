import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import './MainLayout.css';

export default function MainLayout() {
  return (
    <div className="app-layout">
      <main className="app-content"><Outlet /></main>
      <nav className="bottom-nav" aria-label="Main navigation">
        <div className="bottom-nav-container">
          {/* Profile */}
          <NavLink to="/" end className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}>
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <circle cx="12" cy="8" r="4" />
              <path d="M4 21a8 8 0 0 1 16 0" />
            </svg>
            <span className="tab-label">Profile</span>
          </NavLink>

          {/* Orders */}
          <NavLink to="/orders" className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}>
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M9 11l3 3L22 4" />
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
            </svg>
            <span className="tab-label">Orders</span>
          </NavLink>

          {/* Face ID */}
          <NavLink to="/enroll" className={({ isActive }) => `nav-tab ${isActive ? 'active' : ''}`}>
            <svg className="tab-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M7 3H5a2 2 0 0 0-2 2v2M17 3h2a2 2 0 0 1 2 2v2M7 21H5a2 2 0 0 1-2-2v-2M17 21h2a2 2 0 0 0 2-2v-2" />
              <circle cx="9" cy="10" r="1" />
              <circle cx="15" cy="10" r="1" />
              <path d="M8.5 15c2 1.7 5 1.7 7 0" />
            </svg>
            <span className="tab-label">Face ID</span>
          </NavLink>
        </div>
      </nav>
    </div>
  );
}
