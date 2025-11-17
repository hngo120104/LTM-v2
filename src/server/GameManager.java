/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import common.model.Response;
import common.model.User;
import dao.UserDAO;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ASUS
 */
public class GameManager {
    private static GameManager instance;
    
    // Dùng ConcurrentHashMap để an toàn khi nhiều Thread (ClientHandler)
    // cùng truy cập vào danh sách online
    // Key: Tên username, Value: Đối tượng ClientHandler
    private Map<String, ClientHandler> onlineUsers;
    
    //Quản lý các phòng game đang chạy
    private Map<String, GameSession> activeGames;
    
    //Để tra cứu nhanh game của người chơi
    private Map<String, String> playerToSessionMap;

    private GameManager() {
        onlineUsers = new ConcurrentHashMap<>();
        activeGames = new ConcurrentHashMap<>();
        playerToSessionMap = new ConcurrentHashMap<>();
    }
    
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    // --- CHỨC NĂNG 1: QUẢN LÝ DANH SÁCH ONLINE ---

    // Khi ClientHandler login thành công
    public void addUser(ClientHandler handler) {
        onlineUsers.put(handler.getUser().getUsername(), handler);
        System.out.println("User " + handler.getUser().getUsername() + " đã online. Tổng số: " + onlineUsers.size());
        
        // Cập nhật danh sách cho tất cả mọi người
        broadcastOnlineList();
    }
    
    // Khi ClientHandler ngắt kết nối
    public void removeUser(ClientHandler handler) {
        String usernameToRemove = null;
        
        if (onlineUsers.isEmpty()) {
            System.out.println("[GameManager] Danh sách online đã rỗng, không cần xóa.");
            broadcastOnlineList();
            return;
        }

        // Duyệt qua Map để tìm Key (username) dựa trên Value (handler)
        for (Map.Entry<String, ClientHandler> entry : onlineUsers.entrySet()) {
            if (entry.getValue() == handler) { 
                usernameToRemove = entry.getKey();
                break;
            }
        }

        if (usernameToRemove != null) {
            onlineUsers.remove(usernameToRemove); // Xóa khỏi Map
            System.out.println("[GameManager] User " + usernameToRemove + " ĐÃ BỊ XÓA. Tổng online còn lại: " + onlineUsers.size());
        } else {
            // Trường hợp này xảy ra khi client kết nối rồi ngắt ngay
            // mà chưa kịp đăng nhập (nên không có trong list)
            System.out.println("[GameManager] Một client (chưa đăng nhập) đã ngắt kết nối, không cần xóa khỏi list.");
        }
        
        // Luôn luôn phát sóng danh sách mới cho những người còn lại
        System.out.println("[GameManager] Đang phát sóng danh sách mới cho " + onlineUsers.size() + " người còn lại.");
        broadcastOnlineList();
    }
    
    // Gửi danh sách online cho TẤT CẢ client
    public void broadcastOnlineList() {
        System.out.println("[GameManager] Cập nhật lại danh sách");
        // 1. Tạo một danh sách User (chỉ thông tin, không phải handler)
        List<User> userList = getOnlineUserList();
        
        // 2. Tạo Response
        Response response = new Response("UPDATE_LIST", userList);
        
        // 3. Gửi cho mọi người
        for (ClientHandler handler : onlineUsers.values()) {
            handler.sendResponse(response);
        }
    }
    
    public void processInvite(String inviterUsername, String opponentUsername) {
        ClientHandler opponentHandler = onlineUsers.get(opponentUsername);
        
        // Tìm người gửi (để gửi phản hồi nếu thất bại)
        ClientHandler inviterHandler = onlineUsers.get(inviterUsername);

        // Kiểm tra
        if (opponentHandler == null) {
            inviterHandler.sendResponse(new Response("INVITE_FAIL", "Người chơi đã offline."));
            return;
        }
        if (opponentHandler.getUser().getStatus().equals("Bận")) {
            inviterHandler.sendResponse(new Response("INVITE_FAIL", "Người chơi đang bận."));
            return;
        }

        // Mọi thứ OK, gửi lời mời cho đối thủ
        Response inviteResponse = new Response("NEW_INVITE", inviterUsername);
        opponentHandler.sendResponse(inviteResponse);
    }
    
