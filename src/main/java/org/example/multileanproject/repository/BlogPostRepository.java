package org.example.multileanproject.repository;

import org.example.multileanproject.entity.BlogPost;
import org.example.multileanproject.entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Page<BlogPost> findByStatusOrderByPublishedAtDesc(BlogStatus status, Pageable pageable);
    Page<BlogPost> findByCategoryAndStatusOrderByPublishedAtDesc(String category, BlogStatus status, Pageable pageable);
}
