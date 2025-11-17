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
public class TurnData implements Serializable{
    private String letters;
    private int currentTurn;
    private int maxTurns;
    private int turnDurationSeconds;
    
    public TurnData(String letters, int currentTurn, int maxTurns, int turnDurationSeconds) {
        this.letters = letters;
        this.currentTurn = currentTurn;
        this.maxTurns = maxTurns;
        this.turnDurationSeconds = turnDurationSeconds;
    }

    public String getLetters() {
        return letters;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTurnDurationSeconds() {
        return turnDurationSeconds;
    }
    
}
