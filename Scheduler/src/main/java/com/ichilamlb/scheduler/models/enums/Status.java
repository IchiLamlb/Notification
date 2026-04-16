package com.ichilamlb.scheduler.models.enums;

public enum Status {
    pending,    // Đang chờ đến giờ gửi
    processing, // Đang được Scheduler bốc lên xử lý
    sent,       // Đ đã đẩy vào Kafka thành công
    failed,     // Lỗi khi xử lý hoặc quá hạn mà không gửi được
    paused      // Admin tạm dừng chiến dịch
}