package com.itgu.Pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResp {
    private String token;
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private String avatarUrl;
}
