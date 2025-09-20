package com.langdang.langaiwuma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvTestController {
    @Autowired
    private Environment env;

    @GetMapping("/env")
    public String getEnv() {
        // 打印激活的环境
        String profiles = String.join(",", env.getActiveProfiles());
        // 打印local文件中独有的配置（如修改后的端口）
        String port = env.getProperty("server.port");
        return "激活的环境：" + profiles + "，当前端口：" + port;
    }
}