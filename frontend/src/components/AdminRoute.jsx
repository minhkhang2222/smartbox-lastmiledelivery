import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Route guard that allows access only to users with role ADMIN.
 * Regular users are redirected to the home page.
 */
export default function AdminRoute({ children }) {
  const { user, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ width: 32, height: 32, border: '3px solid #E2E7EB', borderTopColor: '#00518C', borderRadius: '50%', animation: 'spin 0.9s linear infinite' }} aria-label="Checking permissions" />
      </div>
    );
  }

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== 'ADMIN') return <Navigate to="/" replace />;

  return children;
}
