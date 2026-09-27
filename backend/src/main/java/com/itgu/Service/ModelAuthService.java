package com.itgu.Service;

import com.itgu.Mapper.ModelAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelAuthService {

    @Autowired
    private ModelAuthMapper modelAuthMapper;

    /**
     * 校验输入密码是否正确
     */
    public boolean checkPassword(String inputPassword) {
        String dbPassword = modelAuthMapper.getPassword();
        if (dbPassword == null) return false;
        return dbPassword.equals(inputPassword);
    }
}