    public void processInviteResponse(String responderUsername, String inviterUsername, String responseType) {
        
        ClientHandler inviterHandler = onlineUsers.get(inviterUsername);
        ClientHandler responderHandler = onlineUsers.get(responderUsername);

        if (inviterHandler == null || responderHandler == null) {
            System.out.println("Một trong hai người chơi đã offline, hủy lời mời.");
            return;
        }

        if (responseType.equals("ACCEPT")) {
            // --- Chấp nhận ---
            System.out.println(responderUsername + " chấp nhận lời mời từ " + inviterUsername);
            createGame(inviterHandler, responderHandler);
            
        } else {
            // --- Từ chối ---
            System.out.println(responderUsername + " từ chối lời mời từ " + inviterUsername);
            inviterHandler.sendResponse(new Response("INVITE_REJECTED", 
                    responderUsername + " đã từ chối lời mời."));
        }
    }
    
    // --- HÀM MỚI: Tạo game ---

    private void createGame(ClientHandler playerA, ClientHandler playerB) {
        // --- THÊM DÒNG NÀY ĐỂ DEBUG ---
        System.out.println("[GameManager] Đang tạo game cho: " + 
                       playerA.getUser().getUsername() + " và " + playerB.getUser().getUsername());
        // 1. Đặt trạng thái 2 người chơi là "Bận"
        playerA.getUser().setStatus("Bận");
        playerB.getUser().setStatus("Bận");
        
        // 2. Tạo GameSession mới
        GameSession newSession = new GameSession(playerA, playerB);
        activeGames.put(newSession.getSessionId(), newSession);
        playerToSessionMap.put(playerA.getUser().getUsername(), newSession.getSessionId());
        playerToSessionMap.put(playerB.getUser().getUsername(), newSession.getSessionId());

        newSession.startGame();
        broadcastOnlineList();
    }
    
    // --- HÀM MỚI ---
    /**
     * Tìm GameSession mà người chơi đang tham gia
     */
    public GameSession findGameSessionForPlayer(String username) {
        String sessionId = playerToSessionMap.get(username);
        if (sessionId != null) {
            return activeGames.get(sessionId);
        }
        return null;
    }
    
    // --- HÀM MỚI ---
    /**
     * Xóa game khi kết thúc
     */
    public void endGame(String sessionId, User userA, User userB, int scoreA, int scoreB) {
        System.out.println("[GameManager] Đang kết thúc game: " + sessionId);
        
        boolean aWon = scoreA > scoreB;
        boolean bWon = scoreB > scoreA;
        
        UserDAO.updateGameResult(userA.getUsername(), scoreA, aWon);
        UserDAO.updateGameResult(userB.getUsername(), scoreB, bWon);
        
        activeGames.remove(sessionId);
        playerToSessionMap.remove(userA.getUsername());
        playerToSessionMap.remove(userB.getUsername());
        
        // (Bạn có thể set status của user về "Rỗi" và broadcast ở đây)
        ClientHandler handlerA = onlineUsers.get(userA.getUsername());
        ClientHandler handlerB = onlineUsers.get(userB.getUsername());
        
        if (handlerA != null && handlerA.getUser() != null) {
            User inMemoryUserA = handlerA.getUser();
            inMemoryUserA.setStatus("Rỗi");
            inMemoryUserA.setTotalScore(inMemoryUserA.getTotalScore() + scoreA);
            if (aWon) {
                inMemoryUserA.setTotalWins(inMemoryUserA.getTotalWins() + 1);
            }
        }
        if (handlerB != null && handlerB.getUser() != null) {
            User inMemoryUserB = handlerB.getUser();
            inMemoryUserB.setStatus("Rỗi");
            inMemoryUserB.setTotalScore(inMemoryUserB.getTotalScore() + scoreB);
            if (bWon) {
                inMemoryUserB.setTotalWins(inMemoryUserB.getTotalWins() + 1);
            }
        }
        
        //Phát sóng danh sách mới cho TẤT CẢ mọi người
        broadcastOnlineList();
    }
    
    public List<User> getOnlineUserList() {
        List<User> userList = new ArrayList<>();
        // Dùng values() để duyệt qua các ClientHandler
        for (ClientHandler handler : onlineUsers.values()) {
            userList.add(handler.getUser());
        }
        return userList;
    }
}
