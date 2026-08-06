import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../utils/api';
import './Admin.css';

// ─── Reusable Modal ────────────────────────────────────────────────────────
function Modal({ title, onClose, children }) {
  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [onClose]);

  return (
    <div
      className="admin-modal-overlay"
      role="dialog"
      aria-modal="true"
      aria-label={title}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="admin-modal">
        <div className="admin-modal-header">
          <h3>{title}</h3>
          <button className="admin-modal-close" onClick={onClose} aria-label="Close">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div className="admin-modal-body">{children}</div>
      </div>
    </div>
  );
}

// ─── Users Tab ─────────────────────────────────────────────────────────────
function UsersTab() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [modal, setModal] = useState(null); // null | 'create' | { user }
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ fullName: '', email: '', phoneNumber: '', password: '', role: 'USER' });
  const [formError, setFormError] = useState('');

  const loadUsers = useCallback(() => {
    setLoading(true);
    apiClient.get('/api/admin/users')
      .then(({ data }) => setUsers(data))
      .catch(() => setError('Failed to load users.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { loadUsers(); }, [loadUsers]);

  const openCreate = () => {
    setForm({ fullName: '', email: '', phoneNumber: '', password: '', role: 'USER' });
    setFormError('');
    setModal('create');
  };

  const openEdit = (u) => {
    setForm({ fullName: u.fullName || '', email: u.email || '', phoneNumber: u.phoneNumber || '', password: '', role: u.role || 'USER' });
    setFormError('');
    setModal(u);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      if (modal === 'create') {
        await apiClient.post('/api/admin/users', form);
      } else {
        await apiClient.put(`/api/admin/users/${modal.id}`, form);
      }
      setModal(null);
      loadUsers();
    } catch (err) {
      setFormError(err.response?.data?.message || 'An error occurred. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const deleteUser = async (id, name) => {
    if (!window.confirm(`Delete user "${name}"? This action cannot be undone.`)) return;
    try {
      await apiClient.delete(`/api/admin/users/${id}`);
      loadUsers();
    } catch {
      alert('Failed to delete this user.');
    }
  };

  return (
    <>
      <div className="admin-tab-header">
        <div>
          <h2>Users</h2>
          <p className="admin-tab-desc">Manage user accounts in the system</p>
        </div>
        <button className="admin-btn-primary" onClick={openCreate}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          Add user
        </button>
      </div>

      {loading ? (
        <div className="admin-loading"><div className="admin-spinner" />Loading...</div>
      ) : error ? (
        <div className="admin-error">{error} <button onClick={loadUsers} className="admin-retry-btn">Retry</button></div>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Full name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th style={{ width: '120px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr><td colSpan={5} className="admin-empty-row">No users found.</td></tr>
              ) : users.map((u) => (
                <tr key={u.id}>
                  <td className="admin-td-name">{u.fullName}</td>
                  <td>{u.email || <span className="admin-na">—</span>}</td>
                  <td>{u.phoneNumber}</td>
                  <td>
                    <span className={`admin-role-badge ${u.role === 'ADMIN' ? 'admin' : 'user'}`}>
                      {u.role === 'ADMIN' ? '⚡ Admin' : '👤 User'}
                    </span>
                  </td>
                  <td>
                    <div className="admin-actions">
                      <button className="admin-action-btn edit" onClick={() => openEdit(u)} aria-label="Edit user">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
                      </button>
                      <button className="admin-action-btn delete" onClick={() => deleteUser(u.id, u.fullName)} aria-label="Delete user">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" /></svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modal && (
        <Modal title={modal === 'create' ? 'Add user' : 'Edit user'} onClose={() => setModal(null)}>
          <form className="admin-form" onSubmit={handleSubmit}>
            {formError && <div className="admin-form-error">{formError}</div>}
            <label>Full name <span className="req">*</span>
              <input value={form.fullName} onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))} required disabled={saving} placeholder="Enter full name" />
            </label>
            <label>Email
              <input type="email" value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} disabled={saving} placeholder="Enter email" />
            </label>
            <label>Phone number <span className="req">*</span>
              <input type="tel" value={form.phoneNumber} onChange={(e) => setForm((f) => ({ ...f, phoneNumber: e.target.value }))} required disabled={saving} placeholder="Enter phone number" />
            </label>
            <label>
              {modal === 'create' ? <>Password <span className="req">*</span></> : 'New password (leave blank to keep current)'}
              <input type="password" value={form.password} onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))} required={modal === 'create'} disabled={saving} placeholder={modal === 'create' ? 'Enter password' : 'Leave blank to keep current'} />
            </label>
            <label>Role
              <select value={form.role} onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))} disabled={saving}>
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>
            <div className="admin-form-actions">
              <button type="submit" className="admin-btn-primary" disabled={saving}>
                {saving ? 'Saving...' : (modal === 'create' ? 'Create account' : 'Save changes')}
              </button>
              <button type="button" className="admin-btn-secondary" onClick={() => setModal(null)} disabled={saving}>Cancel</button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}

