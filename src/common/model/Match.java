/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author ASUS
 */
public class Match implements Serializable {
    private String id;
    private String sessionId;
    private String gameMode;
    private Integer winnerId;
    private Integer loserId;
    private Integer winnerScores;
    private Integer loserScores;
    private Timestamp startTime;
    private Timestamp endTime;
    
    public Match() {
    }
    
    public Match(String id, String sessionId, String gameMode, Integer winnerId, Integer loserId, 
                 Integer winnerScores, Integer loserScores, Timestamp startTime, Timestamp endTime) {
        this.id = id;
        this.sessionId = sessionId;
        this.gameMode = gameMode;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.winnerScores = winnerScores;
        this.loserScores = loserScores;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public Integer getLoserId() {
        return loserId;
    }

    public void setLoserId(Integer loserId) {
        this.loserId = loserId;
    }

    public Integer getWinnerScores() {
        return winnerScores;
    }

    public void setWinnerScores(Integer winnerScores) {
        this.winnerScores = winnerScores;
    }

    public Integer getLoserScores() {
        return loserScores;
    }

    public void setLoserScores(Integer loserScores) {
        this.loserScores = loserScores;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}


