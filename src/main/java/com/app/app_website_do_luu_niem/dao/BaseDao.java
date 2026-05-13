package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.config.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDao {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
}


