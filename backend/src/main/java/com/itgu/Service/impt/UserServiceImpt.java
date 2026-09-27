

package com.itgu.Service.impt;

import com.itgu.Mapper.UserMapper;
import com.itgu.Pojo.User;
import com.itgu.Service.MailService;
import com.itgu.Service.UserService;
import com.itgu.dto.UserRegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@RequiredArgsConstructor  // 构造注入
public class UserServiceImpt implements UserService {

    private final UserMapper userMapper;
    @Autowired
    private MailService mailService;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpt.class);

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.findByEmail(email) != null;
    }

    @Override
    public void save(User user) {
        userMapper.insert(user);
    }

    @Override
    public User userLogin(User user) {
        return userMapper.findByUsernameAndPassword(user.getUsername(), user.getPassword());
    }

    @Override
    public User userRegister(UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        String phone = userRegisterDTO.getPhone();
        String code = userRegisterDTO.getCode();

        // 验证邮箱是否已经注册
        if (userMapper.existsByEmail(email)) {

            log.info("邮箱已存在：" + email);//调试语句
            throw new RuntimeException("该邮箱已被注册");
        }

        // 构造新用户
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        user.setPhone(userRegisterDTO.getPhone());

        log.info(
          "准备创建用户: username={}",
           user.getUsername()
        );
        // 保存到数据库
        try {
            userMapper.insert(user);
            log.info(" 成功插入数据库！");
        } catch (Exception e) {
            log.info("插入数据库失败：" + e.getMessage());
            e.printStackTrace();
        }

        return user;
    }
    @Override
    public void updateUserAvatar(int userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        //log.info("用户 " + userId + " 的头像已更新：" + avatarUrl);
    }

    @Override
    public void updateUsername(int userId, String username) {
        userMapper.updateUsername(userId, username);
        log.info("用户 {} 的用户名已更新为：{}", userId, username);
    }

    @Override
    public void updateGender(int userId, String gender) {
        userMapper.updateGender(userId, gender);
        log.info("用户 {} 的性别已更新为：{}", userId, gender);
    }
    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) return false;
        if (!user.getPassword().equals(oldPassword)) return false;

        userMapper.updatePassword(userId, newPassword);
        return true;
    }



}
