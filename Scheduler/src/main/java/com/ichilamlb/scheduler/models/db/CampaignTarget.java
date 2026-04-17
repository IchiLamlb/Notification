package com.ichilamlb.scheduler.models.db;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "campaign_targets")
@Data
public class CampaignTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long campaignId;

    // Thông tin Recipient
    private String userId;
    private String userEmail;
    private String fcmToken;

    // Dạng JSON String chứa các biến động: {"name": "Mr Le Lam", "otp":"2376667"}
    @Column(columnDefinition = "TEXT")
    private String placeholdersJson;

    // PENDING, QUEUED_TO_KAFKA
    private String status;
}