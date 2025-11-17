/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author ASUS
 */
public class ServerMain {
    
    public static final int PORT = 1234;
    
    public static void main(String[] args) {
        
        try {
            System.out.println("Đang khởi tạo server...");
            Class.forName("server.Dictionary"); 
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Lỗi: Không tìm thấy lớp Dictionary, server không thể chạy.");
            return; // Dừng server nếu không nạp được từ điển
        }
        
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT + "...");
            
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connect: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
