package org.example.multileanproject.service;

import org.example.multileanproject.dto.SaleCampaignRequest;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.SaleCampaign;
import org.example.multileanproject.entity.SaleCampaignItem;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.example.multileanproject.repository.SaleCampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaleCampaignService {

    private final SaleCampaignRepository campaignRepository;
    private final SaleCampaignItemRepository itemRepository;
    private final CourseRepository courseRepository;

    public SaleCampaignService(SaleCampaignRepository campaignRepository,
                               SaleCampaignItemRepository itemRepository,
                               CourseRepository courseRepository) {
        this.campaignRepository = campaignRepository;
        this.itemRepository = itemRepository;
        this.courseRepository = courseRepository;
    }

    // 1. Lấy danh sách tất cả chiến dịch (Kèm trạng thái tính toán real-time)
    public List<SaleCampaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    // 2. Tạo chiến dịch mới
    @Transactional
    public SaleCampaign createCampaign(SaleCampaignRequest request) {
        // Validate dates
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new RuntimeException("Vui lòng chọn thời gian bắt đầu và kết thúc.");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new RuntimeException("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc.");
        }

        // Validate items + chặn trùng course trong cùng request
        List<SaleCampaignRequest.CampaignItemRequest> items = request.getItems();
        if (items != null && !items.isEmpty()) {
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (SaleCampaignRequest.CampaignItemRequest itemReq : items) {
                if (itemReq.getCourseId() == null) {
                    throw new RuntimeException("Có item thiếu courseId.");
                }
                if (!seen.add(itemReq.getCourseId())) {
                    throw new RuntimeException("Một khóa học không được xuất hiện 2 lần trong cùng chiến dịch (courseId="
                            + itemReq.getCourseId() + ").");
                }
                if (itemReq.getPromotionalPrice() == null
                        || itemReq.getPromotionalPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Giá khuyến mãi phải lớn hơn 0.");
                }
                if (itemReq.getTotalSlots() <= 0) {
                    throw new RuntimeException("Số slot phải lớn hơn 0.");
                }
            }

            // Chặn course đã nằm trong campaign khác có thời gian overlap
            List<Long> courseIds = new java.util.ArrayList<>(seen);
            List<SaleCampaignItem> conflicts = itemRepository.findOverlappingItems(
                    courseIds, request.getStartDate(), request.getEndDate(), null);
            if (!conflicts.isEmpty()) {
                java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                // Gom theo campaign: campaignId -> [courseTitles]
                java.util.Map<Long, java.util.List<String>> byCampaign = new java.util.LinkedHashMap<>();
                java.util.Map<Long, SaleCampaign> campMap = new java.util.HashMap<>();
                for (SaleCampaignItem it : conflicts) {
                    Long cid = it.getCampaign().getId();
                    campMap.put(cid, it.getCampaign());
                    byCampaign.computeIfAbsent(cid, k -> new java.util.ArrayList<>())
                            .add(it.getCourse().getTitle());
                }

                StringBuilder sb = new StringBuilder("Trùng thời gian với chiến dịch khác. ");
                int idx = 0;
                for (java.util.Map.Entry<Long, java.util.List<String>> e : byCampaign.entrySet()) {
                    if (idx++ > 0) sb.append(" | ");
                    SaleCampaign c = campMap.get(e.getKey());
                    java.util.List<String> titles = e.getValue();
                    String preview = titles.size() <= 3
                            ? String.join(", ", titles)
                            : String.join(", ", titles.subList(0, 3)) + " +" + (titles.size() - 3) + " khóa khác";
                    sb.append("\"").append(c.getName()).append("\" (")
                      .append(c.getStartDate().format(fmt)).append(" → ")
                      .append(c.getEndDate().format(fmt)).append("): ")
                      .append(titles.size()).append(" khóa trùng [").append(preview).append("]");
                }
                throw new RuntimeException(sb.toString());
            }
        }

        // Lưu thông tin chiến dịch
        SaleCampaign campaign = SaleCampaign.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.isActive())
                .allowCouponStacking(request.isAllowCouponStacking())
                .build();
        campaign = campaignRepository.save(campaign);

        // Lưu các khóa học trong chiến dịch
        if (items != null) {
            for (SaleCampaignRequest.CampaignItemRequest itemReq : items) {
                Course course = courseRepository.findById(itemReq.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học ID: " + itemReq.getCourseId()));

                SaleCampaignItem item = SaleCampaignItem.builder()
                        .campaign(campaign)
                        .course(course)
                        .promotionalPrice(itemReq.getPromotionalPrice())
                        .totalSlots(itemReq.getTotalSlots())
                        .soldSlots(0) // Mới tạo nên chưa bán được slot nào
                        .build();
                itemRepository.save(item);
            }
        }
        return campaign;
    }

    // 3. Đổi trạng thái Bật/Tắt khẩn cấp
    @Transactional
    public void toggleCampaignStatus(Long id) {
        SaleCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch"));
        campaign.setActive(!campaign.isActive());
        campaignRepository.save(campaign);
    }

    // 4. Xóa chiến dịch (Xóa item trước, xóa campaign sau)
    @Transactional
    public void deleteCampaign(Long id) {
        itemRepository.deleteByCampaignId(id);
        campaignRepository.deleteById(id);
    }
    // Lấy chiến dịch đang chạy để public ra ngoài trang chủ/blog
    public SaleCampaign getActiveCampaign() {
        List<SaleCampaign> activeCampaigns = campaignRepository.findCurrentlyActiveCampaigns();
        return activeCampaigns.isEmpty() ? null : activeCampaigns.get(0);
    }
}
