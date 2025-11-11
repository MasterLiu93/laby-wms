package com.laby.framework.codegen.core.service;

import cn.hutool.core.date.DateUtil;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 编码生成服务实现类
 * <p>
 * 核心技术：
 * 1. 基于 Redis INCR 命令实现分布式环境下的编码唯一性
 * 2. 批量生成时一次性获取序号区间，减少 Redis 调用次数
 * 3. 每日重置的编码自动设置过期时间
 * <p>
 * 线程安全：
 * - Redis INCR 命令是原子操作，保证多线程和分布式环境下的序号唯一性
 *
 * @author laby
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String generateCode(CodePrefixEnum codePrefix) {
        return generateBatchCode(codePrefix, 1).get(0);
    }

    @Override
    public String generateCode(String prefix) {
        // 使用默认序列号长度 5 位
        return generateBatchCodeWithLength(prefix, 1, 5).get(0);
    }

    @Override
    public List<String> generateBatchCode(CodePrefixEnum codePrefix, int count) {
        return generateBatchCodeWithLength(codePrefix.getPrefix(), count, codePrefix.getSerialLength());
    }

    @Override
    public List<String> generateBatchCode(String prefix, int count) {
        // 使用默认序列号长度 5 位
        return generateBatchCodeWithLength(prefix, count, 5);
    }

    /**
     * 批量生成编码（内部方法，支持自定义序列号长度）
     *
     * @param prefix       编码前缀
     * @param count        生成数量
     * @param serialLength 序列号长度
     * @return 编码列表
     */
    private List<String> generateBatchCodeWithLength(String prefix, int count, Integer serialLength) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        // 1. 构建 Redis Key：code:前缀:日期
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String redisKey = "code:" + prefix + ":" + dateStr;

        // 2. 批量获取序号（Redis INCR 是原子操作）
        Long endSeq = stringRedisTemplate.opsForValue().increment(redisKey, count);
        if (endSeq == null) {
            throw new RuntimeException("Redis increment failed for key: " + redisKey);
        }
        Long startSeq = endSeq - count + 1;

        // 3. 设置过期时间（到明天凌晨）
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).with(LocalTime.MIN);
        Duration duration = Duration.between(LocalDateTime.now(), tomorrow);
        stringRedisTemplate.expire(redisKey, duration);

        // 4. 生成编码列表（使用动态序列号长度）
        List<String> codes = new ArrayList<>(count);
        String formatPattern = "%s%s%0" + serialLength + "d";
        for (long seq = startSeq; seq <= endSeq; seq++) {
            String code = String.format(formatPattern, prefix, dateStr, seq);
            codes.add(code);
        }

        log.debug("[编码生成器] prefix={}, count={}, serialLength={}, codes={}", 
                  prefix, count, serialLength, codes);
        return codes;
    }
}


