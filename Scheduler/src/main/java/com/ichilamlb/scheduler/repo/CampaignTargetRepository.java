package com.ichilamlb.scheduler.repo;

import com.ichilamlb.scheduler.models.db.CampaignTarget;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignTargetRepository extends JpaRepository<CampaignTarget, Long> {
    // Lấy danh sách cần gửi theo Limit (Pageable)
    List<CampaignTarget> findByCampaignIdAndStatus(Long campaignId, String status, Pageable pageable);
}