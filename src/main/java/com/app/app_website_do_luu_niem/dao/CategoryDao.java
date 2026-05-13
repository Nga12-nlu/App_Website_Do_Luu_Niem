package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {

    List<Category> findAll();

    Optional<Category> findById(int id);

    void save(Category category);

    void update(Category category);

    void delete(int id);
}


