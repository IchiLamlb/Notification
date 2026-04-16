package com.ichilamlb.scheduler.service;

import com.ichilamlb.scheduler.constants.Constants;
import com.ichilamlb.scheduler.models.db.ScheduledNotification; // Đã sửa: đúng path model
import com.ichilamlb.scheduler.models.enums.Status;           // Đã sửa: dùng Enum thay vì String
import com.ichilamlb.scheduler.repo.ScheduledNotificationRepository; // Đã sửa: đúng path repo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobProcessingService {

    @Autowired
    private ScheduledNotificationRepository scheduledRepo; // Đã sửa tên biến cho rõ ràng

    @Autowired
    private TimeWindowService timeWindowService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value("${app.scheduler.batch-size:500}")
    private int batchSize;

    /**
     * Thực hiện quét DB và đẩy tin đi theo cơ chế Throttling
     */
    @Transactional
    public void processScheduledJobs() {
        // 1. Gọi đúng method đã định nghĩa trong Repository: findJobsToProcess
        // Truyền vào: Status enum, thời gian hiện tại, và Pageable để Throttling
        List<ScheduledNotification> pendingJobs = scheduledRepo.findJobsToProcess(
                Status.pending,
                LocalDateTime.now(),
                PageRequest.of(0, batchSize)
        );

        if (pendingJobs.isEmpty()) {
            return;
        }

        for (ScheduledNotification job : pendingJobs) {
            // 2. Kiểm tra khung giờ cho phép của Admin (startWindow, endWindow từ Entity)
            if (timeWindowService.isWithinWindow(job.getStartWindow(), job.getEndWindow())) {

                // 3. Đẩy tin vào Kafka theo Topic đã lưu (priority-1, 2, 3)
                kafkaProducerService.sendMessage(job.getTargetTopic(), job.getPayload());

                // 4. Cập nhật trạng thái thành công (Dùng Enum Status)
                job.setStatus(Status.sent);
                job.setUpdatedAt(LocalDateTime.now());
            } else {
                // Nếu chưa đến khung giờ (Window), bỏ qua để đợt quét sau xử lý tiếp
                continue;
            }
        }

        // 5. Lưu toàn bộ thay đổi trạng thái vào DB (Batch Update)
        scheduledRepo.saveAll(pendingJobs);
    }
}