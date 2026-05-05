package org.example.multileanproject.controller;

import org.example.multileanproject.dto.CourseListDTO;
import org.example.multileanproject.entity.SaleCampaign;
import org.example.multileanproject.entity.SaleCampaignItem;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.example.multileanproject.service.SaleCampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campaigns")
public class PublicCampaignController {

    private final SaleCampaignService campaignService;
    private final SaleCampaignItemRepository itemRepository;

    public PublicCampaignController(SaleCampaignService campaignService, SaleCampaignItemRepository itemRepository) {
        this.campaignService = campaignService;
        this.itemRepository = itemRepository;
    }

    // 1. API lấy thông tin đợt Sale
    @GetMapping("/active")
    public ResponseEntity<SaleCampaign> getActiveCampaign() {
        SaleCampaign active = campaignService.getActiveCampaign();
        if (active == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(active);
    }

    // 2. 🔥 API lấy danh sách khóa học TRONG đợt Sale đó 🔥
    @GetMapping("/active/items")
    public ResponseEntity<List<CourseListDTO>> getActiveCampaignItems() {
        SaleCampaign active = campaignService.getActiveCampaign();
        if (active == null) {
            return ResponseEntity.noContent().build();
        }

        List<SaleCampaignItem> items = itemRepository.findByCampaignId(active.getId());

        List<CourseListDTO> dtoList = items.stream()
                .map(item -> CourseListDTO.fromEntity(item.getCourse(), item))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}