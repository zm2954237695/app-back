package com.guo.app.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guo.app.entity.Files;
import com.guo.app.entity.User;
import com.guo.app.mapper.FileMapper;
import com.guo.app.status.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileMapper fileMapper;
    @Value("${files.upload.path}")
    private String fileUploadPath;
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();

        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid+StrUtil.DOT+type;
        File uploadFile = new File(fileUploadPath+fileUUID);
        if(!uploadFile.getParentFile().exists()){
            uploadFile.getParentFile().mkdirs();
        }
        String url;
        String md5;
        file.transferTo(uploadFile);
        md5 = SecureUtil.md5(uploadFile);
        Files dbFiles = getFileByMd5(md5);
        if (dbFiles!=null){
            url = dbFiles.getUrl();
            uploadFile.delete();
        } else {
            url = "http://localhost:9090/file/"+fileUUID;
        }

        Files files = new Files();
        files.setName(originalFilename);
        files.setType(type);
        files.setSize(size/1024);//文件大小为kb
        files.setUrl(url);
        files.setMd5(md5);
        fileMapper.insert(files);
        return  url;
    }

    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件的唯一标识获取文件
        File uploadFile = new File(fileUploadPath +fileUUID);
        //设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID,"utf-8"));
        response.setContentType("application/octet-stream");
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    private  Files getFileByMd5(String md5){
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<Files> files = fileMapper.selectList(queryWrapper);
        return files.size()==0 ? null:files.get(0);
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                                @RequestParam(defaultValue = "") String name){
        IPage<Files> page = new Page<>(pageNum,pageSize);
        QueryWrapper<Files> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete",false);
        if(!"".equals(name)){
            wrapper.like("name",name);
        }

        return Result.success(fileMapper.selectPage(page,wrapper));

    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id){
        Files files = fileMapper.selectById(id);
        files.setIsDelete(true);
        return Result.success(fileMapper.updateById(files));
    }


    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        List<Files> files = fileMapper.selectList(queryWrapper);
        for(Files file:files){
            file.setIsDelete(true);
            fileMapper.updateById(file);

        }
        return Result.success();
    }
    @PostMapping("/update")
    public Result save(@RequestBody Files files){
        return Result.success(fileMapper.updateById(files));
    }
}
