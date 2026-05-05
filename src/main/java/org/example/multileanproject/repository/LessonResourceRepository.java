package org.example.multileanproject.repository;

import org.example.multileanproject.entity.LessonResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {

    List<LessonResource> findByInstructorIdOrderByUploadedAtDesc(Long instructorId);

    long countByInstructorId(Long instructorId);

    @Query("SELECT COALESCE(SUM(r.fileSize), 0) FROM LessonResource r WHERE r.instructorId = :instructorId")
    Long sumFileSizeByInstructorId(@Param("instructorId") Long instructorId);

    List<LessonResource> findByLessonIdOrderByUploadedAtDesc(Long lessonId);

    void deleteByLessonId(Long lessonId);
}
