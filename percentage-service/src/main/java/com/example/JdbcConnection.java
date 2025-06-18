package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/disysdb?user=disysuser&password=disyspw";

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}