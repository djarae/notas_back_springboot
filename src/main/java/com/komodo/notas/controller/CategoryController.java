package com.komodo.notas.controller;

import com.komodo.notas.model.Category;
import com.komodo.notas.service.CategoryService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(Authentication auth) {
        return ResponseEntity.ok(categoryService.getCategories(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<?> createCategory(Authentication auth, @RequestBody CategoryRequest req) {
        try {
            Category cat = categoryService.createCategory(auth.getName(), req.getName(), req.getColor());
            return ResponseEntity.ok(cat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(Authentication auth, @PathVariable Long id, @RequestBody CategoryRequest req) {
        try {
            Category cat = categoryService.updateCategory(auth.getName(), id, req.getName(), req.getColor());
            return ResponseEntity.ok(cat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(Authentication auth, @PathVariable Long id,
                                             @RequestParam(defaultValue = "true") boolean moveNotes) {
        try {
            categoryService.deleteCategory(auth.getName(), id, moveNotes);
            return ResponseEntity.ok("Categoría eliminada");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class CategoryRequest {
        private String name;
        private String color;
    }
}
