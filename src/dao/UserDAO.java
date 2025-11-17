/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import common.model.User;
import java.sql.*;
import java.util.*;

public class UserDAO {
    public static User checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                
                if (rs.next()) {
                    
                    String dbUsername = rs.getString("username");
                    int totalScore = rs.getInt("total_score");
                    int totalWins = rs.getInt("total_wins");
                    
                    // Tạo đối tượng User để trả về
                    // (Mặc định status là "Rỗi" khi mới login)
                    return new User(dbUsername, totalScore, totalWins, "Rỗi");
                    
                } else {
                    return null;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            // Xảy ra lỗi (ví dụ: không kết nối được DB, sập server, sai tên cột...)
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi
        }
    }
    
    public static List<User> getRankings() {
        List<User> rankings = new ArrayList<>();
        String sql = "SELECT username, total_score, total_wins FROM users " +
                     "ORDER BY total_score DESC, total_wins DESC LIMIT 100";
        
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                rankings.add(new User(
                    rs.getString("username"),
                    rs.getInt("total_score"),
                    rs.getInt("total_wins")
                ));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rankings;
    }
    
    public static void updateGameResult(String username, int score, boolean isWinner) {
        int winToAdd = isWinner ? 1 : 0;
        
        String sql = "UPDATE users SET total_score = total_score + ?, total_wins = total_wins + ? WHERE username = ?";
        
        try(
           Connection conn = DAO.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql);
        ) {
           ps.setInt(1, score);
           ps.setInt(2, winToAdd);
           ps.setString(3, username);
           
           ps.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // Lấy tất cả người chơi từ database
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, total_score, total_wins FROM users ORDER BY total_score DESC, total_wins DESC";
        
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                users.add(new User(
                    rs.getString("username"),
                    rs.getInt("total_score"),
                    rs.getInt("total_wins"),
                    "Offline"
                ));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static String getUsernameById(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (
                Connection conn = DAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
