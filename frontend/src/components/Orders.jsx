import React, { useEffect, useState, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../utils/api';
import './Orders.css';

const STATUS_LABELS = {
  WAITING_FOR_DEPOSIT: { label: 'Awaiting deposit',  cls: 'warning' },
  PENDING:             { label: 'Processing',         cls: 'info'    },
  DELIVERED:           { label: 'Delivered',          cls: 'success' },
  COMPLETED:           { label: 'Completed',          cls: 'success' },
  EXPIRED:             { label: 'Expired',            cls: 'neutral' },
  CANCELLED:           { label: 'Cancelled',          cls: 'danger'  },
};

function formatDate(isoStr) {
  if (!isoStr) return '—';
  try {
    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    }).format(new Date(isoStr));
  } catch { return isoStr; }
}

function OtpReveal({ code }) {
  const [revealed, setRevealed] = useState(false);

  useEffect(() => {
    if (!revealed) return;
    const t = setTimeout(() => setRevealed(false), 5000);
    return () => clearTimeout(t);
  }, [revealed]);

  if (!code) return <span className="otp-empty">No OTP available</span>;

  return (
    <div className="otp-wrapper">
      <span className="otp-code" aria-live="polite">
        {revealed ? code : '•'.repeat(code.length)}
      </span>
      <button
        className={`otp-reveal-btn ${revealed ? 'hide' : 'show'}`}
        onClick={() => setRevealed((v) => !v)}
        aria-label={revealed ? 'Hide OTP' : 'Reveal OTP'}
      >
        {revealed
          ? <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" /><line x1="1" y1="1" x2="23" y2="23" /></svg>
          : <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
        }
        {revealed ? 'Hide' : 'Reveal'}
      </button>
      {revealed && <span className="otp-timer">hides in 5s</span>}
    </div>
  );
}

function OrderCard({ order }) {
  const status = STATUS_LABELS[order.status] || { label: order.status, cls: 'neutral' };
  const shortId = order.orderId?.slice(-8).toUpperCase();

  return (
    <article className="order-card">
      <div className="order-card-header">
        <div className="order-id-group">
          <span className="order-id">#{shortId}</span>
          <span className={`order-status-badge status-${status.cls}`}>{status.label}</span>
        </div>
      </div>

      <div className="order-station">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
        </svg>
        <span className="order-station-name">{order.stationName}</span>
        <span className="order-station-address">{order.stationAddress}</span>
      </div>

      <div className="order-meta-grid">
        <div className="order-meta-item">
          <dt>Recipient</dt>
          <dd>{order.recipientPhoneNumber}</dd>
        </div>
        {order.lockerCodes?.length > 0 && (
          <div className="order-meta-item">
            <dt>Lockers</dt>
            <dd>{order.lockerCodes.join(', ')}</dd>
          </div>
        )}
        <div className="order-meta-item">
          <dt>Created</dt>
          <dd>{formatDate(order.createdAt)}</dd>
        </div>
      </div>

      <div className="order-otp-row">
        <div className="order-otp-label">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
          <span>OTP</span>
        </div>
        <OtpReveal code={order.activeOtpCode} />
      </div>
    </article>
  );
}

export default function Orders() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadOrders = useCallback(() => {
    if (!user?.id) return;
    setLoading(true);
    setError(null);
    apiClient.get(`/api/users/${user.id}/orders`)
      .then(({ data }) => setOrders(data))
      .catch(() => setError('Unable to load orders. Please try again.'))
      .finally(() => setLoading(false));
  }, [user?.id]);

  useEffect(() => { loadOrders(); }, [loadOrders]);

  return (
    <div className="orders-page">
      <div className="orders-container">
        {/* Page header */}
        <div className="orders-header">
          <div>
            <span className="orders-eyebrow">ORDERS</span>
            <h1>My Orders</h1>
            <p className="orders-subtitle">Order history and status with secure OTP codes</p>
          </div>
          <button className="orders-refresh-btn" onClick={loadOrders} aria-label="Refresh" disabled={loading}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"
              style={{ animation: loading ? 'spin 1s linear infinite' : 'none' }}>
              <polyline points="23 4 23 10 17 10" /><polyline points="1 20 1 14 7 14" />
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
            </svg>
            Refresh
          </button>
        </div>

        {/* Content */}
        {loading ? (
          <div className="orders-loading">
            <div className="orders-spinner" aria-label="Loading" />
            <span>Loading orders...</span>
          </div>
        ) : error ? (
          <div className="orders-error" role="alert">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{error}</span>
            <button className="orders-retry-btn" onClick={loadOrders}>Retry</button>
          </div>
        ) : orders.length === 0 ? (
          <div className="orders-empty">
            <div className="orders-empty-icon">📦</div>
            <h3>No orders found</h3>
            <p>You have no incoming orders yet.</p>
          </div>
        ) : (
          <div className="orders-list">
            {orders.map((order) => (
              <OrderCard key={order.orderId} order={order} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
