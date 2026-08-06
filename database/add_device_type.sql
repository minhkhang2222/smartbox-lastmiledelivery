-- device_type không dùng enum. Hai giá trị hợp lệ: ESP32 và RASPBERRY.
ALTER TABLE devices
    ADD COLUMN IF NOT EXISTS device_type VARCHAR(20);

-- Gán loại cho dữ liệu hiện có dựa trên device_code.
UPDATE devices
SET device_type = 'ESP32'
WHERE device_type IS NULL
  AND device_code ILIKE 'ESP32%';

UPDATE devices
SET device_type = 'RASPBERRY'
WHERE device_type IS NULL
  AND (device_code ILIKE 'RASPBERRY%' OR device_code ILIKE 'RASBERRY%');

ALTER TABLE devices
    ALTER COLUMN device_type SET NOT NULL;

-- Vẫn lưu dạng VARCHAR, constraint chỉ ngăn dữ liệu sai chính tả.
ALTER TABLE devices
    DROP CONSTRAINT IF EXISTS devices_device_type_check;

ALTER TABLE devices
    ADD CONSTRAINT devices_device_type_check
    CHECK (device_type IN ('ESP32', 'RASPBERRY'));

COMMENT ON COLUMN devices.device_type IS
    'Device type stored as string; supported values: ESP32, RASPBERRY';
