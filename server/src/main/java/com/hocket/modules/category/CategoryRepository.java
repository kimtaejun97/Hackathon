package com.hocket.modules.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface CategoryRepository extends JpaRepository<Category, Long>{
    Category findByTitle(String title);

    boolean existsByTitle(String categoryTitle);
}
