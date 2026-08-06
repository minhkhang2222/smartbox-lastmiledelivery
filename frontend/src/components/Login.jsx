import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Login.css';

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [isWiggling, setIsWiggling] = useState(false);

  useEffect(() => {
    if (user) navigate('/', { replace: true });
  }, [user, navigate]);

  const triggerWiggle = (message) => {
    setError(message);
    setIsWiggling(true);
    setTimeout(() => setIsWiggling(false), 500);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!username || !password) {
      triggerWiggle('Please enter both username and password.');
      return;
    }
    setLoading(true);
    setError('');
    const result = await login(username, password);
    setLoading(false);
    if (result.success) navigate('/', { replace: true });
    else triggerWiggle(result.error);
  };

  return (
    <div className="login-page">
      {/* Left branding panel */}
      <aside className="login-brand-panel" aria-hidden="true">
        <div className="login-brand-logo">
          <div className="login-brand-logo-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </div>
          <span className="login-brand-logo-text">SMART LOCKER</span>
        </div>

        <h2 className="login-brand-headline">
          Intelligent locker management,<br />
          secure &amp; <span>effortless</span>
        </h2>

        <p className="login-brand-desc">
          A smart locker system with face recognition, OTP security,
          and real-time order tracking — all in one platform.
        </p>

        <div className="login-brand-features">
          {[
            { icon: '🔒', text: 'Face recognition & OTP authentication' },
            { icon: '📦', text: 'Real-time order tracking' },
            { icon: '🏢', text: 'Multi-station management' },
          ].map(({ icon, text }) => (
            <div className="login-brand-feature" key={text}>
              <div className="login-brand-feature-dot">{icon}</div>
              <span>{text}</span>
            </div>
          ))}
        </div>
      </aside>

      {/* Right form panel */}
      <div className="login-form-panel">
        <div className={`login-container ${isWiggling ? 'wiggle' : ''}`}>
          <div className="login-header">
            <div className="logo-badge">
              <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </div>
            <h1>SMART LOCKER</h1>
            <p className="login-subtitle">Sign in to manage your account &amp; lockers</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form" noValidate>
            {error && (
              <div className="error-message" role="alert">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                <span>{error}</span>
              </div>
            )}

            <div className="input-group">
              <label htmlFor="username">Username</label>
              <div className="input-wrapper">
                <span className="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
                  </svg>
                </span>
                <input
                  type="text"
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Email or phone number"
                  autoComplete="username"
                  disabled={loading}
                />
              </div>
            </div>

            <div className="input-group">
              <label htmlFor="password">Password</label>
              <div className="input-wrapper">
                <span className="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  disabled={loading}
                />
                <button
                  type="button"
                  className="toggle-password"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={loading}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword
                    ? <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" /><line x1="1" y1="1" x2="23" y2="23" /></svg>
                    : <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
                  }
                </button>
              </div>
            </div>

            <button
              type="submit"
              id="login-submit"
              className={`submit-btn ${loading ? 'loading' : ''}`}
              disabled={loading}
            >
              {loading
                ? <div className="spinner" aria-label="Signing in" />
                : <>
                    <span>Sign in</span>
                    <svg className="btn-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                      <line x1="5" y1="12" x2="19" y2="12" /><polyline points="12 5 19 12 12 19" />
                    </svg>
                  </>
              }
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
