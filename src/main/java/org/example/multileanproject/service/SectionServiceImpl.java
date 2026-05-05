package org.example.multileanproject.service;



import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.Section;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.SectionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AdminActionLogService adminActionLogService;

    private void assertSectionOwnership(Section section) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        if (isAdmin) return;
        if (section.getCourse() == null) return;
        if (section.getCourse().getInstructor() == null) return;
        if (section.getCourse().getInstructor().getUser() == null) return;
        String ownerEmail = section.getCourse().getInstructor().getUser().getEmail();
        if (!auth.getName().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Bạn không có quyền thao tác với section này.");
        }
    }

    // ================= CREATE =================
    @Override
    public Section createSection(Long courseId, Section section) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Integer maxOrder = sectionRepository.findMaxOrderIndexByCourseId(courseId);
        section.setOrderIndex(maxOrder + 1);
        section.setCourse(course);

        return sectionRepository.save(section);
    }

    // ================= UPDATE =================
    @Override
    public Section updateSection(Long sectionId, Section request) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        assertSectionOwnership(section);

        section.setTitle(request.getTitle());
        section.setDescription(request.getDescription());
        section.setOrderIndex(request.getOrderIndex());

        return sectionRepository.save(section);
    }

    // ================= DELETE =================
    @Override
    public void deleteSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        assertSectionOwnership(section);

        Long courseId = section.getCourse() != null ? section.getCourse().getId() : null;
        if (courseId != null && enrollmentRepository.countByCourse_Id(courseId) > 0) {
            adminActionLogService.log(
                    "BLOCK_HARD_DELETE_SECTION",
                    "Chan xoa sectionId=" + sectionId + " tren khoa hoc da co hoc vien",
                    courseId,
                    "COURSE"
            );
            throw new RuntimeException(
                    "Khoa hoc da co hoc vien. Khong duoc xoa section nay. Hay cap nhat noi dung hoac an section."
            );
        }

        sectionRepository.deleteById(sectionId);
    }

    // ================= GET =================
    @Override
    public List<Section> getSectionsByCourse(Long courseId) {
        return sectionRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
    }

    @Override
    public Section getSectionDetail(Long sectionId) {
        return sectionRepository.findByIdWithLessons(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
    }

    @Override
    public Integer countSectionsByCourse(Long courseId) {
        return sectionRepository.countByCourse_Id(courseId).intValue();
    }
}
