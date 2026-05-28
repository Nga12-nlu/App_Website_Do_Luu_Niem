package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.StaticContent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StaticContentDao {

    List<StaticContent> findAll(int page, int pageSize, String search, String groupName);

    int countAll(String search, String groupName);

    List<String> findGroups();

    Optional<StaticContent> findById(int id);

    Optional<StaticContent> findByKey(String key);

    Map<String, String> findActiveMap();

    long countAllItems();

    long countActiveItems();

    void update(StaticContent item);
}
