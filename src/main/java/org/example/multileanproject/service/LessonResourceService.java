package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.LessonResourceDTO;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Lesson;
import org.example.multileanproject.entity.LessonResource;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.repository.LessonResourceRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonResourceService {

    private final LessonResourceRepository resourceRepository;
    private final LessonRepository lessonRepository;
    private final InstructorRepository instructorRepository;

    private Instructor getCurrentInstructor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return instructorRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giảng viên."));
    }

    // ── Danh sách tài nguyên của instructor ───────────────────────────────
    @Transactional(readOnly = true)
    public List<LessonResourceDTO> getMyResources() {
        Long instructorUserId = getCurrentInstructor().getUser().getId();
        List<LessonResource> resources = resourceRepository
                .findByInstructorIdOrderByUploadedAtDesc(instructorUserId);

        // Build a map lessonId → Lesson (with course info via JOIN FETCH)
        List<Lesson> lessons = lessonRepository.findAllByInstructorUserId(instructorUserId);
        Map<Long, Lesson> lessonMap = lessons.stream()
                .collect(Collectors.toMap(Lesson::getId, l -> l));

        return resources.stream().map(r -> {
            LessonResourceDTO dto = toDTO(r);
            Lesson lesson = lessonMap.get(r.getLessonId());
            if (lesson != null) {
                dto.setLessonTitle(lesson.getTitle());
                if (lesson.getSection() != null && lesson.getSection().getCourse() != null) {
                    dto.setCourseTitle(lesson.getSection().getCourse().getTitle());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ── Thống kê nhanh (số file + tổng dung lượng) ───────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Long instructorUserId = getCurrentInstructor().getUser().getId();
        long fileCount = resourceRepository.countByInstructorId(instructorUserId);
        long totalSize = resourceRepository.sumFileSizeByInstructorId(instructorUserId);
        return Map.of("fileCount", fileCount, "totalSize", totalSize);
    }

    // ── Danh sách bài học để chọn khi upload ─────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyLessonsForPicker() {
        Long instructorUserId = getCurrentInstructor().getUser().getId();
        return lessonRepository.findAllByInstructorUserId(instructorUserId)
                .stream()
                .map(l -> Map.of(
                        "lessonId",    (Object) l.getId(),
                        "lessonTitle", l.getTitle(),
                        "courseTitle", l.getSection().getCourse().getTitle(),
                        "courseId",    l.getSection().getCourse().getId()
                ))
                .collect(Collectors.toList());
    }

    // ── Lưu bản ghi tài nguyên sau khi upload file ───────────────────────
    @Transactional
    public LessonResourceDTO saveResource(LessonResourceDTO dto) {
        Instructor instructor = getCurrentInstructor();
        Long instructorUserId = instructor.getUser().getId();

        // Kiểm tra bài học thuộc instructor này
        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học."));
        if (!lesson.getSection().getCourse().getInstructor().getUser().getId()
                .equals(instructorUserId)) {
            throw new RuntimeException("Bạn không có quyền thêm tài nguyên vào bài học này.");
        }

        LessonResource resource = LessonResource.builder()
                .lessonId(dto.getLessonId())
                .instructorId(instructorUserId)
                .fileName(dto.getFileName())
                .fileUrl(dto.getFileUrl())
                .fileType(dto.getFileType())
                .fileSize(dto.getFileSize())
                .build();

        return toDTO(resourceRepository.save(resource));
    }

    // ── Danh sách tài nguyên theo bài học (instructor) ───────────────────
    @Transactional(readOnly = true)
    public List<LessonResourceDTO> getResourcesByLessonId(Long lessonId) {
        Long instructorUserId = getCurrentInstructor().getUser().getId();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học."));
        if (!lesson.getSection().getCourse().getInstructor().getUser().getId()
                .equals(instructorUserId)) {
            throw new RuntimeException("Bạn không có quyền xem tài nguyên bài học này.");
        }
        return resourceRepository.findByLessonIdOrderByUploadedAtDesc(lessonId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Danh sách tài nguyên theo bài học (học viên) ─────────────────────
    @Transactional(readOnly = true)
    public List<LessonResourceDTO> getResourcesByLessonIdForStudent(Long lessonId) {
        return resourceRepository.findByLessonIdOrderByUploadedAtDesc(lessonId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Xóa tài nguyên ───────────────────────────────────────────────────
    @Transactional
    public void deleteResource(Long resourceId) {
        Long instructorUserId = getCurrentInstructor().getUser().getId();
        LessonResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài nguyên."));
        if (!resource.getInstructorId().equals(instructorUserId)) {
            throw new RuntimeException("Bạn không có quyền xóa tài nguyên này.");
        }
        resourceRepository.delete(resource);
    }

    // ── Mapping ───────────────────────────────────────────────────────────
    private LessonResourceDTO toDTO(LessonResource r) {
        LessonResourceDTO dto = new LessonResourceDTO();
        dto.setId(r.getId());
        dto.setLessonId(r.getLessonId());
        dto.setInstructorId(r.getInstructorId());
        dto.setFileName(r.getFileName());
        dto.setFileUrl(r.getFileUrl());
        dto.setFileType(r.getFileType());
        dto.setFileSize(r.getFileSize());
        dto.setUploadedAt(r.getUploadedAt());
        return dto;
    }
}
