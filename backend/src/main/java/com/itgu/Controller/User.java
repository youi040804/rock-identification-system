package com.itgu.Controller;
import com.itgu.Service.UserService;
import com.itgu.dto.ApiResponse;
import com.itgu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class User {

    @Autowired
    private UserService userService;
    @Value("${user.avatar.path}")
    private String avatarSavePath;

    @Value("${user.avatar.db.prefix}")
    private String avatarDbPrefix;

    @PostMapping("/uploadAvatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("userId") int userId,
            @RequestParam("avatar") MultipartFile avatarFile) {

        if (avatarFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件为空"));
        }

        try {
            // 生成文件名
            String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            // 保存到本地文件夹
            File destFile = new File(avatarSavePath, fileName);
            destFile.getParentFile().mkdirs();
            avatarFile.transferTo(destFile);

            // 数据库存相对路径
            String avatarUrl = avatarDbPrefix + fileName;
            userService.updateUserAvatar(userId, avatarUrl);

            // 返回 JSON
            Map<String, String> result = new HashMap<>();
            result.put("filename", fileName);
            result.put("url", avatarUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/updateUsername")
    public ResponseEntity<Map<String, String>> updateUsername(
            @RequestParam("userId") int userId,
            @RequestParam("username") String username) {

        try {
            userService.updateUsername(userId, username); // 调用 service 更新数据库
            Map<String, String> result = new HashMap<>();
            result.put("message", "用户名更新成功");
            result.put("username", username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "修改失败: " + e.getMessage()));
        }
    }
    @PostMapping("/updateGender")
    public ResponseEntity<Map<String, String>> updateGender(
            @RequestParam("userId") int userId,
            @RequestParam("gender") String gender) {

        try {
            userService.updateGender(userId, gender);
            Map<String, String> result = new HashMap<>();
            result.put("message", "性别更新成功");
            result.put("gender", gender);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "修改失败: " + e.getMessage()));
        }
    }



    // 修改密码接口
    @PostMapping("/changePassword")
    public ApiResponse<?> changePassword(HttpServletRequest request,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return ApiResponse.error("未登录或 token 无效", null);
        }
        Integer userId = (Integer) claims.get("id");

        boolean result = userService.changePassword(userId, oldPassword, newPassword);
        if (result) {
            return ApiResponse.success("密码修改成功", null);
        } else {
            return ApiResponse.error("原密码错误或修改失败", null);
        }
    }


}