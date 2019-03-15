package com.liwenjie.gmall1122.gmallusermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liwenjie.gmall1122.bean.UserAddress;
import com.liwenjie.gmall1122.bean.UserInfo;
import com.liwenjie.gmall1122.config.utils.RedisUtil;
import com.liwenjie.gmall1122.gmallusermanage.mapper.UserAddressMapper;
import com.liwenjie.gmall1122.gmallusermanage.mapper.UserInfoMapper;
import com.liwenjie.gmall1122.service.CartService;
import com.liwenjie.gmall1122.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    public String USERINFOKEY_PREFIX="user:";
    public String USERINFOKEY_SUFFIX=":info";
    public int USERINFOKEY_TIMEOUT=60*60*24;


    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Reference
    private CartService cartService;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> getUserInfoList() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo getUserInfoByUser(UserInfo userInfo) {
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info!=null){
            // 获得到redis ,将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(USERINFOKEY_PREFIX+info.getId()+USERINFOKEY_SUFFIX,USERINFOKEY_TIMEOUT, JSON.toJSONString(info));
            jedis.close();
            return  info;
        }
        return null;

    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String key = USERINFOKEY_PREFIX + userId + USERINFOKEY_SUFFIX;
        String userInfoStr = jedis.get(key);
        jedis.expire(key, USERINFOKEY_TIMEOUT);
        jedis.close();
        if(userInfoStr!=null){
            UserInfo userInfo = JSON.parseObject(userInfoStr, UserInfo.class);
            return userInfo;
        }
        return null;
    }


}
