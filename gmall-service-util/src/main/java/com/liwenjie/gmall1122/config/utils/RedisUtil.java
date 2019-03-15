package com.liwenjie.gmall1122.config.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host, int port, int database){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数
        jedisPoolConfig.setMaxTotal(200);
        //获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        //最少剩余数（可配可不配）
        jedisPoolConfig.setMinIdle(10);
        //如果到最大数设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        //得到连接时 ， 自动检测连接是否可用
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPool = new JedisPool(jedisPoolConfig, host, port, 20*1000);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
