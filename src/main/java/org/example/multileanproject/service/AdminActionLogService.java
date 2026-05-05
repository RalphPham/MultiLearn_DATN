package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.AdminActionLog;
import org.example.multileanproject.repository.AdminActionLogRepository;
import org.example.multileanproject.util.TextEncodingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminActionLogService {

    private final AdminActionLogRepository logRepository;

    /** Lấy username của admin đang đăng nhập */
    private String currentAdmin() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    /** Ghi log bất đồng bộ — không chặn luồng chính */
    @Async
    public void log(String action, String description, Long targetId, String targetType) {
        try {
            String normalizedDescription = TextEncodingUtil.normalize(description);
            AdminActionLog entry = AdminActionLog.builder()
                    .adminUsername(currentAdmin())
                    .action(action)
                    .description(normalizedDescription)
                    .targetId(targetId)
                    .targetType(targetType)
                    .build();
            logRepository.save(entry);
        } catch (Exception e) {
            // Không để lỗi log ảnh hưởng đến nghiệp vụ chính
        }
    }

    /** Danh sách log có filter + phân trang */
    public Page<AdminActionLog> getLogs(String adminUsername, String action,
                                        String targetType, Pageable pageable) {
        Page<AdminActionLog> page = logRepository.searchLogs(
                (adminUsername != null && !adminUsername.isBlank()) ? adminUsername : null,
                (action        != null && !action.isBlank())        ? action        : null,
                (targetType    != null && !targetType.isBlank())    ? targetType    : null,
                pageable
        );
        return page.map(this::normalizeLogForView);
    }

    private AdminActionLog normalizeLogForView(AdminActionLog source) {
        if (source == null) {
            return null;
        }
        return AdminActionLog.builder()
                .id(source.getId())
                .adminUsername(TextEncodingUtil.normalize(source.getAdminUsername()))
                .action(TextEncodingUtil.normalize(source.getAction()))
                .description(TextEncodingUtil.normalize(source.getDescription()))
                .targetId(source.getTargetId())
                .targetType(TextEncodingUtil.normalize(source.getTargetType()))
                .createdAt(source.getCreatedAt())
                .build();
    }
}
