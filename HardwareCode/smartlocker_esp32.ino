/*
 * SmartLocker ESP32 Firmware
 *
 * Luồng WAIT_FOR_DEPOSIT:
 *  1. Server gửi MQTT command: WAIT_FOR_DEPOSIT + lockerCode
 *  2. ESP unlock tủ ngay lập tức
 *  3. Chờ 1 giây (để cửa mở ra trước khi bắt đầu monitor)
 *  4. Bắt đầu theo dõi sensor cửa tủ đó
 *  5. Nếu cửa đóng liên tục ≥ 3 giây → gửi MQTT event: DOOR_CLOSED
 *  6. Nếu cửa không đóng → không gửi gì (server tự timeout 30s)
 *
 * Topic Subscribe: smartlocker/{stationId}/{deviceId}/command
 * Topic Publish:   smartlocker/{stationId}/{deviceId}/event
 */

#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// ==================== CẤU HÌNH ====================
const char* WIFI_SSID     = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

const char* MQTT_HOST     = "your-mqtt-broker.example.com";
const int   MQTT_PORT     = 8883; // TLS
const char* MQTT_USER     = "YOUR_MQTT_USERNAME";
const char* MQTT_PASS     = "YOUR_MQTT_PASSWORD";

// Station và Device ID (phải khớp với DB trên server)
const char* STATION_ID    = "your-station-uuid";
const char* DEVICE_ID     = "your-device-uuid";

// ==================== ĐỊNH NGHĨA TỦ ====================
// Cấu hình từng tủ: lockerCode, solenoid pin, reed switch pin
struct LockerConfig {
    const char* code;       // Tên tủ khớp với lockerCode trong DB, vd: "A01"
    int solenoidPin;        // GPIO điều khiển solenoid (relay)
    int reedSwitchPin;      // GPIO đọc trạng thái cửa (reed switch/hall sensor)
};

// Khai báo các tủ trong trạm này
const LockerConfig LOCKERS[] = {
    { "A01", 26, 34 },
    { "A02", 27, 35 },
    { "A03", 14, 32 },
    // Thêm tủ tại đây...
};
const int LOCKER_COUNT = sizeof(LOCKERS) / sizeof(LOCKERS[0]);

// ==================== MONITORING STATE ====================
struct MonitorState {
    bool     active;          // Đang theo dõi tủ này không
    char     lockerCode[16];  // Code của tủ đang theo dõi
    unsigned long startMs;    // Thời điểm bắt đầu monitor (sau 1s delay)
    bool     doorClosedStart; // Cửa có đang đóng không
    unsigned long doorClosedSince; // Thời điểm cửa bắt đầu đóng
    bool     eventSent;       // Đã gửi DOOR_CLOSED event chưa
};

MonitorState monitors[LOCKER_COUNT];

// ==================== MQTT ====================
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

char topicCommand[128];
char topicEvent[128];

// ==================== TIMING ====================
const unsigned long UNLOCK_TO_MONITOR_DELAY_MS = 1000; // 1 giây
const unsigned long DOOR_CLOSED_CONFIRM_MS      = 3000; // 3 giây liên tục
const int           DOOR_CLOSED_STATE           = HIGH; // HIGH = đóng (tuỳ reed switch)
const int           DOOR_OPEN_STATE             = LOW;

// ==================== FUNCTION DECLARATIONS ====================
void connectWifi();
void connectMqtt();
void mqttCallback(char* topic, byte* payload, unsigned int length);
void handleWaitForDeposit(const char* lockerCode);
void unlockLocker(int lockerIndex);
void lockLocker(int lockerIndex);
int  findLockerIndex(const char* code);
void publishDoorClosedEvent(const char* lockerCode);
void checkMonitors();

