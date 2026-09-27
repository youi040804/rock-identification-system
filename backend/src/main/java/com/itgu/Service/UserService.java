
package com.itgu.Service;

import com.itgu.Pojo.User;
import com.itgu.dto.UserRegisterDTO;


public interface UserService {
    boolean existsByEmail(String email);

    void save(User user);

    User userLogin(User user);

    User userRegister(UserRegisterDTO userRegisterDTO);

    void updateUserAvatar(int userId, String avatarUrl);

    void updateUsername(int userId, String username); // 调用 service 更新数据库
    void updateGender(int userId, String gender);
    boolean changePassword(int userId, String oldPassword, String newPassword);


}
