package org.example.multileanproject.repository;

import org.example.multileanproject.entity.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    @Query(value = """
        SELECT l FROM AdminActionLog l
        WHERE (:adminUsername IS NULL OR :adminUsername = ''
               OR LOWER(l.adminUsername) LIKE LOWER(CONCAT('%', :adminUsername, '%')))
          AND (:action IS NULL OR :action = '' OR l.action = :action)
          AND (:targetType IS NULL OR :targetType = '' OR l.targetType = :targetType)
        """,
        countQuery = """
        SELECT COUNT(l) FROM AdminActionLog l
        WHERE (:adminUsername IS NULL OR :adminUsername = ''
               OR LOWER(l.adminUsername) LIKE LOWER(CONCAT('%', :adminUsername, '%')))
          AND (:action IS NULL OR :action = '' OR l.action = :action)
          AND (:targetType IS NULL OR :targetType = '' OR l.targetType = :targetType)
        """)
    Page<AdminActionLog> searchLogs(
            @Param("adminUsername") String adminUsername,
            @Param("action")       String action,
            @Param("targetType")   String targetType,
            Pageable pageable
    );
}
