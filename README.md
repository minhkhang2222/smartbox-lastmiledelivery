# SmartLocker

SmartLocker là hệ thống tủ gửi và nhận hàng thông minh, kết hợp ứng dụng web, backend Spring Boot, PostgreSQL, MQTT, thiết bị ESP32/Raspberry Pi và dịch vụ AI nhận diện khuôn mặt.

Hệ thống hỗ trợ:

- Đăng nhập bằng email hoặc số điện thoại, trả về JWT.
- Đăng ký khuôn mặt từ nhiều góc chụp.
- Quản lý station, device và locker bằng UUID.
- Hiển thị trạng thái và lựa chọn locker trên giao diện web.
- Tạo order cho một hoặc nhiều locker.
- Gửi lệnh điều khiển locker qua MQTT.
- Nhận sự kiện đóng cửa từ ESP32.
- Tự động timeout khi không nhận được xác nhận đóng cửa.
- Xác thực khuôn mặt để tìm đơn và mở locker.

## Kiến trúc hệ thống

```text
┌──────────────────────┐
│ React + Vite         │
│ http://localhost:3000│
└──────────┬───────────┘
           │ HTTP / JSON / multipart
           ▼
┌────────────────────────────┐
│ Spring Boot API            │
│ http://localhost:8080      │
│ Auth, Order, Locker, Face  │
└───────┬───────────┬────────┘
        │           │
        │ JPA       │ MQTT/TLS
        ▼           ▼
┌──────────────┐  ┌────────────────┐
│ PostgreSQL   │  │ HiveMQ Broker  │
│ :5432        │  └───────┬────────┘
└──────────────┘          │
                         ▼
                 ┌─────────────────┐
                 │ ESP32 / Device  │
                 │ Solenoid/Sensor │
                 └─────────────────┘

Spring Boot ──HTTP multipart──► FastAPI + InsightFace (:9001)
Raspberry Pi ──embedding──────► Spring Boot face-auth API
```

Vai trò dự kiến của thiết bị:

- **ESP32:** điều khiển relay/solenoid, đọc cảm biến cửa và giao tiếp MQTT.
- **Raspberry Pi:** camera/gateway, thu ảnh hoặc embedding để xác thực khuôn mặt.
- **Backend:** quản lý nghiệp vụ, transaction, trạng thái order và phát lệnh MQTT.
- **FastAPI:** phát hiện khuôn mặt và tạo vector embedding 512 chiều.

## Công nghệ sử dụng

### Backend

- Java 17+
- Spring Boot 4.1.0
- Spring MVC và WebFlux `RestClient`
- Spring Data JPA / Hibernate
- PostgreSQL
- Hibernate Vector
- Java JWT
- BCrypt
- HiveMQ MQTT Client
- Maven

### Frontend

- React 18
- React Router
- Axios
- Vite
- CSS responsive theo design system EIU trong `aiinstruction/eiudesign.md`

### AI

- Python
- FastAPI
- Uvicorn
- OpenCV
- NumPy
- InsightFace (`buffalo_l`)

### Hardware

- ESP32
- Arduino framework
- PubSubClient
- ArduinoJson
- Relay/solenoid và reed switch

## Cấu trúc thư mục

```text
smartlocker/
├── src/main/java/smartlocker/smartlocker/
│   ├── Config/              # RestClient và cấu hình Spring
│   ├── controller/          # REST API controllers
│   ├── dto/                 # Request/response và MQTT payload
│   ├── ENUM/                # Event types
│   ├── exception/           # Business exceptions
│   ├── model/               # JPA entities và trạng thái
│   ├── repository/          # Spring Data repositories
│   ├── schedule/            # Timeout và cập nhật trạng thái order
│   ├── service/             # Order, locker, MQTT và nhận diện khuôn mặt
│   ├── utils/               # JWT utility
│   └── SmartlockerApplication.java
├── src/main/resources/
│   └── application.properties
├── src/test/                # Spring Boot tests
├── frontend/
│   ├── src/components/      # Login, dashboard, locker, order, face enrollment
│   ├── src/context/         # AuthContext
│   ├── src/utils/           # Axios client và JWT parser
│   └── vite.config.js
├── microservice/
│   ├── ApiLayer/app.py      # FastAPI vectorization service
│   └── CoreAILayer/         # Model weights
├── HardwareCode/
│   └── smartlocker_esp32.ino
├── postman/                 # Postman assets
├── aiinstruction/
│   └── eiudesign.md         # UI design specification
├── start_all.bat
├── pom.xml
└── README.md
```

