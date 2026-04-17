package com.ichilamlb.scheduler.repo;

import com.ichilamlb.scheduler.models.db.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(String status);
}