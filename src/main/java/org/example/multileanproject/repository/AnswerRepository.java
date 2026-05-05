package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // ❌ XÓA hàm này vì nó gây lỗi: List<Answer> findByQuestionIdIn(List<Long> questionIds);

    // ✅ GIỮ LẠI hàm này (Đúng chuẩn JPA để tìm theo ID của Question)
    List<Answer> findByQuestion_IdIn(List<Long> questionIds);
}