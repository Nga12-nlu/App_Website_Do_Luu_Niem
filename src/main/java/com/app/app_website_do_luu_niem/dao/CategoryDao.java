package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.Category;
import com.app.app_website_do_luu_niem.model.CategoryAdminRow;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {

    List<Category> findAll();

    List<CategoryAdminRow> findAllAdminRows(int page, int pageSize, String search,
                                            String productFilter, String sortBy, String sortOrder);

    int countAll(String search, String productFilter);

    long countTotal();

    long countWithProducts();

    long countEmpty();

    long countTotalProducts();

    long countProductsByCategoryId(int categoryId);

    boolean nameExistsOtherThan(String name, int excludeId);

    Optional<Category> findById(int id);

    void save(Category category);

    void update(Category category);

    void delete(int id);
}
