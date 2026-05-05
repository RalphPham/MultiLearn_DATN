package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.NoteCreateRequest;
import org.example.multileanproject.dto.NoteDTO;
import org.example.multileanproject.dto.NoteUpdateRequest;
import org.example.multileanproject.service.LessonNoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/notes")
@RequiredArgsConstructor
public class LessonNoteController {

    private final LessonNoteService lessonNoteService;

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<NoteDTO>> getByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonNoteService.getNotesByLesson(lessonId));
    }

    @PostMapping
    public ResponseEntity<NoteDTO> create(@RequestBody NoteCreateRequest request) {
        return ResponseEntity.ok(lessonNoteService.createNote(request));
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteDTO> update(
            @PathVariable Long noteId,
            @RequestBody NoteUpdateRequest request
    ) {
        return ResponseEntity.ok(lessonNoteService.updateNote(noteId, request));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long noteId) {
        lessonNoteService.deleteNote(noteId);
        return ResponseEntity.ok().build();
    }
}
