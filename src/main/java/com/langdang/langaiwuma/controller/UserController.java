package com.langdang.langaiwuma.controller;

import cn.hutool.core.bean.BeanUtil;
import com.langdang.langaiwuma.anotation.AuthCheck;
import com.langdang.langaiwuma.common.BaseResponse;
import com.langdang.langaiwuma.common.DeleteRequest;
import com.langdang.langaiwuma.common.ResultUtils;
import com.langdang.langaiwuma.constant.UserConstant;
import com.langdang.langaiwuma.exception.BusinessException;
import com.langdang.langaiwuma.exception.ErrorCode;
import com.langdang.langaiwuma.exception.ThrowUtils;
import com.langdang.langaiwuma.model.dto.user.*;
import com.langdang.langaiwuma.model.vo.LoginUserVO;
import com.langdang.langaiwuma.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.langdang.langaiwuma.model.entity.User;
import com.langdang.langaiwuma.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 控制层。
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        log.info("用户注册请求，账号：{}", userRegisterRequest != null ? userRegisterRequest.getUserAccount() : "null");
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword());
        log.info("用户注册成功，用户ID：{}", result);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        log.info("用户登录请求，账号：{}", userLoginRequest != null ? userLoginRequest.getUserAccount() : "null");
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), request);
        log.info("用户登录成功，用户ID：{}，账号：{}", loginUserVO.getId(), loginUserVO.getUserAccount());
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        log.debug("获取登录用户信息，用户ID：{}", loginUser.getId());
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        log.info("用户注销，用户ID：{}", loginUser.getId());
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        log.info("管理员创建用户，账号：{}", userAddRequest != null ? userAddRequest.getUserAccount() : "null");
        User user = null;
        try {
            ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
            user = new User();
            BeanUtil.copyProperties(userAddRequest, user);
            // 默认密码 12345678
            final String DEFAULT_PASSWORD = "12345678";
            String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
            user.setUserPassword(encryptPassword);
            if (userAddRequest.getUserRole() == null) {
                user.setUserRole(UserConstant.DEFAULT_ROLE);
            }
            boolean result = userService.save(user);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            log.info("管理员创建用户成功，用户ID：{}", user.getId());
        } catch (Exception e) {
            log.error("管理员创建用户失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建用户失败");
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        log.info("管理员获取用户信息，用户ID：{}", id);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        log.debug("获取用户VO信息，用户ID：{}", id);
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        log.info("管理员删除用户，用户ID：{}", deleteRequest != null ? deleteRequest.getId() : "null");
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        log.info("管理员删除用户{}，用户ID：{}", b ? "成功" : "失败", deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        log.info("管理员更新用户信息，用户ID：{}", userUpdateRequest != null ? userUpdateRequest.getId() : "null");
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("管理员更新用户信息成功，用户ID：{}", userUpdateRequest.getId());
        return ResultUtils.success(true);
    }

    /**
     * 用户更新自己的信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest, 
                                               HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        log.info("用户更新自己的信息，用户ID：{}，用户名：{}", loginUser.getId(), userUpdateMyRequest.getUserName());
        User user = new User();
        BeanUtil.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("用户更新自己的信息成功，用户ID：{}", loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        log.info("管理员分页查询用户列表，页码：{}，每页大小：{}", 
                userQueryRequest != null ? userQueryRequest.getPageNum() : "null",
                userQueryRequest != null ? userQueryRequest.getPageSize() : "null");
        Page<UserVO> userVOPage = null;
        try {
            ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
            long pageNum = userQueryRequest.getPageNum();
            long pageSize = userQueryRequest.getPageSize();
            Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                    userService.getQueryWrapper(userQueryRequest));
            // 数据脱敏
            userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
            List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
            userVOPage.setRecords(userVOList);
            log.info("管理员分页查询用户列表成功，总记录数：{}", userPage.getTotalRow());
        } catch (Exception e) {
            log.error("管理员分页查询用户列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "分页获取用户失败");
        }
        return ResultUtils.success(userVOPage);
    }




}
