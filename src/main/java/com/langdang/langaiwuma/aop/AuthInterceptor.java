package com.langdang.langaiwuma.aop;

import com.langdang.langaiwuma.anotation.AuthCheck;
import com.langdang.langaiwuma.exception.BusinessException;
import com.langdang.langaiwuma.exception.ErrorCode;
import com.langdang.langaiwuma.model.entity.User;
import com.langdang.langaiwuma.model.enums.UserRoleEnum;
import com.langdang.langaiwuma.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
       String mustRole=authCheck.mustRole();
       log.debug("权限校验，要求角色：{}", mustRole);
       
       //获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        
        //不需要权限
        if (mustRoleEnum == null) {
            log.debug("无需权限校验，直接放行");
            return joinPoint.proceed();
        }
        
        //以下的代码需要权限
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (enumByValue == null) {
            log.warn("用户角色不存在，用户ID：{}，角色：{}", loginUser.getId(), loginUser.getUserRole());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        //要求必须由管理员权限，但当前登录用户没有权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(enumByValue)) {
            log.warn("权限不足，用户ID：{}，当前角色：{}，要求角色：{}", 
                    loginUser.getId(), enumByValue.getValue(), mustRoleEnum.getValue());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        //通过权限校验
        log.debug("权限校验通过，用户ID：{}，角色：{}", loginUser.getId(), enumByValue.getValue());
        return joinPoint.proceed();
    }
}