## Yêu cầu môi trường

Khuyến nghị cài đặt:

- JDK 17 trở lên.
- Maven 3.9 trở lên hoặc Maven Wrapper hoạt động.
- PostgreSQL 15 trở lên.
- Node.js và npm.
- Python tương thích với InsightFace và ONNX Runtime.
- Broker MQTT hỗ trợ TLS nếu chạy thiết bị thật.

Kiểm tra phiên bản:

```powershell
java -version
mvn -version
node --version
npm --version
python --version
psql --version
```

## Cấu hình

File cấu hình backend:

```text
src/main/resources/application.properties
```

Các thuộc tính chính:

```properties
spring.application.name=smartlocker

spring.datasource.url=jdbc:postgresql://localhost:5432/smartlocker_db
spring.datasource.username=postgres
spring.datasource.password=<POSTGRES_PASSWORD>
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

mqtt.server-uri=<MQTT_HOST>
mqtt.port=8883
mqtt.username=<MQTT_USERNAME>
mqtt.password=<MQTT_PASSWORD>
mqtt.publish-enabled=true
```

### Chế độ test không điều khiển phần cứng

Khi chỉ test API hoặc giao diện, tắt publish MQTT:

```properties
mqtt.publish-enabled=false
```

Hoặc truyền khi chạy JAR:

```powershell
java -jar target/smartlocker-0.0.1-SNAPSHOT.jar --mqtt.publish-enabled=false
```

> `mqtt.publish-enabled=false` chỉ ngăn `OrderService` gửi command tạo order. MQTT service vẫn được khởi tạo theo cấu hình hiện tại.

### Khuyến nghị dùng biến môi trường

Không commit mật khẩu thật. Có thể đổi properties thành:

```properties
spring.datasource.password=${DB_PASSWORD}
mqtt.username=${MQTT_USERNAME}
mqtt.password=${MQTT_PASSWORD}
```

## Khởi tạo database

Tạo database:

```sql
CREATE DATABASE smartlocker_db;
```

Backend đang dùng:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Khi backend khởi động, Hibernate sẽ tạo/cập nhật phần lớn schema dựa trên entity. Với môi trường production, nên thay bằng Flyway hoặc Liquibase để migration có phiên bản và có thể tái lập.

### Quan hệ dữ liệu chính

```text
User ──< UserStationRegistration >── LockerStation
User ──< Order >───────────────────── LockerStation
Order ──< OrderLocker >────────────── Locker
LockerStation ──< Device ──< Locker
User ──< UserFaceEmbedding
```

Điều kiện để user tạo order tại một station:

1. User tồn tại.
2. Station tồn tại và có trạng thái `ACTIVE`.
3. Có `UserStationRegistration` với cùng `userId`, `stationId` và trạng thái `ACTIVE`.
4. Danh sách locker không rỗng hoặc trùng UUID.
5. Tất cả locker tồn tại và thuộc station được chọn.
6. Locker chưa có `OrderLocker` ở `WAIT_FOR_DEPOSIT` hoặc `WAIT_FOR_COLLECTION`.
7. Device điều khiển locker không ở trạng thái `OFFLINE` hoặc `ERROR`.

`Locker.status` là trạng thái vật lý/khả dụng của phần cứng và không được dùng để quyết định locker có đang thuộc một order hay không.

## Cách chạy dự án

### 1. Chạy PostgreSQL

Đảm bảo PostgreSQL đang lắng nghe tại:

```text
localhost:5432
```

và database `smartlocker_db` đã tồn tại.

### 2. Chạy AI microservice

```powershell
cd microservice\ApiLayer
python app.py
```

Service chạy tại:

```text
http://localhost:9001
```

Swagger UI:

```text
http://localhost:9001/docs
```

### 3. Chạy Spring Boot backend

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Nếu Maven Wrapper trên máy không hoạt động:

```powershell
mvn spring-boot:run
```

Hoặc đóng gói và chạy JAR:

```powershell
mvn clean package
java -jar target\smartlocker-0.0.1-SNAPSHOT.jar
```

Backend mặc định chạy tại:

```text
http://localhost:8080
```

Kiểm tra:

```powershell
Invoke-WebRequest http://localhost:8080/api/health
```

### 4. Chạy frontend

```powershell
cd frontend
npm install
npm run dev
```

Trên Windows nếu PowerShell chặn `npm.ps1`:

```powershell
npm.cmd install
npm.cmd run dev
```

Frontend được cấu hình chạy tại:

