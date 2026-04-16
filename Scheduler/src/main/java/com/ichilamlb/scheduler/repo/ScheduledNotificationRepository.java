package com.ichilamlb.scheduler.repo;

import com.ichilamlb.scheduler.models.db.ScheduledNotification;
import com.ichilamlb.scheduler.models.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, Long> {

    /**
     * Tìm các tin nhắn đến hạn gửi theo cơ chế:
     * 1. Trạng thái là PENDING
     * 2. Thời gian đặt lịch (scheduledTime) đã nhỏ hơn hoặc bằng hiện tại
     * 3. Sắp xếp theo thứ tự thời gian cũ nhất lên trước (FIFO)
     * 4. Limit kết quả trả về bằng Pageable (Throttling)
     */
    @Query("SELECT s FROM ScheduledNotification s " +
            "WHERE s.status = :status " +
            "AND s.scheduledTime <= :now " +
            "ORDER BY s.scheduledTime ASC")
    List<ScheduledNotification> findJobsToProcess(
            @Param("status") Status status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // Tìm kiếm nhanh theo trạng thái để thống kê (Dashboard)
    List<ScheduledNotification> findByStatus(Status status);
}