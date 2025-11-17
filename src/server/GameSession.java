/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import common.model.GameResultData;
import common.model.Response;
import common.model.TurnData;
import dao.MatchDAO;
import dao.WordsMatchDAO;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author ASUS
 */
public class GameSession implements Runnable {
    private String sessionId;
    private ClientHandler playerA;
    private ClientHandler playerB;
    
    // --- THÊM CÁC BIẾN TRẠNG THÁI GAME ---
    private final int MAX_TURNS = 3;
    private final int TURN_DURATION_MS = 30000; // 30 giây
    private int scoreA = 0;
    private int scoreB = 0;
    private Set<String> usedWordsThisTurn; // Chống trùng lặp
    private String currentLetters;
    private boolean isTurnActive = false;
    
    // Lưu các từ hợp lệ / không hợp lệ mà mỗi người chơi đã nhập
    private List<String> validWordsA;   // Từ hợp lệ của playerA
    private List<String> invalidWordsA; // Từ không hợp lệ của playerA
    private List<String> validWordsB;   // Từ hợp lệ của playerB
    private List<String> invalidWordsB; // Từ không hợp lệ của playerB
    
    private Thread gameThread;
    
    public GameSession(ClientHandler playerA, ClientHandler playerB) {
        this.sessionId = UUID.randomUUID().toString();
        this.playerA = playerA;
        this.playerB = playerB;
        this.usedWordsThisTurn = new HashSet<>();
        this.validWordsA = new ArrayList<>();
        this.invalidWordsA = new ArrayList<>();
        this.validWordsB = new ArrayList<>();
        this.invalidWordsB = new ArrayList<>();
        System.out.println("GameSession " + sessionId + " đã được tạo cho " + 
                           playerA.getUser().getUsername() + " và " + playerB.getUser().getUsername());
    }

    // Bắt đầu ván game (được gọi bởi GameManager)
    public void startGame() {
        try {
            System.out.println("[GameSession] Bắt đầu hàm startGame()...");
            
            // Kiểm tra null (để chắc chắn)
            if (playerA == null || playerB == null || playerA.getUser() == null || playerB.getUser() == null) {
                System.out.println("[GameSession LỖI] Một trong các người chơi/user bị null. Hủy game.");
                return;
            }

            System.out.println("[GameSession] Đang gửi GAME_START cho " + playerA.getUser().getUsername());
            playerA.sendResponse(new Response("GAME_START", playerB.getUser()));
            
            System.out.println("[GameSession] Đang gửi GAME_START cho " + playerB.getUser().getUsername());
            playerB.sendResponse(new Response("GAME_START", playerA.getUser()));

            System.out.println("[GameSession] Đã gửi xong. Bắt đầu luồng game (run()).");
            
            gameThread = new Thread(this); // <-- Lưu lại Thread
            gameThread.start();
            
        } catch (Exception e) {
            // Đây là nơi bắt lỗi. Nếu bạn thấy log này, lỗi là do sendResponse
            System.out.println("[GameSession LỖI NGHIÊM TRỌNG] Exception trong hàm startGame():");
            e.printStackTrace();
        }
    }
    
    // --- HÀM MỚI: Gửi tin nhắn cho cả 2 người chơi ---
    private void broadcastToPlayers(Response response) {
        playerA.sendResponse(response);
        playerB.sendResponse(response);
    }
    
