/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import gui.LoginWindow;
import javax.swing.SwingUtilities;

/**
 *
 * @author ASUS
 */
public class ClientMain {
    public static void main(String[] args) {
        // Luôn chạy giao diện Swing trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}
