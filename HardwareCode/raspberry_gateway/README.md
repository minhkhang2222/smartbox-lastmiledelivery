# Raspberry Pi local gateway

FastAPI local server for one Smart Locker station. It serves the phone UI, reads the
camera through one worker, streams the latest frame, and proxies locker data from the
central Spring Boot server.

## Raspberry Pi setup

Picamera2 is supplied by Raspberry Pi OS. Create the virtual environment with access
to system packages so Python can import it:

```bash
sudo apt update
sudo apt install -y python3-picamera2 python3-venv
python3 -m venv --system-site-packages .venv
source .venv/bin/activate
pip install -r HardwareCode/raspberry_gateway/requirements.txt
```

Store the InsightFace model permanently on the Pi. The gateway deliberately refuses
to start face inference when the local model is missing, so it never downloads a
model while handling an authentication request:

```bash
sudo mkdir -p /opt/smartlocker/insightface/models
sudo cp -a /path/to/buffalo_l /opt/smartlocker/insightface/models/
sudo chown -R "$USER":"$USER" /opt/smartlocker/insightface
find /opt/smartlocker/insightface/models/buffalo_l -maxdepth 1 -name '*.onnx'
```

Copy the complete `buffalo_l` directory (including its `.onnx` files) from a machine
where it is already available. Its final layout must be
`/opt/smartlocker/insightface/models/buffalo_l/*.onnx`.

Export the device configuration. Keep the token only on the Raspberry Pi:

```bash
export SMARTLOCKER_BACKEND_URL="https://api.smartboxeiu.site"
export SMARTLOCKER_STATION_ID="your-station-uuid"
export SMARTLOCKER_DEVICE_ID="your-device-uuid"
export SMARTLOCKER_DEVICE_TOKEN="your-device-token"
export SMARTLOCKER_FACE_MODEL_ROOT="/opt/smartlocker/insightface"
export SMARTLOCKER_FACE_MODEL_NAME="buffalo_l"
```

Run from the `smartlocker` directory with exactly one Uvicorn worker:

```bash
uvicorn HardwareCode.raspberry_gateway.app:app --host 0.0.0.0 --port 8000 --workers 1
```

Open `http://<raspberry-pi-ip>:8000` on a phone connected to the same LAN/Wi-Fi.

## Test on a Windows PC over Wi-Fi/LAN

From the `smartlocker` directory, run:

```bat
start_gateway_lan.bat
```

The launcher listens on `0.0.0.0`, disables the Raspberry Pi camera by default, and
prints the URLs that other devices on the same Wi-Fi/LAN can open. Keep the terminal
window open while testing. A custom port can be supplied when needed:

```bat
start_gateway_lan.bat --port 8088
```

If Windows asks for network access, allow Python on **Private networks**. Do not expose
this development gateway directly to the public internet.

For local development without a camera, set
`SMARTLOCKER_CAMERA_BACKEND=disabled`. The locker UI remains usable and reports that
the camera is unavailable.

## Local endpoints

- `GET /` - phone UI
- `GET /api/station` - non-secret station/device identity
- `GET /api/lockers` - lockers at the configured station
- `GET /api/camera/status` - camera health
- `GET /api/camera/stream` - MJPEG stream
- `POST /api/camera/capture` - latest frame as JPEG
- `POST /api/face-auth` - process the latest camera frame and request face unlock
- `POST /api/orders` - create a station order from selected lockers and a recipient phone number (no face recognition)
- `POST /api/pickup/otp` - verify a pickup OTP and unlock the matching lockers at this station

The gateway owns the camera through one `CameraWorker`. Pressing the face-auth button
copies its latest frame for inference; it never opens a second camera connection.
InsightFace and the local `buffalo_l` model are loaded lazily on the first
authentication, then reused for later requests. The gateway checks the local model
directory before constructing InsightFace, preventing its automatic downloader from
running when the model is absent.

The browser never receives the device token. Only the gateway adds it to requests to
the central server. The current Spring endpoint accepts `GET
/api/lockers/station/{stationId}`; when device-token authentication is enabled in
Spring Security, it can read the `Authorization` and `X-Device-Id` headers already
sent by this gateway.