    // --- HÀM MỚI: Tạo 7 chữ cái ngẫu nhiên ---
    private String generateLetters() {
        String vowels = "AEIOU";
        String consonants = "BCDGHKLMNPQRSTVXY";

        List<Character> vowelList = new ArrayList<>();
        for (char c : vowels.toCharArray()) {
            vowelList.add(c);
        }

        List<Character> consonantList = new ArrayList<>();
        for (char c : consonants.toCharArray()) {
            consonantList.add(c);
        }

        Collections.shuffle(vowelList, ThreadLocalRandom.current());
        Collections.shuffle(consonantList, ThreadLocalRandom.current());

        List<Character> charList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            charList.add(vowelList.get(i));
        }
        for (int i = 0; i < 4; i++) {
            charList.add(consonantList.get(i));
        }
        Collections.shuffle(charList, ThreadLocalRandom.current());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            sb.append(charList.get(i));
            sb.append("/");
        }

        String initChars = sb.toString();
        return initChars.substring(0, initChars.length() - 1);
    }
    
    @Override
    public void run() {
        System.out.println("Game " + sessionId + " bắt đầu!");
        try {
            // Chờ 2 giây để client mở cửa sổ
            Thread.sleep(2000); 

            for (int turn = 1; turn <= MAX_TURNS; turn++) {
                // 1. Bắt đầu lượt
                currentLetters = generateLetters();
                usedWordsThisTurn.clear();
                isTurnActive = true;
                
                // Tạo TurnData mới
                TurnData turnData = new TurnData(
                        currentLetters, 
                        turn, 
                        MAX_TURNS, 
                        TURN_DURATION_MS / 1000 // Chuyển 15000ms thành 15s
                );
                
                broadcastToPlayers(new Response("NEW_TURN", turnData));
                System.out.println("[Game " + sessionId + "] Lượt " + turn + " với chữ: " + currentLetters);

                // 2. Chờ hết giờ
                Thread.sleep(TURN_DURATION_MS);

                // 3. Kết thúc lượt
                isTurnActive = false;
                broadcastToPlayers(new Response("TURN_END", null));
                
                if (turn < MAX_TURNS) {
                    Thread.sleep(2000);
                }
            }
            
            // 4. Kết thúc game
            String resultMessage = "Kết quả chung cuộc: " + 
                    playerA.getUser().getUsername() + ": " + scoreA + " | " + 
                    playerB.getUser().getUsername() + ": " + scoreB;

            // Tạo dữ liệu kết quả riêng cho từng người chơi
            GameResultData resultA = new GameResultData(
                    resultMessage,
                    new ArrayList<>(validWordsA),
                    new ArrayList<>(invalidWordsA)
            );
            GameResultData resultB = new GameResultData(
                    resultMessage,
                    new ArrayList<>(validWordsB),
                    new ArrayList<>(invalidWordsB)
            );

            playerA.sendResponse(new Response("GAME_OVER", resultA));
            playerB.sendResponse(new Response("GAME_OVER", resultB));
            

        } catch (InterruptedException e) {
            System.out.println("Game " + sessionId + " bị gián đoạn.");
        } catch (Exception e) {
            // --- RẤT QUAN TRỌNG: Bắt mọi lỗi khác (ví dụ: NullPointerException) ---
            System.err.println("[GameSession LỖI NGHIÊM TRỌNG] Thread game bị crash:");
            e.printStackTrace();
            broadcastToPlayers(new Response("GAME_OVER", "Lỗi server, game kết thúc."));
        } finally {
            // --- LƯU LỊCH SỬ ĐẤU VÀ CÁC TỪ VÀO DATABASE ---
            saveMatchHistory();
            
            // --- RẤT QUAN TRỌNG: Dọn dẹp game ---
            // Đảm bảo game luôn được xóa khỏi GameManager dù kết thúc bình thường hay bị lỗi
            GameManager.getInstance().endGame(sessionId, 
                    playerA.getUser(), 
                    playerB.getUser(),
                    scoreA,
                    scoreB
            );
        }
    }
    
    // (Thêm các hàm xử lý game, ví dụ: submitWord, broadcastToPlayers,...)
    // --- HÀM MỚI: Xử lý khi người chơi nộp từ ---
    
    public synchronized void submitWord(ClientHandler player, String word) {
        try {
            // 1. Kiểm tra xem lượt chơi còn không
            if (!isTurnActive) {
                registerInvalidWord(player, word, "Đã hết giờ!");
                return;
            }

            String lowerWord = word.toLowerCase();

            // 2. Kiểm tra trùng lặp
            if (usedWordsThisTurn.contains(lowerWord)) {
                registerInvalidWord(player, word, "Từ này đã được dùng!");
                return;
            }

            // 3. Kiểm tra từ điển
            if (!Dictionary.isValid(word)) {
                registerInvalidWord(player, word, "Không có trong từ điển!");
                return;
            }

            // 4. Kiểm tra chữ cái
            if (!Dictionary.isValidFromLetters(word, currentLetters)) {
                registerInvalidWord(player, word, "Sai chữ cái cho phép!");
                return;
            }

            // 5. HỢP LỆ!
            usedWordsThisTurn.add(lowerWord);

            if (player == playerA) {
                validWordsA.add(word);
                scoreA += 2; // +2 điểm cho từ hợp lệ
            } else {
                validWordsB.add(word);
                scoreB += 2; // +2 điểm cho từ hợp lệ
            }

            // 6. Gửi cập nhật điểm
            playerA.sendResponse(new Response("UPDATE_SCORE", new int[]{scoreA, scoreB}));
            playerB.sendResponse(new Response("UPDATE_SCORE", new int[]{scoreB, scoreA}));

            // Không cần popup ngay, chỉ gửi thông báo đơn giản nếu muốn
            // player.sendResponse(new Response("VALID_WORD", word));
        } catch (Exception e) {
            System.err.println("[GameSession LỖI] Lỗi trong submitWord:");
            e.printStackTrace();
        }
    }

    // Đăng ký một từ không hợp lệ: lưu lại và trừ điểm
    private void registerInvalidWord(ClientHandler player, String word, String reason) {
        if (player == playerA) {
            invalidWordsA.add(word);
            scoreA -= 1; // -1 điểm cho từ sai
        } else {
            invalidWordsB.add(word);
            scoreB -= 1; // -1 điểm cho từ sai
        }

        // Gửi cập nhật điểm
        playerA.sendResponse(new Response("UPDATE_SCORE", new int[]{scoreA, scoreB}));
        playerB.sendResponse(new Response("UPDATE_SCORE", new int[]{scoreB, scoreA}));

        // Gửi lý do sai (client sẽ không popup ngay nữa)
        player.sendResponse(new Response("INVALID_WORD", reason));
    }
    
    // --- HÀM MỚI: XỬ LÝ BỎ CUỘC ---
    public synchronized void processForfeit(ClientHandler forfeiter) {
        // Kiểm tra xem game còn hoạt động không
        if (gameThread == null || !gameThread.isAlive()) {
            return; // Game đã kết thúc rồi
        }
        
        System.out.println("[Game " + sessionId + "] User " + forfeiter.getUser().getUsername() + " đã bỏ cuộc.");
        
        ClientHandler winner;
        String resultMessage;
        
        // 1. Xác định thắng thua (5-0)
        if (forfeiter == playerA) {
            winner = playerB;
            scoreA = 0;
            scoreB = 5;
            // Xóa các từ của playerA khi bỏ cuộc
            validWordsA.clear();
            invalidWordsA.clear();
        } else {
            winner = playerA;
            scoreA = 5;
            scoreB = 0;
            // Xóa các từ của playerB khi bỏ cuộc
            validWordsB.clear();
            invalidWordsB.clear();
        }
        
        // 2. Tạo thông báo
        resultMessage = forfeiter.getUser().getUsername() + " đã thoát.\n" +
                        winner.getUser().getUsername() + " thắng 5-0.";
                        
        // 3. Gửi kết quả cuối cùng (không kèm danh sách từ, vì người bỏ cuộc)
        broadcastToPlayers(new Response("GAME_OVER", resultMessage));
        
        // 4. Ngắt (interrupt) luồng game (để nó dừng Thread.sleep)
        gameThread.interrupt();
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    // Lưu lịch sử đấu và các từ vào database
    private void saveMatchHistory() {
        try {
            String winnerUsername, loserUsername;
            int winnerScore, loserScore;
            
            if (scoreA > scoreB) {
                winnerUsername = playerA.getUser().getUsername();
                loserUsername = playerB.getUser().getUsername();
                winnerScore = scoreA;
                loserScore = scoreB;
            } else if (scoreB > scoreA) {
                winnerUsername = playerB.getUser().getUsername();
                loserUsername = playerA.getUser().getUsername();
                winnerScore = scoreB;
                loserScore = scoreA;
            } else {
                // Hòa - lưu người có điểm cao hơn làm winner (hoặc playerA)
                winnerUsername = playerA.getUser().getUsername();
                loserUsername = playerB.getUser().getUsername();
                winnerScore = scoreA;
                loserScore = scoreB;
            }
            
            // Lưu match vào database
            String matchId = MatchDAO.saveMatch(sessionId, "GAME_MODE_1", 
                    winnerUsername, loserUsername, winnerScore, loserScore);
            
            if (matchId != null) {
                // Lưu các từ hợp lệ của playerA
                for (String word : validWordsA) {
                    WordsMatchDAO.saveWord(matchId, playerA.getUser().getUsername(), word, 2);
                }
                
                // Lưu các từ hợp lệ của playerB
                for (String word : validWordsB) {
                    WordsMatchDAO.saveWord(matchId, playerB.getUser().getUsername(), word, 2);
                }
                
                System.out.println("[GameSession] Đã lưu lịch sử đấu: " + matchId);
            }
        } catch (Exception e) {
            System.err.println("[GameSession] Lỗi khi lưu lịch sử đấu:");
            e.printStackTrace();
        }
    }
    
}
