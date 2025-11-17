/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;

/**
 *
 * @author ASUS
 */
public class DAO {
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/word_game";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "hoang12124";
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // 1. Nạp (load) driver MySQL
        Class.forName(DB_DRIVER);
        
        // 2. Lấy kết nối bằng DriverManager
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
