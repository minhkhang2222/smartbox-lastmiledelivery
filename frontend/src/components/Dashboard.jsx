import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Dashboard.css';

export default function Dashboard() {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);

  const copyToken = () => {
    navigator.clipboard.writeText(token);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const formatExpiry = (exp) => {
    if (!exp) return 'N/A';
    const date = new Date(exp * 1000);
    return date.toLocaleTimeString() + ' ' + date.toLocaleDateString();
  };

  return (
    <div className="dashboard-page">
      <div className="dashboard-container">
        <div className="dashboard-card">
          <div className="dashboard-header">
            <div className="user-avatar">
              <span>{user?.name?.[0]?.toUpperCase() || 'U'}</span>
            </div>
            <h2>Chào mừng, {user?.name || 'Thành viên'}!</h2>
            <div className="badge-role">{user?.role}</div>
          </div>

          <div className="dashboard-content">
            <div className="info-section">
              <h3>Thông tin tài khoản</h3>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">Tên đăng nhập</span>
                  <span className="info-value">{user?.username}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Quyền hạn</span>
                  <span className="info-value text-accent">{user?.role}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Thời hạn Token</span>
                  <span className="info-value">{formatExpiry(user?.exp)}</span>
                </div>
              </div>
            </div>

            <div className="token-section">
              <div className="token-header">
                <h3>JSON Web Token (JWT)</h3>
                <button 
                  onClick={copyToken}
                  className={`copy-btn ${copied ? 'copied' : ''}`}
                  title="Sao chép Token"
                >
                  {copied ? 'Đã sao chép!' : (
                    <>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                      </svg>
                      <span>Sao chép</span>
                    </>
                  )}
                </button>
              </div>
              <div className="token-box">
                <code>{token}</code>
              </div>
              <p className="token-hint">Token này tự động được đính kèm vào header <code>Authorization: Bearer</code> đối với mọi request gửi đi.</p>
            </div>
          </div>

          <div className="dashboard-footer">
            <button onClick={() => navigate('/enroll')} className="enroll-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
                <circle cx="12" cy="13" r="4"></circle>
              </svg>
              <span>Đăng ký khuôn mặt</span>
            </button>

            <button onClick={logout} className="logout-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
              <span>Đăng xuất khỏi hệ thống</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
