import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../utils/api';
import './Dashboard.css';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user?.id) return;
    apiClient.get(`/api/stations/registered/${user.id}`)
      .then(({ data }) => setStations(data || []))
      .catch(() => setStations([]))
      .finally(() => setLoading(false));
  }, [user?.id]);

  return (
    <div className="profile-page">
      <div className="profile-container">
        <header className="profile-hero">
          <div className="profile-avatar">{user?.name?.[0]?.toUpperCase() || 'U'}</div>
          <div>
            <span className="profile-eyebrow">HỒ SƠ NGƯỜI DÙNG</span>
            <h1>{user?.name || 'Thành viên'}</h1>
            <p>@{user?.username}</p>
          </div>
        </header>

        <section className="profile-card face-id-card">
          <div className="profile-card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true">
              <path d="M7 3H5a2 2 0 0 0-2 2v2M17 3h2a2 2 0 0 1 2 2v2M7 21H5a2 2 0 0 1-2-2v-2M17 21h2a2 2 0 0 0 2-2v-2" />
              <circle cx="9" cy="10" r="1" /><circle cx="15" cy="10" r="1" /><path d="M8.5 15c2 1.7 5 1.7 7 0" />
            </svg>
          </div>
          <div className="profile-card-copy">
            <h2>Nhận diện khuôn mặt</h2>
            <p>Đăng ký Face ID để xác thực và mở tủ nhanh chóng, an toàn hơn.</p>
          </div>
          <button className="profile-primary-button" onClick={() => navigate('/enroll')}>Đăng ký Face ID</button>
        </section>

        <section className="profile-card station-section">
          <div className="station-heading">
            <div>
              <span className="profile-eyebrow">STATION CỦA BẠN</span>
              <h2>Các trạm đã đăng ký</h2>
            </div>
            {!loading && <span className="station-count">{stations.length} trạm</span>}
          </div>

          {loading ? (
            <div className="profile-loading"><span />Đang tải station...</div>
          ) : stations.length ? (
            <div className="registered-stations">
              {stations.map((station) => (
                <article className="registered-station" key={station.id}>
                  <div className="station-marker">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true"><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></svg>
                  </div>
                  <div><h3>{station.name}</h3><p>{station.address}</p></div>
                  <span className={`station-status ${station.status?.toLowerCase()}`}>{station.status === 'ACTIVE' ? 'Hoạt động' : station.status}</span>
                </article>
              ))}
            </div>
          ) : (
            <div className="profile-empty"><p>Bạn chưa đăng ký station nào.</p><span>Station đã đăng ký sẽ xuất hiện tại đây.</span></div>
          )}
        </section>

        <button onClick={logout} className="profile-logout">Đăng xuất</button>
      </div>
    </div>
  );
}
