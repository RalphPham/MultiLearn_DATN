-- T-02: Resource Library — Tài liệu đính kèm bài học
CREATE TABLE lesson_resources (
    id            BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    lesson_id     BIGINT        NOT NULL,
    instructor_id BIGINT        NOT NULL,
    file_name     NVARCHAR(500) NOT NULL,
    file_url      NVARCHAR(1000) NOT NULL,
    file_type     NVARCHAR(50)  NULL,
    file_size     BIGINT        NULL,
    uploaded_at   DATETIME2     NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_lesson_resources_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);
