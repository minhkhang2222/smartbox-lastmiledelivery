const grid = document.querySelector('#locker-grid');
const summary = document.querySelector('#locker-summary');
const updated = document.querySelector('#locker-updated');
const stationName = document.querySelector('#station-name');
const stationId = document.querySelector('#station-id');
const connection = document.querySelector('#connection');
const refreshButton = document.querySelector('#refresh-button');
const refreshLabel = refreshButton.querySelector('.button-label');
const cameraBadge = document.querySelector('#camera-badge');
const cameraError = document.querySelector('#camera-error');
const cameraStream = document.querySelector('#camera-stream');
const createTab = document.querySelector('#create-tab');
const pickupTab = document.querySelector('#pickup-tab');
const createPanel = document.querySelector('#create-panel');
const pickupPanel = document.querySelector('#pickup-panel');
const orderForm = document.querySelector('#order-form');
const orderMessage = document.querySelector('#order-message');
const recipientPhone = document.querySelector('#recipient-phone');
const createOrderButton = document.querySelector('#create-order-button');
const createOrderLabel = createOrderButton.querySelector('.button-label');
const orderResult = document.querySelector('#order-result');
const selectedCount = document.querySelector('#selected-count');
const selectedCodes = document.querySelector('#selected-codes');
const otpForm = document.querySelector('#otp-form');
const otpInput = document.querySelector('#pickup-otp');
const otpButton = document.querySelector('#otp-button');
const otpLabel = otpButton.querySelector('.button-label');
const otpResult = document.querySelector('#otp-result');
const faceAuthButton = document.querySelector('#face-auth-button');
const faceAuthLabel = faceAuthButton.querySelector('.button-label');
const faceAuthResult = document.querySelector('#face-auth-result');

const AVAILABLE_STATUSES = new Set(['FREE', 'AVAILABLE']);
let lockers = [];
let selectedIds = new Set();
let loadingLockers = false;
let checkingCamera = false;
let submittingOrder = false;
let currentMode = 'create';

async function getJson(url, options = {}) {
  const response = await fetch(url, { cache: 'no-store', ...options });
  let body = null;
  try { body = await response.json(); } catch (_) { /* response body is optional */ }
  if (!response.ok) throw new Error(body?.detail || body?.message || `HTTP ${response.status}`);
  return body;
}

function setMode(mode) {
  const isCreate = mode === 'create';
  currentMode = mode;
  createPanel.hidden = !isCreate;
  pickupPanel.hidden = isCreate;
  createTab.classList.toggle('active', isCreate);
  pickupTab.classList.toggle('active', !isCreate);
  createTab.setAttribute('aria-selected', String(isCreate));
  pickupTab.setAttribute('aria-selected', String(!isCreate));
  if (isCreate) {
    cameraStream.removeAttribute('src');
    loadLockers();
  } else {
    cameraStream.src = '/api/camera/stream';
    checkCamera();
  }
}

function setConnection(online) {
  connection.className = `connection ${online ? 'online' : 'offline'}`;
  connection.querySelector('.connection-label').textContent = online ? 'Connected' : 'Disconnected';
}

function isAvailable(locker) {
  return AVAILABLE_STATUSES.has(String(locker.status || '').toUpperCase());
}

function updateOrderSummary() {
  const selected = lockers.filter((locker) => selectedIds.has(locker.id));
  selectedCount.textContent = String(selected.length);
  selectedCodes.textContent = selected.length ? selected.map((locker) => locker.lockerCode).join(', ') : 'None selected';
  createOrderButton.disabled = submittingOrder || !selected.length || !recipientPhone.value.trim();
}

function toggleLocker(locker) {
  if (!isAvailable(locker) || submittingOrder) return;
  if (selectedIds.has(locker.id)) selectedIds.delete(locker.id);
  else selectedIds.add(locker.id);
  renderLockers();
}

