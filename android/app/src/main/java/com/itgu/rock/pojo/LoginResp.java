package com.itgu.rock.pojo;

public class LoginResp {
private Integer id;
private String token;
private String username;
private String email;
private String phone;
private String password;
private String gender;
private String avatarUrl;

    //getter methods
    public Integer getId(){return id;}

    public String getToken() {return token;}

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
    public String getPassword(){return  password;}
    public String getGender(){return  gender;}
    public String getAvatarUrl(){return  avatarUrl;}

    public void setId(Integer id){this.id=id; }


    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public  void setPassword(String password){this.password=password;}
    public void setGender(String gender){this.gender=gender;}
    public  void setAvatarurl(String avatarUrl){this.avatarUrl=avatarUrl;}

    @Override
    public String toString() {
        return "LoginResp{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", avatarurl='" + avatarUrl + '\'' +
                '}';
    }

}
