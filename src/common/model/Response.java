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
public class Response implements Serializable{
    private String command;
    private Object data;
    
    public Response(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }
    
    
}