function renderLockers() {
  grid.replaceChildren();
  const availableCount = lockers.filter(isAvailable).length;
  summary.classList.remove('error');
  summary.textContent = `${availableCount}/${lockers.length} available`;

  if (!lockers.length) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.innerHTML = '<strong>No lockers are configured</strong><span>Check the station configuration on the central system.</span>';
    grid.append(empty);
    updateOrderSummary();
    return;
  }

  lockers.forEach((locker) => {
    const available = isAvailable(locker);
    const selected = selectedIds.has(locker.id);
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `locker${selected ? ' selected' : ''}${available ? '' : ' unavailable'}`;
    button.disabled = !available || submittingOrder;
    button.setAttribute('aria-pressed', String(selected));
    button.setAttribute('aria-label', `${locker.lockerCode}, ${selected ? 'selected' : available ? 'available' : 'in use'}`);

    const cabinet = document.createElement('span');
    cabinet.className = 'locker-cabinet';
    cabinet.setAttribute('aria-hidden', 'true');
    cabinet.innerHTML = '<span class="locker-vent"></span><span class="locker-door-line"></span><span class="locker-handle"></span><span class="locker-hinge"></span>';
    const copy = document.createElement('span');
    copy.className = 'locker-copy';
    const code = document.createElement('strong');
    code.className = 'locker-code';
    code.textContent = locker.lockerCode || '—';
    const status = document.createElement('span');
    status.className = 'locker-status';
    status.textContent = selected ? 'Selected' : available ? 'Ready' : 'In use';
    const instruction = document.createElement('span');
    instruction.className = 'locker-select-copy';
    instruction.textContent = selected ? 'Click to remove' : available ? 'Click to select' : 'Unavailable';
    copy.append(code, status, instruction);
    button.append(cabinet, copy);
    button.addEventListener('click', () => toggleLocker(locker));
    grid.append(button);
  });
  updateOrderSummary();
}

async function loadStation() {
  const station = await getJson('/api/station');
  stationName.textContent = station.stationId ? 'Station terminal' : 'Station not configured';
  stationId.textContent = station.stationId ? `Station ${station.stationId.slice(0, 8)}` : 'Missing station ID';
}

async function loadLockers() {
  if (loadingLockers) return;
  loadingLockers = true;
  refreshButton.disabled = true;
  refreshButton.setAttribute('aria-busy', 'true');
  refreshLabel.textContent = 'Refreshing';
  try {
    const data = await getJson('/api/lockers');
    lockers = Array.isArray(data) ? data : [];
    const availableIds = new Set(lockers.filter(isAvailable).map((locker) => locker.id));
    selectedIds = new Set([...selectedIds].filter((id) => availableIds.has(id)));
    renderLockers();
    updated.textContent = `Updated at ${new Date().toLocaleTimeString('en-US')}`;
    setConnection(true);
  } catch (error) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.innerHTML = '<strong>Unable to load lockers</strong><span>Check the connection to the central server and refresh.</span>';
    grid.replaceChildren(empty);
    summary.textContent = 'Disconnected';
    summary.classList.add('error');
    setConnection(false);
  } finally {
    loadingLockers = false;
    refreshButton.disabled = false;
    refreshButton.setAttribute('aria-busy', 'false');
    refreshLabel.textContent = 'Refresh';
  }
}

async function checkCamera() {
  if (checkingCamera) return;
  checkingCamera = true;
  try {
    const camera = await getJson('/api/camera/status');
    cameraBadge.textContent = camera.available ? `Live · ${camera.backend}` : 'No signal';
    cameraBadge.className = `badge ${camera.available ? 'badge-live' : 'badge-error'}`;
    cameraError.classList.toggle('hidden', camera.available);
  } catch (_) {
    cameraBadge.textContent = 'Disconnected';
    cameraBadge.className = 'badge badge-error';
    cameraError.classList.remove('hidden');
  } finally {
    checkingCamera = false;
  }
}

function showOrderMessage(type, text) {
  orderMessage.className = `alert ${type}`;
  orderMessage.textContent = text;
}

