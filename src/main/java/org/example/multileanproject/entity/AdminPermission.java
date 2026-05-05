package org.example.multileanproject.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

public enum AdminPermission {
    DASHBOARD_VIEW,
    STATS_VIEW,

    COURSE_VIEW,
    COURSE_APPROVE,
    COURSE_REJECT,
    COURSE_BULK_APPROVAL,
    COURSE_EDIT,
    COURSE_BATCH_SALE,
    COURSE_DELETE,

    USER_VIEW,
    USER_EDIT,
    USER_LOCK,
    INSTRUCTOR_PROFILE_VIEW,

    ORDER_VIEW,
    ORDER_UPDATE,
    ORDER_EXPORT,

    CATEGORY_VIEW,
    CATEGORY_MANAGE,
    CATEGORY_DELETE,

    VOUCHER_VIEW,
    VOUCHER_MANAGE,

    CAMPAIGN_VIEW,
    CAMPAIGN_MANAGE,

    TICKET_VIEW,
    TICKET_REPLY,

    REFUND_VIEW,
    REFUND_PROCESS,

    WITHDRAW_VIEW,
    WITHDRAW_PROCESS,

    AUDIT_VIEW,

    BLOG_VIEW,
    BLOG_MANAGE;

    public static EnumSet<AdminPermission> allPermissions() {
        return EnumSet.allOf(AdminPermission.class);
    }

    public static EnumSet<AdminPermission> defaultStaffPermissions() {
        return EnumSet.of(
                DASHBOARD_VIEW,
                STATS_VIEW,
                COURSE_VIEW,
                COURSE_APPROVE,
                COURSE_REJECT,
                COURSE_BULK_APPROVAL
        );
    }

    public static EnumSet<AdminPermission> fromNames(Collection<String> names) {
        EnumSet<AdminPermission> result = EnumSet.noneOf(AdminPermission.class);
        if (names == null) {
            return result;
        }
        for (String raw : names) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                result.add(AdminPermission.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    public static EnumSet<AdminPermission> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return EnumSet.noneOf(AdminPermission.class);
        }
        String[] parts = csv.split(",");
        List<String> names = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                names.add(part.trim());
            }
        }
        return fromNames(names);
    }

    public static String toCsv(Set<AdminPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(",");
        permissions.stream()
                .map(Enum::name)
                .sorted()
                .forEach(joiner::add);
        return joiner.toString();
    }

    public static List<String> toNames(Set<AdminPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        return permissions.stream()
                .map(Enum::name)
                .sorted()
                .toList();
    }
}
