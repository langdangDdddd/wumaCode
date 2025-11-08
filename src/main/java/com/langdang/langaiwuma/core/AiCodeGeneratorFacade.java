package com.langdang.langaiwuma.core;

import com.langdang.langaiwuma.ai.AiCodeGeneratorService;
import com.langdang.langaiwuma.ai.model.HtmlCodeResult;
import com.langdang.langaiwuma.ai.model.MultiFileCodeResult;
import com.langdang.langaiwuma.core.CodeFileSaver;
import com.langdang.langaiwuma.exception.BusinessException;
import com.langdang.langaiwuma.exception.ErrorCode;
import com.langdang.langaiwuma.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码(流式)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMutiFileCodeResult(result);
    }

    /**
     * 流式调用单文件
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage){
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        // 定义一个字符串拼接器
        StringBuilder codeBuilder=new StringBuilder();
        return result.doOnNext(chunk->{
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(()->{
            // 流式返回完成后，保存代码
            String completeHtmlCode=codeBuilder.toString();
            HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
            //保存代码到文件
            File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
            log.info("文件创建完成，目录为：{}",saveDir.getAbsoluteFile());
        });
    }

    /**
     * 流式调用多文件
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage){
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        // 定义一个字符串拼接器
        StringBuilder codeBuilder=new StringBuilder();
        return result.doOnNext(chunk->{
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(()->{
            try {
                // 流式返回完成后，保存代码
                String completeMultiFileCode=codeBuilder.toString();
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                //保存代码到文件
                File saveDir = CodeFileSaver.saveMutiFileCodeResult(multiFileCodeResult);
                log.info("文件创建完成，目录为：{}",saveDir.getAbsoluteFile());
            } catch (Exception e) {
                log.error("文件创建失败",e);
            }
        });
    }

}
