package com.komodo.notas.controller;

import com.komodo.notas.model.Note;
import com.komodo.notas.service.NoteService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    public ResponseEntity<List<Note>> getNotes(Authentication auth,
                                                @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(noteService.getNotesByCategory(auth.getName(), categoryId));
    }

    @PostMapping
    public ResponseEntity<?> createNote(Authentication auth, @RequestBody NoteRequest req) {
        try {
            Note note = noteService.createNote(auth.getName(), req.getCategoryId(),
                    req.getTitle(), req.getBody(), req.getNoteType());
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(Authentication auth, @PathVariable UUID id,
                                         @RequestBody NoteRequest req) {
        try {
            Note note = noteService.updateNote(auth.getName(), id, req.getTitle(), req.getBody(), req.getCategoryId());
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(Authentication auth, @PathVariable UUID id) {
        try {
            noteService.deleteNote(auth.getName(), id);
            return ResponseEntity.ok("Nota eliminada");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicateNote(Authentication auth, @PathVariable UUID id) {
        try {
            Note note = noteService.duplicateNote(auth.getName(), id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> moveNote(Authentication auth, @PathVariable UUID id,
                                       @RequestBody MoveRequest req) {
        try {
            Note note = noteService.moveNote(auth.getName(), id, req.getCategoryId());
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class NoteRequest {
        private Long categoryId;
        private String title;
        private String body;
        private Note.NoteType noteType;
    }

    @Data
    public static class MoveRequest {
        private Long categoryId; // null = move to General
    }
}
