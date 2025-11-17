package gui;

import client.ClientSocketHandler;
import common.model.Request;
import common.model.TurnData;
import common.model.User;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.DefaultListModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ASUS
 */
public class GameWindow extends JFrame{
    private ClientSocketHandler socketHandler;
    private User currentUser;
    private User opponentUser;
    
    private JLabel lblStatus;
    private JLabel lblPlayerScore;
    private JLabel lblOpponentScore;
    private JTextField txtWordInput;
    private JButton btnSend;
    
    private JLabel lblTurnInfo; // Hiển thị "Lượt 5/10"
    private JLabel lblTimer;    // Hiển thị "Thời gian: 15s"
    private JButton btnExit;
    
    // Hiển thị các từ đã nhập
    private JList<String> wordsList;
    private DefaultListModel<String> wordsListModel;
    
    private Timer countdownTimer; // Timer đếm ngược của Swing
    private int remainingSeconds;

    public GameWindow(ClientSocketHandler socketHandler, User currentUser, User opponentUser) {
        this.socketHandler = socketHandler;
        this.currentUser = currentUser;
        this.opponentUser = opponentUser;

        setTitle("Vua Tiếng Việt - Đang thi đấu với " + opponentUser.getUsername());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // (Sau này nên đổi thành HIDE_ON_CLOSE)
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Nền gradient
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(187, 222, 251),
                        0, getHeight(), new Color(227, 242, 253)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new BorderLayout(10, 10));
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(background);
        
