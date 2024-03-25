package com.cxy.springbootinit.manager;

import com.cxy.springbootinit.common.ErrorCode;
import com.cxy.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专门提供redis限流基础服务，一种通用服务
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     *
     * @param key 区分不同的限流器，比如不同的用户id分别限流
     */
    public void doRateLimit(String key){

        //获取不同的限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //设置限流规则。1s最多2请求（限制种类，限制速率，限制单位）
        //RateType.OVERALL作用于整个令牌桶，限制所有请求速率
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //获取令牌，1次获取多少令牌，比如1个
        boolean canOps = rateLimiter.tryAcquire(1);
        //没获取到令牌怎么办
        if(!canOps){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }

}
