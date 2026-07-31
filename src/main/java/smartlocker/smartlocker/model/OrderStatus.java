package smartlocker.smartlocker.model;

public enum OrderStatus {
    WAITING_FOR_DEPOSIT, // Order vừa tạo, đang chờ người dùng bỏ đồ vào tủ
    PENDING,             // Tất cả tủ đã đóng lại (đồ đã vào), chờ pickup
    WAITING_FOR_PICKUP,  // Đang chờ người nhận đến lấy
    COMPLETED,           // Hoàn tất
    CANCELLED,           // Bị hủy
    FAILED               // Tất cả tủ timeout, không có đồ nào được bỏ vào
}