```text
http://localhost:3000
```

Vite proxy tất cả request `/api` sang `http://localhost:8080`.

### 5. Chạy tất cả bằng script

```powershell
.\start_all.bat
```

Lưu ý: nội dung hiển thị trong script cũ ghi frontend port `5173`, nhưng `vite.config.js` hiện cấu hình port `3000`.

## Luồng nghiệp vụ order

### Tạo order

1. Frontend lấy user UUID từ JWT.
2. User chọn station đã đăng ký.
3. Frontend tải locker thuộc station.
4. User chọn một hoặc nhiều locker dạng ô vuông.
5. User nhập số điện thoại người nhận.
6. Backend khóa các row locker bằng pessimistic lock để ngăn hai request đồng thời giữ cùng locker.
7. Backend kiểm tra user, registration, station, device và các `OrderLocker` đang hoạt động.
8. Backend tạo `Order` với trạng thái `WAITING_FOR_DEPOSIT`.
9. Backend tạo `OrderLocker` với trạng thái `WAIT_FOR_DEPOSIT`.
10. Nếu MQTT được bật, backend gửi command `WAIT_FOR_DEPOSIT` xuống ESP32.

### Xác nhận gửi đồ

1. ESP32 nhận command.
2. ESP32 mở khóa solenoid.
3. Sau khoảng trễ, ESP32 theo dõi reed switch.
4. Khi cửa đóng ổn định đủ 3 giây, ESP32 publish `DOOR_CLOSED`.
5. Backend chuyển `OrderLocker` sang `WAIT_FOR_COLLECTION`.
6. Scheduler chuyển `Order` sang `PENDING` khi không còn locker chờ gửi và có ít nhất một locker chờ nhận.

### Không nhận xác nhận MQTT

1. `OrderLocker` ở `WAIT_FOR_DEPOSIT` quá 30 giây.
2. Scheduler chuyển `OrderLocker` sang `INACTIVE`.
3. Nếu tất cả locker của order đều timeout, `Order` chuyển sang `FAILED`.

## Trạng thái nghiệp vụ

### OrderStatus

| Trạng thái | Ý nghĩa |
|---|---|
| `WAITING_FOR_DEPOSIT` | Order vừa tạo, chờ người gửi bỏ đồ |
| `PENDING` | Đã có đồ trong ít nhất một locker |
| `WAITING_FOR_PICKUP` | Chờ người nhận lấy đồ |
| `COMPLETED` | Đã hoàn tất |
| `CANCELLED` | Đã hủy |
| `FAILED` | Tạo/gửi đồ thất bại hoặc toàn bộ locker timeout |

### OrderLockerStatus

| Trạng thái | Ý nghĩa |
|---|---|
| `WAIT_FOR_DEPOSIT` | Chờ người gửi bỏ đồ và đóng cửa |
| `WAIT_FOR_COLLECTION` | Đồ đã trong tủ, chờ người nhận |
| `INACTIVE` | Không còn giữ locker, đã hoàn tất hoặc timeout |

### Locker vật lý

Trạng thái vật lý của `Locker` không đại diện cho việc locker đang trống về mặt order. Việc đang được giữ hay không được xác định từ `OrderLockerStatus`.

## MQTT

### Topic chuẩn

Backend và ESP32 dùng cùng thứ tự UUID:

```text
smartlocker/{stationId}/{deviceId}/command
smartlocker/{stationId}/{deviceId}/event
```

ESP32 subscribe:

```text
smartlocker/{stationId}/{deviceId}/command
```

ESP32 publish:

```text
smartlocker/{stationId}/{deviceId}/event
```

Backend subscribe wildcard:

```text
smartlocker/+/+/event
```

### Command tạo order

```json
{
  "commandType": "WAIT_FOR_DEPOSIT",
  "command": "WAIT_FOR_DEPOSIT",
  "type": "WAIT_FOR_DEPOSIT",
  "lockerId": "L01",
  "durationMs": 1000
}
```

### Event đóng cửa tối thiểu

```json
{
  "eventType": "DOOR_CLOSED",
  "lockerCode": "L01"
}
```

### Event đầy đủ

```json
{
  "eventId": "8ad91236-71de-4ed9-a5df-44fced831101",
  "deviceId": "c9d4a620-7b13-49ea-a5c1-62f9578f3202",
  "stationId": "6a0f5e90-1d72-4c31-9a44-7bc63b63d101",
  "lockerCode": "L01",
  "eventType": "DOOR_CLOSED",
  "timestamp": "2026-08-01T00:00:00Z"
}
```

