package com.laby.framework.codegen.config;

import com.laby.framework.codegen.core.service.CodeGeneratorService;
import com.laby.framework.codegen.core.service.CodeGeneratorServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 编码生成器自动配置类
 * <p>
 * 功能说明：
 * 1. 自动配置 CodeGeneratorService Bean
 * 2. 依赖 Redis 环境，需要确保 Redis 配置正确
 * <p>
 * 配置要求：
 * - 项目中已配置 spring.redis 相关配置
 * - Redis 服务正常运行
 * <p>
 * 使用方式：
 * 引入依赖后自动生效，无需额外配置
 *
 * @author laby
 */
@Slf4j
@AutoConfiguration
public class LabyCodeGeneratorAutoConfiguration {

    /**
     * 创建编码生成服务 Bean
     * <p>
     * 依赖说明：
     * - StringRedisTemplate：用于 Redis 操作，存储编码序号
     *
     * @param stringRedisTemplate Spring 提供的 Redis 模板
     * @return 编码生成服务实例
     */
    @Bean
    public CodeGeneratorService codeGeneratorService(StringRedisTemplate stringRedisTemplate) {
        log.info("[编码生成器] 初始化完成，使用 Redis 作为序号存储");
        return new CodeGeneratorServiceImpl(stringRedisTemplate);
    }
}