// ==================== SETUP ====================
void setup() {
    Serial.begin(115200);
    Serial.println("[SmartLocker] Starting...");

    // Khởi tạo pin
    for (int i = 0; i < LOCKER_COUNT; i++) {
        pinMode(LOCKERS[i].solenoidPin, OUTPUT);
        digitalWrite(LOCKERS[i].solenoidPin, LOW); // mặc định khóa
        pinMode(LOCKERS[i].reedSwitchPin, INPUT_PULLUP);

        monitors[i].active = false;
        monitors[i].eventSent = false;
    }

    connectWifi();

    // Tạo topic strings
    snprintf(topicCommand, sizeof(topicCommand),
             "smartlocker/%s/%s/command", STATION_ID, DEVICE_ID);
    snprintf(topicEvent, sizeof(topicEvent),
             "smartlocker/%s/%s/event", STATION_ID, DEVICE_ID);

    mqttClient.setServer(MQTT_HOST, MQTT_PORT);
    mqttClient.setCallback(mqttCallback);
    mqttClient.setBufferSize(1024);

    connectMqtt();
}

// ==================== LOOP ====================
void loop() {
    if (!mqttClient.connected()) {
        connectMqtt();
    }
    mqttClient.loop();
    checkMonitors(); // Kiểm tra trạng thái cửa các tủ đang monitor
}

// ==================== WIFI ====================
void connectWifi() {
    Serial.print("[WiFi] Connecting to ");
    Serial.println(WIFI_SSID);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.print("\n[WiFi] Connected. IP: ");
    Serial.println(WiFi.localIP());
}

// ==================== MQTT ====================
void connectMqtt() {
    while (!mqttClient.connected()) {
        Serial.print("[MQTT] Connecting...");
        String clientId = "esp32-smartlocker-" + String(DEVICE_ID);
        if (mqttClient.connect(clientId.c_str(), MQTT_USER, MQTT_PASS)) {
            Serial.println(" connected.");
            mqttClient.subscribe(topicCommand);
            Serial.print("[MQTT] Subscribed to: ");
            Serial.println(topicCommand);
        } else {
            Serial.print(" failed, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" retrying in 5s...");
            delay(5000);
        }
    }
}

// ==================== MQTT CALLBACK ====================
void mqttCallback(char* topic, byte* payload, unsigned int length) {
    // Parse JSON payload
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, payload, length);
    if (error) {
        Serial.print("[MQTT] JSON parse error: ");
        Serial.println(error.c_str());
        return;
    }

    const char* commandType = doc["commandType"];
    const char* lockerCode  = doc["lockerId"]; // field name từ LockerCommandPayload.java

    Serial.print("[MQTT] Command: ");
    Serial.print(commandType);
    Serial.print(" | Locker: ");
    Serial.println(lockerCode);

    if (commandType == nullptr || lockerCode == nullptr) {
        Serial.println("[MQTT] Missing commandType or lockerId, ignoring.");
        return;
    }

    if (strcmp(commandType, "WAIT_FOR_DEPOSIT") == 0) {
        handleWaitForDeposit(lockerCode);
    } else if (strcmp(commandType, "UNLOCK") == 0) {
        int idx = findLockerIndex(lockerCode);
        if (idx >= 0) unlockLocker(idx);
    } else if (strcmp(commandType, "LOCK") == 0) {
        int idx = findLockerIndex(lockerCode);
        if (idx >= 0) lockLocker(idx);
    } else {
        Serial.print("[MQTT] Unknown command: ");
        Serial.println(commandType);
    }
}

// ==================== WAIT FOR DEPOSIT HANDLER ====================
void handleWaitForDeposit(const char* lockerCode) {
    int idx = findLockerIndex(lockerCode);
    if (idx < 0) {
        Serial.print("[WaitForDeposit] Locker not found: ");
        Serial.println(lockerCode);
        return;
    }

    Serial.print("[WaitForDeposit] Unlocking locker: ");
    Serial.println(lockerCode);

    // 1. Unlock ngay lập tức (dù không biết trạng thái hiện tại)
    unlockLocker(idx);

    // 2. Chờ 1 giây để cửa kịp mở ra trước khi bắt đầu theo dõi
    //    (blocking delay ngắn, OK vì chỉ 1s)
    delay(UNLOCK_TO_MONITOR_DELAY_MS);

    // 3. Kích hoạt monitor cho tủ này
    monitors[idx].active         = true;
    monitors[idx].doorClosedStart = false;
    monitors[idx].doorClosedSince = 0;
    monitors[idx].eventSent      = false;
    monitors[idx].startMs        = millis();
    strncpy(monitors[idx].lockerCode, lockerCode, sizeof(monitors[idx].lockerCode) - 1);

    Serial.print("[WaitForDeposit] Monitoring started for locker: ");
    Serial.println(lockerCode);
}

