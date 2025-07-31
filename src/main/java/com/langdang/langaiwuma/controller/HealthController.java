package com.langdang.langaiwuma.controller;
import com.langdang.langaiwuma.common.BaseResponse;
import com.langdang.langaiwuma.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    /**
     * 健康检查接口
     */
    @GetMapping("/")
    public BaseResponse healthCheck(){
        return ResultUtils.success("ok");
    }
}
