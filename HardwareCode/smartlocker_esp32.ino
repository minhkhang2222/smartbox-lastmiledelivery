/*
 * SmartLocker ESP32 Firmware
 *
 * Luồng stateless theo order:
 *  1. Server gửi MQTT command UNLOCK + lockerId sau khi tạo order.
 *  2. ESP mở đúng tủ nhưng không lưu order hay phiên gửi hàng.
 *  3. ESP luôn theo dõi cảm biến của mọi tủ.
 *  4. Khi cửa đóng ổn định >= 3 giây, ESP khóa tủ và publish LOCK.
 *  5. Server quyết định LOCK có khớp order đang chờ hay không.
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
struct DoorState {
    bool     doorClosedStart; // Cửa có đang đóng không
    unsigned long doorClosedSince; // Thời điểm cửa bắt đầu đóng
    bool     lockPublished;   // Đã gửi LOCK cho lần đóng cửa hiện tại chưa
};

DoorState doorStates[LOCKER_COUNT];

// ==================== MQTT ====================
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

char topicCommand[128];
char topicEvent[128];

// ==================== TIMING ====================
const unsigned long DOOR_CLOSED_CONFIRM_MS      = 3000; // 3 giây liên tục
const int           DOOR_CLOSED_STATE           = HIGH; // HIGH = đóng (tuỳ reed switch)
const int           DOOR_OPEN_STATE             = LOW;

// ==================== FUNCTION DECLARATIONS ====================
void connectWifi();
void connectMqtt();
void mqttCallback(char* topic, byte* payload, unsigned int length);
void unlockLocker(int lockerIndex);
void lockLocker(int lockerIndex);
int  findLockerIndex(const char* code);
void publishLockCommand(const char* lockerCode);
void checkDoors();

// ==================== SETUP ====================
void setup() {
    Serial.begin(115200);
    Serial.println("[SmartLocker] Starting...");

    // Khởi tạo pin
    for (int i = 0; i < LOCKER_COUNT; i++) {
        pinMode(LOCKERS[i].solenoidPin, OUTPUT);
        digitalWrite(LOCKERS[i].solenoidPin, LOW); // mặc định khóa
        pinMode(LOCKERS[i].reedSwitchPin, INPUT_PULLUP);

        bool initiallyClosed = digitalRead(LOCKERS[i].reedSwitchPin) == DOOR_CLOSED_STATE;
        doorStates[i].doorClosedStart = initiallyClosed;
        doorStates[i].doorClosedSince = millis();
        doorStates[i].lockPublished = initiallyClosed;
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
    checkDoors();
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

    if (strcmp(commandType, "UNLOCK") == 0) {
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

// Trạng thái ở đây chỉ phục vụ debounce cảm biến, không gắn với order.
void checkDoors() {
    unsigned long now = millis();

    for (int i = 0; i < LOCKER_COUNT; i++) {
        int pinState = digitalRead(LOCKERS[i].reedSwitchPin);
        bool isDoorClosed = (pinState == DOOR_CLOSED_STATE);

        if (isDoorClosed) {
            if (!doorStates[i].doorClosedStart) {
                doorStates[i].doorClosedStart = true;
                doorStates[i].doorClosedSince = now;
                Serial.print("[Monitor] Locker ");
                Serial.print(LOCKERS[i].code);
                Serial.println(" door closed, start 3s timer...");
            } else if (!doorStates[i].lockPublished
                    && now - doorStates[i].doorClosedSince >= DOOR_CLOSED_CONFIRM_MS) {
                lockLocker(i);
                publishLockCommand(LOCKERS[i].code);
                doorStates[i].lockPublished = true;
            }
        } else {
            if (doorStates[i].doorClosedStart) {
                Serial.print("[Monitor] Locker ");
                Serial.print(LOCKERS[i].code);
                Serial.println(" door reopened, reset timer.");
            }
            doorStates[i].doorClosedStart = false;
            doorStates[i].doorClosedSince = 0;
            doorStates[i].lockPublished = false;
        }
    }
}

void publishLockCommand(const char* lockerCode) {
    StaticJsonDocument<256> doc;
    doc["commandType"] = "LOCK";
    doc["command"] = "LOCK";
    doc["type"] = "LOCK";
    doc["lockerId"] = lockerCode;
    doc["durationMs"] = 1000;

    char buffer[256];
    size_t len = serializeJson(doc, buffer);

    bool success = mqttClient.publish(topicEvent, buffer, len);
    Serial.print("[Event] Published LOCK for ");
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
