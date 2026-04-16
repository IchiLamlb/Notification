package com.ichilamlb.scheduler.models.db;

import com.ichilamlb.scheduler.models.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "scheduled_notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScheduledNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lưu toàn bộ JSON của NotificationRequest từ API đầu vào
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    // Xác định sẽ đẩy vào topic nào: priority-1, priority-2 hay priority-3
    @Column(name = "target_topic", length = 50)
    private String targetTopic;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    // Thời gian cụ thể admin muốn gửi (VD: 2024-12-25 08:00:00)
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    // Cơ chế Window: Chỉ cho phép "nhả" tin trong khung giờ này (VD: 08:00 - 20:00)
    @Column(name = "start_window")
    private LocalTime startWindow;

    @Column(name = "end_window")
    private LocalTime endWindow;

    private Integer retryCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.pending;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}