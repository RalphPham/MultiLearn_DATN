import axiosClient from '@/api/axiosClient';

/**
 * ADMIN SERVICE
 * Chứa toàn bộ logic gọi API của trang Admin.
 * Hiện tại đang dùng chế độ HYBRID:
 * - API nào Backend chưa có: Trả về Mock Data (Giả lập)
 * - API nào Backend đã có: Gọi thẳng axiosClient
 */
export default {
    
    // ==========================================
    // 1. QUẢN LÝ KHÓA HỌC (COURSE APPROVAL)
    // ==========================================

    /**
     * Lấy danh sách khóa học chờ duyệt (PENDING)
     */
    getPendingCourses(page = 0, size = 20) {
        return axiosClient.get(`/admin/courses?status=PENDING&page=${page}&size=${size}`);
    },

    /**
     * Duyệt hoặc Từ chối khóa học
     * Tình trạng Backend: ĐÃ CÓ API [CourseAdminController]
     */
    verifyCourse(id, status, reason) {
        const normalized = String(status || '').toUpperCase();

        // Endpoint backend mới:
        // - APPROVE/PUBLISHED: POST /admin/courses/{id}/approve
        // - REJECT/REJECTED:   POST /admin/courses/{id}/reject
        if (normalized === 'APPROVE' || normalized === 'APPROVED' || normalized === 'PUBLISHED') {
            return axiosClient.post(`/admin/courses/${id}/approve`, {});
        }

        if (normalized === 'REJECT' || normalized === 'REJECTED') {
            return axiosClient.post(`/admin/courses/${id}/reject`, {
                reason: reason || ''
            });
        }

        // Fallback cho status khác (BLOCKED, DRAFT...)
        return axiosClient.put(`/admin/courses/${id}/status`, null, {
            params: { status: normalized }
        });
    },

    // ==========================================
    // 2. QUẢN LÝ NGƯỜI DÙNG (USER MANAGEMENT)
    // ==========================================

    /**
     * Lấy danh sách tất cả người dùng (có phân trang, tìm kiếm, lọc role)
     */
    getAllUsers(q = '', role = '', status = '', page = 0, size = 15) {
        const params = new URLSearchParams({ page, size });
        if (q) params.append('q', q);
        if (role) params.append('role', role);
        if (status) params.append('status', status);
        return axiosClient.get(`/admin/users?${params.toString()}`);
    },

    /**
     * Khóa / Mở khóa tài khoản
     * Tình trạng Backend: ĐÃ CÓ API [UserAdminController]
     */
    lockUser(id) {
        return axiosClient.put(`/admin/users/${id}/status`, null, {
            params: { status: 'LOCKED' }
        });
    },

    unlockUser(id) {
        return axiosClient.put(`/admin/users/${id}/status`, null, {
            params: { status: 'ACTIVE' }
        });
    },

    // ==========================================
    // 3. THỐNG KÊ (DASHBOARD)
    // ==========================================
    getDashboardStats() {
        return axiosClient.get('/admin/dashboard/stats');
    }
};
