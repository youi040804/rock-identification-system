package com.itgu.Mapper;

import com.itgu.Pojo.ModelAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ModelAuthMapper {

    // 查询密码

@Select("SELECT config_value FROM model_auth WHERE config_key = 'model_password'")
String getPassword();

}
