/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import common.model.Match;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author ASUS
 */
public class MatchDAO {
    
    // Lấy user ID từ username
    public static int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // Lưu match vào database
    public static String saveMatch(String sessionId, String gameMode, String winnerUsername, 
                                   String loserUsername, int winnerScores, int loserScores) {
        String matchId = UUID.randomUUID().toString();
        String sql = "INSERT INTO match_history (id, session_id, gamemode, winner_id, loser_id, " +
                     "winner_scores, loser_scores, start_time, end_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            int winnerId = getUserIdByUsername(winnerUsername);
            int loserId = getUserIdByUsername(loserUsername);
            
            if (winnerId == -1 || loserId == -1) {
                System.err.println("Không tìm thấy user ID cho winner hoặc loser");
                return null;
            }
            
            ps.setString(1, matchId);
            ps.setString(2, sessionId);
            ps.setString(3, gameMode);
            ps.setInt(4, winnerId);
            ps.setInt(5, loserId);
            ps.setInt(6, winnerScores);
            ps.setInt(7, loserScores);
            
            ps.executeUpdate();
            return matchId;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Lấy danh sách match của một user
    public static List<Match> getMatchesByUsername(String username) {
        List<Match> matches = new ArrayList<>();
        int userId = getUserIdByUsername(username);
        if (userId == -1) {
            return matches;
        }
        
        String sql = "SELECT m.*, " +
                     "w.username as winner_username, l.username as loser_username " +
                     "FROM match_history m " +
                     "LEFT JOIN users w ON m.winner_id = w.id " +
                     "LEFT JOIN users l ON m.loser_id = l.id " +
                     "WHERE m.winner_id = ? OR m.loser_id = ? " +
                     "ORDER BY m.start_time DESC";
        
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match match = new Match();
                    match.setId(rs.getString("id"));
                    match.setSessionId(rs.getString("session_id"));
                    match.setGameMode(rs.getString("gamemode"));
                    match.setWinnerId(rs.getInt("winner_id"));
                    match.setLoserId(rs.getInt("loser_id"));
                    match.setWinnerScores(rs.getInt("winner_scores"));
                    match.setLoserScores(rs.getInt("loser_scores"));
                    match.setStartTime(rs.getTimestamp("start_time"));
                    match.setEndTime(rs.getTimestamp("end_time"));
                    matches.add(match);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return matches;
    }
    
    // Lấy thông tin match theo match ID
    public static Match getMatchById(String matchId) {
        String sql = "SELECT * FROM match_history WHERE id = ?";
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Match match = new Match();
                    match.setId(rs.getString("id"));
                    match.setSessionId(rs.getString("session_id"));
                    match.setGameMode(rs.getString("gamemode"));
                    match.setWinnerId(rs.getInt("winner_id"));
                    match.setLoserId(rs.getInt("loser_id"));
                    match.setWinnerScores(rs.getInt("winner_scores"));
                    match.setLoserScores(rs.getInt("loser_scores"));
                    match.setStartTime(rs.getTimestamp("start_time"));
                    match.setEndTime(rs.getTimestamp("end_time"));
                    return match;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy username từ user ID

}

