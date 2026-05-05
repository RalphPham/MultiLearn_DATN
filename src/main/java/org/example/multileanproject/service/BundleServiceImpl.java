package org.example.multileanproject.service;


import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BundleDTO;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.example.multileanproject.service.BundleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BundleServiceImpl implements BundleService {

    private final BundleRepository bundleRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    @Override
    @Transactional
    public Bundle createBundle(BundleDTO request, String email) {
        // 1. Tìm giảng viên
        Instructor instructor = instructorRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Bạn không phải là giảng viên"));

        // 2. Tạo Bundle mới
        Bundle bundle = new Bundle();
        if (request.getId() != null) {
            // Nếu có ID thì là cập nhật
            bundle = bundleRepository.findById(request.getId()).orElse(new Bundle());
        }

        bundle.setTitle(request.getTitle());
        bundle.setDescription(request.getDescription());
        bundle.setPrice(request.getPrice());
        bundle.setThumbnail(request.getThumbnail());
        bundle.setInstructor(instructor);
        bundle.setStatus(CourseStatus.valueOf(request.getStatus()));

        // 3. Gán các khóa học vào gói
        if (request.getSelectedCourseIds() != null && !request.getSelectedCourseIds().isEmpty()) {
            List<Course> courses = courseRepository.findAllById(request.getSelectedCourseIds());
            bundle.setCourses(courses);
        }

        return bundleRepository.save(bundle);
    }

    @Override
    public List<Bundle> getMyBundles(String email) {
        return bundleRepository.findByInstructor_User_Email(email);
    }

    @Override
    public BundleDTO getBundleDetail(Long id) {
        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói combo"));
        BundleDTO dto = new BundleDTO();
        dto.setId(bundle.getId());
        dto.setTitle(bundle.getTitle());
        dto.setDescription(bundle.getDescription());
        dto.setThumbnail(bundle.getThumbnail());
        dto.setPrice(bundle.getPrice());
        dto.setStatus(bundle.getStatus() != null ? bundle.getStatus().name() : "DRAFT");
        dto.setSelectedCourseIds(
            bundle.getCourses().stream()
                .map(c -> c.getId())
                .collect(java.util.stream.Collectors.toList())
        );
        return dto;
    }

    @Override
    public void deleteBundle(Long bundleId) {
        bundleRepository.deleteById(bundleId);
    }
}