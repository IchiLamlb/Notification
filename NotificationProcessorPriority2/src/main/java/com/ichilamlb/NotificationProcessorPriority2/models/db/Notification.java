package com.ichilamlb.NotificationProcessorPriority2.models.db;

import com.ichilamlb.NotificationProcessorPriority2.models.enums.Channel;
import com.ichilamlb.NotificationProcessorPriority2.models.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    private Status status = Status.pending;

    @Column(columnDefinition = "TEXT")
    private String message;

    // Chỉ định rõ tên cột trong DB
    @Column(name = "request_content", columnDefinition = "JSON")
    private String requestContent;

    // Khớp với tên cột message_hash trong script SQL của bạn
    @Column(name = "message_hash", length = 128)
    private String notificationHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructor để code Processor gọi không bị lỗi
    public Notification(User user, Channel channel, String message, String requestContent, String notificationHash){
        this.user = user;
        this.channel = channel;
        this.message = message;
        this.requestContent = requestContent;
        this.notificationHash = notificationHash;
    }
}
