package com.itgu.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.Random;

@Slf4j
@Service
public class MailService {


    @Autowired
   private JavaMailSender mailSender;



@Value("${spring.mail.username}")
private String from;
    public void sendCode(String to) {
        if (mailSender == null) {
            log.error("JavaMailSender 尚未配置，邮件发送失败");
            return;
        }

        String code = generateCode();

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【岩石识别系统】验证码");
            helper.setText("你的验证码是：" + code + "，请在5分钟内完成验证。", false);

            mailSender.send(message);
            log.info("验证码 {} 已成功发送到 {}", code, to);
        } catch (MessagingException e) {
            log.error("发送验证码失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请联系管理员");
        }
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6位验证码
        return String.valueOf(code);
    }
}
