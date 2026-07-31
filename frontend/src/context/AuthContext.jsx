import React, { createContext, useState, useEffect, useContext } from 'react';
import { parseJwt, apiClient } from '../utils/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('jwt_token'));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      const decoded = parseJwt(token);
      if (decoded && decoded.exp * 1000 > Date.now()) {
        setUser({
          id: decoded.id,
          username: decoded.sub,
          name: decoded.name || decoded.sub,
          role: decoded.role || 'USER',
          exp: decoded.exp
        });
      } else {
        // Token expired
        logout();
      }
    } else {
      setUser(null);
    }
    setLoading(false);
  }, [token]);

  const login = async (username, password) => {
    try {
      const response = await apiClient.post('/api/auth/login', { username, password });

      const data = response.data;

      if (response.status === 200) {
        localStorage.setItem('jwt_token', data.token);
        setToken(data.token);
        return { success: true };
      } else {
        return { success: false, error: data.message || 'Đăng nhập thất bại!' };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Không thể kết nối đến máy chủ!';
      return { success: false, error: errorMsg };
    }
  };

  const logout = () => {
    localStorage.removeItem('jwt_token');
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    isAuthenticated: !!user
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
