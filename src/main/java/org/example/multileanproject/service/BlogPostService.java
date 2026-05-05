package org.example.multileanproject.service;

import org.example.multileanproject.dto.BlogGenerateRequest;
import org.example.multileanproject.dto.BlogPostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogPostService {
    Page<BlogPostDTO> getPublishedPosts(String category, Pageable pageable);
    BlogPostDTO getPostById(Long id);
    Page<BlogPostDTO> getAllPosts(Pageable pageable);
    BlogPostDTO generatePost(BlogGenerateRequest request);
    BlogPostDTO publishPost(Long id);
    void deletePost(Long id);
}
