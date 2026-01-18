package com.langdang.langaiwuma.service;

import com.langdang.langaiwuma.model.dto.app.AppQueryRequest;
import com.langdang.langaiwuma.model.entity.User;
import com.langdang.langaiwuma.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.langdang.langaiwuma.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 王睿麟
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);


    List<AppVO> getAppVOList(List<App> appList);


    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId,User loginUser);
}
