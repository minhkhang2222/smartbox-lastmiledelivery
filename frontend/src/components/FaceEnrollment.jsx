import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../utils/api';
import './FaceEnrollment.css';

const ANGLES = [
  { id: 'front', label: 'Chính diện', short: 'Nhìn thẳng', icon: 'front' },
  { id: 'left', label: 'Góc trái', short: 'Quay sang trái', icon: 'left' },
  { id: 'right', label: 'Góc phải', short: 'Quay sang phải', icon: 'right' },
  { id: 'up', label: 'Ngẩng nhẹ', short: 'Ngẩng mặt lên', icon: 'up' },
];

const MEDIAPIPE_VERSION = '0.10.22-rc.20250304';
const HOLD_TIME = 1100;

function ShieldIcon({ size = 20 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3 5 6v5c0 4.6 2.7 8.3 7 10 4.3-1.7 7-5.4 7-10V6l-7-3Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
      <path d="m9.3 12 1.8 1.8 3.8-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function FaceGlyph({ direction = 'front', active = false }) {
  const transform = direction === 'left' ? 'translate(-1 0)' : direction === 'right' ? 'translate(1 0)' : '';
  return (
    <svg viewBox="0 0 40 40" aria-hidden="true" className={active ? 'active-glyph' : ''}>
      <g transform={transform}>
        <path d="M13 12c1.6-4.4 11.9-5.8 15.2.2 1.8 3.1 1.2 11.7-.8 15.3-1.8 3.2-4.1 5-7.2 5-3.2 0-5.6-1.8-7.4-5.2-2-3.6-1.8-11.8.2-15.3Z" fill="none" stroke="currentColor" strokeWidth="1.7" />
        <path d="M14 13c.6-5.2 11.6-7.3 14.6.6" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
        {direction === 'left' ? (
          <>
            <path d="M14.5 18.5h3M22 18h1.5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="m16.5 24 2.2.6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </>
        ) : direction === 'right' ? (
          <>
            <path d="M16.5 18H18m4.5.5h3" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="m21.3 24.6 2.2-.6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </>
        ) : (
          <>
            <path d="M15.5 18h3m3 0h3" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="M17.3 25c1.7 1 3.7 1 5.4 0" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </>
        )}
      </g>
      {direction === 'up' && <path d="m20 4-3 3m3-3 3 3" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />}
    </svg>
  );
}

function getPose(landmarks) {
  const leftEye = landmarks[33];
  const rightEye = landmarks[263];
  const nose = landmarks[1];
  const forehead = landmarks[10];
  const chin = landmarks[152];
  const eyeDistance = Math.max(Math.abs(rightEye.x - leftEye.x), 0.001);
  const eyeMidX = (leftEye.x + rightEye.x) / 2;
  const faceHeight = Math.max(chin.y - forehead.y, 0.001);

  return {
    yaw: (nose.x - eyeMidX) / eyeDistance,
    pitch: (nose.y - forehead.y) / faceHeight,
    centerX: (forehead.x + chin.x) / 2,
    centerY: (forehead.y + chin.y) / 2,
    width: Math.abs(rightEye.x - leftEye.x) * 2.25,
  };
}

function meetsAngle(pose, angle) {
  if (angle === 'front') return Math.abs(pose.yaw) < 0.11 && pose.pitch > 0.43;
  if (angle === 'left') return pose.yaw > 0.17;
  if (angle === 'right') return pose.yaw < -0.17;
  return Math.abs(pose.yaw) < 0.16 && pose.pitch < 0.5;
}

const dataURLtoFile = (dataurl, filename) => {
  const arr = dataurl.split(',');
  const mime = arr[0].match(/:(.*?);/)[1];
  const bstr = atob(arr[1]);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], filename, { type: mime });
};

