import React, { useState, useEffect } from 'react';
import { apiClient } from '../utils/api';
import './LockerStationView.css';

export default function LockerStationView() {
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState('');
  const [lockers, setLockers] = useState([]);
  const [loadingStations, setLoadingStations] = useState(true);
  const [loadingLockers, setLoadingLockers] = useState(false);
  const [error, setError] = useState(null);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Fetch stations on mount
  useEffect(() => {
    fetchStations();
  }, []);

  // Fetch lockers whenever selectedStationId changes
  useEffect(() => {
    if (selectedStationId) {
      fetchLockers(selectedStationId);
    } else {
      setLockers([]);
    }
  }, [selectedStationId]);

  const fetchStations = async () => {
    setLoadingStations(true);
    setError(null);
    try {
      const response = await apiClient.get('/api/stations');
      const data = response.data || [];
      setStations(data);
      if (data.length > 0) {
        setSelectedStationId(data[0].id);
      }
    } catch (err) {
      console.error('Error fetching stations:', err);
      setError('Không thể tải danh sách trạm tủ. Vui lòng kiểm tra lại kết nối backend.');
    } finally {
      setLoadingStations(false);
    }
  };

  const fetchLockers = async (stationId) => {
    setLoadingLockers(true);
    setError(null);
    try {
      const response = await apiClient.get(`/api/lockers/station/${stationId}`);
      setLockers(response.data || []);
    } catch (err) {
      console.error('Error fetching lockers:', err);
      setError('Không thể tải danh sách tủ của trạm này.');
    } finally {
      setLoadingLockers(false);
    }
  };

  // Filtered lockers
  const filteredLockers = lockers.filter((locker) => {
    const matchesStatus = filterStatus === 'ALL' || locker.status === filterStatus;
    const matchesSearch = locker.lockerCode.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesStatus && matchesSearch;
  });

  // Calculate statistics
  const stats = {
    total: lockers.length,
    free: lockers.filter((l) => l.status === 'FREE').length,
    deposit: lockers.filter((l) => l.status === 'WAITING_FOR_DEPOSIT').length,
    pickup: lockers.filter((l) => l.status === 'WAITING_FOR_PICKUP').length,
    unknown: lockers.filter((l) => l.status === 'UNKNOWN').length,
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'FREE':
        return <span className="status-badge status-free">Sẵn sàng (FREE)</span>;
      case 'WAITING_FOR_DEPOSIT':
        return <span className="status-badge status-deposit">Chờ gửi đồ</span>;
      case 'WAITING_FOR_PICKUP':
        return <span className="status-badge status-pickup">Chờ nhận đồ</span>;
      case 'UNKNOWN':
      default:
        return <span className="status-badge status-unknown">Không xác định</span>;
    }
  };

  return (
    <div className="station-view-page">
      <div className="station-view-container">
        
        {/* Header Section */}
        <div className="page-header">
          <div>
            <h1>Danh sách & Trạng thái Tủ</h1>
            <p className="subtitle">Theo dõi trạng thái thời gian thực các tủ thuộc trạm SmartLocker</p>
          </div>
          <button 
            onClick={() => selectedStationId && fetchLockers(selectedStationId)} 
            className="refresh-btn"
            disabled={loadingLockers}
            title="Làm mới dữ liệu"
          >
            <svg className={loadingLockers ? 'spin' : ''} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21.5 2v6h-6M2.5 22v-6h6"></path>
              <path d="M2 11.5a10 10 0 0 1 18.8-4.3L21.5 8M22 12.5a10 10 0 0 1-18.8 4.3L2.5 16"></path>
            </svg>
            <span>{loadingLockers ? 'Đang tải...' : 'Làm mới'}</span>
          </button>
        </div>

        {error && (
          <div className="error-banner">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <span>{error}</span>
          </div>
        )}

        {/* Station Selection */}
        <div className="station-selector-card">
          <label className="selector-label">Chọn Trạm Tủ (Station):</label>
          {loadingStations ? (
            <div className="loading-placeholder">Đang tải danh sách trạm...</div>
          ) : stations.length > 0 ? (
            <select
              value={selectedStationId}
              onChange={(e) => setSelectedStationId(e.target.value)}
              className="station-select"
            >
              {stations.map((st) => (
                <option key={st.id} value={st.id}>
                  {st.name} - {st.address || st.id} ({st.status || 'ACTIVE'})
                </option>
              ))}
            </select>
          ) : (
            <div className="station-input-group">
              <input
                type="text"
                placeholder="Nhập UUID của Station..."
                value={selectedStationId}
                onChange={(e) => setSelectedStationId(e.target.value)}
                className="station-input"
              />
              <button 
                onClick={() => fetchLockers(selectedStationId)}
                className="fetch-btn"
                disabled={!selectedStationId}
              >
                Tải tủ
              </button>
            </div>
          )}
        </div>

        {/* Stats Overview */}
        <div className="stats-grid">
          <div className={`stat-card ${filterStatus === 'ALL' ? 'active-stat' : ''}`} onClick={() => setFilterStatus('ALL')}>
            <span className="stat-value">{stats.total}</span>
            <span className="stat-label">Tổng số tủ</span>
          </div>
          <div className={`stat-card stat-free ${filterStatus === 'FREE' ? 'active-stat' : ''}`} onClick={() => setFilterStatus('FREE')}>
            <span className="stat-value">{stats.free}</span>
            <span className="stat-label">Đang rảnh (Free)</span>
          </div>
          <div className={`stat-card stat-deposit ${filterStatus === 'WAITING_FOR_DEPOSIT' ? 'active-stat' : ''}`} onClick={() => setFilterStatus('WAITING_FOR_DEPOSIT')}>
            <span className="stat-value">{stats.deposit}</span>
            <span className="stat-label">Chờ gửi</span>
          </div>
          <div className={`stat-card stat-pickup ${filterStatus === 'WAITING_FOR_PICKUP' ? 'active-stat' : ''}`} onClick={() => setFilterStatus('WAITING_FOR_PICKUP')}>
            <span className="stat-value">{stats.pickup}</span>
            <span className="stat-label">Chờ nhận</span>
          </div>
        </div>

        {/* Filters and Search Bar */}
        <div className="filter-bar">
          <div className="search-box">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              type="text"
              placeholder="Tìm kiếm mã tủ (vd: L01)..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <div className="filter-tabs">
            {['ALL', 'FREE', 'WAITING_FOR_DEPOSIT', 'WAITING_FOR_PICKUP', 'UNKNOWN'].map((st) => (
              <button
                key={st}
                onClick={() => setFilterStatus(st)}
                className={`filter-tab ${filterStatus === st ? 'active' : ''}`}
              >
                {st === 'ALL' ? 'Tất cả' : st === 'FREE' ? 'Rảnh' : st === 'WAITING_FOR_DEPOSIT' ? 'Chờ gửi' : st === 'WAITING_FOR_PICKUP' ? 'Chờ lấy' : 'Khác'}
              </button>
            ))}
          </div>
        </div>

        {/* Lockers Grid */}
        {loadingLockers ? (
          <div className="lockers-loading">
            <div className="spinner"></div>
            <p>Đang tải danh sách tủ...</p>
          </div>
        ) : filteredLockers.length > 0 ? (
          <div className="lockers-grid">
            {filteredLockers.map((locker) => (
              <div key={locker.id} className={`locker-card status-border-${locker.status.toLowerCase()}`}>
                <div className="locker-card-header">
                  <div className="locker-icon">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="9" y1="3" x2="9" y2="21"></line>
                      <path d="M14 12h2"></path>
                    </svg>
                  </div>
                  <span className="locker-code">{locker.lockerCode}</span>
                </div>

                <div className="locker-card-body">
                  <div className="status-container">
                    {getStatusBadge(locker.status)}
                  </div>
                  <div className="meta-info">
                    <span className="meta-label">ID Tủ:</span>
                    <span className="meta-value" title={locker.id}>{locker.id?.substring(0, 8)}...</span>
                  </div>
                  <div className="meta-info">
                    <span className="meta-label">Device ID:</span>
                    <span className="meta-value" title={locker.deviceId}>{locker.deviceId?.substring(0, 8)}...</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="9" y1="3" x2="9" y2="21"></line>
            </svg>
            <h3>Không tìm thấy tủ nào</h3>
            <p>Chưa có tủ nào khớp với bộ lọc hoặc Station được chọn.</p>
          </div>
        )}

      </div>
    </div>
  );
}
