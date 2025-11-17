/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import client.ClientSocketHandler;
import common.model.Request;
import common.model.User;
import common.model.WordsMatch;
import dao.UserDAO;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author ASUS
 */
public class LobbyWindow extends JFrame{
    private ClientSocketHandler socketHandler; // Cần để gửi lời mời
    private User currentUser;
    private String selectedUsernameForHistory; // Lưu username được chọn để xem lịch sử
    
    // Panel Phải (All Players List)
    private JList<User> userJList; // JList để hiển thị User
    private DefaultListModel<User> userListModel; // Model cho JList
    private JButton btnInvite;
    private JButton btnViewHistory; // Nút xem lịch sử đấu
    
    // Panel Giữa (Bảng Xếp Hạng)
    private JTable rankingTable;
    private DefaultTableModel rankingTableModel;
    
    private JLabel welcomeLabel;
    private JLabel scoreLabel;
    
    private final Map<String, User> onlineUsersMap = new HashMap<>();
    private List<User> allUsersCache = new ArrayList<>();

    public LobbyWindow(ClientSocketHandler socketHandler, User currentUser) {
        this.socketHandler = socketHandler;
        this.currentUser = currentUser;
        
        setTitle("Sảnh Chờ");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10)); // 10px padding
        
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(187, 222, 251),
                        0, getHeight(), new Color(227, 242, 253));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new BorderLayout(10, 10));
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(background);
        
        // --- 1. TOP: Panel Thông Tin Cá Nhân ---
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.NORTH);
        
        // --- 2. RIGHT: Panel Tất Cả Người Chơi ---
        JPanel onlinePanel = createAllPlayersPanel();
        add(onlinePanel, BorderLayout.EAST);
        
        // --- 3. CENTER: Panel Bảng Xếp Hạng ---
        JPanel rankingPanel = createRankingPanel();
        add(rankingPanel, BorderLayout.CENTER);

        // --- Xử lý sự kiện ---
        btnInvite.addActionListener(this::onInviteClick);
        btnViewHistory.addActionListener(this::onViewHistoryClick);
        
        socketHandler.sendRequest(new Request("GET_RANKING", null));
        socketHandler.sendRequest(new Request("GET_ALL_USERS", null));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Client closing - disconnecting...");
                socketHandler.closeConnection();
                System.exit(0);
            }
        });
    }
    

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                new EmptyBorder(5, 10, 5, 10),
                new MatteBorder(0, 0, 1, 0, new Color(144, 202, 249))
        ));
        
        welcomeLabel = new JLabel("Chào, " + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(33, 33, 33));
        
        scoreLabel = new JLabel(String.format("Tổng điểm: %d | Thắng: %d", 
                                       currentUser.getTotalScore(), currentUser.getTotalWins()));
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreLabel.setForeground(new Color(66, 66, 66));
        
        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(scoreLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createAllPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 181, 246)),
                "Tất Cả Người Chơi",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(33, 33, 33)
        ));
        
        userListModel = new DefaultListModel<>();
        userJList = new JList<>(userListModel);
        userJList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userJList.setCellRenderer(new UserCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(userJList);
        scrollPane.setBorder(new LineBorder(new Color(187, 222, 251)));
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setOpaque(false);
        
        btnInvite = new JButton("Thách Đấu");
        btnInvite.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnInvite.setBackground(new Color(25, 118, 210));
        btnInvite.setForeground(Color.WHITE);
        btnInvite.setFocusPainted(false);
        btnInvite.setBorder(new EmptyBorder(8, 8, 8, 8));
        btnInvite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnViewHistory = new JButton("Xem Lịch Sử Đấu");
        btnViewHistory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnViewHistory.setBackground(new Color(76, 175, 80));
        btnViewHistory.setForeground(Color.WHITE);
        btnViewHistory.setFocusPainted(false);
        btnViewHistory.setBorder(new EmptyBorder(8, 8, 8, 8));
        btnViewHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover cho nút
        btnInvite.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnInvite.setBackground(new Color(30, 136, 229));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnInvite.setBackground(new Color(25, 118, 210));
            }
        });
        
        btnViewHistory.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnViewHistory.setBackground(new Color(102, 187, 106));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnViewHistory.setBackground(new Color(76, 175, 80));
            }
        });
        
        buttonPanel.add(btnInvite);
        buttonPanel.add(btnViewHistory);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 181, 246)),
                "Bảng Xếp Hạng",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(33, 33, 33)
        ));
        
        // Tạo Bảng (JTable)
        String[] columnNames = {"Hạng", "Tên Người Chơi", "Tổng Điểm", "Trận Thắng"};
        rankingTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; // Không cho phép sửa
            }
        };
        
        rankingTable = new JTable(rankingTableModel);
        rankingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rankingTable.setRowHeight(28);
        rankingTable.setGridColor(new Color(224, 224, 224));
        
        JTableHeader header = rankingTable.getTableHeader();
        header.setBackground(new Color(25, 118, 210));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(rankingTable);
        scrollPane.setBorder(new LineBorder(new Color(187, 222, 251)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // (Bạn có thể thêm nút "Làm mới" ở đây nếu muốn)
        
        return panel;
    }
    
    // Nút "Thách Đấu" được click
    private void onInviteClick(ActionEvent e) {
        User selectedUser = userJList.getSelectedValue();
        
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một người chơi để thách đấu.");
            return;
        }
        
        if (selectedUser.getUsername().equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "Bạn không thể tự thách đấu chính mình.");
            return;
        }
        
        if (!selectedUser.getStatus().equals("Online") && !selectedUser.getStatus().equals("Rỗi")) {
            JOptionPane.showMessageDialog(this, "Người chơi này không online hoặc đang bận.");
            return;
        }
        
        // Gửi Request "INVITE"
        System.out.println("Gửi lời mời tới: " + selectedUser.getUsername());
        socketHandler.sendRequest(new Request("INVITE", selectedUser.getUsername()));
    }
    
    // Nút "Xem Lịch Sử Đấu" được click
    private void onViewHistoryClick(ActionEvent e) {
        User selectedUser = userJList.getSelectedValue();
        
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một người chơi để xem lịch sử đấu.");
            return;
        }
        
        // Lưu username được chọn
        selectedUsernameForHistory = selectedUser.getUsername();
        
        // Gửi Request để lấy lịch sử đấu
        socketHandler.sendRequest(new Request("GET_MATCH_HISTORY", selectedUsernameForHistory));
    }
    


    // Cập nhật JList với danh sách mới (cho online list - giữ lại để tương thích)
    public void updateOnlineList(List<User> userList) {
        System.out.println("[UI] updateOnlineList() được gọi. Số user: " + userList.size());
        onlineUsersMap.clear();
        for (User user : userList) {
            onlineUsersMap.put(user.getUsername(), user);
        }
        refreshUserListWithStatuses();
    }
    
    // Cập nhật JList với tất cả người chơi từ database
    public void updateAllUsersList(List<User> userList) {
        System.out.println("[UI] updateAllUsersList() được gọi. Số user: " + userList.size());
        allUsersCache = new ArrayList<>();
        for (User user : userList) {
            System.out.println(" - " + user.getUsername() + ": " + user.getStatus());
            allUsersCache.add(new User(
                user.getUsername(),
                user.getTotalScore(),
                user.getTotalWins(),
                user.getStatus()
            ));
        }
        refreshUserListWithStatuses();
    }
    
    // Hiển thị popup khi có lời mời
    public void showInvitePopup(String inviterUsername) {
        int choice = JOptionPane.showConfirmDialog(
            this, 
            inviterUsername + " muốn thách đấu bạn. Bạn có đồng ý?", 
            "Lời Mời Thách Đấu", 
            JOptionPane.YES_NO_OPTION
        );

        String responseType = (choice == JOptionPane.YES_OPTION) ? "ACCEPT" : "REJECT";
        
        // Gửi phản hồi lại cho Server
        // Dữ liệu là 1 mảng String: [người mời, câu trả lời]
        String[] responseData = {inviterUsername, responseType};
        socketHandler.sendRequest(new Request("INVITE_RESPONSE", responseData));
    }

    public void updateRankingTable(List<User> rankings) {
        // Xóa dữ liệu cũ
        rankingTableModel.setRowCount(0); 
        
        // Thêm dữ liệu mới
        int rank = 1;
        for (User user : rankings) {
            rankingTableModel.addRow(new Object[]{
                rank++,
                user.getUsername(),
                user.getTotalScore(),
                user.getTotalWins()
            });
        }
    }
    
    public void updateCurrentUserInfo(User user) {
        this.currentUser = user;
        scoreLabel.setText(String.format("Tổng điểm: %d | Thắng: %d", 
                            currentUser.getTotalScore(), currentUser.getTotalWins()));
        
        scoreLabel.revalidate();
        scoreLabel.repaint();
    }
    
    // Lấy user được chọn (để dùng trong ClientSocketHandler)
    public User getSelectedUser() {
        return userJList.getSelectedValue();
    }
    
    // Lấy username đã lưu cho lịch sử đấu
    public String getSelectedUsernameForHistory() {
        return selectedUsernameForHistory;
    }

    private void refreshUserListWithStatuses() {
        if (userListModel == null) {
            return;
        }

        int previousSelection = userJList.getSelectedIndex();

        userListModel.clear();

        for (User baseUser : allUsersCache) {
            User onlineUser = onlineUsersMap.get(baseUser.getUsername());
            User displayUser;

            if (onlineUser != null) {
                displayUser = new User(
                        onlineUser.getUsername(),
                        onlineUser.getTotalScore(),
                        onlineUser.getTotalWins(),
                        onlineUser.getStatus()
                );
            } else {
                displayUser = new User(
                        baseUser.getUsername(),
                        baseUser.getTotalScore(),
                        baseUser.getTotalWins(),
                        "Offline"
                );
            }

            userListModel.addElement(displayUser);

            if (displayUser.getUsername().equals(currentUser.getUsername())) {
                currentUser.setTotalScore(displayUser.getTotalScore());
                currentUser.setTotalWins(displayUser.getTotalWins());
            }
        }

        if (onlineUsersMap.containsKey(currentUser.getUsername())) {
            currentUser.setStatus(onlineUsersMap.get(currentUser.getUsername()).getStatus());
        } else {
            currentUser.setStatus("Offline");
        }
        updateCurrentUserInfo(currentUser);

        if (previousSelection >= 0 && previousSelection < userListModel.size()) {
            userJList.setSelectedIndex(previousSelection);
        }

        userJList.revalidate();
        userJList.repaint();
    }
    
    // Hiển thị lịch sử đấu
    public void showMatchHistory(List<Map<String, Object>> matchHistory, String username) {
        // Tạo cửa sổ hiển thị lịch sử đấu
        JDialog historyDialog = new JDialog(this, "Lịch sử đấu của " + username, true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tạo bảng hiển thị lịch sử
        String[] columnNames = {"Thời gian", "Đối thủ", "Kết quả", "Điểm", "Chi tiết"};
        DefaultTableModel historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(25);
        
        // Điền dữ liệu vào bảng
        for (Map<String, Object> matchInfo : matchHistory) {
            common.model.Match match = (common.model.Match) matchInfo.get("match");
            String winnerName = (String) matchInfo.get("winnerName");
            String loserName = (String) matchInfo.get("loserName");
            
            String opponent = username.equals(winnerName) ? loserName : winnerName;
            String result = username.equals(winnerName) ? "Thắng" : "Thua";
            String score = username.equals(winnerName) 
                ? match.getWinnerScores() + "-" + match.getLoserScores()
                : match.getLoserScores() + "-" + match.getWinnerScores();
            
            String timeStr = match.getStartTime() != null 
                ? match.getStartTime().toString().substring(0, 16) 
                : "N/A";
            
            historyTableModel.addRow(new Object[]{timeStr, opponent, result, score, "Xem"});
        }
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Nút xem chi tiết
        JButton btnViewDetail = new JButton("Xem Chi Tiết Trận Đấu");
        btnViewDetail.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow >= 0) {
                Map<String, Object> matchInfo = matchHistory.get(selectedRow);
                showMatchDetail(matchInfo, username);
            } else {
                JOptionPane.showMessageDialog(historyDialog, "Hãy chọn một trận đấu để xem chi tiết.");
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnViewDetail);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.add(mainPanel);
        historyDialog.setVisible(true);
    }
    
    // Hiển thị chi tiết trận đấu
    @SuppressWarnings("unchecked")
    private void showMatchDetail(Map<String, Object> matchInfo, String username) {
        common.model.Match match = (common.model.Match) matchInfo.get("match");
        List<WordsMatch> words = (List<WordsMatch>) matchInfo.get("words");
        String winnerName = (String) matchInfo.get("winnerName");
        String loserName = (String) matchInfo.get("loserName");
        
        // Tạo cửa sổ chi tiết
        JDialog detailDialog = new JDialog(this, "Chi tiết trận đấu", true);
        detailDialog.setSize(600, 400);
        detailDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Thông tin trận đấu
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin trận đấu"));
        infoPanel.add(new JLabel("Thời gian:"));
        infoPanel.add(new JLabel(match.getStartTime() != null ? match.getStartTime().toString() : "N/A"));
        infoPanel.add(new JLabel("Người thắng:"));
        infoPanel.add(new JLabel(winnerName + " (" + match.getWinnerScores() + " điểm)"));
        infoPanel.add(new JLabel("Người thua:"));
        infoPanel.add(new JLabel(loserName + " (" + match.getLoserScores() + " điểm)"));
        infoPanel.add(new JLabel("Kết quả:"));
        String result = username.equals(winnerName) ? "Bạn thắng" : "Bạn thua";
        infoPanel.add(new JLabel(result));
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Danh sách từ
        JPanel wordsPanel = new JPanel(new BorderLayout());
        wordsPanel.setBorder(BorderFactory.createTitledBorder("Các từ đã nhập"));
        
        DefaultListModel<String> wordsModel = new DefaultListModel<>();
        for (WordsMatch wm : words) {
            String playerName = UserDAO.getUsernameById(wm.getPlayerId());
            wordsModel.addElement(playerName + ": " + wm.getWord() + " (+" + wm.getScore() + " điểm)");
        }
        
        JList<String> wordsList = new JList<>(wordsModel);
        wordsList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane wordsScroll = new JScrollPane(wordsList);
        wordsPanel.add(wordsScroll, BorderLayout.CENTER);
        
        mainPanel.add(wordsPanel, BorderLayout.CENTER);
        
        detailDialog.add(mainPanel);
        detailDialog.setVisible(true);
    }
    
    // (Tùy chọn) Lớp nội bộ để hiển thị JList đẹp hơn
    class UserCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof User) {
                User user = (User) value;
                setText(String.format("%s (%d điểm) - [%s]", user.getUsername(), user.getTotalScore(), user.getStatus()));
            }
            return this;
        }
    }
}
