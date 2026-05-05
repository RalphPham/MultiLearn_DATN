package org.example.multileanproject.repository;

import org.example.multileanproject.entity.SaleCampaignItem;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleCampaignItemRepository extends JpaRepository<SaleCampaignItem, Long> {

    @Query("SELECT item FROM SaleCampaignItem item " +
            "JOIN item.campaign camp " +
            "WHERE item.course.id = :courseId " +
            "AND camp.isActive = true " +
            "AND camp.startDate <= :now " +
            "AND camp.endDate >= :now " +
            "AND item.soldSlots < item.totalSlots " +
            "ORDER BY item.promotionalPrice ASC")
    List<SaleCampaignItem> findActiveSalesForCourse(@Param("courseId") Long courseId,
                                                    @Param("now") LocalDateTime now,
                                                    Pageable pageable);

    /**
     * Trả về 1 sale active có giá khuyến mãi thấp nhất cho 1 khóa học.
     * Nếu khóa học nằm trong nhiều campaign active cùng lúc (do data trùng),
     * sẽ chọn 1 cái thay vì throw lỗi "non-unique result".
     */
    default Optional<SaleCampaignItem> findActiveSaleForCourse(Long courseId, LocalDateTime now) {
        List<SaleCampaignItem> list = findActiveSalesForCourse(courseId, now, PageRequest.of(0, 1));
        return list.stream().findFirst();
    }

    void deleteByCampaignId(Long campaignId);

    // Dọn các item trỏ tới course trước khi xóa course (tránh FK_CampaignItem_Course)
    void deleteByCourse_Id(Long courseId);

    // 🔥 HÀM MỚI: Lấy danh sách khóa học thuộc về 1 đợt Sale
    List<SaleCampaignItem> findByCampaignId(Long campaignId);

    /**
     * Tìm các item của các course đã nằm trong campaign khác mà thời gian
     * (campaign.startDate, campaign.endDate) overlap với khoảng [newStart, newEnd].
     * Dùng để chặn tạo campaign trùng thời gian + trùng course.
     * Loại trừ campaign có id = excludeCampaignId (dùng khi update).
     */
    @Query("SELECT item FROM SaleCampaignItem item " +
            "JOIN item.campaign camp " +
            "WHERE item.course.id IN :courseIds " +
            "AND camp.startDate <= :newEnd " +
            "AND camp.endDate >= :newStart " +
            "AND (:excludeCampaignId IS NULL OR camp.id <> :excludeCampaignId)")
    List<SaleCampaignItem> findOverlappingItems(@Param("courseIds") List<Long> courseIds,
                                                @Param("newStart") LocalDateTime newStart,
                                                @Param("newEnd") LocalDateTime newEnd,
                                                @Param("excludeCampaignId") Long excludeCampaignId);
}
