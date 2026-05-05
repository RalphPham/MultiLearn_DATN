package org.example.multileanproject.controller;

import org.example.multileanproject.dto.SaleCampaignRequest;
import org.example.multileanproject.entity.SaleCampaign;
import org.example.multileanproject.service.SaleCampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/campaigns")
public class SaleCampaignController {

    private final SaleCampaignService campaignService;

    public SaleCampaignController(SaleCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CAMPAIGN_VIEW')")
    public ResponseEntity<List<SaleCampaign>> getAll() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CAMPAIGN_MANAGE')")
    public ResponseEntity<SaleCampaign> create(@RequestBody SaleCampaignRequest request) {
        return ResponseEntity.ok(campaignService.createCampaign(request));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CAMPAIGN_MANAGE')")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        campaignService.toggleCampaignStatus(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CAMPAIGN_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok().build();
    }
}