Backend lấy `stationId` từ topic; `stationId` trong payload hiện chỉ mang tính mô tả.

## Tài liệu API

Base URL:

```text
http://localhost:8080
```

### Health

```http
GET /api/health
```

Response:

```text
Smart Locker Server is running
```

### Đăng nhập

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "smartlocker.test@example.com",
  "password": "<PASSWORD>"
}
```

`username` có thể là email hoặc số điện thoại.

Response thành công:

```json
{
  "token": "<JWT>",
  "message": "Đăng nhập thành công!"
}
```

### Danh sách station

```http
GET /api/stations
```

```json
[
  {
    "id": "6a0f5e90-1d72-4c31-9a44-7bc63b63d101",
    "name": "Smart Locker Station 01",
    "address": "Station test - Ho Chi Minh City",
    "status": "ACTIVE"
  }
]
```

### Station user đã đăng ký

```http
GET /api/stations/registered/{userId}
```

Chỉ trả registration có trạng thái `ACTIVE`.

### Danh sách locker

```http
GET /api/lockers
GET /api/lockers/{id}
GET /api/lockers/station/{stationId}
```

Response theo station:

```json
[
  {
    "id": "d1000001-5a8c-4e29-9d41-111111111101",
    "lockerCode": "L01",
    "status": "FREE",
    "stationId": "6a0f5e90-1d72-4c31-9a44-7bc63b63d101",
    "deviceId": "c9d4a620-7b13-49ea-a5c1-62f9578f3202"
  }
]
```

### Tạo locker

```http
POST /api/lockers
Content-Type: application/json
```

Endpoint này nhận trực tiếp entity và chưa phải API khuyến nghị cho production. Nên bổ sung DTO và validation trước khi public.

### Tạo order

```http
POST /api/orders
Content-Type: application/json
```

```json
{
  "userId": "e7a91c30-24d6-4f18-8a52-91b63e900101",
  "stationId": "6a0f5e90-1d72-4c31-9a44-7bc63b63d101",
  "lockerIds": [
    "d1000001-5a8c-4e29-9d41-111111111101"
  ],
  "recipientPhoneNumber": "0987654321"
}
```

Response `201 Created`:

```json
{
  "orderId": "31f41f95-59a8-4b96-a1dc-6b938eb1c775",
  "userId": "e7a91c30-24d6-4f18-8a52-91b63e900101",
  "stationId": "6a0f5e90-1d72-4c31-9a44-7bc63b63d101",
  "lockerIds": ["d1000001-5a8c-4e29-9d41-111111111101"],
  "status": "WAITING_FOR_DEPOSIT",
  "createdAt": "2026-08-01T00:00:00",
  "expiredAt": "2026-08-02T00:00:00"
}
```

Mã lỗi thường gặp:

| HTTP | Error | Ý nghĩa |
|---:|---|---|
| `400` | `INVALID_REQUEST` | Payload, user, station hoặc registration không hợp lệ |
| `409` | `LOCKERS_NOT_AVAILABLE` | Locker không tồn tại, đang có order hoặc device không khả dụng |
| `500` | `INTERNAL_SERVER_ERROR` | Lỗi database, MQTT hoặc lỗi chưa xử lý |

### Danh sách và chi tiết order

```http
GET /api/orders
GET /api/orders/{orderId}
```

### Đăng ký khuôn mặt

```http
POST /api/face/register
Content-Type: multipart/form-data
```

Các field:

```text
userId
midFace
leftFace
rightFace
upFace
```

Ví dụ curl:

```bash
curl -X POST http://localhost:8080/api/face/register \
  -F "userId=<USER_UUID>" \
  -F "midFace=@mid.jpg" \
  -F "leftFace=@left.jpg" \
  -F "rightFace=@right.jpg" \
  -F "upFace=@up.jpg"
