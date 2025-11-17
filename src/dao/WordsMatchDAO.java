/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import common.model.WordsMatch;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class WordsMatchDAO {
    
    // Lưu từ vào database
    public static void saveWord(String matchId, String username, String word, int score) {
        int playerId = MatchDAO.getUserIdByUsername(username);
        if (playerId == -1) {
            System.err.println("Không tìm thấy user ID cho: " + username);
            return;
        }
        
        String sql = "INSERT INTO match_words (match_id, player_id, word, score) VALUES (?, ?, ?, ?)";
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, matchId);
            ps.setInt(2, playerId);
            ps.setString(3, word);
            ps.setInt(4, score);
            
            ps.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    // Lấy tất cả từ của một match
    public static List<WordsMatch> getWordsByMatchId(String matchId) {
        List<WordsMatch> words = new ArrayList<>();
        String sql = "SELECT mw.*, u.username " +
                     "FROM match_words mw " +
                     "JOIN users u ON mw.player_id = u.id " +
                     "WHERE mw.match_id = ? " +
                     "ORDER BY mw.id";
        
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WordsMatch wm = new WordsMatch();
                    wm.setId(rs.getInt("id"));
                    wm.setMatchId(rs.getString("match_id"));
                    wm.setPlayerId(rs.getInt("player_id"));
                    wm.setWord(rs.getString("word"));
                    wm.setScore(rs.getInt("score"));
                    words.add(wm);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return words;
    }
    
    // Lấy từ của một player trong một match
    public static List<WordsMatch> getWordsByMatchIdAndPlayer(String matchId, String username) {
        List<WordsMatch> words = new ArrayList<>();
        int playerId = MatchDAO.getUserIdByUsername(username);
        if (playerId == -1) {
            return words;
        }
        
        String sql = "SELECT * FROM match_words WHERE match_id = ? AND player_id = ? ORDER BY id";
        try (
            Connection conn = DAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, matchId);
            ps.setInt(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WordsMatch wm = new WordsMatch();
                    wm.setId(rs.getInt("id"));
                    wm.setMatchId(rs.getString("match_id"));
                    wm.setPlayerId(rs.getInt("player_id"));
                    wm.setWord(rs.getString("word"));
                    wm.setScore(rs.getInt("score"));
                    words.add(wm);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return words;
    }
}

