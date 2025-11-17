/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common.model;

import java.io.Serializable;

/**
 *
 * @author ASUS
 */
public class WordsMatch implements Serializable {
    private int id;
    private String matchId;
    private int playerId;
    private String word;
    private int score;
    
    public WordsMatch() {
    }
    
    public WordsMatch(String matchId, int playerId, String word, int score) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.word = word;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

