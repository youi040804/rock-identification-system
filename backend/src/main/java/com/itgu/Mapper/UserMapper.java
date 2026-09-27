
package com.itgu.Mapper;

import com.itgu.Pojo.User;
import org.apache.ibatis.annotations.*;


@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User findByUsernameAndPassword(String username, String password);

    @Select("SELECT COUNT(*) FROM user WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO user(username, password, email, phone) VALUES(#{username}, #{password}, #{email}, #{phone})")
    void insert(User user);

    @Update("UPDATE user SET avatar_url = #{avatarUrl} WHERE id = #{userId}")
    void updateAvatar(@Param("userId") int userId, @Param("avatarUrl") String avatarUrl);

    @Update("UPDATE user SET username = #{username} WHERE id = #{userId}")
    void updateUsername(@Param("userId") int userId, @Param("username") String username);

    @Update("UPDATE user SET gender = #{gender} WHERE id = #{userId}")
    void updateGender(@Param("userId") int userId, @Param("gender") String gender);


    // 根据用户ID查找用户
    @Select("SELECT * FROM user WHERE id = #{userId}")
    User findById(@Param("userId") int userId);
    // 修改用户密码
    @Update("UPDATE user SET password = #{newPassword} WHERE id = #{userId}")
    void updatePassword(@Param("userId") int userId, @Param("newPassword") String newPassword);
}

