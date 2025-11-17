/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common.model;

import java.io.Serializable;
import java.util.List;

// Lớp này dùng để gói dữ liệu trả về khi login thành công
public class LoginSuccessData implements Serializable {
    
    private User currentUser;  
    private List<User> onlineList;

    public LoginSuccessData(User currentUser, List<User> onlineList) {
        this.currentUser = currentUser;
        this.onlineList = onlineList;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getOnlineList() {
        return onlineList;
    }
}
