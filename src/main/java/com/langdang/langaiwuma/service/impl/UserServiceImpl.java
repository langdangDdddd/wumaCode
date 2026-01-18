package com.langdang.langaiwuma.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.langdang.langaiwuma.exception.BusinessException;
import com.langdang.langaiwuma.exception.ErrorCode;
import com.langdang.langaiwuma.model.dto.user.UserQueryRequest;
import com.langdang.langaiwuma.model.enums.UserRoleEnum;
import com.langdang.langaiwuma.model.vo.LoginUserVO;
import com.langdang.langaiwuma.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.langdang.langaiwuma.model.entity.User;
import com.langdang.langaiwuma.mapper.UserMapper;
import com.langdang.langaiwuma.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.langdang.langaiwuma.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 判断用户账号是否为空
        if (StringUtils.isBlank(userAccount)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号不能为空");
        }
        // 判断用户密码是否为空,且密码大小不能小于8位
        if (StringUtils.isBlank(userPassword) || userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码不能为空,且密码大小不能小于8位");
        }
        // 确认两次密码是否一致
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        // 判断数据库中是否有用户
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在");
        }
        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 创建用户
       User user=new User();
       user.setUserAccount(userAccount);
       user.setUserPassword(encryptPassword);
       String name="wuMa会员"+ RandomUtil.randomString(10);
       user.setUserName(name);
       user.setUserRole(UserRoleEnum.USER.getValue());
       boolean saveResult = this.save(user);
       if (!saveResult){
           throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据库异常");
       }
        return user.getId();
    }


    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isBlank(userAccount)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号不能为空");
        }
        if (StringUtils.isBlank(userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码不能为空");
        }
        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);

    }

    /**
     * 对用户信息进行脱敏
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "当前未登录");
        }
        //从数据库里更新信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "当前未登录");
        }
        return currentUser;
    }
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息（分页）
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "当前未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 将UserQueryRequest转换成QueryWrapper
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    // 将密码加盐值进行转换
    public String getEncryptPassword(String password){
        // 盐值
        String salt = "langdang5201314";
        return DigestUtils.md5DigestAsHex((password + salt).getBytes());
    }



}
