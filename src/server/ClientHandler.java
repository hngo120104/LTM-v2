package server;

import common.model.LoginSuccessData;
import common.model.Match;
import common.model.Request;
import common.model.Response;
import common.model.User;
import common.model.WordsMatch;
import dao.MatchDAO;
import dao.UserDAO;
import dao.WordsMatchDAO;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


public class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User user;
    
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                //Chờ và đọc Request từ client
                Request request = (Request) ois.readObject();
                
                processRequest(request);
            }
        } catch (Exception e) {
            System.out.println("Client " + socket.getInetAddress() + " đã ngắt kết nối.");
            // (Bạn sẽ cần thêm code để xóa user này khỏi danh sách online)
        }
    }
    
    private void processRequest(Request request) {
        String command = request.getCommand();
        
        if (command.equals("LOGIN")) {
            handleLogin((User) request.getData());
        } else if (command.equals("DISCONNECT")) {
            GameManager.getInstance().removeUser(this);
        } else if (command.equals("GET_RANKING")) {
            List<User> ranks = UserDAO.getRankings();
            
            sendResponse(new Response("UPDATE_RANKING", ranks));
        } else if (command.equals("GET_ALL_USERS")) {
            // Lấy tất cả người chơi từ database
            List<User> allUsers = UserDAO.getAllUsers();
            // Cập nhật status cho những người đang online
            for (User dbUser : allUsers) {
                String username = dbUser.getUsername();
                // Kiểm tra xem user này có đang online không
                if (GameManager.getInstance().getOnlineUserList().stream()
                    .anyMatch(u -> u.getUsername().equals(username))) {
                    dbUser.setStatus("Online");
                }
            }
            sendResponse(new Response("UPDATE_ALL_USERS", allUsers));
        } else if (command.equals("GET_MATCH_HISTORY")) {
            // Lấy lịch sử đấu của một người chơi
            String username = (String) request.getData();
            List<Match> matches = MatchDAO.getMatchesByUsername(username);
            
            // Tạo danh sách chứa thông tin match và words
            List<Map<String, Object>> matchHistory = new ArrayList<>();
            for (Match match : matches) {
                Map<String, Object> matchInfo = new HashMap<>();
                matchInfo.put("match", match);
                
                // Lấy các từ của match này
                List<WordsMatch> words = WordsMatchDAO.getWordsByMatchId(match.getId());
                matchInfo.put("words", words);
                
                // Lấy tên người thắng và thua
                String winnerName = UserDAO.getUsernameById(match.getWinnerId());
                String loserName = UserDAO.getUsernameById(match.getLoserId());
                matchInfo.put("winnerName", winnerName);
                matchInfo.put("loserName", loserName);
                
                matchHistory.add(matchInfo);
            }
            
            sendResponse(new Response("MATCH_HISTORY", matchHistory));
        } else if (command.equals("INVITE")) {
            String opponentUsername = (String) request.getData();
            // Yêu cầu GameManager xử lý lời mời
            GameManager.getInstance().processInvite(this.user.getUsername(), opponentUsername);
        } else if (command.equals("INVITE_RESPONSE")) {
            // 1. Dữ liệu gửi lên là String[] {người mời, câu trả lời}
            String[] responseData = (String[]) request.getData();
            String inviterUsername = responseData[0];
            String responseType = responseData[1]; // "ACCEPT" hoặc "REJECT"
            
            // --- THÊM DÒNG NÀY ĐỂ DEBUG ---
            System.out.println("[Handler " + this.user.getUsername() + "] Nhận phản hồi mời: " + 
                           inviterUsername + ", Phản hồi: " + responseType);

            // 2. Yêu cầu GameManager xử lý phản hồi
            GameManager.getInstance().processInviteResponse(
                    this.user.getUsername(), // (this.user là người phản hồi)
                    inviterUsername, 
                    responseType
            );
        } else if (command.equals("SUBMIT_WORD")) {
            String word = (String) request.getData();
            // 1. Tìm game session của user này
            GameSession session = GameManager.getInstance().findGameSessionForPlayer(this.user.getUsername());
            
            // 2. Gửi từ cho session đó xử lý
            if (session != null) {
                session.submitWord(this, word);
            } else {
                // --- THÊM PHẢN HỒI LỖI NÀY ---
                // Điều này xảy ra nếu game đã kết thúc hoặc bị crash
                sendResponse(new Response("INVALID_WORD", "Lỗi: Không tìm thấy phòng game. Vui lòng thử lại."));
            }
        } else if (command.equals("FORFEIT_GAME")) {
            GameSession session = GameManager.getInstance().findGameSessionForPlayer(this.user.getUsername());
            if (session != null) {
                // Yêu cầu GameSession xử lý việc bỏ cuộc
                session.processForfeit(this); // "this" là người chơi bỏ cuộc
            }
        }
    }
    
    private void handleLogin(User userToLogin) {
        // 3. Gọi DAO để kiểm tra CSDL
        User authUser = UserDAO.checkLogin(userToLogin.getUsername(), userToLogin.getPassword());

        if (authUser != null) {
            this.user = authUser;
            
            // (Thêm user này vào GameManager.onlineUsers)
            GameManager.getInstance().addUser(this);
            
            List<User> currentOnlineList = GameManager.getInstance().getOnlineUserList();
            
            LoginSuccessData successData = new LoginSuccessData(this.user, currentOnlineList);
            
            // 5a. Gửi Response thành công về client
            sendResponse(new Response("LOGIN_SUCCESS", successData));
            
            // (Gọi GameManager.broadcastOnlineList() để cập nhật sảnh cho mọi người)
            GameManager.getInstance().broadcastOnlineList();

        } else {
            sendResponse(new Response("LOGIN_FAIL", "Sai tên đăng nhập hoặc mật khẩu!"));
        }
    }
    
    public void sendResponse(Response response) {
        // Kiểm tra xem socket còn "sống" không
        if (socket.isClosed() || !socket.isConnected()) {
            // Ghi log lỗi nhưng không làm gì cả
            System.err.println("[Handler " + (user != null ? user.getUsername() : "Guest") + 
                               "] Lỗi: Cố gửi data cho socket đã đóng.");
            return; 
        }
        
        try {
            synchronized (oos) {
                oos.reset();
                oos.writeObject(response);
                oos.flush();
            }
        } catch (SocketException se) {
            // --- BẮT LỖI NÀY ---
            // Đây chính là lỗi "Connection reset", "Broken pipe", v.v...
            // Lỗi này xảy ra khi client ngắt kết nối đột ngột.
            // Chúng ta chỉ ghi log, KHÔNG ném lỗi ra ngoài.
            System.err.println("[Handler " + (user != null ? user.getUsername() : "Guest") + 
                               "] LỖI GỬI DATA (SocketException): " + se.getMessage());
            // Không làm gì thêm, để cho khối catch(Exception e) ở hàm run()
            // xử lý việc dọn dẹp ClientHandler này.
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Thêm getter này để GameManager có thể lấy thông tin User
    public User getUser() {
        return user;
    }
}
