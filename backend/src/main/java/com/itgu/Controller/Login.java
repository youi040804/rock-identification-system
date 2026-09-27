package com.itgu.Controller;

import com.itgu.Pojo.LoginResp;
import com.itgu.Pojo.User;
import com.itgu.Service.UserService;
import com.itgu.dto.ApiResponse;
import com.itgu.dto.UserRegisterDTO;
import com.itgu.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class Login {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody UserRegisterDTO userDTO) {
        log.info("用户注册信息：{}", userDTO);
        try {
            User user = userService.userRegister(userDTO); // 修改 userRegister 返回 User 对象
            // 自动生成 token
            String jwt = JwtUtils.generateToken(user);
            LoginResp resp = LoginResp.builder()
                    .token(jwt)
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("注册成功", resp));
        } catch (DataIntegrityViolationException e) {
            log.error("用户名重复", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("用户名已存在，请换一个", null));
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("注册失败：" + e.getMessage(), null));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody User user) {
        log.info("用户登录：{}", user.getUsername());
        User userInfo = userService.userLogin(user);
        if (userInfo != null) {
            // 生成 JWT
            String jwt = JwtUtils.generateToken(userInfo);

            // 构造返回对象
            LoginResp resp = LoginResp.builder()
                    .token(jwt)
                    .id(userInfo.getId())
                    .username(userInfo.getUsername())
                    .email(userInfo.getEmail())
                    .phone(userInfo.getPhone())
                    .gender(userInfo.getGender())
                    .avatarUrl(userInfo.getAvatarUrl())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("登录成功", resp));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("用户名或密码错误", null));
    }



    @GetMapping("/testtoken")
    public ResponseEntity<ApiResponse<?>> testToken() {
        return ResponseEntity.ok(ApiResponse.success("Token有效", null));
    }
}