async function createOrder(event) {
  event.preventDefault();
  const phone = recipientPhone.value.trim();
  if (!selectedIds.size || !phone) {
    showOrderMessage('error', 'Select at least one locker and enter the recipient phone number.');
    return;
  }

  submittingOrder = true;
  createOrderButton.setAttribute('aria-busy', 'true');
  createOrderLabel.textContent = 'Creating...';
  orderResult.className = 'action-result';
  orderResult.textContent = 'Creating your order.';
  renderLockers();
  try {
    const result = await getJson('/api/orders', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ lockerIds: [...selectedIds], recipientPhoneNumber: phone }) });
    const selectedNames = lockers.filter((locker) => selectedIds.has(locker.id)).map((locker) => locker.lockerCode).join(', ');
    showOrderMessage('success', `Order created. Selected lockers: ${selectedNames}.`);
    orderResult.className = 'action-result success';
    orderResult.textContent = result.orderId ? `Order ID: ${result.orderId}` : 'The order has been recorded.';
    selectedIds.clear();
    recipientPhone.value = '';
  } catch (_) {
    showOrderMessage('error', 'Unable to create the order. Please try again.');
    orderResult.className = 'action-result error';
    orderResult.textContent = 'The order was not created.';
  } finally {
    submittingOrder = false;
    createOrderButton.setAttribute('aria-busy', 'false');
    createOrderLabel.textContent = 'Create Order';
    await loadLockers();
    updateOrderSummary();
  }
}

async function pickupWithOtp(event) {
  event.preventDefault();
  const otpCode = otpInput.value.trim();
  if (!otpCode) return;
  otpButton.disabled = true;
  otpButton.setAttribute('aria-busy', 'true');
  otpLabel.textContent = 'Verifying...';
  otpResult.className = 'action-result';
  otpResult.textContent = 'Checking your pickup code.';
  try {
    const result = await getJson('/api/pickup/otp', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ otpCode }) });
    otpResult.className = 'action-result success';
    otpResult.textContent = result.unlockedLockers?.length ? `Unlocked: ${result.unlockedLockers.join(', ')}.` : 'Pickup locker unlocked.';
    otpInput.value = '';
    await loadLockers();
  } catch (error) {
    otpResult.className = 'action-result error';
    otpResult.textContent = error.message || 'OTP verification failed. Check the code and try again.';
  } finally {
    otpButton.disabled = false;
    otpButton.setAttribute('aria-busy', 'false');
    otpLabel.textContent = 'Verify OTP';
  }
}

async function authenticateFace() {
  faceAuthButton.disabled = true;
  faceAuthButton.setAttribute('aria-busy', 'true');
  faceAuthLabel.textContent = 'Recognizing...';
  faceAuthResult.className = 'action-result';
  faceAuthResult.textContent = 'Keep your face in the frame.';
  try {
    const result = await getJson('/api/face-auth', { method: 'POST' });
    faceAuthResult.className = `action-result ${result.success ? 'success' : 'error'}`;
    faceAuthResult.textContent = result.unlockedLockers?.length ? `Unlocked: ${result.unlockedLockers.join(', ')}.` : 'No active locker is ready to unlock.';
    if (result.success) await loadLockers();
  } catch (_) {
    faceAuthResult.className = 'action-result error';
    faceAuthResult.textContent = 'Face verification failed. Please try again.';
  } finally {
    faceAuthButton.disabled = false;
    faceAuthButton.setAttribute('aria-busy', 'false');
    faceAuthLabel.textContent = 'Verify Face ID';
  }
}

createTab.addEventListener('click', () => setMode('create'));
pickupTab.addEventListener('click', () => setMode('pickup'));
refreshButton.addEventListener('click', loadLockers);
recipientPhone.addEventListener('input', updateOrderSummary);
orderForm.addEventListener('submit', createOrder);
otpForm.addEventListener('submit', pickupWithOtp);
faceAuthButton.addEventListener('click', authenticateFace);

setMode('create');
loadStation().catch(() => { stationName.textContent = 'Unable to read station configuration'; });
loadLockers();
setInterval(() => { if (!document.hidden) loadLockers(); }, 5000);
setInterval(() => { if (!document.hidden && currentMode === 'pickup') checkCamera(); }, 3000);
