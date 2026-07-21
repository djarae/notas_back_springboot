package com.komodo.notas.controller;

import com.komodo.notas.model.Note;
import com.komodo.notas.service.NoteService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @GetMapping
    public ResponseEntity<List<Note>> getNotes(Authentication auth,
                                                @RequestParam(required = false) Long categoryId) {
        log.debug("[NoteController] GET /notes user={} categoryId={}", auth.getName(), categoryId);
        return ResponseEntity.ok(noteService.getNotesByCategory(auth.getName(), categoryId));
    }

    @PostMapping
    public ResponseEntity<?> createNote(Authentication auth, @RequestBody NoteRequest req) {
        log.debug("[NoteController] POST /notes user={} categoryId={} title={}", auth.getName(), req.getCategoryId(), req.getTitle());
        try {
            Note note = noteService.createNote(auth.getName(), req.getCategoryId(),
                    req.getTitle(), req.getBody(), req.getNoteType());
            log.debug("[NoteController] POST /notes OK noteId={}", note.getId());
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("[NoteController] POST /notes ERROR: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(Authentication auth, @PathVariable UUID id,
                                         @RequestBody NoteRequest req) {
        log.debug("[NoteController] PUT /notes/{} user={} categoryId={}", id, auth.getName(), req.getCategoryId());
        try {
            Note note = noteService.updateNote(auth.getName(), id, req.getTitle(), req.getBody(), req.getCategoryId());
            log.debug("[NoteController] PUT /notes/{} OK", id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("[NoteController] PUT /notes/{} ERROR: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(Authentication auth, @PathVariable UUID id) {
        log.debug("[NoteController] DELETE /notes/{} user={}", id, auth.getName());
        try {
            noteService.deleteNote(auth.getName(), id);
            log.debug("[NoteController] DELETE /notes/{} OK", id);
            return ResponseEntity.ok("Nota eliminada");
        } catch (Exception e) {
            log.error("[NoteController] DELETE /notes/{} ERROR: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicateNote(Authentication auth, @PathVariable UUID id) {
        log.debug("[NoteController] POST /notes/{}/duplicate user={}", id, auth.getName());
        try {
            Note note = noteService.duplicateNote(auth.getName(), id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("[NoteController] POST /notes/{}/duplicate ERROR: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> moveNote(Authentication auth, @PathVariable UUID id,
                                       @RequestBody MoveRequest req) {
        log.debug("[NoteController] PUT /notes/{}/move user={} targetCategoryId={}", id, auth.getName(), req.getCategoryId());
        try {
            Note note = noteService.moveNote(auth.getName(), id, req.getCategoryId());
            log.debug("[NoteController] PUT /notes/{}/move OK", id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("[NoteController] PUT /notes/{}/move ERROR: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/copy-to")
    public ResponseEntity<?> copyNoteToCategories(Authentication auth, @PathVariable UUID id,
                                                  @RequestBody MultiCategoryRequest req) {
        log.debug("[NoteController] POST /notes/{}/copy-to user={} categoryIds={}", id, auth.getName(), req.getCategoryIds());
        try {
            noteService.copyNoteToCategories(auth.getName(), id, req.getCategoryIds());
            log.debug("[NoteController] POST /notes/{}/copy-to OK", id);
            return ResponseEntity.ok("Nota copiada correctamente");
        } catch (Exception e) {
            log.error("[NoteController] POST /notes/{}/copy-to ERROR: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/move-to")
    public ResponseEntity<?> moveNoteToCategories(Authentication auth, @PathVariable UUID id,
                                                  @RequestBody MultiCategoryRequest req) {
        log.debug("[NoteController] POST /notes/{}/move-to user={} categoryIds={}", id, auth.getName(), req.getCategoryIds());
        try {
            noteService.moveNoteToCategories(auth.getName(), id, req.getCategoryIds());
            log.debug("[NoteController] POST /notes/{}/move-to OK", id);
            return ResponseEntity.ok("Nota movida correctamente");
        } catch (Exception e) {
            log.error("[NoteController] POST /notes/{}/move-to ERROR: {}", id, e.getMessage());
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

    @Data
    public static class MultiCategoryRequest {
        private List<Long> categoryIds; // null in the list means 'General'
    }
}
