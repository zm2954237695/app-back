package com.guo.app.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guo.app.controller.dto.UserDTO;
import com.guo.app.entity.User;

import com.guo.app.service.IUserService;
import com.guo.app.service.UserService;
import com.guo.app.status.Constants;
import com.guo.app.status.Result;
import com.sun.deploy.net.HttpResponse;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;
    @RequestMapping("/")
    public List<User> findAll(){
        return  userService.list();
    }

    @PostMapping
    public boolean saveUser(@RequestBody User user){
        return userService.saveOrUpdate(user);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Integer id){
        return userService.removeById(id);
    }


    @PostMapping("/del/batch")
    public boolean deleteBatch(@RequestBody List<Integer> ids){
        return userService.removeByIds(ids);
    }
//    @GetMapping("/page")
//    public Map<String, Object> findPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
//                                        @RequestParam String username){
//          pageNum=(pageNum-1)*pageSize;
//          Integer total = userMapper.selectTotal();
//          username = '%' + username +'%';
//        List<User> data = userMapper.selectPage(pageNum, pageSize,username);
//        Map<String ,Object> res = new HashMap<>();
//        res.put("data",data);
//        res.put("total",total);
//        return res;
//
//    }

    @GetMapping("/page")
    public IPage<User> findPage(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                                @RequestParam(defaultValue = "") String username,
                                @RequestParam(defaultValue = "") String nickname,
                                @RequestParam(defaultValue = "") String address){
        IPage<User> page = new Page<>(pageNum,pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if(!"".equals(username)){
            wrapper.like("username",username);
        }
        if(!"".equals(nickname)){
            wrapper.like("nickname",nickname);
        }
        if(!"".equals(address)){
            wrapper.like("address",address);
        }
        wrapper.orderByDesc("id");
        return  userService.page(page,wrapper);

    }

    @GetMapping("/export")//导出
    public void export(HttpServletResponse response) throws Exception {
        List<User> list = userService.list();
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("username","用户名");
        writer.addHeaderAlias("password","密码");
        writer.addHeaderAlias("nickname","昵称");
        writer.addHeaderAlias("email","邮箱");
        writer.addHeaderAlias("phone","电话");
        writer.addHeaderAlias("address","地址");
        writer.addHeaderAlias("createTime","创建时间");
      //  writer.addHeaderAlias("avatarUrl","头像");

        writer.write(list,true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息","utf-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream out = response.getOutputStream() ;

        writer.flush(out,true);
        out.close();
        writer.close();



    }

    @PostMapping("/import")//导入
    public Boolean imp(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        List<List<Object>> list = reader.read(1);
        List<User> users = CollUtil.newArrayList();
        for(List<Object> row :list){
            User user =new User();
            user.setUsername(row.get(0).toString());
            user.setPassword(row.get(1).toString());
            user.setNickname(row.get(2).toString());
            user.setEmail(row.get(3).toString());
            user.setPhone(row.get(4).toString());
            user.setAddress(row.get(5).toString());
          //  user.setAvatarUrl(row.get(6).toString());
            users.add(user);

        }
        return userService.saveBatch(users);
    }


    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO){
       String username = userDTO.getUsername();
       String password = userDTO.getPassword();
       if(StrUtil.isBlank(username)||StrUtil.isBlank(password)){
           return  Result.error(Constants.CODE_400,"参数错误");
       }
        UserDTO dto = userService.login(userDTO);
        return Result.success(dto);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO){
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if(StrUtil.isBlank(username)||StrUtil.isBlank(password)){
            return  Result.error(Constants.CODE_400,"参数错误");
        }
       return Result.success(userService.register(userDTO));
    }

    @GetMapping("/username/{username}")
    public Result findOne(@PathVariable String username){
        return Result.success(userService.findOne(username));
    }
}
