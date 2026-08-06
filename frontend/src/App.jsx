import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import MainLayout from './components/MainLayout';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import FaceEnrollment from './components/FaceEnrollment';
import Orders from './components/Orders';
import Admin from './components/Admin';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<Login />} />

          {/* Admin-only Route (no bottom nav, full-screen layout) */}
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <Admin />
              </AdminRoute>
            }
          />

          {/* Protected User Routes (inside MainLayout with bottom nav) */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="orders" element={<Orders />} />
            <Route path="enroll" element={<FaceEnrollment />} />
            {/* Legacy redirects */}
            <Route path="profile" element={<Navigate to="/" replace />} />
            <Route path="lockers" element={<Navigate to="/" replace />} />
            <Route path="order" element={<Navigate to="/orders" replace />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
