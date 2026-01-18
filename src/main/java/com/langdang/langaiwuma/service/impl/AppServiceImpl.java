package com.langdang.langaiwuma.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.langdang.langaiwuma.constant.AppConstant;
import com.langdang.langaiwuma.core.AiCodeGeneratorFacade;
import com.langdang.langaiwuma.exception.BusinessException;
import com.langdang.langaiwuma.exception.ErrorCode;
import com.langdang.langaiwuma.exception.ThrowUtils;
import com.langdang.langaiwuma.model.dto.app.AppQueryRequest;
import com.langdang.langaiwuma.model.entity.User;
import com.langdang.langaiwuma.model.enums.CodeGenTypeEnum;
import com.langdang.langaiwuma.model.vo.AppVO;
import com.langdang.langaiwuma.model.vo.UserVO;
import com.langdang.langaiwuma.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.langdang.langaiwuma.model.entity.App;
import com.langdang.langaiwuma.mapper.AppMapper;
import com.langdang.langaiwuma.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 王睿麟
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }





    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId==null||appId<=0,ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        App app=this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        if (!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限部署该应用");
        }
        //  查询是deployKey是否存在
        if (StringUtils.isBlank(app.getDeployKey())) {
            // 如果没有生成6为deployKey（字母加数字）
            app.setDeployKey(RandomUtil.randomString(6));
            this.updateById(app);
        }
        // 获取代码生成类型，获取原始代码的生成路径（应用访问目录）
        String codeGenType=app.getCodeGenType();
        String sourceDirName=codeGenType+"_"+appId;
        String sourceDirPath= AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+sourceDirName;
        log.info("App{},当前路径为",sourceDirPath);
        // 检查路径是否存在
        File sourceDir=new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用代码路径不存在，请先生成应用");
        }
        // 复制文件部署目录
        String deployDirPath=AppConstant.CODE_DEPLOY_ROOT_DIR+File.separator+app.getDeployKey();
        try {
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用部署失败"+e.getMessage());
        }
        App updateApp=new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(app.getDeployKey());
        updateApp.setDeployedTime(LocalDateTime.now());
        try {
            this.updateById(updateApp);
        } catch (Exception e) {
            log.error("APP{}更新应用部署信息失败",app.getAppName());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新应用部署失败"+e.getMessage());
        }
        // 返回可访问的URL地址
        return String.format("%s%s",AppConstant.CODE_DEPLOY_HOST,app.getDeployKey());
    }

}
