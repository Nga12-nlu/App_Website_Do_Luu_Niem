package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.SystemLog;
import java.util.List;

public interface SystemLogDao {
    void save(SystemLog log);
    List<SystemLog> findAll(int page, int pageSize);
    int countAll();
}
