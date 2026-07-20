package com.komodo.notas.service;

import com.komodo.notas.model.Category;
import com.komodo.notas.model.User;
import com.komodo.notas.repository.CategoryRepository;
import com.komodo.notas.repository.NoteRepository;
import com.komodo.notas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NoteRepository noteRepository;
    @Autowired private UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<Category> getCategories(String email) {
        return categoryRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtAsc(getUser(email));
    }

    public Category createCategory(String email, String name, String color) {
        User user = getUser(email);
        Category cat = new Category();
        cat.setUser(user);
        cat.setName(name);
        cat.setColor(color != null ? color : "#8B5CF6");
        return categoryRepository.save(cat);
    }

    public Category updateCategory(String email, Long id, String name, String color) {
        User user = getUser(email);
        Category cat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(id, user)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        if (name != null) cat.setName(name);
        if (color != null) cat.setColor(color);
        return categoryRepository.save(cat);
    }

    public void deleteCategory(String email, Long id, boolean moveNotesToGeneral) {
        User user = getUser(email);
        Category cat = categoryRepository.findByIdAndUserAndDeletedAtIsNull(id, user)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (moveNotesToGeneral) {
            // Move notes to "General" (set category to null)
            noteRepository.findByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(cat)
                    .forEach(note -> {
                        note.setCategory(null);
                        noteRepository.save(note);
                    });
        } else {
            // Soft-delete all notes in the category
            noteRepository.findByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(cat)
                    .forEach(note -> {
                        note.setDeletedAt(LocalDateTime.now());
                        noteRepository.save(note);
                    });
        }

        cat.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(cat);
    }
}
