package com.ichilamlb.scheduler.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ichilamlb.scheduler.models.db.Campaign;
import com.ichilamlb.scheduler.models.db.CampaignTarget;
import com.ichilamlb.scheduler.models.*;
import com.ichilamlb.scheduler.repo.CampaignRepository;
import com.ichilamlb.scheduler.repo.CampaignTargetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CampaignSchedulerService {

    private final KafkaProducerService kafkaProducerService;
    private final CampaignRepository campaignRepository;
    private final CampaignTargetRepository targetRepository;
    private final ObjectMapper objectMapper;

    // Cấu hình khung giờ chạy
    private static final LocalTime START_TIME = LocalTime.of(8, 0);
    private static final LocalTime END_TIME = LocalTime.of(22, 0);

    public CampaignSchedulerService(KafkaProducerService kafkaProducerService,
                                    CampaignRepository campaignRepository,
                                    CampaignTargetRepository targetRepository,
                                    ObjectMapper objectMapper) {
        this.kafkaProducerService = kafkaProducerService;
        this.campaignRepository = campaignRepository;
        this.targetRepository = targetRepository;
        this.objectMapper = objectMapper;
    }

    // Chạy định kỳ mỗi phút
    @Scheduled(cron = "0/30 * * * * *")
    @Transactional
    public void processActiveCampaigns() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (now.isBefore(START_TIME) || now.isAfter(END_TIME)) {
            log.info("Ngoài giờ hành chính (hiện tại: {}). Scheduler đang tạm ngủ.", now);
            return;
        }

        List<Campaign> activeCampaigns = campaignRepository.findByStatus("ACTIVE");

        for (Campaign campaign : activeCampaigns) {
            // Tính số lượng cần gửi trong 1 phút
            int ratePerHour = campaign.getRatePerHour();
            int batchSize = (int) Math.ceil((double) ratePerHour / 120.0);

            Pageable limit = PageRequest.of(0, batchSize);
            List<CampaignTarget> pendingTargets = targetRepository
                    .findByCampaignIdAndStatus(campaign.getId(), "PENDING", limit);

            if (pendingTargets.isEmpty()) {
                log.info("Campaign ID {} đã gửi xong.", campaign.getId());
                // campaign.setStatus("COMPLETED"); campaignRepository.save(campaign);
                continue;
            }

            for (CampaignTarget target : pendingTargets) {
                NotificationRequest request = buildRequest(campaign, target);

                // Đẩy vào Kafka
                kafkaProducerService.sendToNotificationTopic(request);

                target.setStatus("QUEUED_TO_KAFKA");
            }

            targetRepository.saveAll(pendingTargets);
            log.info("Đã đẩy {} targets của campaign '{}' vào Kafka.", pendingTargets.size(), campaign.getName());
        }
    }

    private NotificationRequest buildRequest(Campaign campaign, CampaignTarget target) {
        NotificationRequest req = new NotificationRequest();
        req.setNotificationId(target.getId().intValue());
        req.setNotificationPriority(campaign.getPriority());
        req.setChannels(List.of("push"));

        Recipient recipient = new Recipient();
        recipient.setUserId(target.getUserId());
        recipient.setUserEmail(target.getUserEmail());
        recipient.setFcmToken(target.getFcmToken());
        req.setRecipient(recipient);

        Content content = new Content();
        content.setUsingTemplates(true);
        content.setTemplateName(campaign.getTemplateName());

        // Parse Placeholders Json
        try {
            if (target.getPlaceholdersJson() != null && !target.getPlaceholdersJson().isEmpty()) {
                Map<String, String> map = objectMapper.readValue(target.getPlaceholdersJson(), new TypeReference<>() {});
                content.setPlaceholders(map);
            }
        } catch (Exception e) {
            log.error("Lỗi parse JSON placeholders cho target ID: {}", target.getId(), e);
        }

        PushNotification push = new PushNotification();
        push.setTitle(campaign.getPushTitle());
        push.setBody(campaign.getPushBody());

        Action action = new Action();
        action.setUrl(campaign.getPushActionUrl());
        push.setAction(action);

        content.setPushNotification(push);
        req.setContent(content);

        return req;
    }
}