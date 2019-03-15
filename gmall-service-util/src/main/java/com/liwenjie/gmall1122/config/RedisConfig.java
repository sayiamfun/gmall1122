package com.liwenjie.gmall1122.config;

import com.liwenjie.gmall1122.config.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:disabled}")
    String host;

    @Value("${spring.redis.port:0}")
    int port;

    @Value("${spring.redis.database:0}")
    int database;

    @Bean
    public RedisUtil getRedisUtil(){

        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }

}
