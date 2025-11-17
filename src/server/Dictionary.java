package server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class Dictionary {
    // --- GIẢ LẬP TỪ ĐIỂN ---
    // (Trong dự án thật, bạn sẽ tải 1 file .txt chứa hàng ngàn từ vào đây)
    private static final Set<String> validWords = new HashSet<>();
    private static final String FILENAME = "server/tudien.txt";
    
    static {
        System.out.println("Đang nạp từ điển từ file: " + FILENAME + "...");
        try (
            // 3. Lấy file từ "classpath" (bên trong src)
            InputStream is = Dictionary.class.getClassLoader().getResourceAsStream(FILENAME);
            
            // 4. Dùng InputStreamReader để đọc UTF-8 (Rất quan trọng cho Tiếng Việt)
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            
            // 5. Dùng BufferedReader để đọc từng dòng
            BufferedReader reader = new BufferedReader(isr)
        ) {
            if (is == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file từ điển: " + FILENAME);
                throw new RuntimeException("Không tìm thấy file từ điển: " + FILENAME);
            }

            String line;
            int wordCount = 0;
            
            // 6. Đọc từng dòng trong file
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase(); // Xóa khoảng trắng, chuyển chữ thường
                if (!word.isEmpty()) {
                    validWords.add(word);
                    wordCount++;
                }
            }
            System.out.println("Đã nạp thành công " + wordCount + " từ vào từ điển.");
            
        } catch (Exception e) {
            e.printStackTrace();
            // Ném ra lỗi để server dừng lại nếu không nạp được từ điển
            throw new RuntimeException("Lỗi nghiêm trọng khi nạp từ điển", e);
        }     
    }
    
    
    //Kiểm tra xem từ có trong từ điển không
    public static boolean isValid(String word) {
        return validWords.contains(word.toLowerCase());
    }
    
    // Biểu thức regex để tìm các dấu (diacritics)
    private static final Pattern DIACRITICS_REGEX = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    private static String normalize(String input) {
        try {
            String temp = input.toLowerCase();
            
            //Tách dấu
            temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
            
            //Xóa tất cả các dấu (ký tự combining) đã được tách
            temp = DIACRITICS_REGEX.matcher(temp).replaceAll("");
            
            //Riêng chữ 'đ' phải thay thế thủ công
            temp = temp.replaceAll("đ", "d");
            
            return temp;
        } catch (Exception e) {
            // Xử lý nếu input là null
            return ""; 
        }
        
    }
    
    /**
     * Kiểm tra xem từ có được ghép đúng từ các chữ cái cho trước không
     * (Đây là một hàm kiểm tra đơn giản)
     */
    public static boolean isValidFromLetters(String word, String letters) {
        String availableLetters = letters.toLowerCase();
        String wordToCheck = normalize(word);

        // Tạo một mảng đếm ký tự cho các chữ cái có sẵn
        int[] letterCounts = new int[256]; // (Giả sử dùng ASCII)
        for (char c : availableLetters.toCharArray()) {
            letterCounts[c]++;
        }

        // Kiểm tra từng ký tự của từ
        for (char c : wordToCheck.toCharArray()) {
            if (c >= 256) {
                return false; 
            }
            
            if (letterCounts[c] > 0) {
                letterCounts[c]--; // Giảm số lượng ký tự đã dùng
            } else {
                return false; // Ký tự không có hoặc đã dùng hết
            }
        }
        return true; // Tất cả ký tự đều hợp lệ
    }

    public static String getRandomWordFromDictionary() {
        List<String> words = new ArrayList<>(validWords);
        Collections.shuffle(words);
        return normalize(words.get(0));
    }
}
