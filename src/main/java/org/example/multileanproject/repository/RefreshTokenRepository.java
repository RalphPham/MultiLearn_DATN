package org.example.multileanproject.repository;

import org.example.multileanproject.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Tìm token theo chuỗi - dùng khi /refresh được gọi
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Xoá toàn bộ token của 1 user - dùng cho /logout-all
     * @Modifying + @Transactional ở Service để đảm bảo chạy trong transaction.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId AND rt.userType = :userType")
    void deleteAllByUserIdAndUserType(@Param("userId") Long userId, @Param("userType") String userType);

    /** Đếm số phiên đang active của 1 user - dùng để debug / hiển thị */
    int countByUserId(Long userId);
}
