/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import client.ClientSocketHandler;
import common.model.Request;
import common.model.User;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 *
 * @author ASUS
 */
public class LoginWindow extends JFrame{
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    
    // Mỗi Client chỉ có 1 SocketHandler
    private ClientSocketHandler socketHandler;

    public LoginWindow() {
        setTitle("Đăng Nhập - Vua Tiếng Việt");
        setSize(380, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // 🌈 Nền gradient nhẹ
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(66, 165, 245),
                                                     0, getHeight(), new Color(21, 101, 192));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(null);
        setContentPane(backgroundPanel);

        // 🏷️ Tiêu đề
        JLabel lblTitle = new JLabel("Đăng Nhập Hệ Thống", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(0, 10, getWidth(), 40);
        backgroundPanel.add(lblTitle);

        // 🧾 Nhãn Tài khoản
        JLabel lblUser = new JLabel("Tài khoản:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);
        lblUser.setBounds(50, 70, 80, 25);
        backgroundPanel.add(lblUser);

        // 🔒 Nhãn Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setForeground(Color.WHITE);
        lblPass.setBounds(50, 110, 80, 25);
        backgroundPanel.add(lblPass);

        // ✏️ Ô nhập tài khoản
        txtUsername = new JTextField();
        txtUsername.setBounds(140, 70, 180, 25);
        txtUsername.setBackground(new Color(255, 255, 255, 220));
        txtUsername.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        backgroundPanel.add(txtUsername);

        // 🔑 Ô nhập mật khẩu
        txtPassword = new JPasswordField();
        txtPassword.setBounds(140, 110, 180, 25);
        txtPassword.setBackground(new Color(255, 255, 255, 220));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        backgroundPanel.add(txtPassword);

        // 🚪 Nút đăng nhập
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setBounds(130, 160, 120, 35);
        btnLogin.setFocusPainted(false);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(0, 102, 204));
        btnLogin.setBorder(BorderFactory.createEmptyBorder());
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng hover
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(30, 136, 229));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(0, 102, 204));
            }
        });

        backgroundPanel.add(btnLogin);

        // ⚙️ Logic gốc giữ nguyên
        this.socketHandler = new ClientSocketHandler(this);

        // 🖱️ Bắt sự kiện click nút
        btnLogin.addActionListener(this::onLoginClick);
    }

    // Hàm xử lý khi click nút "Đăng Nhập"
    private void onLoginClick(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin.");
            return;
        }

        // 1. Tạo đối tượng User để gửi đi
        User userToLogin = new User(username, password);
        
        // 2. Tạo Request
        Request loginRequest = new Request("LOGIN", userToLogin);
        
        // 3. Gửi Request qua SocketHandler
        socketHandler.sendRequest(loginRequest);
    }
}
