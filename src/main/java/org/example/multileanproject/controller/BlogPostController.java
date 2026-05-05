package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BlogGenerateRequest;
import org.example.multileanproject.dto.BlogPostDTO;
import org.example.multileanproject.service.BlogPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BlogPostController {

    private final BlogPostService blogPostService;

    // ── PUBLIC ──────────────────────────────────────────────────

    @GetMapping("/api/public/blog/posts")
    public ResponseEntity<Page<BlogPostDTO>> getPublishedPosts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(
                blogPostService.getPublishedPosts(category, PageRequest.of(page, size))
        );
    }

    @GetMapping("/api/public/blog/posts/{id}")
    public ResponseEntity<BlogPostDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.getPostById(id));
    }

    // ── ADMIN ────────────────────────────────────────────────────

    @GetMapping("/api/admin/blog/posts")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','BLOG_VIEW')")
    public ResponseEntity<Page<BlogPostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                blogPostService.getAllPosts(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
        );
    }

    @PostMapping("/api/admin/blog/generate")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','BLOG_MANAGE')")
    public ResponseEntity<BlogPostDTO> generatePost(@RequestBody BlogGenerateRequest request) {
        return ResponseEntity.ok(blogPostService.generatePost(request));
    }

    @PutMapping("/api/admin/blog/posts/{id}/publish")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','BLOG_MANAGE')")
    public ResponseEntity<BlogPostDTO> publishPost(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.publishPost(id));
    }

    @DeleteMapping("/api/admin/blog/posts/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','BLOG_MANAGE')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