// ==================== MONITOR LOGIC ====================
void checkMonitors() {
    unsigned long now = millis();

    for (int i = 0; i < LOCKER_COUNT; i++) {
        if (!monitors[i].active || monitors[i].eventSent) continue;

        // Đọc trạng thái reed switch (LOW = mở, HIGH = đóng, tuỳ phần cứng)
        int pinState = digitalRead(LOCKERS[i].reedSwitchPin);
        bool isDoorClosed = (pinState == DOOR_CLOSED_STATE);

        if (isDoorClosed) {
            if (!monitors[i].doorClosedStart) {
                // Cửa vừa đóng → bắt đầu đếm
                monitors[i].doorClosedStart = true;
                monitors[i].doorClosedSince = now;
                Serial.print("[Monitor] Locker ");
                Serial.print(monitors[i].lockerCode);
                Serial.println(" door closed, start 3s timer...");
            } else {
                // Cửa đang đóng → kiểm tra đủ 3 giây chưa
                unsigned long closedDuration = now - monitors[i].doorClosedSince;
                if (closedDuration >= DOOR_CLOSED_CONFIRM_MS) {
                    // ✅ Xác nhận: cửa đóng liên tục ≥ 3 giây
                    Serial.print("[Monitor] Locker ");
                    Serial.print(monitors[i].lockerCode);
                    Serial.println(" door confirmed closed (3s). Sending event...");

                    publishDoorClosedEvent(monitors[i].lockerCode);
                    monitors[i].eventSent = true;
                    monitors[i].active    = false;
                }
            }
        } else {
            // Cửa mở lại → reset timer
            if (monitors[i].doorClosedStart) {
                Serial.print("[Monitor] Locker ");
                Serial.print(monitors[i].lockerCode);
                Serial.println(" door reopened, reset timer.");
                monitors[i].doorClosedStart = false;
                monitors[i].doorClosedSince = 0;
            }
        }
    }
}

// ==================== PUBLISH DOOR_CLOSED EVENT ====================
void publishDoorClosedEvent(const char* lockerCode) {
    StaticJsonDocument<256> doc;
    doc["eventType"]  = "DOOR_CLOSED";
    doc["lockerCode"] = lockerCode;
    doc["deviceId"]   = DEVICE_ID;
    doc["stationId"]  = STATION_ID;

    char buffer[256];
    size_t len = serializeJson(doc, buffer);

    bool success = mqttClient.publish(topicEvent, buffer, len);
    Serial.print("[Event] Published DOOR_CLOSED for ");
    Serial.print(lockerCode);
    Serial.print(" → ");
    Serial.println(success ? "OK" : "FAILED");
}

// ==================== HELPERS ====================
int findLockerIndex(const char* code) {
    for (int i = 0; i < LOCKER_COUNT; i++) {
        if (strcmp(LOCKERS[i].code, code) == 0) return i;
    }
    return -1;
}

void unlockLocker(int idx) {
    digitalWrite(LOCKERS[idx].solenoidPin, HIGH); // HIGH = kích solenoid = mở
    Serial.print("[Locker] Unlocked: ");
    Serial.println(LOCKERS[idx].code);
}

void lockLocker(int idx) {
    digitalWrite(LOCKERS[idx].solenoidPin, LOW); // LOW = tắt solenoid = khóa
    Serial.print("[Locker] Locked: ");
    Serial.println(LOCKERS[idx].code);
}
