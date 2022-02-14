package com.guo.app.controller.dto;


import lombok.Data;
/*
接收前端登录请求的参数
 */
@Data
public class UserDTO {

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String token;
}
