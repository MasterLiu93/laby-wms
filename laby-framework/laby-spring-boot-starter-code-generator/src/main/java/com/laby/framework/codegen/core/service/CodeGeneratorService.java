package com.laby.framework.codegen.core.service;

import com.laby.framework.codegen.core.enums.CodePrefixEnum;

import java.util.List;

/**
 * 编码生成服务接口
 *
 * @author laby
 */
public interface CodeGeneratorService {

    /**
     * 生成单个编码
     *
     * @param codePrefix 编码前缀枚举
     * @return 生成的编码（如：IN20251030001）
     */
    String generateCode(CodePrefixEnum codePrefix);

    /**
     * 生成单个编码（使用字符串前缀）
     *
     * @param prefix 编码前缀字符串
     * @return 生成的编码
     */
    String generateCode(String prefix);

    /**
     * 批量生成编码
     *
     * @param codePrefix 编码前缀枚举
     * @param count      生成数量
     * @return 编码列表
     */
    List<String> generateBatchCode(CodePrefixEnum codePrefix, int count);

    /**
     * 批量生成编码（使用字符串前缀）
     *
     * @param prefix 编码前缀字符串
     * @param count  生成数量
     * @return 编码列表
     */
    List<String> generateBatchCode(String prefix, int count);
}