// ─── Stations Tab ──────────────────────────────────────────────────────────
function StationsTab() {
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [modal, setModal] = useState(null);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ name: '', address: '', status: 'ACTIVE' });
  const [formError, setFormError] = useState('');

  const loadStations = useCallback(() => {
    setLoading(true);
    apiClient.get('/api/admin/stations')
      .then(({ data }) => setStations(data))
      .catch(() => setError('Failed to load stations.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { loadStations(); }, [loadStations]);

  const openCreate = () => {
    setForm({ name: '', address: '', status: 'ACTIVE' });
    setFormError('');
    setModal('create');
  };

  const openEdit = (s) => {
    setForm({ name: s.name || '', address: s.address || '', status: s.status || 'ACTIVE' });
    setFormError('');
    setModal(s);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      if (modal === 'create') {
        await apiClient.post('/api/admin/stations', form);
      } else {
        await apiClient.put(`/api/admin/stations/${modal.id}`, form);
      }
      setModal(null);
      loadStations();
    } catch (err) {
      setFormError(err.response?.data?.message || 'An error occurred. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const deleteStation = async (id, name) => {
    if (!window.confirm(`Delete station "${name}"?`)) return;
    try {
      await apiClient.delete(`/api/admin/stations/${id}`);
      loadStations();
    } catch {
      alert('Failed to delete this station. It may have active orders.');
    }
  };

  const STATUS_STYLE = {
    ACTIVE: 'success', INACTIVE: 'neutral', UNDER_MAINTENANCE: 'warning',
  };

  return (
    <>
      <div className="admin-tab-header">
        <div>
          <h2>Locker Stations</h2>
          <p className="admin-tab-desc">Manage smart locker stations</p>
        </div>
        <button className="admin-btn-primary" onClick={openCreate}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          Add station
        </button>
      </div>

      {loading ? (
        <div className="admin-loading"><div className="admin-spinner" />Loading...</div>
      ) : error ? (
        <div className="admin-error">{error} <button onClick={loadStations} className="admin-retry-btn">Retry</button></div>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Station name</th>
                <th>Address</th>
                <th>Status</th>
                <th style={{ width: '120px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {stations.length === 0 ? (
                <tr><td colSpan={4} className="admin-empty-row">No stations found.</td></tr>
              ) : stations.map((s) => (
                <tr key={s.id}>
                  <td className="admin-td-name">{s.name}</td>
                  <td>{s.address}</td>
                  <td>
                    <span className={`admin-status-badge status-${STATUS_STYLE[s.status] || 'neutral'}`}>
                      {s.status}
                    </span>
                  </td>
                  <td>
                    <div className="admin-actions">
                      <button className="admin-action-btn edit" onClick={() => openEdit(s)} aria-label="Edit station">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
                      </button>
                      <button className="admin-action-btn delete" onClick={() => deleteStation(s.id, s.name)} aria-label="Delete station">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" /></svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modal && (
        <Modal title={modal === 'create' ? 'Add station' : 'Edit station'} onClose={() => setModal(null)}>
          <form className="admin-form" onSubmit={handleSubmit}>
            {formError && <div className="admin-form-error">{formError}</div>}
            <label>Station name <span className="req">*</span>
              <input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required disabled={saving} placeholder="e.g. Station A – Building B" />
            </label>
            <label>Address <span className="req">*</span>
              <input value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} required disabled={saving} placeholder="e.g. 122 Street X, District Y" />
            </label>
            <label>Status
              <select value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))} disabled={saving}>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
                <option value="UNDER_MAINTENANCE">UNDER_MAINTENANCE</option>
              </select>
            </label>
            <div className="admin-form-actions">
              <button type="submit" className="admin-btn-primary" disabled={saving}>
                {saving ? 'Saving...' : (modal === 'create' ? 'Create station' : 'Save changes')}
              </button>
              <button type="button" className="admin-btn-secondary" onClick={() => setModal(null)} disabled={saving}>Cancel</button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}

// ─── Admin Page ────────────────────────────────────────────────────────────
export default function Admin() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('users');

  if (!user || user.role !== 'ADMIN') {
    navigate('/', { replace: true });
    return null;
  }

  return (
    <div className="admin-page">
      {/* Sidebar */}
      <aside className="admin-sidebar">
        <div className="admin-sidebar-logo">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
          <span>Admin</span>
        </div>

        <nav className="admin-nav" aria-label="Admin navigation">
          {[
            {
              key: 'users',
              icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>,
              label: 'Users',
            },
            {
              key: 'stations',
              icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /></svg>,
              label: 'Stations',
            },
          ].map(({ key, icon, label }) => (
            <button
              key={key}
              className={`admin-nav-item ${activeTab === key ? 'active' : ''}`}
              onClick={() => setActiveTab(key)}
              aria-current={activeTab === key ? 'page' : undefined}
            >
              {icon}
              <span>{label}</span>
            </button>
          ))}
        </nav>

        <div className="admin-sidebar-footer">
          <button className="admin-back-btn" onClick={() => navigate('/')}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" />
            </svg>
            <span>Back to home</span>
          </button>
          <button className="admin-logout-btn" onClick={logout}>Sign out</button>
        </div>
      </aside>

      {/* Main content */}
      <main className="admin-main">
        <div className="admin-topbar">
          <div className="admin-topbar-user">
            <div className="admin-topbar-avatar">{user?.name?.[0]?.toUpperCase()}</div>
            <div>
              <div className="admin-topbar-name">{user?.name}</div>
              <div className="admin-topbar-role">Administrator</div>
            </div>
          </div>
        </div>

        <div className="admin-content">
          {activeTab === 'users'    && <UsersTab />}
          {activeTab === 'stations' && <StationsTab />}
        </div>
      </main>
    </div>
  );
}
