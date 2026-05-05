-- T-16: Student Notes (ghi chu theo bai hoc)
-- Chay script nay truoc khi khoi dong Spring Boot (vi ddl-auto=validate)

CREATE TABLE notes (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    student_id BIGINT         NOT NULL,
    lesson_id  BIGINT         NOT NULL,
    content    NVARCHAR(MAX)  NOT NULL,
    timestamp_seconds INT      NULL,
    created_at DATETIME2      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_notes_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT FK_notes_lesson  FOREIGN KEY (lesson_id)  REFERENCES lessons(id)  ON DELETE CASCADE
);

CREATE INDEX idx_notes_student_lesson_created
    ON notes(student_id, lesson_id, created_at DESC);
