import React, { useEffect, useMemo, useState } from 'react';
import { apiClient } from '../utils/api';
import { useAuth } from '../context/AuthContext';
import './CreateOrder.css';

const BUSY_STATUSES = new Set(['WAITING_FOR_DEPOSIT', 'PENDING', 'WAITING_FOR_PICKUP']);

export default function CreateOrder() {
  const { user } = useAuth();
  const [stations, setStations] = useState([]);
  const [stationId, setStationId] = useState('');
  const [lockers, setLockers] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [recipientPhoneNumber, setRecipientPhoneNumber] = useState('');
  const [loadingStations, setLoadingStations] = useState(true);
  const [loadingLockers, setLoadingLockers] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    apiClient.get('/api/stations')
      .then(({ data }) => {
        const active = (data || []).filter((station) => !station.status || station.status.toUpperCase() === 'ACTIVE');
        setStations(active);
        if (active.length) setStationId(active[0].id);
      })
      .catch(() => setMessage({ type: 'error', text: 'Không thể tải danh sách trạm. Vui lòng kiểm tra backend.' }))
      .finally(() => setLoadingStations(false));
  }, []);

  useEffect(() => {
    if (!stationId) return setLockers([]);
    setLoadingLockers(true);
    setSelectedIds([]);
    setMessage(null);
    apiClient.get(`/api/lockers/station/${stationId}`)
      .then(({ data }) => setLockers(data || []))
      .catch(() => setMessage({ type: 'error', text: 'Không thể tải danh sách tủ của trạm này.' }))
      .finally(() => setLoadingLockers(false));
  }, [stationId]);

  const selectedLockers = useMemo(
    () => lockers.filter((locker) => selectedIds.includes(locker.id)),
    [lockers, selectedIds]
  );

  const isAvailable = (locker) => !BUSY_STATUSES.has(locker.status);

  const toggleLocker = (locker) => {
    if (!isAvailable(locker) || submitting) return;
    setSelectedIds((current) => current.includes(locker.id)
      ? current.filter((id) => id !== locker.id)
      : [...current, locker.id]);
  };

  const submitOrder = async (event) => {
    event.preventDefault();
    const phone = recipientPhoneNumber.trim();
    if (!stationId || !selectedIds.length || !phone) {
      setMessage({ type: 'error', text: 'Vui lòng chọn trạm, ít nhất một tủ và nhập số điện thoại người nhận.' });
      return;
    }

    setSubmitting(true);
    setMessage(null);
    try {
      const { data } = await apiClient.post('/api/orders', {
        userId: user.id,
        stationId,
        lockerIds: selectedIds,
        recipientPhoneNumber: phone,
      });
      setMessage({ type: 'success', text: `Tạo order thành công. Mã đơn: ${data.orderId}` });
      setSelectedIds([]);
      setRecipientPhoneNumber('');
      const refreshed = await apiClient.get(`/api/lockers/station/${stationId}`);
      setLockers(refreshed.data || []);
    } catch (error) {
      setMessage({ type: 'error', text: error.response?.data?.message || 'Không thể tạo order. Vui lòng thử lại.' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="order-page">
      <div className="order-container">
        <header className="order-header">
          <span className="order-eyebrow">SMART LOCKER</span>
          <h1>Đặt tủ gửi hàng</h1>
          <p>Chọn trạm, chọn các ngăn tủ và nhập thông tin người nhận để bắt đầu.</p>
        </header>

        {message && (
          <div className={`order-message ${message.type}`} role="alert">
            <span aria-hidden="true">{message.type === 'success' ? '✓' : '!'}</span>
            <p>{message.text}</p>
          </div>
        )}

        <form className="order-workspace" onSubmit={submitOrder}>
          <section className="order-main-card" aria-labelledby="locker-title">
            <div className="order-section-heading">
              <div><span className="step-label">BƯỚC 1</span><h2 id="locker-title">Chọn ngăn tủ</h2></div>
              <span className="selection-count">Đã chọn {selectedIds.length}</span>
            </div>

            <label className="order-field-label" htmlFor="order-station">Trạm tủ</label>
            <select id="order-station" className="order-select" value={stationId}
              onChange={(event) => setStationId(event.target.value)} disabled={loadingStations || submitting}>
              {loadingStations && <option>Đang tải trạm...</option>}
              {!loadingStations && !stations.length && <option value="">Không có trạm đang hoạt động</option>}
              {stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.address}</option>)}
            </select>

            <div className="locker-legend" aria-label="Chú thích trạng thái">
              <span><i className="legend-dot available" /> Có thể chọn</span>
              <span><i className="legend-dot selected" /> Đang chọn</span>
              <span><i className="legend-dot unavailable" /> Đang có đơn</span>
            </div>

            {loadingLockers ? <div className="order-loading"><span className="order-spinner" />Đang tải các tủ...</div>
              : lockers.length ? (
                <div className="order-locker-grid">
                  {lockers.map((locker) => {
                    const available = isAvailable(locker);
                    const selected = selectedIds.includes(locker.id);
                    return (
                      <button key={locker.id} type="button"
                        className={`order-locker ${selected ? 'selected' : ''} ${!available ? 'unavailable' : ''}`}
                        onClick={() => toggleLocker(locker)} disabled={!available || submitting}
                        aria-pressed={selected} aria-label={`${locker.lockerCode}, ${available ? 'có thể chọn' : 'đang có đơn'}`}>
                        <span className="locker-door-line" />
                        <strong>{locker.lockerCode}</strong>
                        <span className="locker-state">{selected ? 'Đã chọn' : available ? 'Sẵn sàng' : 'Đang dùng'}</span>
                        <span className="locker-handle" />
                      </button>
                    );
                  })}
                </div>
              ) : <div className="order-empty">Trạm này chưa có tủ để đặt.</div>}
          </section>

          <aside className="order-summary-card">
            <div className="order-section-heading compact"><div><span className="step-label">BƯỚC 2</span><h2>Hoàn tất order</h2></div></div>
            <label className="order-field-label" htmlFor="recipient-phone">Số điện thoại người nhận</label>
            <input id="recipient-phone" className="order-input" type="tel" inputMode="tel" autoComplete="tel"
              placeholder="Ví dụ: 0987654321" value={recipientPhoneNumber}
              onChange={(event) => setRecipientPhoneNumber(event.target.value)} disabled={submitting} required />
            <p className="field-help">Nếu số điện thoại đã có tài khoản, hệ thống sẽ tự liên kết người nhận.</p>
            <div className="order-divider" />
            <div className="order-review">
              <div><span>Người gửi</span><strong>{user?.name}</strong></div>
              <div><span>Số tủ đã chọn</span><strong>{selectedIds.length}</strong></div>
              <div className="selected-codes"><span>Ngăn tủ</span><strong>{selectedLockers.length ? selectedLockers.map((locker) => locker.lockerCode).join(', ') : 'Chưa chọn'}</strong></div>
            </div>
            <button className="create-order-button" type="submit"
              disabled={submitting || !stationId || !selectedIds.length || !recipientPhoneNumber.trim()}>
              {submitting ? <><span className="button-spinner" />Đang tạo order...</> : 'Tạo order'}
            </button>
          </aside>
        </form>
      </div>
    </div>
  );
}