        // --- Khu vực điểm số ---
        JPanel scorePanel = new JPanel(new BorderLayout());
        scorePanel.setOpaque(false);
        scorePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 181, 246)),
                "Trạng thái trận đấu",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(33, 33, 33)
        ));
        
        lblTurnInfo = new JLabel("Lượt: 0/10", SwingConstants.CENTER);
        lblTurnInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTurnInfo.setForeground(new Color(25, 118, 210));
        
        lblTimer = new JLabel("Thời gian: --s", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTimer.setForeground(new Color(66, 66, 66));
        
        JPanel scoreLabels = new JPanel();
        scoreLabels.setOpaque(false);
        scoreLabels.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        
        lblPlayerScore = new JLabel(currentUser.getUsername() + ": 0");
        lblOpponentScore = new JLabel(opponentUser.getUsername() + ": 0");
        lblPlayerScore.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOpponentScore.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPlayerScore.setForeground(new Color(25, 118, 210));
        lblOpponentScore.setForeground(new Color(198, 40, 40));
        
        scoreLabels.add(lblPlayerScore);
        scoreLabels.add(new JLabel("  |  "));
        scoreLabels.add(lblOpponentScore);
        
        scorePanel.add(lblTurnInfo, BorderLayout.NORTH);
        scorePanel.add(scoreLabels, BorderLayout.CENTER);
        scorePanel.add(lblTimer, BorderLayout.SOUTH);
       
        background.add(scorePanel, BorderLayout.NORTH);
        

        // --- Khu vực hiển thị chữ cái (Status) ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        
        lblStatus = new JLabel("Đang chờ lượt mới...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblStatus.setOpaque(true);
        lblStatus.setBackground(Color.WHITE);
        lblStatus.setForeground(new Color(33, 33, 33));
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 222, 251), 2),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));
        centerPanel.add(lblStatus, BorderLayout.CENTER);
        
        // --- Khu vực hiển thị các từ đã nhập ---
        JPanel wordsPanel = new JPanel(new BorderLayout());
        wordsPanel.setOpaque(false);
        wordsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 181, 246)),
                "Các từ đã nhập",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(33, 33, 33)
        ));
        wordsPanel.setPreferredSize(new Dimension(0, 120));
        
        wordsListModel = new DefaultListModel<>();
        wordsList = new JList<>(wordsListModel);
        wordsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        wordsList.setBackground(Color.WHITE);
        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane wordsScrollPane = new JScrollPane(wordsList);
        wordsScrollPane.setBorder(new LineBorder(new Color(187, 222, 251)));
        wordsPanel.add(wordsScrollPane, BorderLayout.CENTER);
        
        centerPanel.add(wordsPanel, BorderLayout.SOUTH);
        background.add(centerPanel, BorderLayout.CENTER);

        // --- Khu vực nhập liệu ---
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setOpaque(false);
        
        txtWordInput = new JTextField();
        txtWordInput.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtWordInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(144, 202, 249)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        btnSend = new JButton("Gửi");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setBackground(new Color(25, 118, 210));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSend.setBackground(new Color(30, 136, 229));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSend.setBackground(new Color(25, 118, 210));
            }
        });
        
        btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExit.setBackground(new Color(198, 40, 40));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        btnExit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnExit.setBackground(new Color(229, 57, 53));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnExit.setBackground(new Color(198, 40, 40));
            }
        });
        
        southPanel.add(txtWordInput, BorderLayout.CENTER);
        southPanel.add(btnSend, BorderLayout.EAST);
        southPanel.add(btnExit, BorderLayout.WEST);
        background.add(southPanel, BorderLayout.SOUTH);

        
        // --- KHỞI TẠO TIMER ---
        // (Timer này sẽ tick mỗi 1 giây)
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            lblTimer.setText("Thời gian: " + remainingSeconds + "s");
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
            }
        });
        countdownTimer.setRepeats(true);
        
        // --- THÊM SỰ KIỆN CHO NÚT GỬI ---
        btnSend.addActionListener(this::onSendClick);
        txtWordInput.addActionListener(this::onSendClick);
        btnExit.addActionListener(this::onExitClick);
        
        // Vô hiệu hóa nút gửi lúc đầu
        txtWordInput.setEnabled(false);
        btnSend.setEnabled(false);
    }
    
    // --- HÀM MỚI: Xử lý nhấn nút "Gửi" ---
    private void onSendClick(ActionEvent e) {
        String word = txtWordInput.getText().trim();
        if (!word.isEmpty()) {
            // Gửi request lên server
            socketHandler.sendRequest(new Request("SUBMIT_WORD", word));
            // Hiển thị luôn trong danh sách để người chơi theo dõi
            wordsListModel.addElement(word);
            wordsList.ensureIndexIsVisible(wordsListModel.getSize() - 1);
            // Xóa text
            txtWordInput.setText("");
        }
    }
    
    // --- HÀM MỚI: Xử lý nhấn nút "Thoát" ---
    private void onExitClick(ActionEvent e) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn thoát?\nBạn sẽ bị xử thua 0-5.",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Gửi request "FORFEIT_GAME" (Bỏ cuộc)
            socketHandler.sendRequest(new Request("FORFEIT_GAME", null));
        }
    }
    
    // --- CÁC HÀM MỚI (Được gọi bởi SocketHandler) ---
    
    /** Hiển thị chữ cái của lượt mới **/
    public void displayNewTurn(TurnData data) {
        lblStatus.setText(data.getLetters());
        lblTurnInfo.setText("Lượt: " + data.getCurrentTurn() + "/" + data.getMaxTurns());
        
        // Khởi động lại timer
        remainingSeconds = data.getTurnDurationSeconds();
        lblTimer.setText("Thời gian: " + remainingSeconds + "s");
        countdownTimer.start(); // <-- Bắt đầu đếm ngược

        txtWordInput.setEnabled(true);
        btnSend.setEnabled(true);
        txtWordInput.requestFocus();
    }
    
    /** Thông báo hết lượt **/
    public void displayTurnEnd() {
        lblStatus.setText("Hết giờ! Đang chờ lượt mới...");
        lblTimer.setText("Hết giờ!");
        countdownTimer.stop();
        
        txtWordInput.setEnabled(false);
        btnSend.setEnabled(false);
    }
    
    /** Cập nhật điểm số **/
    public void updateScores(int myScore, int opponentScore) {
        lblPlayerScore.setText(currentUser.getUsername() + ": " + myScore);
        lblOpponentScore.setText(opponentUser.getUsername() + ": " + opponentScore);
    }
    
    /** Hiển thị lỗi (từ sai, trùng...) - không popup ngay nữa **/
    public void showInvalidWord(String reason) {
        // Không hiển thị JOptionPane trong lúc chơi, có thể log nếu cần
        System.out.println("[GameWindow] INVALID_WORD: " + reason);
    }
    
    /** Hiển thị thông báo từ hợp lệ - hiện tại không cần làm gì thêm **/
    public void showValidWord(String word) {
        // Không cần popup, từ đã được thêm vào danh sách khi người chơi gửi
    }
    
    /** Hiển thị thông báo kết thúc game (kèm danh sách từ hợp lệ / sai) **/
    public void showGameOver(common.model.GameResultData result) {
        countdownTimer.stop(); // <-- Dừng timer nếu game kết thúc
        txtWordInput.setEnabled(false);
        btnSend.setEnabled(false);
        btnExit.setEnabled(false); // Vô hiệu hóa nút thoát
        
        // Tạo JDialog hiển thị kết quả chi tiết
        JDialog resultDialog = new JDialog(this, "Trận Đấu Kết Thúc", true);
        resultDialog.setSize(500, 400);
        resultDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblSummary = new JLabel(result.getFinalMessage());
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblSummary, BorderLayout.NORTH);
        
        // Panel chứa hai danh sách: hợp lệ và không hợp lệ
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Danh sách từ hợp lệ
        DefaultListModel<String> validModel = new DefaultListModel<>();
        for (String w : result.getValidWords()) {
            validModel.addElement(w);
        }
        JList<String> validList = new JList<>(validModel);
        validList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel validPanel = new JPanel(new BorderLayout());
        validPanel.setBorder(BorderFactory.createTitledBorder("Từ hợp lệ (+2 điểm)"));
        validPanel.add(new JScrollPane(validList), BorderLayout.CENTER);
        
        // Danh sách từ không hợp lệ
        DefaultListModel<String> invalidModel = new DefaultListModel<>();
        for (String w : result.getInvalidWords()) {
            invalidModel.addElement(w);
        }
        JList<String> invalidList = new JList<>(invalidModel);
        invalidList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel invalidPanel = new JPanel(new BorderLayout());
        invalidPanel.setBorder(BorderFactory.createTitledBorder("Từ sai (-1 điểm)"));
        invalidPanel.add(new JScrollPane(invalidList), BorderLayout.CENTER);
        
        centerPanel.add(validPanel);
        centerPanel.add(invalidPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> resultDialog.dispose());
        JPanel southPanel = new JPanel();
        southPanel.add(btnClose);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        resultDialog.setContentPane(mainPanel);
        resultDialog.setVisible(true);
        
        this.dispose(); // Đóng cửa sổ game
        // (Bạn cần code để ClientSocketHandler mở lại LobbyWindow)
    }
}
