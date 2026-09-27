package com.itgu.utils;

import com.alibaba.fastjson.JSONObject;
import com.itgu.dto.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/*")
public class RockFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        String url = req.getRequestURI();

        // 白名单放行
        if (url.contains("login") || url.contains("register") || url.contains("sendCode") || url.contains("testtoken")
                || url.startsWith("/feedback_image/") || url.startsWith("/userAvatar/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 获取 Authorization header
        String authHeader = req.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring("Bearer ".length()).trim();
        }

        if (token == null || token.isEmpty()) {
            ApiResponse<String> error = ApiResponse.error("NOT_LOGIN", null);
            res.getWriter().write(JSONObject.toJSONString(error));
            log.warn("拦截到未登录请求: {}", url);
            return;
        }

        try {
            // 解析 token
            Claims claims = JwtUtils.parseJwt(token);
            if (claims == null) {
                ApiResponse<String> error = ApiResponse.error("NOT_LOGIN", null);
                res.getWriter().write(JSONObject.toJSONString(error));
                log.warn("解析 token 失败: {}", token);
                return;
            }
            req.setAttribute("claims", claims); // 保存解析结果
            log.info("解析 token 成功, userId={}, url={}", claims.get("id"), url);
        } catch (Exception e) {
            log.error("解析 token 异常: [{}]", token, e);
            ApiResponse<String> error = ApiResponse.error("NOT_LOGIN", null);
            res.getWriter().write(JSONObject.toJSONString(error));
            return;
        }

        // 放行请求
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
