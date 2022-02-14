package com.guo.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.app.controller.dto.UserDTO;
import com.guo.app.entity.User;
import org.springframework.stereotype.Service;


public interface IUserService extends IService<User> {
      boolean login(UserDTO userDTO);
     // boolean saveUser(User user);
}
