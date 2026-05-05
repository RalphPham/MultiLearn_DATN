package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    // Tìm giảng viên dựa trên thực thể Student
    Optional<Instructor> findByUser(Student user);

    // Kiểm tra xem Student này đã tồn tại trong bảng Instructor chưa
    boolean existsByUser(Student user);

    /**
     * Tìm giảng viên dựa trên Email của User liên kết.
     * Spring Data JPA sẽ tự động hiểu: Instructor -> Student user -> String email.
     */
    Optional<Instructor> findByUser_Email(String email);
    Optional<Instructor> findByUser_Id(Long userId);
    /**
     * Kiểm tra sự tồn tại của giảng viên qua Email.
     */
    boolean existsByUser_Email(String email);
    // Thêm hàm này vào trong interface InstructorRepository
    @Query("SELECT i FROM Instructor i WHERE i.user.id = :userId")
    Optional<Instructor> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT i FROM Instructor i JOIN i.user u
        WHERE (:q IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%',:q,'%'))
                          OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))
                          OR LOWER(i.fullName) LIKE LOWER(CONCAT('%',:q,'%')))
        ORDER BY i.id DESC
        """)
    Page<Instructor> searchInstructors(@Param("q") String q, Pageable pageable);
}