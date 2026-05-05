package org.example.multileanproject.repository;

import org.example.multileanproject.entity.SaleCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleCampaignRepository extends JpaRepository<SaleCampaign, Long> {
    @Query("SELECT c FROM SaleCampaign c WHERE c.isActive = true AND c.startDate <= CURRENT_TIMESTAMP AND c.endDate >= CURRENT_TIMESTAMP ORDER BY c.endDate ASC")
    List<SaleCampaign> findCurrentlyActiveCampaigns();
}