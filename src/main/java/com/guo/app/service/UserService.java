package com.guo.app.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.app.controller.dto.UserDTO;
import com.guo.app.entity.User;
import com.guo.app.ex.ServiceException;
import com.guo.app.mapper.UserMapper;
import com.guo.app.status.Constants;
import com.guo.app.status.Result;
import com.guo.app.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Queue;

@Service
@Transactional
public class UserService extends ServiceImpl<UserMapper,User> {
    public UserDTO login(UserDTO userDTO) {


        User one=getUserInfo(userDTO);
        if (one != null) {
            BeanUtil.copyProperties(one, userDTO,true);
            String token = TokenUtils.genToken(one.getId().toString(),one.getPassword());
            userDTO.setToken(token);
            return userDTO;
        } else {
            throw new ServiceException(Constants.CODE_600,"用户名或密码错误");
        }
    }

    public User register(UserDTO userDTO) {
        User one = getUserInfo(userDTO);
        if(one==null){
            one = new User();
            BeanUtil.copyProperties( userDTO,one,true);
            save(one);
        } else {
            throw new ServiceException(Constants.CODE_600,"用户已存在");
        }
        return one;

    }
    private User getUserInfo(UserDTO userDTO){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userDTO.getUsername());
        queryWrapper.eq("password",userDTO.getPassword());
        /*有可能出现脏数据，特殊处理一下
//        List<User> ls= list(queryWrapper);
//        return ls.size()!=0;

         */
        User one;
        try{
            one = getOne(queryWrapper);

        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException(Constants.CODE_500,"系统错误");
        }

        return one;
    }

    public User findOne(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return getOne(queryWrapper);
    }


//    @Autowired
//    private userMapper userMapper;

//    public int save(User user){
//        if(user.getId()==null){
//           return  userMapper.insert(user);
//        } else{
//            return userMapper.update(user);
//        }
//    }
}
