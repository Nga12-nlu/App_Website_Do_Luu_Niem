package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {

    List<Product> findAll(int page, int size, String keyword, Integer categoryId, String sort);

    int countAll(String keyword, Integer categoryId);

    Optional<Product> findById(int id);

    void save(Product product);

    void update(Product product);

    void delete(int id);
}


