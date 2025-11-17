/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class GameResultData implements Serializable {
    private String finalMessage;
    private List<String> validWords;
    private List<String> invalidWords;

    public GameResultData(String finalMessage, List<String> validWords, List<String> invalidWords) {
        this.finalMessage = finalMessage;
        this.validWords = validWords;
        this.invalidWords = invalidWords;
    }

    public String getFinalMessage() {
        return finalMessage;
    }

    public List<String> getValidWords() {
        return validWords;
    }

    public List<String> getInvalidWords() {
        return invalidWords;
    }
}



