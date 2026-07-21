package com.komodo.notas.service;

import com.komodo.notas.model.Category;
import com.komodo.notas.model.Note;
import com.komodo.notas.model.User;
import com.komodo.notas.repository.CategoryRepository;
import com.komodo.notas.repository.NoteRepository;
import com.komodo.notas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    @Autowired private NoteRepository noteRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<Note> getNotesByCategory(String email, Long categoryId) {
        User user = getUser(email);
        if (categoryId == null) {
            return noteRepository.findByUserAndCategoryIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(user);
        }
        Category cat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(categoryId, user)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return noteRepository.findByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(cat);
    }

    public Note createNote(String email, Long categoryId, String title, String body, Note.NoteType noteType) {
        User user = getUser(email);
        Category cat = null;
        if (categoryId != null) {
            cat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(categoryId, user)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        }
        Note note = new Note();
        note.setUser(user);
        note.setCategory(cat);
        note.setTitle(title);
        note.setBody(body);
        note.setNoteType(noteType != null ? noteType : Note.NoteType.TEXT);
        return noteRepository.save(note);
    }

    public Note updateNote(String email, UUID noteId, String title, String body, Long categoryId) {
        User user = getUser(email);
        Note note = noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, user)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));

        if (title != null) note.setTitle(title);
        if (body != null) note.setBody(body);
        if (categoryId != null) {
            Category cat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(categoryId, user)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            note.setCategory(cat);
        }
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public void deleteNote(String email, UUID noteId) {
        User user = getUser(email);
        Note note = noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, user)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));
        note.setDeletedAt(LocalDateTime.now());
        noteRepository.save(note);
    }

    public Note duplicateNote(String email, UUID noteId) {
        User user = getUser(email);
        Note original = noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, user)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));
        Note copy = new Note();
        copy.setUser(user);
        copy.setCategory(original.getCategory());
        copy.setTitle(original.getTitle() != null ? original.getTitle() + " (copia)" : null);
        copy.setBody(original.getBody());
        copy.setNoteType(original.getNoteType());
        return noteRepository.save(copy);
    }

    public Note moveNote(String email, UUID noteId, Long targetCategoryId) {
        User user = getUser(email);
        Note note = noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, user)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));
        if (targetCategoryId == null) {
            note.setCategory(null); // Move to General
        } else {
            Category cat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(targetCategoryId, user)
                    .orElseThrow(() -> new RuntimeException("Categoría destino no encontrada"));
            note.setCategory(cat);
        }
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public void copyNoteToCategories(String email, UUID noteId, List<Long> categoryIds) {
        User user = getUser(email);
        Note original = noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, user)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));

        if (categoryIds == null || categoryIds.isEmpty()) return;

        for (Long catId : categoryIds) {
            Category targetCat = null;
            if (catId != null) {
                targetCat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(catId, user)
                        .orElseThrow(() -> new RuntimeException("Categoría destino no encontrada"));
            }
            
            // Si la nota ya está en la categoría destino, omitimos (opcional, pero útil)
            if (original.getCategory() == null && targetCat == null) continue;
            if (original.getCategory() != null && targetCat != null && original.getCategory().getId().equals(targetCat.getId())) continue;

            Note copy = new Note();
            copy.setUser(user);
            copy.setCategory(targetCat);
            copy.setTitle(original.getTitle());
            copy.setBody(original.getBody());
            copy.setNoteType(original.getNoteType());
            noteRepository.save(copy);
        }
    }

    public void moveNoteToCategories(String email, UUID noteId, List<Long> categoryIds) {
        // Copiamos a los nuevos destinos
        copyNoteToCategories(email, noteId, categoryIds);
        
        // Eliminamos la original
        deleteNote(email, noteId);
    }
}