```

### Mở locker bằng embedding khuôn mặt

```http
POST /api/face-auth/unlock
Content-Type: application/json
```

```json
{
  "deviceId": "b3e80147-a2f4-4d77-8b90-18e6d522d201",
  "embedding": [0.0123, -0.0421, 0.0087]
}
```

Trong thực tế `embedding` phải có đúng kích thước mà model và database sử dụng, thường là 512 chiều.

## Frontend

Các route chính:

| Route | Chức năng |
|---|---|
| `/login` | Đăng nhập |
| `/` | Dashboard |
| `/enroll` | Đăng ký khuôn mặt |
| `/lockers` | Xem station và locker |
| `/order` | Chọn locker dạng ô vuông và tạo order |

Bottom navigation có bốn mục:

1. Trang chủ
2. Face ID
3. Quản lý Tủ
4. Order

Trang Order:

- Tải station đang hoạt động.
- Tải locker theo station.
- Hiển thị locker bằng các khối vuông responsive.
- Cho phép chọn nhiều locker.
- Không cho chọn locker đang có order.
- Nhập số điện thoại người nhận.
- Hiển thị summary trước khi tạo.
- Gửi request bằng user UUID lấy từ JWT.
- Có trạng thái loading, disabled, success và error.

Build frontend:

```powershell
cd frontend
npm.cmd run build
```

Output nằm trong:

```text
frontend/dist
```

## AI microservice

Endpoint:

```http
POST http://localhost:9001/vectorize
Content-Type: multipart/form-data
```

Form field:

```text
image
```

Luồng xử lý:

1. Đọc bytes ảnh upload.
2. Decode bằng OpenCV.
3. InsightFace phát hiện khuôn mặt.
4. Nếu có nhiều khuôn mặt, chọn khuôn mặt có bounding box lớn nhất.
5. Lấy embedding.
6. Chuẩn hóa L2.
7. Trả vector JSON.

Response:

```json
{
  "status": "success",
  "embedding": [0.0123, -0.0421]
}
```

Mã lỗi:

- `400`: file ảnh không hợp lệ.
- `404`: không phát hiện khuôn mặt.
- `500`: lỗi model hoặc xử lý ảnh.

## Firmware ESP32

File:

```text
HardwareCode/smartlocker_esp32.ino
```

Cần cấu hình:

```cpp
const char* WIFI_SSID = "...";
const char* WIFI_PASSWORD = "...";
const char* MQTT_HOST = "...";
const int MQTT_PORT = 8883;
const char* MQTT_USER = "...";
const char* MQTT_PASS = "...";
const char* STATION_ID = "<STATION_UUID>";
const char* DEVICE_ID = "<ESP32_DEVICE_UUID>";
```

Khai báo locker và GPIO:

```cpp
const LockerConfig LOCKERS[] = {
    { "L01", 26, 34 },
    { "L02", 27, 35 },
    { "L03", 14, 32 },
};
```

`lockerCode` trong firmware phải khớp chính xác `locker_code` trong database.

## Dữ liệu test cục bộ

Các UUID dưới đây được tạo trong database phát triển cục bộ. Không giả định chúng tồn tại ở môi trường khác.

### Station

```text
6a0f5e90-1d72-4c31-9a44-7bc63b63d101
Smart Locker Station 01
```

### Devices

```text
Raspberry Pi: b3e80147-a2f4-4d77-8b90-18e6d522d201
ESP32:        c9d4a620-7b13-49ea-a5c1-62f9578f3202
```

### Lockers

```text
L01: d1000001-5a8c-4e29-9d41-111111111101
L02: d1000002-5a8c-4e29-9d41-111111111102
L03: d1000003-5a8c-4e29-9d41-111111111103
```

### User test

```text
User ID: e7a91c30-24d6-4f18-8a52-91b63e900101
Email: smartlocker.test@example.com
Phone: 0900000001
```

User này có `UserStationRegistration = ACTIVE` với station test. Mật khẩu test không nên ghi vào README hoặc dùng ở production; hãy lưu trong secret manager hoặc chia sẻ qua kênh riêng.

## Kiểm thử

### Backend

```powershell
mvn test
```

Hoặc:

```powershell
.\mvnw.cmd test
```

### Frontend

```powershell
cd frontend
npm.cmd run build
```

### Kiểm tra API nhanh

```powershell
Invoke-WebRequest http://localhost:8080/api/health
Invoke-WebRequest http://localhost:8080/api/stations
```

### Test MQTT thủ công

1. Tạo order mới.
2. Subscribe command topic của ESP32.
3. Kiểm tra command `WAIT_FOR_DEPOSIT`.
4. Publish `DOOR_CLOSED` vào event topic trong vòng timeout.
5. Kiểm tra `OrderLocker` chuyển sang `WAIT_FOR_COLLECTION`.

## Xử lý sự cố

### Bấm “Tạo order” nhưng chờ rất lâu

Dấu hiệu trong PostgreSQL:

```text
idle in transaction
wait_event_type = Lock
SELECT ... FOR NO KEY UPDATE
```

Nguyên nhân thường gặp:

- Request đầu tiên giữ pessimistic lock trên locker.
- Request bị kẹt khi MQTT chưa kết nối hoặc broker không phản hồi.
- Những lần bấm tiếp theo chờ row lock của request đầu.

Xử lý khi phát triển:

1. Không bấm nút nhiều lần.
2. Tắt MQTT bằng `mqtt.publish-enabled=false` nếu không test phần cứng.
3. Restart backend để rollback transaction bị treo.
4. Kiểm tra `pg_stat_activity` và lock trước khi gửi request lại.

Kiến trúc production nên publish MQTT **sau khi database commit**, đặt timeout và dùng retry/outbox thay vì giữ transaction database trong lúc chờ hệ thống bên ngoài.

### Frontend không tải được station

- Kiểm tra backend cổng 8080.
- Kiểm tra `GET /api/stations` trả JSON phẳng qua `StationResponseDto`.
- Kiểm tra Vite proxy trong `frontend/vite.config.js`.
- Kiểm tra JWT còn hạn.

### User không thể tạo order

Nếu API báo:

```text
User chưa đăng ký trạng thái ACTIVE tại trạm tủ này.
```

hãy tạo hoặc cập nhật `user_station_registrations` cho đúng `user_id`, `station_id` và `status = ACTIVE`.

### Locker luôn báo đang dùng

Kiểm tra `order_lockers` có bản ghi ở:

```text
WAIT_FOR_DEPOSIT
WAIT_FOR_COLLECTION
```

`locker.status` không phải nguồn dữ liệu xác định locker đang thuộc order.

### Không nhận command MQTT

- Kiểm tra broker host, port, username và password.
- Kiểm tra TLS.
- Kiểm tra `mqtt.publish-enabled=true`.
- Kiểm tra đúng topic `stationId/deviceId`.
- Kiểm tra UUID trong firmware khớp database.
- Kiểm tra ESP32 subscribe sau khi kết nối lại.

### AI service không khởi động

- Kiểm tra `insightface`, OpenCV, FastAPI và Uvicorn.
- Kiểm tra model `buffalo_l` đã tải đầy đủ.
- Nếu không có GPU, cấu hình execution provider phù hợp với CPU.
- Kiểm tra cổng 9001 chưa bị chiếm.

### Maven Wrapper không chạy trên Windows

Nếu `mvnw.cmd` lỗi nhưng Maven đã được cài:

```powershell
mvn test
mvn spring-boot:run
```

Kiểm tra file `.mvn/wrapper/maven-wrapper.properties` và quyền tải dependency từ Maven Central.

## Lưu ý bảo mật

- Không commit mật khẩu PostgreSQL, MQTT, Wi-Fi hoặc JWT secret.
- Các credential đã từng xuất hiện trong source/history nên được rotate trước khi deploy.
- Không ghi mật khẩu test trong README production.
- Không dùng `@CrossOrigin(origins = "*")` trong production; giới hạn origin frontend.
- Thêm Spring Security filter để xác thực JWT ở backend. Việc frontend gửi JWT không tự bảo vệ API nếu backend chưa kiểm tra token.
- Validate kích thước và MIME type của ảnh upload.
- Giới hạn số lần xác thực khuôn mặt và OTP.
- Không log embedding, JWT, mật khẩu hoặc ảnh khuôn mặt.
- Mã hóa dữ liệu sinh trắc học khi lưu và truyền.
- Dùng migration có phiên bản thay cho `ddl-auto=update` ở production.
- Thêm timeout/circuit breaker cho AI và MQTT.
- Không giữ database transaction mở trong khi gọi hệ thống bên ngoài.

## Hướng phát triển đề xuất

- Dùng Flyway/Liquibase cho schema migration.
- Thêm Spring Security JWT authentication/authorization.
- Dùng transactional outbox cho MQTT command.
- Thêm command status và retry có giới hạn.
- Tách DTO khỏi entity cho toàn bộ endpoint.
- Thêm endpoint quản lý user/station/device thay vì chèn SQL trực tiếp.
- Hoàn thiện luồng người nhận lấy đồ và chuyển `OrderLocker` sang `INACTIVE`.
- Thêm lịch sử order trong frontend.
- Thêm WebSocket/SSE để giao diện nhận trạng thái locker thời gian thực.
- Thêm test service/controller/repository và concurrency test cho tạo order.
- Thêm Docker Compose cho PostgreSQL, backend, frontend và AI service.

## License

Dự án hiện chưa khai báo license. Hãy thêm file `LICENSE` trước khi phân phối công khai.
