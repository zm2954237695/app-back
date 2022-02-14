package com.guo.app;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.guo.app.mapper")
public class AppBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppBackApplication.class, args);
    }





}
