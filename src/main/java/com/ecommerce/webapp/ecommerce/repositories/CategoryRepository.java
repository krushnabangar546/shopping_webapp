package com.ecommerce.webapp.ecommerce.repositories;

import com.ecommerce.webapp.ecommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    public Boolean existsByName (String name);

    public List<Category> findByIsActiveTrue();
}
