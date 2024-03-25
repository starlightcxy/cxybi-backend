package com.cxy.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//从application.yml文件中读取前缀为"spring.redis"的配置项
@ConfigurationProperties(prefix="spring.redis")
@Data
public class RedissonConfig {

    private Integer database;

    private String host;

    private Integer port;

    //方法名不能乱起哈，等会manager要用实例呢
    @Bean
    public RedissonClient redissonClient(){
        //获取配置对象
        Config config = new Config();
        //设置单机配置，无密码则不用设置密码(String类型)
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port);
        //创建Redisson实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
