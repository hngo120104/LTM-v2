/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common.model;

import java.io.Serializable;

public class User implements Serializable{
    private String username;
    private String password;
    private int totalScore;
    private int totalWins;
    private String status;
   
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public User(String username, int totalScore, int totalWins) {
        this.username = username;
        this.totalScore = totalScore;
        this.totalWins = totalWins;
        this.status = "Offline";
    }
    
    public User(String username, int totalScore, int totalWins, String status) {
        this.username = username;
        this.totalScore = totalScore;
        this.totalWins = totalWins;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getTotalWins() {
        return totalWins;
    }
    
    public String getStatus() {
        return status;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
