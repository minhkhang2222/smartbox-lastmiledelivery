package smartlocker.smartlocker.model;

public enum OrderLockerStatus {
    ASSIGNED,          // Tủ vừa được gán vào order (chưa xử lý gì)
    WAIT_FOR_DEPOSIT,  // Đã gửi lệnh unlock, đang chờ người dùng bỏ đồ vào
    PENDING,           // Cửa đã đóng lại đủ 3 giây → đồ đã được bỏ vào
    ACTIVE,            // Đang sử dụng (đồ đã bên trong)
    FAILED,            // Timeout 30 giây — người dùng không bỏ đồ vào
    INACTIVE           // Hoàn thành / không còn hoạt động
}
