package com.itgu.Controller;

import com.itgu.Service.MailService;
import com.itgu.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @GetMapping("/sendCode")
    public ApiResponse<?> sendCode(@RequestParam String email) {
        mailService.sendCode(email);
        return ApiResponse.success("验证码发送成功", null);
    }
}