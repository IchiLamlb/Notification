package com.ichilamlb.scheduler.service;

import org.springframework.stereotype.Service;
import java.time.LocalTime;

@Service
public class TimeWindowService {

    /**
     * Kiểm tra xem giờ hiện tại có nằm trong khung giờ cho phép không.
     * @param start Giờ bắt đầu (ví dụ: 08:00)
     * @param end Giờ kết thúc (ví dụ: 21:00)
     * @return true nếu hợp lệ
     */
    public boolean isWithinWindow(LocalTime start, LocalTime end) {
        LocalTime now = LocalTime.now();

        // Trường hợp khung giờ trong cùng một ngày (vd: 08:00 - 20:00)
        if (start.isBefore(end)) {
            return !now.isBefore(start) && !now.isAfter(end);
        }

        // Trường hợp khung giờ qua đêm (vd: 22:00 - 05:00 sáng hôm sau)
        return now.isAfter(start) || now.isBefore(end);
    }
}