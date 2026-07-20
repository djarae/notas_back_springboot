package com.komodo.notas.repository;

import com.komodo.notas.model.Category;
import com.komodo.notas.model.Note;
import com.komodo.notas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    // Notes in a specific category (not deleted)
    List<Note> findByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(Category category);
    // Notes without a category ("General")
    List<Note> findByUserAndCategoryIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    // All notes for a user (not deleted)
    List<Note> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    // Find one note by id and user (for ownership check)
    Optional<Note> findByIdAndUserAndDeletedAtIsNull(UUID id, User user);
}
