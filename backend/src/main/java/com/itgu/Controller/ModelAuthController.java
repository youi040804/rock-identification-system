package com.itgu.Controller;

import com.itgu.Service.ModelAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/modelAuth")
public class ModelAuthController {

    @Autowired
    private ModelAuthService modelAuthService;

    /**
     * 前端发送密码进行校验
     * POST /modelAuth/check
     * body: { "password": "用户输入密码" }
     */
    @PostMapping("/check")
    public Map<String, Object> checkPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean result = modelAuthService.checkPassword(password);



        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        if (result) {
            response.put("message", "密码正确");
        } else {
            response.put("message", "密码错误");
        }
        return response;
    }
}
