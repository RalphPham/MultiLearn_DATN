package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.WishlistResponse;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.Wishlist;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.WishlistRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<WishlistResponse> getWishlist(Long studentId) {
        Student student = getValidatedCurrentStudent(studentId);

        List<Wishlist> list = wishlistRepository.findByStudentId(student.getId());

        return list.stream().map(item -> WishlistResponse.builder()
                .id(item.getId())
                .courseId(item.getCourse().getId())
                .title(item.getCourse().getTitle())
                .slug(item.getCourse().getSlug())
                .thumbnail(item.getCourse().getThumbnail())
                .price(item.getCourse().getPrice())
                .salePrice(item.getCourse().getSalePrice())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToWishlist(Long studentId, Long courseId) {
        Student student = getValidatedCurrentStudent(studentId);

        if (wishlistRepository.existsByStudent_IdAndCourse_Id(student.getId(), courseId)) {
            return;
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateCourseCanBeWishlistedByStudent(course, student);

        Wishlist wishlist = new Wishlist();
        wishlist.setStudent(student);
        wishlist.setCourse(course);
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long studentId, Long courseId) {
        Student student = getValidatedCurrentStudent(studentId);
        wishlistRepository.deleteByStudent_IdAndCourse_Id(student.getId(), courseId);
    }

    @Override
    @Transactional
    public void toggleWishlist(Long studentId, Long courseId) {
        Student student = getValidatedCurrentStudent(studentId);

        if (wishlistRepository.existsByStudent_IdAndCourse_Id(student.getId(), courseId)) {
            removeFromWishlist(student.getId(), courseId);
        } else {
            addToWishlist(student.getId(), courseId);
        }
    }

    private Student getValidatedCurrentStudent(Long studentId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getEmail() == null || !currentEmail.equalsIgnoreCase(student.getEmail())) {
            throw new RuntimeException("Bạn không có quyền thao tác với wishlist này.");
        }

        return student;
    }

    private void validateCourseCanBeWishlistedByStudent(Course course, Student student) {
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new RuntimeException("Khóa học hiện không mở bán.");
        }

        if (course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && course.getInstructor().getUser().getEmail() != null
                && student.getEmail() != null
                && student.getEmail().equalsIgnoreCase(course.getInstructor().getUser().getEmail())) {
            throw new RuntimeException("Bạn không thể thêm khóa học của chính mình vào yêu thích.");
        }
    }
}