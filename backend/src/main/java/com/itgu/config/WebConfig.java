package com.itgu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${feedback.image.path}")
    private String feedbackImagePath;

    @Value("${user.avatar.path}")
    private String userAvatarPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // feedback 图片映射
        registry.addResourceHandler("/feedback_image/**")
                .addResourceLocations("file:" + feedbackImagePath + "/");

        // userAvatar 映射
        registry.addResourceHandler("/userAvatar/**")
                .addResourceLocations("file:" + userAvatarPath + "/");
    }
}
