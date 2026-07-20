package com.komodo.notas.repository;

import com.komodo.notas.model.Category;
import com.komodo.notas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserAndDeletedAtIsNullOrderByCreatedAtAsc(User user);
    Optional<Category> findByIdAndUserAndDeletedAtIsNull(Long id, User user);
}
