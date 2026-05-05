-- Migration: Bảng yêu cầu chỉnh sửa thông tin cơ bản khóa học đang bán (PUBLISHED)
-- Mục tiêu: Instructor không được sửa trực tiếp; phải gửi request, Admin duyệt mới áp dụng.
-- Chạy script này TRƯỚC khi khởi động Spring Boot (ddl-auto=validate)
-- DB: SQL Server (EduMarketplace)
-- Rollback: xem phần cuối file

-- ============================================================
-- 1. TẠO BẢNG
-- ============================================================

CREATE TABLE course_change_requests (
    id              BIGINT          IDENTITY(1,1)   PRIMARY KEY,

    -- Khóa học liên quan
    course_id       BIGINT          NOT NULL,

    -- Giảng viên gửi yêu cầu (FK → instructors.id)
    instructor_id   BIGINT          NOT NULL,

    -- Loại yêu cầu — phase này chỉ có BASIC_INFO
    request_type    NVARCHAR(50)    NOT NULL    DEFAULT 'BASIC_INFO',

    -- Trạng thái: PENDING / APPROVED / REJECTED / CANCELED
    status          NVARCHAR(20)    NOT NULL    DEFAULT 'PENDING',

    -- Snapshot JSON dữ liệu TRƯỚC thay đổi (title, description, thumbnail, ...)
    payload_before  NVARCHAR(MAX)   NOT NULL,

    -- Snapshot JSON dữ liệu SAU thay đổi (instructor muốn áp dụng)
    payload_after   NVARCHAR(MAX)   NOT NULL,

    -- Lý do instructor gửi (tùy chọn, giúp admin duyệt nhanh hơn)
    request_note    NVARCHAR(500)   NULL,

    -- Ghi chú admin khi duyệt hoặc từ chối (bắt buộc khi REJECTED)
    admin_note      NVARCHAR(500)   NULL,

    -- Admin đã duyệt/từ chối (FK → admins.id)
    reviewed_by     BIGINT          NULL,

    reviewed_at     DATETIME2       NULL,
    created_at      DATETIME2       NOT NULL    DEFAULT GETDATE(),
    updated_at      DATETIME2       NOT NULL    DEFAULT GETDATE(),

    -- --------------------------------------------------------
    -- FOREIGN KEYS
    -- --------------------------------------------------------
    CONSTRAINT fk_ccr_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ccr_instructor
        FOREIGN KEY (instructor_id)
        REFERENCES instructors(id),

    CONSTRAINT fk_ccr_reviewer
        FOREIGN KEY (reviewed_by)
        REFERENCES admins(id),

    -- --------------------------------------------------------
    -- CHECK CONSTRAINTS
    -- --------------------------------------------------------

    -- Chỉ cho phép request_type hợp lệ (mở rộng sau nếu cần)
    CONSTRAINT chk_ccr_request_type
        CHECK (request_type IN ('BASIC_INFO')),

    -- Chỉ cho phép status hợp lệ
    CONSTRAINT chk_ccr_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELED')),

    -- admin_note bắt buộc khi REJECTED
    -- (enforce cứng tại service layer; constraint này là safety net)
    CONSTRAINT chk_ccr_reject_note
        CHECK (status != 'REJECTED' OR admin_note IS NOT NULL)
);

-- ============================================================
-- 2. INDEXES
-- ============================================================

-- Tra cứu nhanh request của 1 course theo status (admin xem, service auto-cancel)
CREATE INDEX idx_ccr_course_status
    ON course_change_requests (course_id, status);

-- Instructor xem lịch sử request của mình, sắp xếp mới nhất trước
CREATE INDEX idx_ccr_instructor_created
    ON course_change_requests (instructor_id, created_at DESC);

-- Admin lọc toàn bộ PENDING, sắp xếp theo thời gian gửi
CREATE INDEX idx_ccr_status_created
    ON course_change_requests (status, created_at DESC);

-- ============================================================
-- ROLLBACK (chạy khi cần revert — KHÔNG chạy lúc deploy)
-- ============================================================
--
-- DROP TABLE IF EXISTS course_change_requests;
--
-- ============================================================
