package smartlocker.smartlocker.model;

public enum OrderLockerStatus {
    WAIT_FOR_DEPOSIT,    // Đang chờ người gửi bỏ đồ vào tủ
    WAIT_FOR_COLLECTION, // Đồ đã ở trong tủ, đang chờ người nhận lấy
    INACTIVE             // Đã hoàn thành hoặc không còn giữ tủ
}
