package com.ichilamlb.scheduler.models.db;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Data
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int ratePerHour; // Tốc độ gửi: VD 10000
    private String status;   // ACTIVE, PAUSED, COMPLETED
    private int priority;    // VD: 3
    private String templateName; // "Birthday Wish"

    // Lưu các thông tin cố định của Push Notification
    private String pushTitle;
    private String pushBody;
    private String pushActionUrl;
}