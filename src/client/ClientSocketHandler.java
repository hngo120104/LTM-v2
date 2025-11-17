/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import common.model.GameResultData;
import common.model.LoginSuccessData;
import common.model.Request;
import common.model.Response;
import common.model.TurnData;
import common.model.User;
import gui.GameWindow;
import gui.LobbyWindow;
import gui.LoginWindow;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 *
 * @author ASUS
 */
public class ClientSocketHandler {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    
    // Tham chiếu đến các cửa sổ GUI
    private LoginWindow loginWindow;
    private LobbyWindow lobbyWindow;
    private User currentUser;
    private GameWindow gameWindow;

    public ClientSocketHandler(LoginWindow loginWindow) {
        this.loginWindow = loginWindow;
        connectToServer();
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234); // Host và Port của Server
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // 2. Khởi động 1 luồng riêng chỉ để "Lắng nghe" Server
            new Thread(this::listenToServer).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(loginWindow, 
                "Không thể kết nối tới server.", "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void listenToServer() {
        try {
            while (true) {
                // 6. Chờ và đọc Response từ Server (luồng bị block ở đây)
                Response response = (Response) ois.readObject();
                
                // 7. Xử lý Response (trên 1 luồng riêng)
                processResponse(response);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Mất kết nối server!");
        }
    }
    
    private void processResponse(Response response) {
        String command = response.getCommand();
        
        // ⚠️ RẤT QUAN TRỌNG:
        // Mọi cập nhật cho GUI (Swing) phải được thực hiện
        // trên luồng Event Dispatch Thread (EDT)
        // Dùng SwingUtilities.invokeLater để làm điều đó
        
        SwingUtilities.invokeLater(() -> {
            if (command.equals("LOGIN_SUCCESS")) {
                LoginSuccessData data = (LoginSuccessData) response.getData();
                
                this.currentUser = (User) data.getCurrentUser();
                List<User> initialOnlineList = data.getOnlineList();
                
                // 9a. Đăng nhập thành công
                loginWindow.dispose(); // Đóng cửa sổ Login
                
                // Mở cửa sổ Sảnh chờ (Lobby)
                lobbyWindow = new LobbyWindow(this, this.currentUser); 
                lobbyWindow.updateOnlineList(initialOnlineList);
                lobbyWindow.setVisible(true);
                // (Bạn có thể gọi lobbyWindow.updateUserInfo((User) response.getData());)

            } else if (command.equals("LOGIN_FAIL")) {
                // 9b. Đăng nhập thất bại
                String errorMessage = (String) response.getData();
                JOptionPane.showMessageDialog(loginWindow, 
                    errorMessage, "Đăng Nhập Thất Bại", JOptionPane.ERROR_MESSAGE);
            }
            else if (command.equals("UPDATE_LIST")) {
                // 4. Nhận danh sách online mới từ Server
                List<User> userList = (List<User>) response.getData();
                if (lobbyWindow != null) {
                    lobbyWindow.updateOnlineList(userList);
                    
                    if (currentUser != null) {
                        for (User user : userList) {
                            if (user.getUsername().equals(currentUser.getUsername())) {
                                
                                currentUser.setTotalScore(user.getTotalScore());
                                currentUser.setTotalWins(user.getTotalWins());
                                
                                lobbyWindow.updateCurrentUserInfo(this.currentUser); 
                                break;
                            }
                        }
                    }
                    
                }
            } else if (command.equals("NEW_INVITE")) {
                // 5. Nhận lời mời mới
                String inviterUsername = (String) response.getData();
                if (lobbyWindow != null) {
                    lobbyWindow.showInvitePopup(inviterUsername);
                }
            
            } else if (command.equals("INVITE_FAIL")) {
                // 6. Xử lý khi mời thất bại
                String message = (String) response.getData();
                JOptionPane.showMessageDialog(lobbyWindow, message, "Lỗi Mời", JOptionPane.WARNING_MESSAGE);
            } else if (command.equals("INVITE_REJECTED")) {
                // --- MỚI: Xử lý khi bị từ chối ---
                String message = (String) response.getData();
                JOptionPane.showMessageDialog(lobbyWindow, message, "Bị Từ Chối", JOptionPane.INFORMATION_MESSAGE);
                
            } else if (command.equals("GAME_START")) {
                // --- MỚI: Bắt đầu game ---
                System.out.println("Nhận được lệnh GAME_START");
                
                // 1. Lấy thông tin đối thủ
                User opponentUser = (User) response.getData();
                
                // 2. Ẩn sảnh chờ
                if (lobbyWindow != null) {
                    lobbyWindow.setVisible(false);
                }
                
                // 3. Tạo và hiển thị cửa sổ game
                gameWindow = new GameWindow(this, this.currentUser, opponentUser);
                gameWindow.setVisible(true);
            }
            else if (command.equals("NEW_TURN")) {
                if (gameWindow != null) {
                    gameWindow.displayNewTurn((TurnData) response.getData());
                }
            } else if (command.equals("TURN_END")) {
                if (gameWindow != null) {
                    gameWindow.displayTurnEnd();
                }
            } else if (command.equals("UPDATE_SCORE")) {
                if (gameWindow != null) {
                    int[] scores = (int[]) response.getData();
                    gameWindow.updateScores(scores[0], scores[1]); // [điểm mình, điểm đối thủ]
                }
            } // --- THÊM ELSE IF NÀY ---
            else if (command.equals("VALID_WORD")) {
                if (gameWindow != null) {
                    // Hiện tại không cần hiển thị popup cho từ hợp lệ
                    gameWindow.showValidWord((String) response.getData());
                }
            }
            else if (command.equals("INVALID_WORD")) {
                if (gameWindow != null) {
                    // Không popup, chỉ log trong GameWindow
                    gameWindow.showInvalidWord((String) response.getData());
                }
            } else if (command.equals("GAME_OVER")) {
                if (gameWindow != null) {
                    Object data = response.getData();
                    if (data instanceof GameResultData) {
                        gameWindow.showGameOver((GameResultData) data);
                    } else if (data instanceof String) {
                        // Trường hợp cũ (ví dụ khi bỏ cuộc)
                        JOptionPane.showMessageDialog(gameWindow, (String) data,
                                "Trận Đấu Kết Thúc", JOptionPane.INFORMATION_MESSAGE);
                        gameWindow.dispose();
                    }
                    gameWindow = null; // Hủy cửa sổ game
                    
                    // Mở lại sảnh chờ
                    if (lobbyWindow != null) {
                        lobbyWindow.setVisible(true);
                        
                        sendRequest(new Request("GET_RANKING", null));
                    } else {
                        // Trường hợp dự phòng nếu lobby bị null
                        System.err.println("Lỗi: LobbyWindow bị null, tạo lại.");
                        lobbyWindow = new LobbyWindow(this, this.currentUser);
                        lobbyWindow.setVisible(true);
                        // (Lưu ý: Trường hợp này có thể vẫn bị lỗi rỗng list,
                        // nhưng nó giúp chương trình không bị crash)
                    }
                    // (Bạn nên gửi 1 request "GET_LOBBY_DATA" để lấy list mới)
                }
            } else if (command.equals("UPDATE_RANKING")) {
                if (lobbyWindow != null) {
                    List<User> rankingList = (List<User>) response.getData();
                    lobbyWindow.updateRankingTable(rankingList);
                }
            } else if (command.equals("UPDATE_ALL_USERS")) {
                if (lobbyWindow != null) {
                    List<User> allUsers = (List<User>) response.getData();
                    lobbyWindow.updateAllUsersList(allUsers);
                }
            } else if (command.equals("MATCH_HISTORY")) {
                if (lobbyWindow != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> matchHistory = (List<Map<String, Object>>) response.getData();
                    // Lấy username đã lưu
                    String username = lobbyWindow.getSelectedUsernameForHistory();
                    if (username != null) {
                        lobbyWindow.showMatchHistory(matchHistory, username);
                    } else {
                        JOptionPane.showMessageDialog(lobbyWindow, 
                            "Không thể xác định người chơi. Vui lòng chọn lại.");
                    }
                }
            }
        });
    }

    // 4. Hàm để GUI gọi khi muốn "Gửi" Request
    public void sendRequest(Request request) {
        try {
            oos.writeObject(request); // 5. Gửi đối tượng Request
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void closeConnection() {
        Request request = new Request("DISCONNECT", null);
        try {
            sendRequest(request);
            System.out.println("Disconnected to server");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
