package org.example.multileanproject.entity;

public enum AdminRole {
    SUPER_ADMIN,  // Toàn quyền: duyệt, từ chối, xóa, khóa, mở khóa
    STAFF         // Quyền hạn chế: chỉ xem, duyệt nhẹ, không được xóa hay khóa user
}
