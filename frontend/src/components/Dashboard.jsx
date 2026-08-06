import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../utils/api';
import './Dashboard.css';

export default function Dashboard() {
  const { user, logout, updateUser } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState({ fullName: '', email: '', phoneNumber: '' });
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    if (!user?.id) return;
    apiClient.get(`/api/users/${user.id}/profile`)
      .then(({ data }) => { setProfile(data); updateUser(data); })
      .catch(() => setMessage({ type: 'error', text: 'Unable to load your profile.' }))
      .finally(() => setLoading(false));
  }, [user?.id]);

  const changeField = (e) => setProfile((cur) => ({ ...cur, [e.target.name]: e.target.value }));

  const saveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const { data } = await apiClient.put(`/api/users/${user.id}/profile`, profile);
      setProfile(data);
      updateUser(data);
      setEditing(false);
      setMessage({ type: 'success', text: 'Profile updated successfully.' });
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Unable to save changes. Please try again.' });
    } finally {
      setSaving(false);
    }
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="profile-page">
      {/* Hero banner */}
      <header className="profile-hero">
        <div className="profile-avatar">{user?.name?.[0]?.toUpperCase() || 'U'}</div>
        <div className="profile-hero-info">
          <span className="profile-eyebrow">USER PROFILE</span>
          <h1>{user?.name || 'Member'}</h1>
          <p className="profile-hero-username">{user?.username}</p>
        </div>
        <div className="profile-hero-badge">
          <span className={`role-badge ${isAdmin ? 'admin' : ''}`}>
            {isAdmin ? '⚡ Admin' : '👤 User'}
          </span>
        </div>
      </header>

      <div className="profile-container">
        {message && (
          <div className={`profile-message ${message.type}`} role="alert">
            {message.type === 'success'
              ? <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12" /></svg>
              : <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" /></svg>
            }
            {message.text}
          </div>
        )}

        {/* Face enrollment card */}
        <section className="profile-card face-id-card">
          <div className="profile-card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true">
              <path d="M7 3H5a2 2 0 0 0-2 2v2M17 3h2a2 2 0 0 1 2 2v2M7 21H5a2 2 0 0 1-2-2v-2M17 21h2a2 2 0 0 0 2-2v-2" />
              <circle cx="9" cy="10" r="1" /><circle cx="15" cy="10" r="1" />
              <path d="M8.5 15c2 1.7 5 1.7 7 0" />
            </svg>
          </div>
          <div className="profile-card-copy">
            <h2>Face recognition</h2>
            <p>Enroll your face for secure verification at a Smart Locker station.</p>
          </div>
          <button className="profile-primary-button" onClick={() => navigate('/enroll')}>
            Enroll face
          </button>
        </section>

        {/* Admin panel shortcut */}
        {isAdmin && (
          <section className="profile-card face-id-card">
            <div className="profile-card-icon" style={{ background: 'rgba(242,178,51,0.12)', color: '#B7791F' }}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
              </svg>
            </div>
            <div className="profile-card-copy">
              <h2>Admin panel</h2>
              <p>Manage users, locker stations, and system configuration.</p>
            </div>
            <button
              className="profile-primary-button"
              style={{ background: '#B7791F' }}
              onClick={() => navigate('/admin')}
            >
              Open Admin
            </button>
          </section>
        )}

        {/* Account info */}
        <section className="profile-card account-section">
          <div className="account-heading">
            <div>
              <span className="profile-eyebrow" style={{ color: 'var(--color-brand-700)' }}>PERSONAL DETAILS</span>
              <h2>Account information</h2>
            </div>
            {!editing && (
              <button className="profile-secondary-button" onClick={() => setEditing(true)} disabled={loading}>
                Edit profile
              </button>
            )}
          </div>

          {loading ? (
            <div className="profile-loading"><span />Loading profile...</div>
          ) : editing ? (
            <form className="profile-form" onSubmit={saveProfile}>
              <label>
                Full name <span style={{ color: 'var(--color-danger-600)' }}>*</span>
                <input name="fullName" value={profile.fullName || ''} onChange={changeField} required disabled={saving} placeholder="Enter your full name" />
              </label>
              <label>
                Email
                <input name="email" type="email" value={profile.email || ''} onChange={changeField} disabled={saving} placeholder="Enter your email" />
              </label>
              <label>
                Phone number <span style={{ color: 'var(--color-danger-600)' }}>*</span>
                <input name="phoneNumber" type="tel" inputMode="tel" value={profile.phoneNumber || ''} onChange={changeField} required disabled={saving} placeholder="Enter your phone number" />
              </label>
              <div className="profile-form-actions">
                <button className="profile-primary-button" type="submit" disabled={saving}>
                  {saving ? 'Saving...' : 'Save changes'}
                </button>
                <button className="profile-secondary-button" type="button" onClick={() => setEditing(false)} disabled={saving}>
                  Cancel
                </button>
              </div>
            </form>
          ) : (
            <dl className="account-details">
              <div><dt>Full name</dt><dd>{profile.fullName || 'Not provided'}</dd></div>
              <div><dt>Email</dt><dd>{profile.email || 'Not provided'}</dd></div>
              <div><dt>Phone number</dt><dd>{profile.phoneNumber || 'Not provided'}</dd></div>
            </dl>
          )}
        </section>

        <button onClick={logout} className="profile-logout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          Sign out
        </button>
      </div>
    </div>
  );
}
