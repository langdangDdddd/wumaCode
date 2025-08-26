package com.langdang.langaiwuma;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.langdang.langaiwuma.mapper")
public class LangAiWuMaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangAiWuMaApplication.class, args);
    }

}