export default function FaceEnrollment() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const landmarkerRef = useRef(null);
  const animationRef = useRef(null);
  const holdStartRef = useRef(null);
  const captureLockRef = useRef(false);
  const lastVideoTimeRef = useRef(-1);

  const [phase, setPhase] = useState('ready');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [captures, setCaptures] = useState({});
  const [message, setMessage] = useState('Đặt khuôn mặt vào giữa khung hình');
  const [quality, setQuality] = useState(0);
  const [modelReady, setModelReady] = useState(false);
  const [error, setError] = useState('');
  const [flash, setFlash] = useState(false);
  const [showHelp, setShowHelp] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const currentAngle = ANGLES[currentIndex];

  const stopCamera = useCallback(() => {
    if (animationRef.current) cancelAnimationFrame(animationRef.current);
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
  }, []);

  useEffect(() => stopCamera, [stopCamera]);

  const loadModel = async () => {
    if (landmarkerRef.current) return true;
    try {
      const moduleUrl = `https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@${MEDIAPIPE_VERSION}/vision_bundle.mjs`;
      const { FaceLandmarker, FilesetResolver } = await import(/* @vite-ignore */ moduleUrl);
      const vision = await FilesetResolver.forVisionTasks(
        `https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@${MEDIAPIPE_VERSION}/wasm`
      );
      const commonOptions = {
        runningMode: 'VIDEO',
        numFaces: 1,
        minFaceDetectionConfidence: 0.6,
        minFacePresenceConfidence: 0.6,
        minTrackingConfidence: 0.6,
      };
      const modelAssetPath = 'https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task';
      try {
        landmarkerRef.current = await FaceLandmarker.createFromOptions(vision, {
          ...commonOptions,
          baseOptions: { modelAssetPath, delegate: 'GPU' },
        });
      } catch {
        landmarkerRef.current = await FaceLandmarker.createFromOptions(vision, {
          ...commonOptions,
          baseOptions: { modelAssetPath, delegate: 'CPU' },
        });
      }
      setModelReady(true);
      return true;
    } catch (modelError) {
      console.error(modelError);
      setError('Không thể tải bộ nhận diện khuôn mặt. Hãy kiểm tra kết nối mạng và thử lại.');
      return false;
    }
  };

  const getBrightness = () => {
    const canvas = canvasRef.current;
    const video = videoRef.current;
    if (!canvas || !video || !video.videoWidth) return 128;
    const ctx = canvas.getContext('2d', { willReadFrequently: true });
    canvas.width = 64;
    canvas.height = 48;
    ctx.drawImage(video, 0, 0, 64, 48);
    const data = ctx.getImageData(0, 0, 64, 48).data;
    let sum = 0;
    for (let i = 0; i < data.length; i += 16) sum += (data[i] + data[i + 1] + data[i + 2]) / 3;
    return sum / (data.length / 16);
  };

  const takeSnapshot = useCallback((angleId) => {
    if (captureLockRef.current) return;
    captureLockRef.current = true;
    const video = videoRef.current;
    const canvas = canvasRef.current;
    const size = Math.min(video.videoWidth, video.videoHeight);
    const sx = (video.videoWidth - size) / 2;
    const sy = (video.videoHeight - size) / 2;
    canvas.width = 720;
    canvas.height = 720;
    const ctx = canvas.getContext('2d');
    ctx.translate(720, 0);
    ctx.scale(-1, 1);
    ctx.drawImage(video, sx, sy, size, size, 0, 0, 720, 720);
    const image = canvas.toDataURL('image/jpeg', 0.9);
    setCaptures((old) => ({ ...old, [angleId]: image }));
    setFlash(true);
    setTimeout(() => setFlash(false), 220);

    if (currentIndex < ANGLES.length - 1) {
      setCurrentIndex((index) => index + 1);
      setQuality(0);
      setMessage('Tốt lắm! Tiếp tục với góc tiếp theo');
      setTimeout(() => {
        captureLockRef.current = false;
        holdStartRef.current = null;
      }, 900);
    } else {
      setPhase('review');
      stopCamera();
      captureLockRef.current = false;
    }
  }, [currentIndex, stopCamera]);

  const detectLoop = useCallback(() => {
    const video = videoRef.current;
    const detector = landmarkerRef.current;
    if (!video || !detector || video.readyState < 2) {
      animationRef.current = requestAnimationFrame(detectLoop);
      return;
    }

    if (video.currentTime !== lastVideoTimeRef.current && !captureLockRef.current) {
      lastVideoTimeRef.current = video.currentTime;
      const results = detector.detectForVideo(video, performance.now());
      const landmarks = results.faceLandmarks?.[0];

      if (!landmarks) {
        setMessage('Chưa tìm thấy khuôn mặt');
        setQuality(0);
        holdStartRef.current = null;
      } else {
        const pose = getPose(landmarks);
        const centered = Math.abs(pose.centerX - 0.5) < 0.15 && Math.abs(pose.centerY - 0.5) < 0.17;
        const rightSize = pose.width > 0.34 && pose.width < 0.73;
        const brightness = getBrightness();
        const wellLit = brightness > 45 && brightness < 235;
        const correctAngle = meetsAngle(pose, ANGLES[currentIndex].id);

        if (!wellLit) {
          setMessage(brightness <= 45 ? 'Di chuyển tới nơi sáng hơn' : 'Tránh ánh sáng chiếu trực tiếp');
          setQuality(0);
          holdStartRef.current = null;
        } else if (!centered) {
          setMessage('Đưa khuôn mặt vào giữa khung');
          setQuality(0);
          holdStartRef.current = null;
        } else if (!rightSize) {
          setMessage(pose.width <= 0.34 ? 'Di chuyển lại gần hơn một chút' : 'Di chuyển ra xa một chút');
          setQuality(0);
          holdStartRef.current = null;
        } else if (!correctAngle) {
          setMessage(ANGLES[currentIndex].short);
          setQuality(0);
          holdStartRef.current = null;
        } else {
          if (!holdStartRef.current) holdStartRef.current = performance.now();
          const progress = Math.min((performance.now() - holdStartRef.current) / HOLD_TIME, 1);
          setQuality(progress);
          setMessage(progress < 1 ? 'Giữ nguyên…' : 'Đã nhận diện');
          if (progress >= 1) takeSnapshot(ANGLES[currentIndex].id);
        }
      }
    }

    if (phase === 'scanning') animationRef.current = requestAnimationFrame(detectLoop);
  }, [currentIndex, phase, takeSnapshot]);

  useEffect(() => {
    if (phase === 'scanning' && modelReady) {
      animationRef.current = requestAnimationFrame(detectLoop);
      return () => cancelAnimationFrame(animationRef.current);
    }
  }, [phase, modelReady, detectLoop]);

  const startCamera = async () => {
    setError('');
    setMessage('Đang khởi động camera…');
    try {
      const streamPromise = navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 1280 }, height: { ideal: 1280 } },
        audio: false,
      });
      const [stream, loaded] = await Promise.all([streamPromise, loadModel()]);
      if (!loaded) {
        stream.getTracks().forEach((track) => track.stop());
        return;
      }
      streamRef.current = stream;
      videoRef.current.srcObject = stream;
      await videoRef.current.play();
      setPhase('scanning');
      setMessage(ANGLES[currentIndex].short);
    } catch (cameraError) {
      const blocked = cameraError?.name === 'NotAllowedError';
      setError(blocked
        ? 'Quyền camera đang bị chặn. Hãy cho phép camera trong cài đặt trình duyệt rồi thử lại.'
        : 'Không tìm thấy camera. Hãy kiểm tra thiết bị và thử lại.');
    }
  };

  const retryAngle = async (index) => {
    const nextCaptures = { ...captures };
    delete nextCaptures[ANGLES[index].id];
    setCaptures(nextCaptures);
    setCurrentIndex(index);
    setPhase('ready');
    setQuality(0);
    setMessage('Đặt khuôn mặt vào giữa khung hình');
  };

  const restart = () => {
    setCaptures({});
    setCurrentIndex(0);
    setPhase('ready');
    setQuality(0);
    setError('');
    setMessage('Đặt khuôn mặt vào giữa khung hình');
  };

  const finish = async () => {
    if (!user || !user.id) {
      setError('Bạn cần đăng nhập trước khi thực hiện đăng ký khuôn mặt.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      const formData = new FormData();
      formData.append('userId', user.id);

      if (captures.front) {
        formData.append('midFace', dataURLtoFile(captures.front, 'front.jpg'));
      }
      if (captures.left) {
        formData.append('leftFace', dataURLtoFile(captures.left, 'left.jpg'));
      }
      if (captures.right) {
        formData.append('rightFace', dataURLtoFile(captures.right, 'right.jpg'));
      }
      if (captures.up) {
        formData.append('upFace', dataURLtoFile(captures.up, 'up.jpg'));
      }

      const response = await apiClient.post('/api/face/register', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.status === 200) {
        setPhase('success');
      } else {
        setError('Đăng ký khuôn mặt thất bại. Vui lòng thử lại.');
      }
    } catch (err) {
      console.error(err);
      const errMsg = err.response?.data?.message || err.response?.data || 'Đã xảy ra lỗi khi gửi dữ liệu lên máy chủ.';
      setError(errMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="face-page">
      <header className="topbar">
        <Link className="brand" to="/" aria-label="TrueID - Trang chủ">
          <span className="brand-mark"><ShieldIcon size={23} /></span>
          <span>True<span>ID</span></span>
        </Link>
        <div className="secure-note"><ShieldIcon size={17} /> Kết nối được bảo mật</div>
        <button className="help-button" type="button" onClick={() => setShowHelp(true)}>
          <span>?</span> Trợ giúp
        </button>
      </header>

      <section className="enrollment-shell">
        <div className="intro-copy">
          <span className="eyebrow">XÁC THỰC KHUÔN MẶT</span>
          <h1>Đăng ký khuôn mặt</h1>
          <p>Vui lòng thực hiện theo hướng dẫn để hoàn tất xác thực.</p>
        </div>

        <div className="stepper" aria-label={`Bước ${currentIndex + 1} trên 4`}>
          {ANGLES.map((angle, index) => {
            const done = Boolean(captures[angle.id]);
            const active = index === currentIndex && phase !== 'success';
            return (
              <div className={`step ${done ? 'done' : ''} ${active ? 'active' : ''}`} key={angle.id}>
                <div className="step-line" />
                <div className="step-dot">
                  {done ? (
                    <svg viewBox="0 0 20 20"><path d="m5.5 10 3 3 6-6" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
                  ) : <FaceGlyph direction={angle.icon} active={active} />}
                </div>
                <span>{angle.label}</span>
              </div>
            );
          })}
        </div>

        {phase === 'review' ? (
          <section className="review-card">
            <div className="review-heading">
              <span className="success-badge">
                <svg viewBox="0 0 24 24"><path d="m7 12 3 3 7-7" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" /></svg>
              </span>
              <div><h2>Kiểm tra ảnh khuôn mặt</h2><p>Đảm bảo hình ảnh rõ nét trước khi hoàn tất.</p></div>
            </div>
            <div className="capture-grid">
              {ANGLES.map((angle, index) => (
                <button key={angle.id} className="capture-tile" type="button" onClick={() => retryAngle(index)}>
                  <img src={captures[angle.id]} alt={angle.label} />
                  <span>{angle.label}</span>
                  <i>
                    <svg viewBox="0 0 20 20"><path d="M14.5 8a5 5 0 1 0 .1 4M14.5 8V4m0 4h-4" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></svg>
                  </i>
                </button>
              ))}
            </div>
            <div className="review-actions">
              <button className="secondary-button" type="button" onClick={restart}>Chụp lại tất cả</button>
              <button className="primary-button" type="button" onClick={finish}>
                Hoàn tất đăng ký
                <svg viewBox="0 0 20 20"><path d="m7 4 6 6-6 6" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></svg>
              </button>
            </div>
          </section>
        ) : phase === 'success' ? (
          <section className="success-card">
            <div className="success-rings">
              <div><svg viewBox="0 0 34 34"><path d="m9 17 5 5 11-11" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" /></svg></div>
            </div>
            <span className="eyebrow">HOÀN TẤT</span>
            <h2>Đăng ký khuôn mặt thành công</h2>
            <p>Bốn góc khuôn mặt đã được ghi nhận an toàn. Bạn có thể sử dụng khuôn mặt để xác thực từ bây giờ.</p>
            <button className="primary-button success-button" type="button" onClick={() => navigate('/')}>Về trang chính</button>
          </section>
        ) : (
          <section className="camera-card">
            <div className="camera-stage">
              <video ref={videoRef} className={phase === 'scanning' ? 'visible' : ''} playsInline muted />
              <div className="camera-gradient" />
              <div className={`face-guide ${quality > 0 ? 'detecting' : ''}`}>
                <svg viewBox="0 0 280 340" preserveAspectRatio="none">
                  <path d="M48 116C55 40 98 18 140 18s85 22 92 98c5 51-10 140-40 179-15 19-34 27-52 27s-37-8-52-27c-30-39-45-128-40-179Z" />
                </svg>
                <div className="scan-line" style={{ opacity: phase === 'scanning' ? 1 : 0 }} />
              </div>
              {phase === 'ready' && (
                <div className="camera-placeholder">
                  <div className="placeholder-face"><FaceGlyph direction={currentAngle.icon} /></div>
                  <h2>{captures[currentAngle.id] ? 'Chụp lại góc mặt' : 'Sẵn sàng xác thực'}</h2>
                  <p>Camera chỉ được bật sau khi bạn cho phép.</p>
                </div>
              )}
              <div className={`camera-flash ${flash ? 'show' : ''}`} />
              {phase === 'scanning' && (
                <div className="status-pill">
                  <span className={quality > 0 ? 'status-ok' : ''}>
                    {quality > 0 ? (
                      <svg viewBox="0 0 20 20"><path d="m5.5 10 3 3 6-6" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
                    ) : <i />}
                  </span>
                  {message}
                </div>
              )}
            </div>

            <div className="camera-panel">
              <div className="angle-instruction">
                <div className="angle-number">{currentIndex + 1}</div>
                <div>
                  <span>BƯỚC {currentIndex + 1} / 4</span>
                  <h2>{currentAngle.short}</h2>
                </div>
                <FaceGlyph direction={currentAngle.icon} active />
              </div>
              <div className="hold-progress" aria-hidden="true"><span style={{ width: `${quality * 100}%` }} /></div>
              <div className="tips">
                <span><i className="light-icon">☼</i> Đủ ánh sáng</span>
                <span><i className="glasses-icon">⌁</i> Bỏ kính & khẩu trang</span>
                <span><i className="still-icon">◎</i> Giữ yên thiết bị</span>
              </div>
              {error && <div className="error-message" role="alert">{error}</div>}
              {phase === 'ready' && (
                <button className="primary-button start-button" type="button" onClick={startCamera}>
                  <svg viewBox="0 0 22 22"><path d="M6 7.5 7.5 5h7L16 7.5h2A2 2 0 0 1 20 9.5v7a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h2Z" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><circle cx="11" cy="13" r="3" fill="none" stroke="currentColor" strokeWidth="1.7" /></svg>
                  Cho phép camera
                </button>
              )}
            </div>
          </section>
        )}

        <p className="privacy-copy"><ShieldIcon size={16} /> Dữ liệu khuôn mặt của bạn được mã hóa và bảo vệ theo tiêu chuẩn bảo mật.</p>
      </section>

      <canvas ref={canvasRef} hidden />

      {showHelp && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setShowHelp(false)}>
          <section className="help-modal" role="dialog" aria-modal="true" aria-labelledby="help-title" onMouseDown={(event) => event.stopPropagation()}>
            <button className="modal-close" type="button" onClick={() => setShowHelp(false)} aria-label="Đóng">×</button>
            <span className="brand-mark"><ShieldIcon size={24} /></span>
            <h2 id="help-title">Mẹo để xác thực nhanh</h2>
            <ol>
              <li>Đứng ở nơi đủ sáng, tránh ánh sáng mạnh phía sau.</li>
              <li>Giữ điện thoại ngang tầm mắt và cách mặt khoảng 30–40 cm.</li>
              <li>Bỏ kính, khẩu trang, mũ và giữ tóc không che khuôn mặt.</li>
              <li>Di chuyển đầu chậm theo chỉ dẫn, hệ thống sẽ tự chụp.</li>
            </ol>
            <button className="primary-button" type="button" onClick={() => setShowHelp(false)}>Đã hiểu</button>
          </section>
        </div>
      )}
    </main>
  );
}
